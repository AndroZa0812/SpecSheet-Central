package com.specsheetcentral.controller;

import com.specsheetcentral.security.CustomUserDetailsService;
import com.specsheetcentral.security.JwtService;
import com.specsheetcentral.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@ActiveProfiles("test")
@SuppressWarnings("deprecation")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void uploadShouldReturnUrl() throws Exception {
        when(fileStorageService.store(any())).thenReturn("/uploads/test-uuid.png");

        MockMultipartFile file = new MockMultipartFile(
            "file", "test.png", "image/png", "content".getBytes());

        mockMvc.perform(multipart("/api/files/upload").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url").value("/uploads/test-uuid.png"));
    }
}
