package com.aischolartwin.service;

import com.aischolartwin.exception.InvalidPayloadException;
import com.aischolartwin.service.extraction.DocxExtractionService;
import com.aischolartwin.service.extraction.ExtractionResult;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocxExtractionServiceTest {

    private DocxExtractionService docxExtractionService;

    @BeforeEach
    void setUp() {
        docxExtractionService = new DocxExtractionService();
    }

    @Test
    @DisplayName("Successfully extract paragraphs and tables from valid DOCX")
    void shouldExtractParagraphsAndTablesSuccessfully(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("sample.docx").toFile();

        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph p1 = doc.createParagraph();
            XWPFRun r1 = p1.createRun();
            r1.setText("Introduction to Neural Retrieval in Academic Systems.");

            XWPFTable table = doc.createTable(2, 2);
            table.getRow(0).getCell(0).setText("Model");
            table.getRow(0).getCell(1).setText("Accuracy");
            table.getRow(1).getCell(0).setText("BM25");
            table.getRow(1).getCell(1).setText("78.4%");

            XWPFParagraph p2 = doc.createParagraph();
            XWPFRun r2 = p2.createRun();
            r2.setText("Conclusion: Dense embeddings outperform sparse baselines.");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                doc.write(fos);
            }
        }

        ExtractionResult result = docxExtractionService.extract(file);

        assertThat(result).isNotNull();
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isOcrRequired()).isFalse();
        assertThat(result.getFullText()).contains("Introduction to Neural Retrieval");
        assertThat(result.getFullText()).contains("| Model | Accuracy |");
        assertThat(result.getFullText()).contains("| BM25 | 78.4% |");
        assertThat(result.getFullText()).contains("Conclusion: Dense embeddings outperform sparse baselines.");
    }

    @Test
    @DisplayName("Fail when DOCX has no readable text")
    void shouldFailWhenDocxHasNoReadableText(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("empty.docx").toFile();

        try (XWPFDocument doc = new XWPFDocument()) {
            // Document with no paragraphs or empty paragraphs
            doc.createParagraph();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                doc.write(fos);
            }
        }

        assertThatThrownBy(() -> docxExtractionService.extract(file))
                .isInstanceOf(InvalidPayloadException.class)
                .hasMessageContaining("no readable text content");
    }

    @Test
    @DisplayName("Fail when DOCX file is null or does not exist")
    void shouldFailWhenFileDoesNotExist(@TempDir Path tempDir) {
        File missing = tempDir.resolve("missing.docx").toFile();
        assertThatThrownBy(() -> docxExtractionService.extract(missing))
                .isInstanceOf(InvalidPayloadException.class)
                .hasMessageContaining("does not exist");
    }
}
