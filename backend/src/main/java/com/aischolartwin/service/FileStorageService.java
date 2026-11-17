package com.aischolartwin.service;

import com.aischolartwin.exception.InvalidPayloadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageRoot;

    public FileStorageService(@Value("${app.document.storage-dir:uploads/documents}") String storageDir) {
        this.storageRoot = Paths.get(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageRoot);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize document storage directory: " + this.storageRoot, e);
        }
    }

    public Path getStorageRoot() {
        return storageRoot;
    }

    /**
     * Stores a file on disk using a safe server-generated UUID filename.
     *
     * @param file the uploaded file
     * @param extension clean extension without leading dot (e.g. "pdf", "docx")
     * @return the relative storage filename/path
     */
    public String store(MultipartFile file, String extension) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPayloadException("Cannot store an empty file.");
        }

        String safeExtension = extension != null ? extension.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() : "bin";
        String uniqueFilename = UUID.randomUUID() + "." + safeExtension;
        Path destination = this.storageRoot.resolve(uniqueFilename).normalize();

        // Path traversal defense
        if (!destination.startsWith(this.storageRoot)) {
            throw new InvalidPayloadException("Invalid storage path generated.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            return uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store document file to disk: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves a stored filename safely, checking against path traversal.
     */
    public Path resolve(String filename) {
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new InvalidPayloadException("Invalid file reference.");
        }
        Path path = this.storageRoot.resolve(filename).normalize();
        if (!path.startsWith(this.storageRoot)) {
            throw new InvalidPayloadException("Path traversal attempt detected.");
        }
        return path;
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new InvalidPayloadException("Document file not found or not readable: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new InvalidPayloadException("Invalid document resource URL.", e);
        }
    }

    public void delete(String filename) {
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path path = resolve(filename);
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // Logging can be done, but deletion should not crash transaction rollback
        }
    }

    public boolean exists(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }
        try {
            Path path = resolve(filename);
            return Files.exists(path);
        } catch (Exception e) {
            return false;
        }
    }
}

