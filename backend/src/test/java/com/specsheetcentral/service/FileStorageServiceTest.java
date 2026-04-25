package com.specsheetcentral.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(url).startsWith("/uploads/");
        assertThat(url).endsWith(".png");
    }

    @Test
    void shouldHandleDifferentExtensions() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "datasheet.pdf", "application/pdf", "pdf-content".getBytes());

        String url = fileStorageService.store(file);

        assertThat(url).endsWith(".pdf");
    }
}
