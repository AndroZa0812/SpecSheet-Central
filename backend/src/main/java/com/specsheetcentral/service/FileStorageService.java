package com.specsheetcentral.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public String store(MultipartFile file) {
        String filename = storeFile(file);
        return "/uploads/" + filename;
    }

    public String storeFile(MultipartFile file) {
        try {
            Path dir = getUploadDir();
            Files.createDirectories(dir);
            String ext = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + ext;
            Path target = safeResolve(dir, filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }

    public void deleteFile(String filename) {
        try {
            Path dir = getUploadDir();
            Path target = safeResolve(dir, filename);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file", e);
        }
    }

    public String fetchAndStore(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IllegalArgumentException("Failed to fetch PDF from URL: HTTP " + responseCode);
            }

            String contentType = connection.getContentType();
            if (contentType == null || !contentType.toLowerCase().contains("pdf")) {
                throw new IllegalArgumentException("Not a PDF: Content-Type is " + contentType);
            }

            Path dir = getUploadDir();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + ".pdf";
            Path target = safeResolve(dir, filename);

            try (InputStream in = connection.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to fetch PDF from URL", e);
        }
    }

    private Path getUploadDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private Path safeResolve(Path dir, String filename) {
        try {
            Path target = dir.resolve(filename).toAbsolutePath().normalize();
            if (!target.startsWith(dir)) {
                throw new IllegalArgumentException("Invalid filename");
            }
            return target;
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid filename");
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }
        return originalFilename.substring(dotIndex);
    }
}
