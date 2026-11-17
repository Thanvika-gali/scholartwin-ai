package com.aischolartwin.service.extraction;

public class ExtractedPage {

    private int pageNumber;
    private String text;
    private int characterCount;
    private boolean isLowText;
    private boolean ocrApplied;

    public ExtractedPage() {
    }

    public ExtractedPage(int pageNumber, String text, boolean isLowText, boolean ocrApplied) {
        this.pageNumber = pageNumber;
        this.text = text != null ? text : "";
        this.characterCount = this.text.length();
        this.isLowText = isLowText;
        this.ocrApplied = ocrApplied;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.characterCount = text != null ? text.length() : 0;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public boolean isLowText() {
        return isLowText;
    }

    public void setLowText(boolean lowText) {
        isLowText = lowText;
    }

    public boolean isOcrApplied() {
        return ocrApplied;
    }

    public void setOcrApplied(boolean ocrApplied) {
        this.ocrApplied = ocrApplied;
    }
}

