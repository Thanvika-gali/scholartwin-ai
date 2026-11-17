package com.aischolartwin.service;

import com.aischolartwin.entity.ResumeFileType;
import com.aischolartwin.exception.InvalidPayloadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class ResumeFileValidatorServiceTest {

    private ResumeFileValidatorService validatorService;

    @BeforeEach
    void setUp() {
        validatorService = new ResumeFileValidatorService(20 * 1024 * 1024);
    }

    @Test
    @DisplayName("Valid PDF with %PDF- header succeeds")
    void validPdf_succeeds() {
        byte[] pdfContent = "%PDF-1.4 sample content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "my_resume.pdf", "application/pdf", pdfContent);

        ResumeFileType type = validatorService.validateAndDetermineType(file);
        assertEquals(ResumeFileType.PDF, type);
    }

    @Test
    @DisplayName("Valid DOCX with PK header succeeds")
    void validDocx_succeeds() {
        byte[] docxContent = new byte[]{0x50, 0x4B, 0x03, 0x04, 0x14, 0x00, 0x06, 0x00};
        MockMultipartFile file = new MockMultipartFile(
                "file", "my_resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxContent);

        ResumeFileType type = validatorService.validateAndDetermineType(file);
        assertEquals(ResumeFileType.DOCX, type);
    }

    @Test
    @DisplayName("File exceeding 20 MB limit is rejected with informative message")
    void oversizedFile_rejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "large_resume.pdf", "application/pdf", new byte[21 * 1024 * 1024]);

        InvalidPayloadException ex = assertThrows(InvalidPayloadException.class,
                () -> validatorService.validateAndDetermineType(file));
        assertTrue(ex.getMessage().contains("exceeds the maximum allowed limit of 20 MB"));
    }

    @Test
    @DisplayName("Empty file is rejected")
    void emptyFile_rejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        assertThrows(InvalidPayloadException.class, () -> validatorService.validateAndDetermineType(file));
    }

    @Test
    @DisplayName("Malicious file with .pdf extension but executable content is rejected")
    void disguisedFile_rejectedByMagicBytes() {
        byte[] exeContent = new byte[]{0x4D, 0x5A, (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile(
                "file", "trojan.pdf", "application/pdf", exeContent);

        InvalidPayloadException ex = assertThrows(InvalidPayloadException.class,
                () -> validatorService.validateAndDetermineType(file));
        assertTrue(ex.getMessage().contains("does not match a valid PDF document signature"));
    }

    @Test
    @DisplayName("Unsupported file extension is rejected")
    void unsupportedExtension_rejected() {
        byte[] content = "some code".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.sh", "text/plain", content);

        assertThrows(InvalidPayloadException.class, () -> validatorService.validateAndDetermineType(file));
    }

    @Test
    @DisplayName("Path traversal in filename is rejected")
    void pathTraversalFilename_rejected() {
        byte[] pdfContent = "%PDF-1.4 test".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../etc/passwd.pdf", "application/pdf", pdfContent);

        InvalidPayloadException ex = assertThrows(InvalidPayloadException.class,
                () -> validatorService.validateAndDetermineType(file));
        assertTrue(ex.getMessage().contains("path traversal"));
    }
}
