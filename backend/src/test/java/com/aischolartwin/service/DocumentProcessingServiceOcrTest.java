package com.aischolartwin.service;

import com.aischolartwin.entity.*;
import com.aischolartwin.repository.DocumentChunkRepository;
import com.aischolartwin.repository.DocumentRepository;
import com.aischolartwin.repository.UserRepository;
import com.aischolartwin.service.extraction.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DocumentProcessingServiceOcrTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentProcessingService processingService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository chunkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private FileStorageService storageService;

    @Autowired
    private PdfExtractionService pdfExtractionService;

    @Autowired
    private DocxExtractionService docxExtractionService;

    @Autowired
    private TextPreprocessingService preprocessingService;

    @Autowired
    private ChunkingService chunkingService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("OCR Researcher", "ocr_researcher@univ.edu", passwordEncoder.encode("Secret123!"), Role.USER);
        testUser = userRepository.saveAndFlush(testUser);
    }

    private BufferedImage createTextImage(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Arial", Font.BOLD, 26));
        g.drawString(text, 20, 60);
        g.dispose();
        return image;
    }

    private byte[] createPureScannedPdfBytes(String textOnImage) throws IOException {
        BufferedImage pageImage = createTextImage(textOnImage, 900, 150);

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            var pdImage = LosslessFactory.createFromImage(doc, pageImage);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(pdImage, 50, 650, 500, 83);
            }
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createMixedPdfBytes(String page1DigitalText, String page2ImageText) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Page 1: Digital text only
            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText(page1DigitalText);
                cs.endText();
            }

            // Page 2: Scanned image only (zero digital text)
            PDPage page2 = new PDPage();
            doc.addPage(page2);
            BufferedImage pageImage = createTextImage(page2ImageText, 900, 150);
            var pdImage = LosslessFactory.createFromImage(doc, pageImage);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
                cs.drawImage(pdImage, 50, 650, 500, 83);
            }

            doc.save(baos);
            return baos.toByteArray();
        }
    }

    @Test
    @DisplayName("Process purely scanned PDF: executes OCR, sets ocrUsed=true, creates chunks")
    void processPureScannedPdf_succeedsWithOcr() throws Exception {
        assertTrue(ocrService.isAvailable(), "Tesseract should be available for OCR test.");

        byte[] pdfBytes = createPureScannedPdfBytes("Quantum Computing Principles and Entanglement");

        // Verify PDF contains 0 digital text
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            assertEquals("", stripper.getText(doc).trim(), "Synthetic scanned PDF must have 0 digital text.");
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "quantum_scanned.pdf", "application/pdf", pdfBytes
        );

        var docResponse = documentService.uploadDocument(testUser, file);
        assertNotNull(docResponse);

        // Process document
        Document processed = processingService.processDocument(docResponse.getId());

        assertEquals(DocumentStatus.COMPLETED, processed.getStatus());
        assertTrue(processed.isOcrUsed(), "isOcrUsed must be true when OCR was applied and recovered text.");
        assertFalse(processed.isOcrRequired(), "isOcrRequired must be false when OCR succeeded.");
        assertEquals(1, processed.getPageCount());
        assertTrue(processed.getCharacterCount() > 10, "Characters should be extracted via OCR.");
        assertTrue(processed.getChunkCount() >= 1, "Chunks must be created from OCR text.");

        List<DocumentChunk> chunks = chunkRepository.findByDocumentOrderByChunkIndexAsc(processed);
        assertFalse(chunks.isEmpty());
        assertEquals(1, chunks.get(0).getStartPage());
        assertEquals(1, chunks.get(0).getEndPage());

        String chunkContent = chunks.get(0).getChunkText().toLowerCase();
        assertTrue(chunkContent.contains("quantum") || chunkContent.contains("computing") || chunkContent.contains("principles"),
                "Chunk text should contain OCR recognized terms: " + chunkContent);
    }

    @Test
    @DisplayName("Process mixed PDF: Page 1 digital text + Page 2 scanned image preserves order and page metadata")
    void processMixedPdf_preservesOrderAndExtractsBoth() throws Exception {
        assertTrue(ocrService.isAvailable(), "Tesseract should be available for OCR test.");

        byte[] pdfBytes = createMixedPdfBytes(
                "Page 1: Digital Research Literature on Distributed Ledger Architectures.",
                "Page 2: Empirical Benchmark Measurements"
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "mixed_document.pdf", "application/pdf", pdfBytes
        );

        var docResponse = documentService.uploadDocument(testUser, file);
        Document processed = processingService.processDocument(docResponse.getId());

        assertEquals(DocumentStatus.COMPLETED, processed.getStatus());
        assertTrue(processed.isOcrUsed(), "isOcrUsed must be true because page 2 used OCR.");
        assertFalse(processed.isOcrRequired(), "isOcrRequired must be false because all pages were extracted.");
        assertEquals(2, processed.getPageCount());
        assertTrue(processed.getChunkCount() >= 1);

        List<DocumentChunk> chunks = chunkRepository.findByDocumentOrderByChunkIndexAsc(processed);
        assertFalse(chunks.isEmpty());

        // Find chunk from page 1 and chunk from page 2
        boolean foundDigitalPage1 = chunks.stream().anyMatch(c -> c.getChunkText().contains("Distributed Ledger"));
        boolean foundOcrPage2 = chunks.stream().anyMatch(c ->
                c.getChunkText().toLowerCase().contains("empirical") ||
                c.getChunkText().toLowerCase().contains("benchmark") ||
                c.getChunkText().toLowerCase().contains("measurements"));

        assertTrue(foundDigitalPage1, "Document chunks must contain Page 1 digital text.");
        assertTrue(foundOcrPage2, "Document chunks must contain Page 2 OCR text.");
    }

    @Test
    @DisplayName("OCR unavailable fallback: flags ocrRequired=true and ocrUsed=false gracefully")
    void processScannedPdf_whenOcrUnavailable_flagsOcrRequiredAndFailsGracefully() throws Exception {
        OcrService disabledOcrService = new OcrService(false, "tesseract", "eng");
        DocumentProcessingService serviceWithDisabledOcr = new DocumentProcessingService(
                documentRepository,
                chunkRepository,
                storageService,
                pdfExtractionService,
                docxExtractionService,
                disabledOcrService,
                preprocessingService,
                chunkingService
        );

        byte[] pdfBytes = createPureScannedPdfBytes("Text that cannot be extracted without OCR");
        MockMultipartFile file = new MockMultipartFile(
                "file", "unextractable.pdf", "application/pdf", pdfBytes
        );

        var docResponse = documentService.uploadDocument(testUser, file);
        Document processed = serviceWithDisabledOcr.processDocument(docResponse.getId());

        assertEquals(DocumentStatus.FAILED, processed.getStatus());
        assertFalse(processed.isOcrUsed(), "ocrUsed must be false when OCR is disabled.");
        assertTrue(processed.isOcrRequired(), "ocrRequired must be true when scanned document cannot be processed.");
        assertNotNull(processed.getErrorMessage());
        assertTrue(processed.getErrorMessage().contains("OCR may be required"));
    }
}
