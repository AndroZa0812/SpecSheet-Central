package com.specsheetcentral.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
    }

    @Test
    void shouldStoreFileAndReturnUrl() {
        final MockMultipartFile file = new MockMultipartFile(
            "file", "test.png", "image/png", "test-image-content".getBytes());

        final String url = fileStorageService.store(file);

        assertThat(url).startsWith("/uploads/").endsWith(".png");
    }

    @Test
    void shouldHandleDifferentExtensions() {
        final MockMultipartFile file = new MockMultipartFile(
            "file", "datasheet.pdf", "application/pdf", "pdf-content".getBytes());

        final String url = fileStorageService.store(file);

        assertThat(url).endsWith(".pdf");
    }

    @Test
    void storeFile_shouldStoreFileAndReturnOnlyFilename() {
        final MockMultipartFile file = new MockMultipartFile(
            "file", "test.png", "image/png", "test-image-content".getBytes());

        final String filename = fileStorageService.storeFile(file);

        assertThat(filename).doesNotStartWith("/uploads/");
        assertThat(filename).endsWith(".png");
        assertThat(tempDir.resolve(filename)).exists();
    }

    @Test
    void storeFile_shouldUseUuidAndOriginalExtension() {
        final MockMultipartFile file = new MockMultipartFile(
            "file", "datasheet.pdf", "application/pdf", "pdf-content".getBytes());

        final String filename = fileStorageService.storeFile(file);

        assertThat(filename).matches("^[a-f0-9\\-]+\\.pdf$");
        assertThat(tempDir.resolve(filename)).exists();
    }

    @Test
    void storeFile_shouldCreateDirectoriesIfNeeded(@TempDir final Path rootTempDir) {
        final Path nestedDir = rootTempDir.resolve("nested/uploads");
        final FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "uploadDir", nestedDir.toString());

        final MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "content".getBytes());

        final String filename = service.storeFile(file);

        assertThat(nestedDir.resolve(filename)).exists();
    }

    @Test
    void storeFile_shouldHandleFilesWithoutExtension() {
        final MockMultipartFile file = new MockMultipartFile(
            "file", "README", "text/plain", "content".getBytes());

        final String filename = fileStorageService.storeFile(file);

        assertThat(filename).doesNotContain(".");
        assertThat(tempDir.resolve(filename)).exists();
    }

    @Test
    void storeFile_shouldSafelyStoreFileDespiteMaliciousOriginalFilename() {
        final MockMultipartFile file = new MockMultipartFile(
            "file", "evil.txt/../../passwd", "text/plain", "content".getBytes());

        final String filename = fileStorageService.storeFile(file);

        assertThat(filename).doesNotContain("/");
        assertThat(tempDir.resolve(filename)).exists();
        assertThat(tempDir.resolve(filename).getParent()).isEqualTo(tempDir);
    }

    @Test
    void deleteFile_shouldDeleteExistingFile() throws IOException {
        final Path existingFile = tempDir.resolve("existing-file.txt");
        Files.writeString(existingFile, "content");

        fileStorageService.deleteFile("existing-file.txt");

        assertThat(existingFile).doesNotExist();
    }

    @Test
    void deleteFile_shouldRejectPathTraversal() {
        assertThatThrownBy(() -> fileStorageService.deleteFile("../../../etc/passwd"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid filename");
    }

    @Test
    void deleteFile_shouldRejectPathTraversalWithNullBytes() {
        assertThatThrownBy(() -> fileStorageService.deleteFile("file.txt\0../../etc/passwd"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid filename");
    }

    @Test
    void fetchAndStore_shouldFetchPdfAndStoreIt() throws IOException {
        final HttpServer server = startServer("/test.pdf", 200, "application/pdf", "pdf-content".getBytes());
        final int port = server.getAddress().getPort();
        try {
            final String url = "http://localhost:" + port + "/test.pdf";

            final String filename = fileStorageService.fetchAndStore(url);

            assertThat(filename).endsWith(".pdf");
            assertThat(tempDir.resolve(filename)).exists();
            assertThat(Files.readString(tempDir.resolve(filename))).isEqualTo("pdf-content");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void fetchAndStore_shouldThrowForNon200Status() throws IOException {
        final HttpServer server = startServer("/error.pdf", 404, null, null);
        final int port = server.getAddress().getPort();
        try {
            final String url = "http://localhost:" + port + "/error.pdf";

            assertThatThrownBy(() -> fileStorageService.fetchAndStore(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to fetch PDF");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void fetchAndStore_shouldThrowForNonPdfContentType() throws IOException {
        final HttpServer server = startServer("/not-pdf.txt", 200, "text/plain", "text-content".getBytes());
        final int port = server.getAddress().getPort();
        try {
            final String url = "http://localhost:" + port + "/not-pdf.txt";

            assertThatThrownBy(() -> fileStorageService.fetchAndStore(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not a PDF");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void fetchAndStore_shouldAllowNullContentType() throws IOException {
        final HttpServer server = startServer("/no-content-type.pdf", 200, null, "pdf-content".getBytes());
        final int port = server.getAddress().getPort();
        try {
            final String url = "http://localhost:" + port + "/no-content-type.pdf";

            final String filename = fileStorageService.fetchAndStore(url);

            assertThat(filename).endsWith(".pdf");
            assertThat(tempDir.resolve(filename)).exists();
            assertThat(Files.readString(tempDir.resolve(filename))).isEqualTo("pdf-content");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void fetchAndStore_shouldRejectNonHttpScheme() {
        assertThatThrownBy(() -> fileStorageService.fetchAndStore("ftp://example.com/file.pdf"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Only HTTP and HTTPS URLs are supported");
    }

    @Test
    void store_shouldRejectNullFile() {
        assertThatThrownBy(() -> fileStorageService.store(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("File must not be null");
    }

    @Test
    void storeFile_shouldRejectNullFile() {
        assertThatThrownBy(() -> fileStorageService.storeFile(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("File must not be null");
    }

    @Test
    void deleteFile_shouldRejectNullFilename() {
        assertThatThrownBy(() -> fileStorageService.deleteFile(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("Filename must not be null");
    }

    @Test
    void fetchAndStore_shouldRejectNullUrl() {
        assertThatThrownBy(() -> fileStorageService.fetchAndStore(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("URL must not be null");
    }

    private HttpServer startServer(final String path, final int responseCode, final String contentType, final byte[] body) throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext(path, (final HttpExchange exchange) -> {
            if (contentType != null) {
                exchange.getResponseHeaders().set("Content-Type", contentType);
            }
            exchange.sendResponseHeaders(responseCode, body != null ? body.length : -1);
            if (body != null) {
                try (final OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }
        });
        server.start();
        return server;
    }
}
