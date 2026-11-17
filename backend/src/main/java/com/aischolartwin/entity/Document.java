package com.aischolartwin.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "documents",
        indexes = {
                @Index(name = "idx_documents_user_updated", columnList = "user_id, updated_at DESC"),
                @Index(name = "idx_documents_user_status", columnList = "user_id, status")
        }
)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "storage_path", nullable = false, length = 500)
    private String storagePath;

    @Column(name = "file_extension", nullable = false, length = 10)
    private String fileExtension;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "character_count", nullable = false)
    private int characterCount = 0;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount = 0;

    @Column(name = "ocr_used", nullable = false)
    private boolean ocrUsed = false;

    @Column(name = "ocr_required", nullable = false)
    private boolean ocrRequired = false;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "folder_id")
    private Long folderId;

    @Column(name = "is_favorite", nullable = false)
    private boolean isFavorite = false;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned = false;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "version_number", nullable = false)
    private int versionNumber = 1;

    @Column(name = "parent_document_id")
    private Long parentDocumentId;

    @Column(name = "document_summary", columnDefinition = "TEXT")
    private String documentSummary;

    @Column(name = "extracted_metadata_json", columnDefinition = "MEDIUMTEXT")
    private String extractedMetadataJson;

    @Column(name = "last_viewed_at")
    private Instant lastViewedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentChunk> chunks = new ArrayList<>();

    public Document() {
    }

    public Document(User user, String originalFilename, String storagePath, String fileExtension,
                    String contentType, long fileSize, DocumentFormat format) {
        this.user = user;
        this.originalFilename = originalFilename;
        this.storagePath = storagePath;
        this.fileExtension = fileExtension;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.format = format;
        this.status = DocumentStatus.UPLOADED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public DocumentFormat getFormat() {
        return format;
    }

    public void setFormat(DocumentFormat format) {
        this.format = format;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public boolean isOcrUsed() {
        return ocrUsed;
    }

    public void setOcrUsed(boolean ocrUsed) {
        this.ocrUsed = ocrUsed;
    }

    public boolean isOcrRequired() {
        return ocrRequired;
    }

    public void setOcrRequired(boolean ocrRequired) {
        this.ocrRequired = ocrRequired;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<DocumentChunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<DocumentChunk> chunks) {
        this.chunks = chunks;
    }

    public void addChunk(DocumentChunk chunk) {
        chunks.add(chunk);
        chunk.setDocument(this);
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Long getParentDocumentId() {
        return parentDocumentId;
    }

    public void setParentDocumentId(Long parentDocumentId) {
        this.parentDocumentId = parentDocumentId;
    }

    public String getDocumentSummary() {
        return documentSummary;
    }

    public void setDocumentSummary(String documentSummary) {
        this.documentSummary = documentSummary;
    }

    public String getExtractedMetadataJson() {
        return extractedMetadataJson;
    }

    public void setExtractedMetadataJson(String extractedMetadataJson) {
        this.extractedMetadataJson = extractedMetadataJson;
    }

    public Instant getLastViewedAt() {
        return lastViewedAt;
    }

    public void setLastViewedAt(Instant lastViewedAt) {
        this.lastViewedAt = lastViewedAt;
    }
}

