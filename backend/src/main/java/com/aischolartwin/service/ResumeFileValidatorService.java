package com.aischolartwin.service;

import com.aischolartwin.entity.ResumeFileType;
import com.aischolartwin.exception.InvalidPayloadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

@Service
public class ResumeFileValidatorService {

    private static final long DEFAULT_MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx");

    // PDF Magic Bytes: %PDF-
    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D};
    // DOCX Magic Bytes: PK\x03\x04 (ZIP container header)
    private static final byte[] ZIP_MAGIC = new byte[]{0x50, 0x4B, 0x03, 0x04};

    private final long maxSizeBytes;

    public ResumeFileValidatorService(@Value("${app.resume.max-file-size-bytes:20971520}") long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes > 0 ? maxSizeBytes : DEFAULT_MAX_FILE_SIZE_BYTES;
    }

    public ResumeFileType validateAndDetermineType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPayloadException("Resume file cannot be empty.");
        }

        if (file.getSize() > this.maxSizeBytes) {
            double actualMb = (double) file.getSize() / (1024 * 1024);
            double limitMb = (double) this.maxSizeBytes / (1024 * 1024);
            throw new InvalidPayloadException(String.format(
                    "Resume file size (%.2f MB) exceeds the maximum allowed limit of %.0f MB.",
                    actualMb, limitMb
            ));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidPayloadException("File must have a valid filename.");
        }

        // Check path traversal in filename
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new InvalidPayloadException("Filename contains illegal path traversal characters.");
        }

        String cleanExtension = extractExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(cleanExtension)) {
            throw new InvalidPayloadException("Unsupported resume file format. Allowed formats are: PDF, DOCX.");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String lowerType = contentType.toLowerCase();
            boolean validMime = lowerType.contains("pdf")
                    || lowerType.contains("word")
                    || lowerType.contains("officedocument")
                    || lowerType.equals("application/octet-stream");
            if (!validMime) {
                throw new InvalidPayloadException("File MIME type (" + contentType + ") does not match allowed resume document types.");
            }
        }

        // Magic Bytes Verification
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < 4) {
                throw new InvalidPayloadException("File is corrupted or too small to be a valid document.");
            }
        } catch (IOException e) {
            throw new InvalidPayloadException("Could not read file header: " + e.getMessage());
        }

        if ("pdf".equals(cleanExtension)) {
            if (!matchesHeader(header, PDF_MAGIC)) {
                throw new InvalidPayloadException("File header does not match a valid PDF document signature.");
            }
            return ResumeFileType.PDF;
        } else if ("docx".equals(cleanExtension)) {
            if (!matchesHeader(header, ZIP_MAGIC)) {
                throw new InvalidPayloadException("File header does not match a valid DOCX document signature.");
            }
            return ResumeFileType.DOCX;
        }

        throw new InvalidPayloadException("Unsupported resume format.");
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1).trim().toLowerCase();
    }

    private boolean matchesHeader(byte[] buffer, byte[] expected) {
        if (buffer.length < expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (buffer[i] != expected[i]) {
                return false;
            }
        }
        return true;
    }
}

