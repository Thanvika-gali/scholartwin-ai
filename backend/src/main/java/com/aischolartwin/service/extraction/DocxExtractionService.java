package com.aischolartwin.service.extraction;

import com.aischolartwin.exception.InvalidPayloadException;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocxExtractionService {

    private static final Logger log = LoggerFactory.getLogger(DocxExtractionService.class);

    public ExtractionResult extract(File docxFile) {
        if (docxFile == null || !docxFile.exists() || docxFile.length() == 0) {
            throw new InvalidPayloadException("DOCX file does not exist or is empty.");
        }

        try (FileInputStream fis = new FileInputStream(docxFile);
             XWPFDocument document = new XWPFDocument(fis)) {

            StringBuilder contentBuilder = new StringBuilder();

            // Iterate through body elements in document order (paragraphs and tables)
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph paragraph) {
                    String text = paragraph.getText();
                    if (text != null && !text.isBlank()) {
                        String style = paragraph.getStyle();
                        if (style != null && style.toLowerCase().contains("heading")) {
                            contentBuilder.append("\n### ").append(text.trim()).append("\n\n");
                        } else {
                            contentBuilder.append(text.trim()).append("\n\n");
                        }
                    }
                } else if (element instanceof XWPFTable table) {
                    contentBuilder.append("\n[Table]\n");
                    for (XWPFTableRow row : table.getRows()) {
                        List<String> cellTexts = new ArrayList<>();
                        for (XWPFTableCell cell : row.getTableCells()) {
                            String cellText = cell.getText() != null ? cell.getText().trim() : "";
                            cellTexts.add(cellText);
                        }
                        contentBuilder.append("| ").append(String.join(" | ", cellTexts)).append(" |\n");
                    }
                    contentBuilder.append("\n");
                }
            }

            String fullText = contentBuilder.toString().trim();
            if (fullText.isEmpty()) {
                throw new InvalidPayloadException("The Word document contains no readable text content.");
            }

            // Word documents have a logical page representation
            List<ExtractedPage> pages = new ArrayList<>();
            pages.add(new ExtractedPage(1, fullText, false, false));

            return new ExtractionResult(pages, 1, fullText.length(), false, false);

        } catch (InvalidPayloadException ipe) {
            throw ipe;
        } catch (Exception e) {
            log.error("Failed to parse Word document: {}", e.getMessage(), e);
            throw new InvalidPayloadException("Invalid or corrupted Word (.docx) document: " + e.getMessage());
        }
    }
}

