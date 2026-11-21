package com.sarpraskost.app.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Gagal membuat direktori upload: " + this.uploadDir, ex);
        }
    }

    public String storeFoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ukuran foto maksimal 5 MB.");
        }

        String extension = resolveExtension(file.getOriginalFilename(), file.getContentType());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Format foto harus JPG, PNG, atau WEBP."
            );
        }

        String filename = "lpr-" + UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama file tidak valid.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Gagal menyimpan file foto ke server."
            );
        }

        return "/uploads/" + filename;
    }

    public Path getUploadDir() {
        return uploadDir;
    }

    public void deleteFoto(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.trim().isEmpty()) {
            return;
        }
        String trimmed = fotoUrl.trim();
        String relative;
        if (trimmed.startsWith("/uploads/")) {
            relative = trimmed.substring("/uploads/".length());
        } else if (trimmed.startsWith("uploads/")) {
            relative = trimmed.substring("uploads/".length());
        } else {
            return;
        }
        if (relative.isEmpty()) {
            return;
        }
        try {
            Path target = uploadDir.resolve(relative).normalize();
            if (!target.startsWith(uploadDir) || target.equals(uploadDir)) {
                return;
            }
            Files.deleteIfExists(target);
        } catch (InvalidPathException | IOException ex) {
            log.warn("Gagal menghapus file foto {}: {}", fotoUrl, ex.getMessage());
        }
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                return originalFilename.substring(dotIndex).toLowerCase();
            }
        }
        if (contentType != null) {
            return switch (contentType.toLowerCase()) {
                case "image/jpeg", "image/jpg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> "";
            };
        }
        return "";
    }
}
