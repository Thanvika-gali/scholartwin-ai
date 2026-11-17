package com.aischolartwin.dto;

import com.aischolartwin.entity.Document;
import com.aischolartwin.entity.DocumentFormat;
import com.aischolartwin.entity.DocumentStatus;

import java.time.Instant;

public class DocumentResponse {

    private Long id;
    private String originalFilename;
    private String fileExtension;
    private String contentType;
    private long fileSize;
    private DocumentFormat format;
    private DocumentStatus status;
    private Integer pageCount;
    private int characterCount;
    private int chunkCount;
    private boolean ocrUsed;
    private boolean ocrRequired;
    private String errorMessage;
    private Long folderId;
    private boolean isFavorite;
    private boolean isPinned;
    private boolean isArchived;
    private String documentSummary;
    private int versionNumber = 1;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    public DocumentResponse() {
    }

    public static DocumentResponse fromEntity(Document doc) {
        DocumentResponse resp = new DocumentResponse();
        resp.setId(doc.getId());
        resp.setOriginalFilename(doc.getOriginalFilename());
        resp.setFileExtension(doc.getFileExtension());
        resp.setContentType(doc.getContentType());
        resp.setFileSize(doc.getFileSize());
        resp.setFormat(doc.getFormat());
        resp.setStatus(doc.getStatus());
        resp.setPageCount(doc.getPageCount());
        resp.setCharacterCount(doc.getCharacterCount());
        resp.setChunkCount(doc.getChunkCount());
        resp.setOcrUsed(doc.isOcrUsed());
        resp.setOcrRequired(doc.isOcrRequired());
        resp.setErrorMessage(doc.getErrorMessage());
        resp.setFolderId(doc.getFolderId());
        resp.setFavorite(doc.isFavorite());
        resp.setPinned(doc.isPinned());
        resp.setArchived(doc.isArchived());
        resp.setDocumentSummary(doc.getDocumentSummary());
        resp.setVersionNumber(doc.getVersionNumber());
        resp.setCreatedAt(doc.getCreatedAt());
        resp.setUpdatedAt(doc.getUpdatedAt());
        resp.setCompletedAt(doc.getCompletedAt());
        return resp;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public String getDocumentSummary() {
        return documentSummary;
    }

    public void setDocumentSummary(String documentSummary) {
        this.documentSummary = documentSummary;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
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

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public DocumentFormat getFormat() {
        return format;
    }

    public void setFormat(DocumentFormat format) {
        this.format = format;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}

