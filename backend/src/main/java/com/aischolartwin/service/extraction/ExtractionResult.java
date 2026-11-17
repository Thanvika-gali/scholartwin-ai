package com.aischolartwin.service.extraction;

import java.util.ArrayList;
import java.util.List;

public class ExtractionResult {

    private List<ExtractedPage> pages = new ArrayList<>();
    private int totalPages;
    private int totalCharacters;
    private boolean ocrRequired;
    private boolean ocrUsed;

    public ExtractionResult() {
    }

    public ExtractionResult(List<ExtractedPage> pages, int totalPages, int totalCharacters,
                            boolean ocrRequired, boolean ocrUsed) {
        this.pages = pages != null ? pages : new ArrayList<>();
        this.totalPages = totalPages;
        this.totalCharacters = totalCharacters;
        this.ocrRequired = ocrRequired;
        this.ocrUsed = ocrUsed;
    }

    public List<ExtractedPage> getPages() {
        return pages;
    }

    public void setPages(List<ExtractedPage> pages) {
        this.pages = pages;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalCharacters() {
        return totalCharacters;
    }

    public void setTotalCharacters(int totalCharacters) {
        this.totalCharacters = totalCharacters;
    }

    public boolean isOcrRequired() {
        return ocrRequired;
    }

    public void setOcrRequired(boolean ocrRequired) {
        this.ocrRequired = ocrRequired;
    }

    public boolean isOcrUsed() {
        return ocrUsed;
    }

    public void setOcrUsed(boolean ocrUsed) {
        this.ocrUsed = ocrUsed;
    }

    public String getCombinedText() {
        StringBuilder sb = new StringBuilder();
        for (ExtractedPage page : pages) {
            if (page.getText() != null && !page.getText().isBlank()) {
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append(page.getText().trim());
            }
        }
        return sb.toString();
    }

    public String getFullText() {
        return getCombinedText();
    }
}
