package com.specsheetcentral.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.specsheetcentral.dto.CategoryRequest;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        categoryRepository.deleteAll();
    }

    @Test
    void getAllShouldReturnCategories() throws Exception {
        Category cat = new Category();
        cat.setName("Sensors");
        categoryRepository.save(cat);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Sensors"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShouldReturnCreatedCategory() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Microcontrollers");

        mockMvc.perform(post("/api/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Microcontrollers"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateShouldReturnUpdatedCategory() throws Exception {
        Category cat = new Category();
        cat.setName("Old");
        cat = categoryRepository.save(cat);

        CategoryRequest request = new CategoryRequest();
        request.setName("Updated");

        mockMvc.perform(put("/api/admin/categories/{id}", cat.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteShouldReturnNoContent() throws Exception {
        Category cat = new Category();
        cat.setName("Displays");
        cat = categoryRepository.save(cat);

        mockMvc.perform(delete("/api/admin/categories/{id}", cat.getId()))
                .andExpect(status().isNoContent());
    }
}
