package com.aischolartwin.dto;

import com.aischolartwin.entity.DocumentChunk;

import java.time.Instant;

public class DocumentChunkResponse {

    private Long id;
    private Long documentId;
    private int chunkIndex;
    private String chunkText;
    private int startPage;
    private int endPage;
    private int characterCount;
    private Instant createdAt;

    public DocumentChunkResponse() {
    }

    public static DocumentChunkResponse fromEntity(DocumentChunk chunk) {
        DocumentChunkResponse resp = new DocumentChunkResponse();
        resp.setId(chunk.getId());
        resp.setDocumentId(chunk.getDocument() != null ? chunk.getDocument().getId() : null);
        resp.setChunkIndex(chunk.getChunkIndex());
        resp.setChunkText(chunk.getChunkText());
        resp.setStartPage(chunk.getStartPage());
        resp.setEndPage(chunk.getEndPage());
        resp.setCharacterCount(chunk.getCharacterCount());
        resp.setCreatedAt(chunk.getCreatedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
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

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

