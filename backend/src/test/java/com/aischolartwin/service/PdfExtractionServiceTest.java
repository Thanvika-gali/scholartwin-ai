package com.aischolartwin.service;

import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.service.extraction.ExtractedPage;
import com.aischolartwin.service.extraction.ExtractionResult;
import com.aischolartwin.service.extraction.PdfExtractionService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PdfExtractionServiceTest {

    private PdfExtractionService pdfExtractionService;
    private File tempPdfFile;

    @BeforeEach
    void setUp() {
        pdfExtractionService = new PdfExtractionService(100);
    }

    @AfterEach
    void tearDown() {
        if (tempPdfFile != null && tempPdfFile.exists()) {
            tempPdfFile.delete();
        }
    }

    @Test
    void extract_validMultiPagePdf_extractsPageTextSuccessfully() throws IOException {
        tempPdfFile = File.createTempFile("test_valid_", ".pdf");

        try (PDDocument doc = new PDDocument()) {
            // Page 1
            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page1)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Page 1: Introduction to Distributed Systems and Fault Tolerance");
                stream.endText();
            }

            // Page 2
            PDPage page2 = new PDPage();
            doc.addPage(page2);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page2)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Page 2: Raft Consensus Algorithm and Quorum Mechanics");
                stream.endText();
            }

            doc.save(tempPdfFile);
        }

        ExtractionResult result = pdfExtractionService.extract(tempPdfFile);

        assertNotNull(result);
        assertEquals(2, result.getTotalPages());
        assertTrue(result.getTotalCharacters() > 50);
        assertFalse(result.isOcrRequired());
        assertEquals(2, result.getPages().size());

        ExtractedPage p1 = result.getPages().get(0);
        assertEquals(1, p1.getPageNumber());
        assertTrue(p1.getText().contains("Distributed Systems"));

        ExtractedPage p2 = result.getPages().get(1);
        assertEquals(2, p2.getPageNumber());
        assertTrue(p2.getText().contains("Raft Consensus"));
    }

    @Test
    void extract_emptyPagePdf_flagsOcrRequired() throws IOException {
        tempPdfFile = File.createTempFile("test_empty_", ".pdf");

        try (PDDocument doc = new PDDocument()) {
            PDPage emptyPage = new PDPage();
            doc.addPage(emptyPage);
            doc.save(tempPdfFile);
        }

        ExtractionResult result = pdfExtractionService.extract(tempPdfFile);

        assertNotNull(result);
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getTotalCharacters());
        assertTrue(result.isOcrRequired());
    }

    @Test
    void extract_corruptedOrNonPdf_throwsInvalidPayloadException() throws IOException {
        tempPdfFile = File.createTempFile("test_corrupt_", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempPdfFile)) {
            fos.write("This is not a real PDF document content.".getBytes());
        }

        assertThrows(InvalidPayloadException.class, () -> pdfExtractionService.extract(tempPdfFile));
    }
}

