package com.aischolartwin.service;

import com.aischolartwin.service.extraction.OcrService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OcrServiceTest {

    private OcrService ocrService;
    private File tempPdfFile;

    @BeforeEach
    void setUp() {
        ocrService = new OcrService(true, "tesseract", "eng");
    }

    @AfterEach
    void tearDown() {
        if (tempPdfFile != null && tempPdfFile.exists()) {
            tempPdfFile.delete();
        }
    }

    private BufferedImage createTextImage(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString(text, 20, 60);
        g.dispose();
        return image;
    }

    @Test
    @DisplayName("Tesseract is operational and candidate paths are detected")
    void isAvailable_detectsOperationalTesseract() {
        boolean available = ocrService.isAvailable();
        assertTrue(available, "Tesseract should be detected and operational in this environment.");
        assertNotNull(ocrService.getResolvedTesseractPath());
        assertNotNull(ocrService.getResolvedTessdataDir());
        assertEquals("eng", ocrService.getOcrLanguage());
    }

    @Test
    @DisplayName("Direct image OCR recognizes text from BufferedImage")
    void recognizeImage_extractsTextFromBufferedImage() {
        BufferedImage img = createTextImage("ScholarTwin Neural Network Architecture", 800, 150);
        String result = ocrService.recognizeImage(img);

        assertNotNull(result);
        assertFalse(result.isBlank());
        assertTrue(result.toLowerCase().contains("scholartwin"), "Result should contain 'ScholarTwin': " + result);
        assertTrue(result.toLowerCase().contains("neural"), "Result should contain 'Neural': " + result);
    }

    @Test
    @DisplayName("Scanned PDF (image-only, zero digital text) extracts text via OCR")
    void recognizePage_fromSyntheticScannedPdf_extractsText() throws IOException {
        tempPdfFile = File.createTempFile("ocr_test_scanned_", ".pdf");

        // Create a synthetic scanned PDF: image with text embedded into PDF, NO text drawing commands
        BufferedImage pageImage = createTextImage("Optical Character Recognition Benchmark 2026", 900, 150);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            var pdImage = LosslessFactory.createFromImage(doc, pageImage);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(pdImage, 50, 650, 500, 83);
            }
            doc.save(tempPdfFile);
        }

        // Verify PDFTextStripper extracts 0 text
        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(tempPdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String digitalText = stripper.getText(doc).trim();
            assertEquals("", digitalText, "Digital extraction must yield 0 text on an image-only PDF.");
        }

        // Now run OCR on page 1
        String ocrText = ocrService.recognizePage(tempPdfFile, 1);
        assertNotNull(ocrText);
        assertFalse(ocrText.isBlank(), "OCR text should not be blank.");
        assertTrue(ocrText.toLowerCase().contains("optical") || ocrText.toLowerCase().contains("character") || ocrText.toLowerCase().contains("recognition"),
                "OCR should recognize words from the image: " + ocrText);
    }

    @Test
    @DisplayName("OCR disabled returns unavailable and empty text")
    void ocrDisabled_returnsUnavailableAndEmpty() {
        OcrService disabledService = new OcrService(false, "tesseract", "eng");
        assertFalse(disabledService.isAvailable());

        BufferedImage img = createTextImage("Should not be processed", 400, 100);
        String result = disabledService.recognizeImage(img);
        assertEquals("", result);
    }
}

