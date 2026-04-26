package com.specsheetcentral.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final String UPLOADS_PATH_PREFIX = "/uploads/";

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.allow-internal-urls:false}")
    private boolean allowInternalUrls;

    public String store(final MultipartFile file) {
        Objects.requireNonNull(file, "File must not be null");
        final String filename = storeFile(file);
        return UPLOADS_PATH_PREFIX + filename;
    }

    public String storeFile(final MultipartFile file) {
        Objects.requireNonNull(file, "File must not be null");
        try {
            final Path dir = getUploadDir();
            Files.createDirectories(dir);
            final String ext = getExtension(file.getOriginalFilename());
            final String filename = UUID.randomUUID() + ext;
            final Path target = safeResolve(dir, filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }

    public void deleteFile(final String filename) {
        Objects.requireNonNull(filename, "Filename must not be null");
        try {
            final Path dir = getUploadDir();
            final Path target = safeResolve(dir, filename);
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file", e);
        }
    }

    public String sanitizeUrl(final String url) {
        Objects.requireNonNull(url, "URL must not be null");
        try {
            final URL urlObj = URI.create(url).toURL();
            if (!"http".equalsIgnoreCase(urlObj.getProtocol()) && !"https".equalsIgnoreCase(urlObj.getProtocol())) {
                throw new IllegalArgumentException("Only HTTP and HTTPS URLs are supported");
            }
            validateNotInternalUrl(urlObj);
            return url;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to validate URL", e);
        }
    }

    public String fetchAndStore(final String url) {
        Objects.requireNonNull(url, "URL must not be null");
        try {
            final URL urlObj = URI.create(url).toURL();
            if (!"http".equalsIgnoreCase(urlObj.getProtocol()) && !"https".equalsIgnoreCase(urlObj.getProtocol())) {
                throw new IllegalArgumentException("Only HTTP and HTTPS URLs are supported");
            }
            validateNotInternalUrl(urlObj);
            final HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(30_000);
            connection.setRequestMethod("GET");

            final int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IllegalArgumentException("Failed to fetch PDF from URL: HTTP " + responseCode);
            }

            final String contentType = connection.getContentType();
            if (contentType != null && !contentType.toLowerCase().contains("pdf")) {
                throw new IllegalArgumentException("Not a PDF: Content-Type is " + contentType);
            }

            final Path dir = getUploadDir();
            Files.createDirectories(dir);
            final String filename = UUID.randomUUID() + ".pdf";
            final Path target = safeResolve(dir, filename);

            try (final InputStream in = connection.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            return filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to fetch PDF from URL", e);
        }
    }

    private void validateNotInternalUrl(final URL url) throws IOException {
        if (allowInternalUrls) {
            return;
        }
        final InetAddress resolved = InetAddress.getByName(url.getHost());
        if (resolved.isLoopbackAddress() || resolved.isSiteLocalAddress()
                || resolved.isLinkLocalAddress() || resolved.isAnyLocalAddress()) {
            throw new IllegalArgumentException("URLs pointing to internal networks are not allowed");
        }
    }

    private Path getUploadDir() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private Path safeResolve(final Path dir, final String filename) {
        try {
            final Path target = dir.resolve(filename).toAbsolutePath().normalize();
            if (!target.startsWith(dir)) {
                throw new IllegalArgumentException("Invalid filename");
            }
            return target;
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid filename");
        }
    }

    private String getExtension(final String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        final int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex == -1) {
            return "";
        }
        final String ext = originalFilename.substring(dotIndex);
        if (ext.contains("/") || ext.contains("\\") || ext.contains("\0")) {
            return "";
        }
        return ext;
    }
}
