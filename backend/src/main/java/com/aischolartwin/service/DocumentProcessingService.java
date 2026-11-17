package com.aischolartwin.service;

import com.aischolartwin.entity.Document;
import com.aischolartwin.entity.DocumentChunk;
import com.aischolartwin.entity.DocumentFormat;
import com.aischolartwin.entity.DocumentStatus;
import com.aischolartwin.repository.DocumentChunkRepository;
import com.aischolartwin.repository.DocumentRepository;
import com.aischolartwin.service.extraction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(DocumentProcessingService.class);

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final FileStorageService storageService;
    private final PdfExtractionService pdfExtractionService;
    private final DocxExtractionService docxExtractionService;
    private final OcrService ocrService;
    private final TextPreprocessingService preprocessingService;
    private final ChunkingService chunkingService;

    public DocumentProcessingService(
            DocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            FileStorageService storageService,
            PdfExtractionService pdfExtractionService,
            DocxExtractionService docxExtractionService,
            OcrService ocrService,
            TextPreprocessingService preprocessingService,
            ChunkingService chunkingService
    ) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.storageService = storageService;
        this.pdfExtractionService = pdfExtractionService;
        this.docxExtractionService = docxExtractionService;
        this.ocrService = ocrService;
        this.preprocessingService = preprocessingService;
        this.chunkingService = chunkingService;
    }

    @Transactional
    public Document processDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));

        document.setStatus(DocumentStatus.PROCESSING);
        document.setErrorMessage(null);
        document = documentRepository.save(document);

        try {
            Path filePath = storageService.resolve(document.getStoragePath());
            File file = filePath.toFile();

            ExtractionResult extractionResult;
            if (document.getFormat() == DocumentFormat.PDF) {
                extractionResult = pdfExtractionService.extract(file);

                // Determine if OCR is required/needed
                boolean hasLowTextPages = extractionResult.getPages().stream().anyMatch(ExtractedPage::isLowText);

                if (hasLowTextPages || extractionResult.isOcrRequired()) {
                    if (ocrService.isAvailable()) {
                        log.info("Running OCR for scanned/low-text PDF document ID {}", document.getId());
                        boolean anyOcrApplied = false;

                        for (ExtractedPage page : extractionResult.getPages()) {
                            if (page.isLowText()) {
                                String ocrText = ocrService.recognizePage(file, page.getPageNumber());
                                if (ocrText != null && !ocrText.isBlank()) {
                                    page.setText(ocrText);
                                    page.setOcrApplied(true);
                                    page.setLowText(false);
                                    anyOcrApplied = true;
                                    log.info("OCR successfully extracted {} characters for page {} of document ID {}",
                                            ocrText.length(), page.getPageNumber(), document.getId());
                                } else {
                                    log.warn("OCR produced empty output for page {} of document ID {}",
                                            page.getPageNumber(), document.getId());
                                }
                            }
                        }

                        if (anyOcrApplied) {
                            extractionResult.setOcrUsed(true);
                        }

                        // Check if any low-text pages still remain unextracted
                        boolean remainingLowText = extractionResult.getPages().stream().anyMatch(p -> p.isLowText() || p.getText() == null || p.getText().isBlank());
                        extractionResult.setOcrRequired(remainingLowText);
                    } else {
                        log.info("OCR is required for document ID {} but Tesseract is not available.", document.getId());
                        extractionResult.setOcrRequired(true);
                        extractionResult.setOcrUsed(false);
                    }
                } else {
                    extractionResult.setOcrRequired(false);
                    extractionResult.setOcrUsed(false);
                }
            } else if (document.getFormat() == DocumentFormat.DOCX) {
                extractionResult = docxExtractionService.extract(file);
                extractionResult.setOcrRequired(false);
                extractionResult.setOcrUsed(false);
            } else {
                throw new UnsupportedOperationException("Unsupported document format: " + document.getFormat());
            }

            // Preprocess extracted text page by page
            List<ExtractedPage> preprocessedPages = new ArrayList<>();
            int totalCharacters = 0;
            for (ExtractedPage page : extractionResult.getPages()) {
                String cleaned = preprocessingService.preprocess(page.getText());
                ExtractedPage cleanPage = new ExtractedPage(page.getPageNumber(), cleaned, page.isLowText(), page.isOcrApplied());
                preprocessedPages.add(cleanPage);
                totalCharacters += cleaned.length();
            }

            // Check if any readable content exists
            if (totalCharacters == 0) {
                document.setStatus(DocumentStatus.FAILED);
                document.setOcrRequired(extractionResult.isOcrRequired());
                document.setOcrUsed(extractionResult.isOcrUsed());
                document.setErrorMessage("No readable text could be extracted from this document (OCR may be required if this is an image-only scan).");
                return documentRepository.save(document);
            }

            // Clean any old chunks if retrying
            chunkRepository.deleteByDocument(document);

            // Generate sequential, paragraph-aware chunks
            List<GeneratedChunk> generatedChunks = chunkingService.chunkPages(preprocessedPages);
            List<DocumentChunk> entityChunks = new ArrayList<>();
            for (GeneratedChunk gc : generatedChunks) {
                DocumentChunk chunk = new DocumentChunk(
                        document,
                        gc.getChunkIndex(),
                        gc.getText(),
                        gc.getStartPage(),
                        gc.getEndPage(),
                        gc.getCharacterCount()
                );
                entityChunks.add(chunk);
            }
            chunkRepository.saveAll(entityChunks);

            // Update document metrics and completion status
            document.setPageCount(extractionResult.getTotalPages());
            document.setCharacterCount(totalCharacters);
            document.setChunkCount(entityChunks.size());
            document.setOcrUsed(extractionResult.isOcrUsed());
            document.setOcrRequired(extractionResult.isOcrRequired());
            document.setStatus(DocumentStatus.COMPLETED);
            document.setCompletedAt(Instant.now());
            document.setErrorMessage(null);

            return documentRepository.save(document);

        } catch (Exception e) {
            log.error("Failed processing document ID {}: {}", document.getId(), e.getMessage(), e);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Document processing failed.");
            return documentRepository.save(document);
        }
    }
}
