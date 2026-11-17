package com.aischolartwin.dto;

import com.aischolartwin.entity.Document;
import com.aischolartwin.entity.DocumentStatus;

import java.time.Instant;

public class DocumentStatusResponse {

    private Long id;
    private String originalFilename;
    private DocumentStatus status;
    private Integer pageCount;
    private int characterCount;
    private int chunkCount;
    private boolean ocrUsed;
    private boolean ocrRequired;
    private String errorMessage;
    private Instant completedAt;

    public DocumentStatusResponse() {
    }

    public static DocumentStatusResponse fromEntity(Document doc) {
        DocumentStatusResponse resp = new DocumentStatusResponse();
        resp.setId(doc.getId());
        resp.setOriginalFilename(doc.getOriginalFilename());
        resp.setStatus(doc.getStatus());
        resp.setPageCount(doc.getPageCount());
        resp.setCharacterCount(doc.getCharacterCount());
        resp.setChunkCount(doc.getChunkCount());
        resp.setOcrUsed(doc.isOcrUsed());
        resp.setOcrRequired(doc.isOcrRequired());
        resp.setErrorMessage(doc.getErrorMessage());
        resp.setCompletedAt(doc.getCompletedAt());
        return resp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
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

    public boolean isOcrRequired() {
        return ocrRequired;
    }

    public void setOcrRequired(boolean ocrRequired) {
        this.ocrRequired = ocrRequired;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}

