package com.specsheetcentral.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.model.Category;
import com.specsheetcentral.repository.CategoryRepository;
import com.specsheetcentral.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
class ProductControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        category = new Category();
        category.setName("Microcontrollers");
        category = categoryRepository.save(category);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllShouldReturnProducts() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Arduino Uno");
        request.setSku("ARD-UNO");
        request.setPrice(24.99);
        request.setStockQuantity(10);
        request.setCategoryId(category.getId());

        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Arduino Uno"));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Arduino Uno"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteShouldReturnNoContent() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Delete Me");
        request.setSku("DEL-001");
        request.setPrice(10.00);
        request.setStockQuantity(5);
        request.setCategoryId(category.getId());

        String json = mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(delete("/api/admin/products/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createShouldReturnCreatedProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Arduino Uno");
        request.setSku("ARD-UNO-R3");
        request.setPrice(24.99);
        request.setStockQuantity(10);
        request.setCategoryId(category.getId());
        request.setManufacturer("Arduino");

        mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Arduino Uno"))
                .andExpect(jsonPath("$.sku").value("ARD-UNO-R3"))
                .andExpect(jsonPath("$.price").value(24.99));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStockShouldReturnUpdatedProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Stock Item");
        request.setSku("STK-001");
        request.setPrice(15.00);
        request.setStockQuantity(5);
        request.setCategoryId(category.getId());

        String json = mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(patch("/api/admin/products/{id}/stock", id)
                        .param("quantity", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(25));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByIdShouldReturnProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Find Me");
        request.setSku("FND-001");
        request.setPrice(99.99);
        request.setStockQuantity(1);
        request.setCategoryId(category.getId());

        String json = mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Find Me"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadDatasheetShouldStoreFile() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Datasheet Product");
        request.setSku("DS-001");
        request.setPrice(10.00);
        request.setStockQuantity(5);
        request.setCategoryId(category.getId());

        String json = mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        MockMultipartFile datasheetFile = new MockMultipartFile(
                "datasheetFile", "test.pdf", "application/pdf", "test content".getBytes());

        mockMvc.perform(multipart("/api/admin/products/{id}/datasheet", id)
                        .file(datasheetFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasheetFilename").isNotEmpty())
                .andExpect(jsonPath("$.datasheetUrl").value(org.hamcrest.Matchers.startsWith("/uploads/")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateShouldReturnUpdatedProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Original Name");
        request.setSku("ORIG-001");
        request.setPrice(10.00);
        request.setStockQuantity(5);
        request.setCategoryId(category.getId());

        String json = mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        request.setName("Updated Name");
        
        mockMvc.perform(put("/api/admin/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void clearDatasheetShouldRemoveDatasheet() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Clear DS Product");
        request.setSku("CLR-001");
        request.setPrice(10.00);
        request.setStockQuantity(5);
        request.setCategoryId(category.getId());

        String json = mockMvc.perform(post("/api/admin/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        MockMultipartFile datasheetFile = new MockMultipartFile(
                "datasheetFile", "test.pdf", "application/pdf", "test content".getBytes());

        mockMvc.perform(multipart("/api/admin/products/{id}/datasheet", id)
                        .file(datasheetFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasheetFilename").isNotEmpty());

        mockMvc.perform(multipart("/api/admin/products/{id}/datasheet", id)
                        .param("clearDatasheet", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasheetFilename").doesNotExist());
    }
}