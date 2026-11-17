package com.aischolartwin.service.extraction;

import com.aischolartwin.exception.InvalidPayloadException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfExtractionService {

    private static final Logger log = LoggerFactory.getLogger(PdfExtractionService.class);
    private static final int LOW_TEXT_THRESHOLD_CHARS = 25;

    private final int maxPageCount;

    public PdfExtractionService(@Value("${app.document.max-page-count:100}") int maxPageCount) {
        this.maxPageCount = maxPageCount;
    }

    public ExtractionResult extract(File pdfFile) {
        if (pdfFile == null || !pdfFile.exists() || pdfFile.length() == 0) {
            throw new InvalidPayloadException("PDF file does not exist or is empty.");
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            if (document.isEncrypted()) {
                throw new InvalidPayloadException("The uploaded PDF is password-protected or encrypted. Please upload an unencrypted document.");
            }

            int pageCount = document.getNumberOfPages();
            if (pageCount == 0) {
                throw new InvalidPayloadException("The uploaded PDF contains 0 pages.");
            }
            if (pageCount > maxPageCount) {
                throw new InvalidPayloadException(String.format(
                        "PDF page count (%d) exceeds the maximum allowed limit of %d pages.",
                        pageCount, maxPageCount
                ));
            }

            List<ExtractedPage> pages = new ArrayList<>(pageCount);
            int totalCharacters = 0;
            int lowTextPageCount = 0;

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            for (int pageNum = 1; pageNum <= pageCount; pageNum++) {
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);

                String pageText = "";
                try {
                    pageText = stripper.getText(document);
                } catch (Exception e) {
                    log.warn("Failed to extract text from PDF page {}: {}", pageNum, e.getMessage());
                }

                String trimmed = pageText != null ? pageText.trim() : "";
                boolean isLowText = trimmed.length() < LOW_TEXT_THRESHOLD_CHARS;
                if (isLowText) {
                    lowTextPageCount++;
                }

                totalCharacters += trimmed.length();
                pages.add(new ExtractedPage(pageNum, trimmed, isLowText, false));
            }

            boolean ocrRequired = (totalCharacters == 0) || (lowTextPageCount > 0);

            return new ExtractionResult(pages, pageCount, totalCharacters, ocrRequired, false);

        } catch (InvalidPayloadException ipe) {
            throw ipe;
        } catch (IOException e) {
            log.error("Failed to parse PDF file: {}", e.getMessage(), e);
            throw new InvalidPayloadException("Invalid or corrupted PDF file: " + e.getMessage());
        }
    }
}
