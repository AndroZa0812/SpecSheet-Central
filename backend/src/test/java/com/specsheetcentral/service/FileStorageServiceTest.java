package com.specsheetcentral.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.png", "image/png", "test-image-content".getBytes());

        String url = fileStorageService.store(file);

        assertThat(url).startsWith("/uploads/").endsWith(".png");
    }

    @Test
    void shouldHandleDifferentExtensions() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "datasheet.pdf", "application/pdf", "pdf-content".getBytes());

        String url = fileStorageService.store(file);

        assertThat(url).endsWith(".pdf");
    }

    @Test
    void storeFile_shouldStoreFileAndReturnOnlyFilename() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.png", "image/png", "test-image-content".getBytes());

        String filename = fileStorageService.storeFile(file);

        assertThat(filename).doesNotStartWith("/uploads/");
        assertThat(filename).endsWith(".png");
        assertThat(tempDir.resolve(filename)).exists();
    }

    @Test
    void storeFile_shouldUseUuidAndOriginalExtension() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "datasheet.pdf", "application/pdf", "pdf-content".getBytes());

        String filename = fileStorageService.storeFile(file);

        assertThat(filename).matches("^[a-f0-9\\-]+\\.pdf$");
        assertThat(tempDir.resolve(filename)).exists();
    }

    @Test
    void storeFile_shouldCreateDirectoriesIfNeeded(@TempDir Path rootTempDir) {
        Path nestedDir = rootTempDir.resolve("nested/uploads");
        FileStorageService service = new FileStorageService();
        ReflectionTestUtils.setField(service, "uploadDir", nestedDir.toString());

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "content".getBytes());

        String filename = service.storeFile(file);

        assertThat(nestedDir.resolve(filename)).exists();
    }

    @Test
    void storeFile_shouldHandleFilesWithoutExtension() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "README", "text/plain", "content".getBytes());

        String filename = fileStorageService.storeFile(file);

        assertThat(filename).doesNotContain(".");
        assertThat(tempDir.resolve(filename)).exists();
    }

    @Test
    void deleteFile_shouldDeleteExistingFile() throws IOException {
        Path existingFile = tempDir.resolve("existing-file.txt");
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
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/test.pdf", (HttpExchange exchange) -> {
            exchange.getResponseHeaders().set("Content-Type", "application/pdf");
            byte[] response = "pdf-content".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        try {
            String url = "http://localhost:" + port + "/test.pdf";

            String filename = fileStorageService.fetchAndStore(url);

            assertThat(filename).endsWith(".pdf");
            assertThat(tempDir.resolve(filename)).exists();
            assertThat(Files.readString(tempDir.resolve(filename))).isEqualTo("pdf-content");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void fetchAndStore_shouldThrowForNon200Status() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/error.pdf", (HttpExchange exchange) -> {
            exchange.sendResponseHeaders(404, -1);
        });
        server.start();
        int port = server.getAddress().getPort();
        try {
            String url = "http://localhost:" + port + "/error.pdf";

            assertThatThrownBy(() -> fileStorageService.fetchAndStore(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to fetch PDF");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void fetchAndStore_shouldThrowForNonPdfContentType() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/not-pdf.txt", (HttpExchange exchange) -> {
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            byte[] response = "text-content".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        try {
            String url = "http://localhost:" + port + "/not-pdf.txt";

            assertThatThrownBy(() -> fileStorageService.fetchAndStore(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not a PDF");
        } finally {
            server.stop(0);
        }
    }

    @Test
    void fetchAndStore_shouldSetConnectionAndReadTimeouts() throws IOException {
        HttpURLConnection connMock = mock(HttpURLConnection.class);
        when(connMock.getResponseCode()).thenReturn(200);
        when(connMock.getContentType()).thenReturn("application/pdf");
        InputStream dummyIn = new ByteArrayInputStream("pdf-content".getBytes());
        when(connMock.getInputStream()).thenReturn(dummyIn);

        try (var mocked = mockConstruction(URL.class,
                (mock, context) -> when(mock.openConnection()).thenReturn(connMock))) {

            String filename = fileStorageService.fetchAndStore("http://example.com/test.pdf");

            assertThat(filename).endsWith(".pdf");
            assertThat(tempDir.resolve(filename)).exists();
        }

        verify(connMock).setConnectTimeout(10_000);
        verify(connMock).setReadTimeout(30_000);
    }

    @Test
    void fetchAndStore_shouldAllowNullContentType() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/no-content-type.pdf", (HttpExchange exchange) -> {
            byte[] response = "pdf-content".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        try {
            String url = "http://localhost:" + port + "/no-content-type.pdf";

            String filename = fileStorageService.fetchAndStore(url);

            assertThat(filename).endsWith(".pdf");
            assertThat(tempDir.resolve(filename)).exists();
            assertThat(Files.readString(tempDir.resolve(filename))).isEqualTo("pdf-content");
        } finally {
            server.stop(0);
        }
    }
}
