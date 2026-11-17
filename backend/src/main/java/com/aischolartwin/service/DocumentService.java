package com.aischolartwin.service;

import com.aischolartwin.dto.DocumentChunkResponse;
import com.aischolartwin.dto.DocumentContentResponse;
import com.aischolartwin.dto.DocumentIntelligenceResponse;
import com.aischolartwin.dto.DocumentResponse;
import com.aischolartwin.dto.DocumentStatusResponse;
import com.aischolartwin.dto.DuplicateCheckResponse;
import com.aischolartwin.entity.*;
import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.exception.ResourceNotFoundException;
import com.aischolartwin.repository.DocumentChunkRepository;
import com.aischolartwin.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final FileStorageService storageService;
    private final DocumentProcessingService processingService;
    private final DocumentIntelligenceService intelligenceService;
    private final KnowledgeOrganizationService organizationService;
    private final long maxFileSizeBytes;

    @Autowired
    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            FileStorageService storageService,
            DocumentProcessingService processingService,
            DocumentIntelligenceService intelligenceService,
            KnowledgeOrganizationService organizationService,
            @Value("${app.document.max-file-size-bytes:104857600}") long maxFileSizeBytes
    ) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.storageService = storageService;
        this.processingService = processingService;
        this.intelligenceService = intelligenceService;
        this.organizationService = organizationService;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            FileStorageService storageService,
            DocumentProcessingService processingService,
            long maxFileSizeBytes
    ) {
        this(documentRepository, chunkRepository, storageService, processingService, null, null, maxFileSizeBytes);
    }

    @Transactional
    public DocumentResponse uploadDocument(User user, MultipartFile file) {
        return uploadDocument(user, file, null, null, false);
    }

    @Transactional
    public DocumentResponse uploadDocument(User user, MultipartFile file, Long folderId, Long parentDocumentId, Boolean allowDuplicate) {
        validateUploadedFile(file);

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "document";
        }

        // Sanitize original filename (remove directory paths if provided by client)
        originalFilename = PathSanitizer.cleanFilename(originalFilename);

        String extension = getFileExtension(originalFilename);
        DocumentFormat format = DocumentFormat.fromExtension(extension);
        if (format == null) {
            throw new InvalidPayloadException("Unsupported document format. Only PDF (.pdf) and Word (.docx) files are supported.");
        }

        // Verify content signature (magic numbers) to prevent file extension spoofing
        verifyContentSignature(file, format);

        String fileHash = calculateFileHash(file);
        if (!Boolean.TRUE.equals(allowDuplicate) && parentDocumentId == null && fileHash != null) {
            List<Document> dupes = documentRepository.findByUserAndFileHash(user, fileHash);
            if (!dupes.isEmpty()) {
                log.info("Duplicate document detected for user {}: existing doc id {}", user.getEmail(), dupes.get(0).getId());
            }
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = format == DocumentFormat.PDF ? "application/pdf" : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }

        // Store original document securely on disk with unique UUID filename
        String storagePath = storageService.store(file, extension);

        Document document = new Document(
                user,
                originalFilename,
                storagePath,
                extension,
                contentType,
                file.getSize(),
                format
        );
        document.setFolderId(folderId);
        document.setFileHash(fileHash);

        if (parentDocumentId != null) {
            Document parent = findUserDocumentOrThrow(parentDocumentId, user);
            document.setParentDocumentId(parent.getId());
            document.setVersionNumber(parent.getVersionNumber() + 1);
        }

        document = documentRepository.save(document);

        // Process document immediately (synchronous extraction and chunking pipeline)
        document = processingService.processDocument(document.getId());

        // Generate Document Intelligence & Smart Folder suggestion
        try {
            if (intelligenceService != null) {
                DocumentIntelligenceResponse intel = intelligenceService.generateOrGetDocumentIntelligence(user, document.getId());
                if (organizationService != null && document.getFolderId() == null) {
                    organizationService.generateSuggestion(
                            user,
                            ResourceType.DOCUMENT,
                            document.getId(),
                            document.getOriginalFilename(),
                            document.getOriginalFilename(),
                            intel.summary(),
                            document.getFileExtension(),
                            intel.mainTopics(),
                            intel.estimatedSubject()
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Non-fatal intelligence/organization error for doc id {}: {}", document.getId(), e.getMessage());
        }

        return DocumentResponse.fromEntity(document);
    }

    public DuplicateCheckResponse checkDuplicate(User user, MultipartFile file) {
        String hash = calculateFileHash(file);
        if (hash == null) {
            return new DuplicateCheckResponse(false, null, null, null, null);
        }
        List<Document> existing = documentRepository.findByUserAndFileHash(user, hash);
        if (!existing.isEmpty()) {
            Document first = existing.get(0);
            return new DuplicateCheckResponse(true, hash, first.getId(), first.getOriginalFilename(), first.getFolderId());
        }
        return new DuplicateCheckResponse(false, hash, null, null, null);
    }

    private String calculateFileHash(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.warn("Failed to compute SHA-256 hash for uploaded file: {}", e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listDocuments(User user, DocumentStatus status, DocumentFormat format, String search) {
        List<Document> docs = documentRepository.searchDocuments(user, status, format, search != null && !search.isBlank() ? search.trim() : null);
        return docs.stream().map(DocumentResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long id, User user) {
        Document document = findUserDocumentOrThrow(id, user);
        return DocumentResponse.fromEntity(document);
    }

    @Transactional(readOnly = true)
    public DocumentStatusResponse getDocumentStatus(Long id, User user) {
        Document document = findUserDocumentOrThrow(id, user);
        return DocumentStatusResponse.fromEntity(document);
    }

    @Transactional(readOnly = true)
    public DocumentContentResponse getDocumentContent(Long id, User user) {
        Document document = findUserDocumentOrThrow(id, user);
        List<DocumentChunk> chunks = chunkRepository.findByDocumentOrderByChunkIndexAsc(document);

        StringBuilder fullTextBuilder = new StringBuilder();
        for (DocumentChunk chunk : chunks) {
            if (fullTextBuilder.length() > 0) {
                fullTextBuilder.append("\n\n---\n\n");
            }
            fullTextBuilder.append(chunk.getChunkText());
        }

        List<DocumentChunkResponse> chunkResponses = chunks.stream()
                .map(DocumentChunkResponse::fromEntity)
                .toList();

        return new DocumentContentResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getFormat(),
                document.getPageCount(),
                document.getCharacterCount(),
                document.getChunkCount(),
                document.isOcrUsed(),
                fullTextBuilder.toString(),
                chunkResponses
        );
    }

    @Transactional(readOnly = true)
    public List<DocumentChunkResponse> getDocumentChunks(Long id, User user) {
        Document document = findUserDocumentOrThrow(id, user);
        List<DocumentChunk> chunks = chunkRepository.findByDocumentOrderByChunkIndexAsc(document);
        return chunks.stream().map(DocumentChunkResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public DownloadFileInfo downloadDocument(Long id, User user) {
        Document document = findUserDocumentOrThrow(id, user);
        Resource resource = storageService.loadAsResource(document.getStoragePath());
        return new DownloadFileInfo(resource, document.getOriginalFilename(), document.getContentType());
    }

    @Transactional
    public DocumentResponse retryProcessing(Long id, User user) {
        Document document = findUserDocumentOrThrow(id, user);
        if (document.getStatus() == DocumentStatus.PROCESSING) {
            throw new InvalidPayloadException("Document is already actively being processed.");
        }

        document = processingService.processDocument(document.getId());
        return DocumentResponse.fromEntity(document);
    }

    @Transactional
    public void deleteDocument(Long id, User user) {
        Document document = findUserDocumentOrThrow(id, user);
        String storagePath = document.getStoragePath();

        // Remove dependent chunks first
        chunkRepository.deleteByDocument(document);

        // Remove document from DB
        documentRepository.delete(document);
        documentRepository.flush();

        // Delete underlying physical file on disk
        storageService.delete(storagePath);
    }

    private Document findUserDocumentOrThrow(Long id, User user) {
        return documentRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + id));
    }

    private void validateUploadedFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPayloadException("Please select a valid, non-empty file to upload.");
        }

        if (file.getSize() > maxFileSizeBytes) {
            double actualMb = (double) file.getSize() / (1024 * 1024);
            double limitMb = (double) maxFileSizeBytes / (1024 * 1024);
            throw new InvalidPayloadException(String.format(
                    "File size (%.2f MB) exceeds the maximum allowed limit of %.0f MB.",
                    actualMb, limitMb
            ));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
                throw new InvalidPayloadException("Filename contains invalid or dangerous path traversal characters.");
            }
        }
    }

    private void verifyContentSignature(MultipartFile file, DocumentFormat format) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) {
                throw new InvalidPayloadException("File is too small or truncated.");
            }

            // Reject Executable (MZ or ELF)
            if (header[0] == 'M' && header[1] == 'Z') {
                throw new InvalidPayloadException("Executable files (.exe, .dll) are strictly prohibited.");
            }
            if (header[0] == 0x7F && header[1] == 'E' && header[2] == 'L' && header[3] == 'F') {
                throw new InvalidPayloadException("ELF binary executables are strictly prohibited.");
            }

            if (format == DocumentFormat.PDF) {
                // PDF magic bytes: "%PDF" (0x25, 0x50, 0x44, 0x46)
                if (!(header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F')) {
                    throw new InvalidPayloadException("File claims to be a PDF but does not have a valid PDF header signature.");
                }
            } else if (format == DocumentFormat.DOCX) {
                // DOCX is a zip container starting with PK\x03\x04 (0x50, 0x4B, 0x03, 0x04)
                if (!(header[0] == 0x50 && header[1] == 0x4B && header[2] == 0x03 && header[3] == 0x04)) {
                    throw new InvalidPayloadException("File claims to be a Word (.docx) document but does not have a valid ZIP/DOCX header signature.");
                }
            }
        } catch (IOException e) {
            throw new InvalidPayloadException("Unable to inspect document signature: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static class DownloadFileInfo {
        private final Resource resource;
        private final String originalFilename;
        private final String contentType;

        public DownloadFileInfo(Resource resource, String originalFilename, String contentType) {
            this.resource = resource;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
        }

        public Resource getResource() {
            return resource;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public String getContentType() {
            return contentType;
        }
    }

    private static class PathSanitizer {
        static String cleanFilename(String filename) {
            int lastUnix = filename.lastIndexOf('/');
            int lastWindows = filename.lastIndexOf('\\');
            int lastSlash = Math.max(lastUnix, lastWindows);
            if (lastSlash >= 0) {
                filename = filename.substring(lastSlash + 1);
            }
            return filename.trim();
        }
    }
}
