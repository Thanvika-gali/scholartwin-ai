package com.aischolartwin.service.extraction;

public class GeneratedChunk {

    private int chunkIndex;
    private String text;
    private int startPage;
    private int endPage;
    private int characterCount;

    public GeneratedChunk() {
    }

    public GeneratedChunk(int chunkIndex, String text, int startPage, int endPage) {
        this.chunkIndex = chunkIndex;
        this.text = text != null ? text.trim() : "";
        this.startPage = startPage;
        this.endPage = endPage;
        this.characterCount = this.text.length();
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text != null ? text.trim() : "";
        this.characterCount = this.text.length();
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }

    public int getCharacterCount() {
        return characterCount;
    }
}

