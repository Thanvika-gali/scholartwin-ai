package com.aischolartwin.controller;

import com.aischolartwin.entity.Role;
import com.aischolartwin.entity.User;
import com.aischolartwin.repository.DocumentChunkRepository;
import com.aischolartwin.repository.DocumentRepository;
import com.aischolartwin.repository.UserRepository;
import com.aischolartwin.security.JwtService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DocumentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User primaryUser;
    private User otherUser;
    private String primaryToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        primaryUser = new User("Primary Researcher", "researcher1@univ.edu", passwordEncoder.encode("Secret123!"), Role.USER);
        primaryUser = userRepository.saveAndFlush(primaryUser);
        primaryToken = jwtService.generateToken(primaryUser);

        otherUser = new User("Other Researcher", "researcher2@univ.edu", passwordEncoder.encode("Secret123!"), Role.USER);
        otherUser = userRepository.saveAndFlush(otherUser);
        otherToken = jwtService.generateToken(otherUser);
    }

    private byte[] createSamplePdfBytes(String text) throws IOException {
        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(100, 700);
                cs.showText(text);
                cs.endText();
            }
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] createSampleDocxBytes(String text) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText(text);
            doc.write(baos);
            return baos.toByteArray();
        }
    }

    @Test
    @DisplayName("Unauthenticated requests return 401 Unauthorized")
    void unauthenticatedRequestsShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Upload valid PDF extracts text, creates chunks, and marks status COMPLETED")
    void uploadValidPdfShouldSucceed() throws Exception {
        byte[] pdfBytes = createSamplePdfBytes("Foundations of Machine Learning in Academic Settings.");
        MockMultipartFile file = new MockMultipartFile("file", "ml_foundations.pdf", "application/pdf", pdfBytes);

        mockMvc.perform(multipart("/api/documents")
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.originalFilename").value("ml_foundations.pdf"))
                .andExpect(jsonPath("$.format").value("PDF"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.pageCount").value(1))
                .andExpect(jsonPath("$.chunkCount").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.ocrRequired").value(false));
    }

    @Test
    @DisplayName("Upload valid DOCX extracts text, creates chunks, and marks status COMPLETED")
    void uploadValidDocxShouldSucceed() throws Exception {
        byte[] docxBytes = createSampleDocxBytes("Academic Thesis Chapter on Knowledge Management.");
        MockMultipartFile file = new MockMultipartFile("file", "thesis_ch1.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docxBytes);

        mockMvc.perform(multipart("/api/documents")
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.originalFilename").value("thesis_ch1.docx"))
                .andExpect(jsonPath("$.format").value("DOCX"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.pageCount").value(1))
                .andExpect(jsonPath("$.chunkCount").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Upload invalid file type or spoofed extension returns 400 Bad Request")
    void uploadInvalidFileShouldFail() throws Exception {
        MockMultipartFile exeFile = new MockMultipartFile("file", "malware.exe", "application/octet-stream",
                new byte[]{'M', 'Z', 0, 0, 0});

        mockMvc.perform(multipart("/api/documents")
                .file(exeFile)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));

        // Spoofed PDF with text content but not %PDF header
        MockMultipartFile fakePdf = new MockMultipartFile("file", "fake.pdf", "application/pdf",
                "This is not a real PDF file".getBytes());

        mockMvc.perform(multipart("/api/documents")
                .file(fakePdf)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Document retrieval, content, chunks, status, and download work for owner")
    void documentLifecycleShouldWorkForOwner() throws Exception {
        byte[] pdfBytes = createSamplePdfBytes("Full text of research paper on Deep Learning.");
        MockMultipartFile file = new MockMultipartFile("file", "deep_learning.pdf", "application/pdf", pdfBytes);

        MvcResult uploadResult = mockMvc.perform(multipart("/api/documents")
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = uploadResult.getResponse().getContentAsString();
        // Parse ID from response
        Long documentId = com.jayway.jsonpath.JsonPath.parse(responseBody).read("$.id", Long.class);

        // 1. List documents
        mockMvc.perform(get("/api/documents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(documentId));

        // 2. Get metadata
        mockMvc.perform(get("/api/documents/" + documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentId))
                .andExpect(jsonPath("$.originalFilename").value("deep_learning.pdf"));

        // 3. Get status
        mockMvc.perform(get("/api/documents/" + documentId + "/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 4. Get content
        mockMvc.perform(get("/api/documents/" + documentId + "/content")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullText", containsString("Deep Learning")))
                .andExpect(jsonPath("$.chunks", hasSize(greaterThanOrEqualTo(1))));

        // 5. Get chunks
        mockMvc.perform(get("/api/documents/" + documentId + "/chunks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].chunkIndex").value(0))
                .andExpect(jsonPath("$[0].startPage").value(1));

        // 6. Download original
        mockMvc.perform(get("/api/documents/" + documentId + "/download")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("deep_learning.pdf")));

        // 7. Retry processing
        mockMvc.perform(post("/api/documents/" + documentId + "/retry")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 8. Delete document
        mockMvc.perform(delete("/api/documents/" + documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isNoContent());

        // Verify document is deleted
        mockMvc.perform(get("/api/documents/" + documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cross-user isolation: User B cannot access or modify User A's document")
    void crossUserIsolationShouldBeStrictlyEnforced() throws Exception {
        byte[] pdfBytes = createSamplePdfBytes("Confidential research data belonging to User A.");
        MockMultipartFile file = new MockMultipartFile("file", "user_a_private.pdf", "application/pdf", pdfBytes);

        MvcResult uploadResult = mockMvc.perform(multipart("/api/documents")
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + primaryToken))
                .andExpect(status().isCreated())
                .andReturn();

        Long documentId = com.jayway.jsonpath.JsonPath.parse(uploadResult.getResponse().getContentAsString()).read("$.id", Long.class);

        // User B tries to view User A's document -> 404
        mockMvc.perform(get("/api/documents/" + documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // User B tries to get status -> 404
        mockMvc.perform(get("/api/documents/" + documentId + "/status")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // User B tries to get content -> 404
        mockMvc.perform(get("/api/documents/" + documentId + "/content")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // User B tries to get chunks -> 404
        mockMvc.perform(get("/api/documents/" + documentId + "/chunks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // User B tries to download -> 404
        mockMvc.perform(get("/api/documents/" + documentId + "/download")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // User B tries to retry -> 404
        mockMvc.perform(post("/api/documents/" + documentId + "/retry")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // User B tries to delete -> 404
        mockMvc.perform(delete("/api/documents/" + documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        // User B listing documents sees 0 documents
        mockMvc.perform(get("/api/documents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
