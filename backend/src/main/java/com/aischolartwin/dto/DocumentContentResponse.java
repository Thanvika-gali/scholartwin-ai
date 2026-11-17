package com.aischolartwin.dto;

import com.aischolartwin.entity.DocumentFormat;

import java.util.List;

public class DocumentContentResponse {

    private Long documentId;
    private String originalFilename;
    private DocumentFormat format;
    private Integer pageCount;
    private int characterCount;
    private int chunkCount;
    private boolean ocrUsed;
    private String fullText;
    private List<DocumentChunkResponse> chunks;

    public DocumentContentResponse() {
    }

    public DocumentContentResponse(Long documentId, String originalFilename, DocumentFormat format,
                                   Integer pageCount, int characterCount, int chunkCount,
                                   boolean ocrUsed, String fullText, List<DocumentChunkResponse> chunks) {
        this.documentId = documentId;
        this.originalFilename = originalFilename;
        this.format = format;
        this.pageCount = pageCount;
        this.characterCount = characterCount;
        this.chunkCount = chunkCount;
        this.ocrUsed = ocrUsed;
        this.fullText = fullText;
        this.chunks = chunks;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public DocumentFormat getFormat() {
        return format;
    }

    public void setFormat(DocumentFormat format) {
        this.format = format;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }

    public boolean isOcrUsed() {
        return ocrUsed;
    }

    public void setOcrUsed(boolean ocrUsed) {
        this.ocrUsed = ocrUsed;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public List<DocumentChunkResponse> getChunks() {
        return chunks;
    }

    public void setChunks(List<DocumentChunkResponse> chunks) {
        this.chunks = chunks;
    }
}

