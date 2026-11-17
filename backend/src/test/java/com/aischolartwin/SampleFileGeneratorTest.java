package com.aischolartwin;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SampleFileGeneratorTest {

    @Test
    void generateSampleDocuments() throws IOException {
        File outDir = new File("target/sample_docs");
        outDir.mkdirs();

        // 1. Generate multi-page PDF
        File pdfFile = new File(outDir, "ai_research_paper.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Page 1: Deep Neural Architectures in Modern Knowledge Retrieval Systems.");
                cs.newLineAtOffset(0, -30);
                cs.showText("Section 1: Vector Embeddings and Dense Representations for Academic Documents.");
                cs.endText();
            }

            PDPage page2 = new PDPage();
            doc.addPage(page2);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Page 2: Hierarchical Semantic Chunking and Overlap Analysis.");
                cs.newLineAtOffset(0, -30);
                cs.showText("Conclusions and Empirical Evaluation across Benchmark Datasets.");
                cs.endText();
            }
            doc.save(pdfFile);
        }
        System.out.println("GENERATED_PDF: " + pdfFile.getAbsolutePath());

        // 2. Generate DOCX
        File docxFile = new File(outDir, "thesis_methodology.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream fos = new FileOutputStream(docxFile)) {
            XWPFParagraph p1 = doc.createParagraph();
            p1.createRun().setText("Chapter 3: Methodology and Extraction Infrastructure.");

            XWPFTable table = doc.createTable(2, 2);
            table.getRow(0).getCell(0).setText("Component");
            table.getRow(0).getCell(1).setText("Technology");
            table.getRow(1).getCell(0).setText("PDF Parsing");
            table.getRow(1).getCell(1).setText("Apache PDFBox 3.0.3");

            XWPFParagraph p2 = doc.createParagraph();
            p2.createRun().setText("Discussion on OCR fallback thresholds and heuristic image detection.");
            doc.write(fos);
        }
        System.out.println("GENERATED_DOCX: " + docxFile.getAbsolutePath());

        // 3. Generate pure scanned PDF (image only, zero digital text)
        File scannedPdf = new File(outDir, "synthetic_scanned_paper.pdf");
        BufferedImage img = new BufferedImage(1000, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 1000, 200);
        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.drawString("AI ScholarTwin Autonomous Scanned Document OCR Benchmark 2026", 25, 70);
        g.drawString("Verification of local offline Tesseract engine pipeline.", 25, 120);
        g.dispose();

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            var pdImage = LosslessFactory.createFromImage(doc, img);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.drawImage(pdImage, 50, 550, 500, 100);
            }
            doc.save(scannedPdf);
        }
        System.out.println("GENERATED_SCANNED_PDF: " + scannedPdf.getAbsolutePath());

        // 4. Generate mixed PDF (Page 1 digital text, Page 2 scanned image)
        File mixedPdf = new File(outDir, "synthetic_mixed_paper.pdf");
        try (PDDocument doc = new PDDocument()) {
            // Page 1 Digital
            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Page 1: Digital Section on Distributed Machine Learning Systems.");
                cs.newLineAtOffset(0, -30);
                cs.showText("Review of Parameter Servers and AllReduce Communication Topologies.");
                cs.endText();
            }

            // Page 2 Scanned
            PDPage page2 = new PDPage();
            doc.addPage(page2);
            BufferedImage img2 = new BufferedImage(1000, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img2.createGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, 1000, 200);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            g2.drawString("Page 2: Scanned Experimental Telemetry and Loss Graphs", 25, 70);
            g2.drawString("Measured network bandwidth: 100 Gbps RoCE v2 interconnect.", 25, 120);
            g2.dispose();

            var pdImage2 = LosslessFactory.createFromImage(doc, img2);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page2)) {
                cs.drawImage(pdImage2, 50, 550, 500, 100);
            }
            doc.save(mixedPdf);
        }
        System.out.println("GENERATED_MIXED_PDF: " + mixedPdf.getAbsolutePath());
    }

    @Test
    void generateAlexResume() throws IOException {
        File outDir = new File("target/sample_docs");
        outDir.mkdirs();

        File resumePdf = new File(outDir, "candidate_alex_resume.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("Dr. Alexander Wright");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -18);
                cs.showText("alexander.wright@scholartwin.edu | +1 (555) 019-2834");

                cs.newLineAtOffset(0, -14);
                cs.showText("https://github.com/alexwright | https://linkedin.com/in/alexwright | https://alexwright.dev");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("SUMMARY");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("Results-driven Senior Full Stack and AI Systems Engineer with 4+ years experience.");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("EDUCATION");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("Stanford University - Master of Science in Computer Science (2024)");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("EXPERIENCE");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("Senior Software Engineer at CloudScale Systems (2022 - Present)");
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("- Architected and scaled distributed backend pipelines using Java, Spring Boot, MySQL, and Redis.");
                cs.newLineAtOffset(0, -14);
                cs.showText("- Reduced API p99 latency by 35% through query caching, indexing, and connection pool optimization.");
                cs.newLineAtOffset(0, -14);
                cs.showText("- Led migration of monolithic services to Docker and Kubernetes on AWS.");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("PROJECTS");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("ScholarTwin Academic Knowledge Engine");
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("Tech: Java, Spring Boot, React, MySQL, Docker, REST");
                cs.newLineAtOffset(0, -14);
                cs.showText("https://github.com/alexwright/scholartwin-engine | https://scholartwin.demo.app");
                cs.newLineAtOffset(0, -14);
                cs.showText("- Designed end-to-end full-stack academic document processing platform with PDFBox extraction.");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("TECHNICAL SKILLS");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("Java, Python, JavaScript, TypeScript, Spring Boot, React, Node.js, MySQL, PostgreSQL, Redis, Docker, Kubernetes, AWS, Git, REST, Microservices, Algorithms, Data Structures");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("CERTIFICATIONS");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("AWS Certified Solutions Architect | Amazon Web Services | 2023");
                cs.newLineAtOffset(0, -14);
                cs.showText("Oracle Certified Professional: Java SE 17 Developer | Oracle | 2023");
                cs.endText();
            }
            doc.save(resumePdf);
        }
        System.out.println("GENERATED_ALEX_RESUME_PDF: " + resumePdf.getAbsolutePath());
    }

    @Test
    void generateLargeAcademicPaper() throws IOException {
        File outDir = new File("target/sample_docs");
        outDir.mkdirs();

        File largePdf = new File(outDir, "large_academic_paper_28mb.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page1)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Large Academic Research Paper on Distributed Systems Architecture.");
                cs.newLineAtOffset(0, -30);
                cs.showText("Section 1: Distributed Memory and High-Throughput IO Pipelines.");
                cs.endText();
            }

            // Create 3100x3000 image with pseudo-random noise to make it incompressible by Flate
            int width = 3100;
            int height = 3000;
            byte[] noise = new byte[width * height * 3];
            java.util.Random rnd = new java.util.Random(1337);
            rnd.nextBytes(noise);

            java.awt.image.DataBufferByte buffer = new java.awt.image.DataBufferByte(noise, noise.length);
            java.awt.image.WritableRaster raster = java.awt.image.Raster.createInterleavedRaster(
                    buffer, width, height, 3 * width, 3, new int[]{0, 1, 2}, null);
            java.awt.image.ColorModel cm = new java.awt.image.ComponentColorModel(
                    java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_sRGB),
                    false, false, java.awt.Transparency.OPAQUE, java.awt.image.DataBuffer.TYPE_BYTE);
            BufferedImage img = new BufferedImage(cm, raster, false, null);

            var pdImage = LosslessFactory.createFromImage(doc, img);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page1, PDPageContentStream.AppendMode.APPEND, false)) {
                cs.drawImage(pdImage, 50, 100, 200, 200);
            }

            doc.save(largePdf);
        }
        System.out.println("GENERATED_LARGE_PDF: " + largePdf.getAbsolutePath() + " (" + (largePdf.length() / (1024 * 1024)) + " MB)");
    }

    @Test
    void generateSampleResumes() throws IOException {
        File outDir = new File("target/sample_docs");
        outDir.mkdirs();

        // 1. Generate realistic candidate resume PDF (v1)
        File resumePdf = new File(outDir, "candidate_resume_v1.pdf");
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                cs.newLineAtOffset(50, 750);
                cs.showText("Alice Developer");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -18);
                cs.showText("Email: alice.developer@scholartwin.io | Phone: +1 (555) 987-6543 | Location: Seattle, WA");

                cs.newLineAtOffset(0, -14);
                cs.showText("GitHub: https://github.com/alicedev | LinkedIn: https://linkedin.com/in/alicedev | Portfolio: https://alicedev.io");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("PROFESSIONAL SUMMARY");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("Software Engineer with 4+ years experience architecting distributed backend microservices and modern web apps.");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("TECHNICAL SKILLS");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("Languages & Frameworks: Java, Spring Boot, React, TypeScript, Python, SQL");
                cs.newLineAtOffset(0, -14);
                cs.showText("Databases & Cloud: MySQL, PostgreSQL, Redis, Docker, AWS, Git, Maven, REST APIs");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("WORK EXPERIENCE");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("Senior Software Engineer - CloudScale Technologies (2022 - Present)");
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("- Architected scalable microservices using Java and Spring Boot handling 10M daily requests.");
                cs.newLineAtOffset(0, -14);
                cs.showText("- Decreased query latency by 35% through Redis caching and MySQL index optimizations.");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("PROJECTS");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("ScholarTwin Distributed Knowledge Hub");
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("- Built end-to-end full stack application using React, Spring Boot, and MySQL with 99.9% uptime.");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.newLineAtOffset(0, -22);
                cs.showText("EDUCATION & CERTIFICATIONS");

                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                cs.newLineAtOffset(0, -14);
                cs.showText("B.S. in Computer Science - University of Washington (2020)");
                cs.newLineAtOffset(0, -14);
                cs.showText("Certifications: AWS Certified Solutions Architect (Associate)");
                cs.endText();
            }
            doc.save(resumePdf);
        }
        System.out.println("GENERATED_RESUME_PDF: " + resumePdf.getAbsolutePath());

        // 2. Generate updated candidate resume DOCX (v2)
        File resumeDocx = new File(outDir, "candidate_resume_v2.docx");
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream fos = new FileOutputStream(resumeDocx)) {
            XWPFParagraph p1 = doc.createParagraph();
            p1.createRun().setText("Alice Developer - Senior Full Stack Engineer");

            XWPFParagraph p2 = doc.createParagraph();
            p2.createRun().setText("Contact: alice.developer@scholartwin.io | +1 (555) 987-6543 | Seattle, WA");

            XWPFParagraph p3 = doc.createParagraph();
            p3.createRun().setText("Skills: Java, Spring Boot, React, TypeScript, MySQL, Redis, Docker, Kubernetes, AWS, Microservices");

            XWPFParagraph p4 = doc.createParagraph();
            p4.createRun().setText("Experience: Lead Engineer at CloudScale Technologies. Orchestrated Kubernetes clusters and microservices.");

            doc.write(fos);
        }
        System.out.println("GENERATED_RESUME_DOCX: " + resumeDocx.getAbsolutePath());
    }
}
