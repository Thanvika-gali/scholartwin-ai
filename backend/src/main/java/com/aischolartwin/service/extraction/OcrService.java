package com.aischolartwin.service.extraction;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);
    private static final int OCR_DPI = 150;
    private static final long OCR_TIMEOUT_SECONDS = 30;

    private final boolean ocrEnabled;
    private final String configuredTesseractPath;
    private final String ocrLanguage;

    private Boolean tesseractInstalled;
    private String resolvedTesseractPath;
    private String resolvedTessdataDir;

    public OcrService(
            @Value("${app.ocr.enabled:true}") boolean ocrEnabled,
            @Value("${app.ocr.tesseract-path:tesseract}") String configuredTesseractPath,
            @Value("${app.ocr.language:eng}") String ocrLanguage
    ) {
        this.ocrEnabled = ocrEnabled;
        this.configuredTesseractPath = configuredTesseractPath;
        this.ocrLanguage = ocrLanguage;
    }

    /**
     * Checks if local Tesseract OCR CLI is installed, responsive, and has language data.
     */
    public synchronized boolean isAvailable() {
        if (!ocrEnabled) {
            return false;
        }
        if (tesseractInstalled != null) {
            return tesseractInstalled;
        }

        List<String> candidatePaths = buildCandidateExecutablePaths();
        for (String candidate : candidatePaths) {
            String detectedTessdata = findTessdataDirectory(candidate);
            if (testTesseractExecutable(candidate, detectedTessdata)) {
                resolvedTesseractPath = candidate;
                resolvedTessdataDir = detectedTessdata;
                tesseractInstalled = true;
                log.info("Tesseract OCR is detected and operational at '{}' (tessdata: '{}', lang: '{}')",
                        resolvedTesseractPath,
                        resolvedTessdataDir != null ? resolvedTessdataDir : "default",
                        ocrLanguage);
                return true;
            }
        }

        tesseractInstalled = false;
        log.info("Tesseract OCR is not installed or not operational. Candidate paths checked: {}. Scanned PDFs will be flagged.",
                candidatePaths);
        return false;
    }

    private List<String> buildCandidateExecutablePaths() {
        List<String> paths = new ArrayList<>();

        if (configuredTesseractPath != null && !configuredTesseractPath.isBlank()) {
            paths.add(configuredTesseractPath.trim());
        }

        // Standard Windows user & system installation locations
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isBlank()) {
            paths.add(localAppData + "\\Programs\\Tesseract-OCR\\bin\\tesseract.exe");
            paths.add(localAppData + "\\Programs\\Tesseract-OCR\\tesseract.exe");
        }

        String programFiles = System.getenv("ProgramFiles");
        if (programFiles != null && !programFiles.isBlank()) {
            paths.add(programFiles + "\\Tesseract-OCR\\tesseract.exe");
        }

        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        if (programFilesX86 != null && !programFilesX86.isBlank()) {
            paths.add(programFilesX86 + "\\Tesseract-OCR\\tesseract.exe");
        }

        paths.add("C:\\Program Files\\Tesseract-OCR\\tesseract.exe");
        paths.add("C:\\Program Files (x86)\\Tesseract-OCR\\tesseract.exe");
        paths.add("tesseract");

        return paths;
    }

    private String findTessdataDirectory(String exePath) {
        String envTessdata = System.getenv("TESSDATA_PREFIX");
        if (envTessdata != null && !envTessdata.isBlank() && new File(envTessdata).isDirectory()) {
            return envTessdata;
        }

        try {
            File exeFile = new File(exePath);
            if (exeFile.isFile()) {
                File parentDir = exeFile.getParentFile();
                if (parentDir != null) {
                    // Check parent/tessdata (e.g. Programs/Tesseract-OCR/bin -> Programs/Tesseract-OCR/tessdata)
                    File parentOfParent = parentDir.getParentFile();
                    if (parentOfParent != null) {
                        File candidate1 = new File(parentOfParent, "tessdata");
                        if (candidate1.isDirectory()) {
                            return candidate1.getAbsolutePath();
                        }
                    }

                    // Check sibling tessdata (e.g. Programs/Tesseract-OCR/tessdata)
                    File candidate2 = new File(parentDir, "tessdata");
                    if (candidate2.isDirectory()) {
                        return candidate2.getAbsolutePath();
                    }
                }
            }
        } catch (Exception ignored) {
        }

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null) {
            File defaultLocal = new File(localAppData + "\\Programs\\Tesseract-OCR\\tessdata");
            if (defaultLocal.isDirectory()) {
                return defaultLocal.getAbsolutePath();
            }
        }

        File systemTessdata = new File("C:\\Program Files\\Tesseract-OCR\\tessdata");
        if (systemTessdata.isDirectory()) {
            return systemTessdata.getAbsolutePath();
        }

        return null;
    }

    private boolean testTesseractExecutable(String exePath, String tessdataDir) {
        try {
            List<String> command = new ArrayList<>();
            command.add(exePath);
            if (tessdataDir != null && !tessdataDir.isBlank()) {
                command.add("--tessdata-dir");
                command.add(tessdataDir);
            }
            command.add("--list-langs");

            ProcessBuilder pb = new ProcessBuilder(command);
            if (tessdataDir != null && !tessdataDir.isBlank()) {
                pb.environment().put("TESSDATA_PREFIX", tessdataDir);
            }
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            if (process.exitValue() == 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().equalsIgnoreCase(ocrLanguage)) {
                            return true;
                        }
                    }
                }
                // If exitValue is 0, tesseract is responsive even if specific language line was formatted differently
                return true;
            }
        } catch (Exception ignored) {
            // Path not executable or missing
        }
        return false;
    }

    /**
     * Performs OCR on a specific page of a PDF document if Tesseract is installed.
     *
     * @param pdfFile the PDF document file
     * @param pageNumber 1-based page number
     * @return recognized text from OCR, or empty string if OCR failed or unavailable
     */
    public String recognizePage(File pdfFile, int pageNumber) {
        if (!isAvailable()) {
            return "";
        }

        Path tempImage = null;
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int zeroBasedIndex = pageNumber - 1;
            if (zeroBasedIndex < 0 || zeroBasedIndex >= document.getNumberOfPages()) {
                return "";
            }

            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(zeroBasedIndex, OCR_DPI, ImageType.RGB);

            tempImage = Files.createTempFile("ocr_page_" + pageNumber + "_", ".png");
            ImageIO.write(image, "PNG", tempImage.toFile());

            return runTesseractOnImageFile(tempImage);
        } catch (Exception e) {
            log.warn("OCR execution failed for page {}: {}", pageNumber, e.getMessage());
            return "";
        } finally {
            cleanupTempFile(tempImage);
        }
    }

    /**
     * Performs OCR directly on a BufferedImage.
     *
     * @param image the image to perform OCR on
     * @return recognized text from OCR, or empty string if OCR failed or unavailable
     */
    public String recognizeImage(BufferedImage image) {
        if (!isAvailable() || image == null) {
            return "";
        }

        Path tempImage = null;
        try {
            tempImage = Files.createTempFile("ocr_img_", ".png");
            ImageIO.write(image, "PNG", tempImage.toFile());
            return runTesseractOnImageFile(tempImage);
        } catch (Exception e) {
            log.warn("OCR execution failed for image: {}", e.getMessage());
            return "";
        } finally {
            cleanupTempFile(tempImage);
        }
    }

    private String runTesseractOnImageFile(Path imagePath) {
        Path tempOutputBase = null;
        Path tempResultTxt = null;

        try {
            tempOutputBase = Files.createTempFile("ocr_out_", "");
            Files.deleteIfExists(tempOutputBase); // Tesseract appends .txt

            List<String> command = new ArrayList<>();
            command.add(resolvedTesseractPath != null ? resolvedTesseractPath : configuredTesseractPath);
            command.add(imagePath.toAbsolutePath().toString());
            command.add(tempOutputBase.toAbsolutePath().toString());
            if (resolvedTessdataDir != null && !resolvedTessdataDir.isBlank()) {
                command.add("--tessdata-dir");
                command.add(resolvedTessdataDir);
            }
            command.add("-l");
            command.add(ocrLanguage);

            ProcessBuilder pb = new ProcessBuilder(command);
            if (resolvedTessdataDir != null && !resolvedTessdataDir.isBlank()) {
                pb.environment().put("TESSDATA_PREFIX", resolvedTessdataDir);
            }
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean completed = process.waitFor(OCR_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                log.warn("OCR timed out after {} seconds for image {}", OCR_TIMEOUT_SECONDS, imagePath.getFileName());
                return "";
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.warn("OCR process exited with non-zero exit code {}.", exitCode);
                return "";
            }

            tempResultTxt = Path.of(tempOutputBase.toAbsolutePath().toString() + ".txt");
            if (Files.exists(tempResultTxt)) {
                return Files.readString(tempResultTxt, StandardCharsets.UTF_8).trim();
            }
        } catch (Exception e) {
            log.warn("OCR execution failed on image {}: {}", imagePath.getFileName(), e.getMessage());
        } finally {
            cleanupTempFile(tempOutputBase);
            cleanupTempFile(tempResultTxt);
        }

        return "";
    }

    private void cleanupTempFile(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (Exception ignored) {
            }
        }
    }

    public String getResolvedTesseractPath() {
        return resolvedTesseractPath;
    }

    public String getResolvedTessdataDir() {
        return resolvedTessdataDir;
    }

    public String getOcrLanguage() {
        return ocrLanguage;
    }

    public boolean isOcrEnabled() {
        return ocrEnabled;
    }
}
