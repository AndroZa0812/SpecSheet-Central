package com.specsheetcentral.controller;

import com.specsheetcentral.dto.CategoryRequest;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.security.CustomUserDetailsService;
import com.specsheetcentral.security.JwtService;
import com.specsheetcentral.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getAllShouldReturnCategories() throws Exception {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Sensors");
        when(categoryService.findAll()).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Sensors"));
    }

    @Test
    void getByIdShouldReturnCategory() throws Exception {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Sensors");
        when(categoryService.findById(1L)).thenReturn(cat);

        mockMvc.perform(get("/api/categories/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Sensors"));
    }

    @Test
    void createShouldReturnCreatedCategory() throws Exception {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Microcontrollers");
        when(categoryService.create(any(Category.class))).thenReturn(cat);

        CategoryRequest request = new CategoryRequest();
        request.setName("Microcontrollers");

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Microcontrollers"));
    }

    @Test
    void updateShouldReturnUpdatedCategory() throws Exception {
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Updated");
        when(categoryService.update(eq(1L), any(Category.class))).thenReturn(cat);

        CategoryRequest request = new CategoryRequest();
        request.setName("Updated");

        mockMvc.perform(put("/api/categories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
            .andExpect(status().isNoContent());
    }
}
