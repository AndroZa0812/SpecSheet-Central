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
import org.springframework.test.util.ReflectionTestUtils;
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
    void getAllShouldReturnProducts() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Arduino Uno");
        request.setSku("ARD-UNO");
        request.setPrice(24.99);
        request.setStockQuantity(10);
        request.setCategoryId(category.getId());

        mockMvc.perform(post("/api/products")
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
    void deleteShouldReturnNoContent() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Delete Me");
        request.setSku("DEL-001");
        request.setPrice(10.00);
        request.setStockQuantity(5);
        request.setCategoryId(category.getId());

        String json = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void createShouldReturnCreatedProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Arduino Uno");
        request.setSku("ARD-UNO-R3");
        request.setPrice(24.99);
        request.setStockQuantity(10);
        request.setCategoryId(category.getId());
        request.setManufacturer("Arduino");

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Arduino Uno"))
                .andExpect(jsonPath("$.sku").value("ARD-UNO-R3"))
                .andExpect(jsonPath("$.price").value(24.99));
    }

    @Test
    void updateStockShouldReturnUpdatedProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Stock ITem");
        request.setSku("STK-001");
        request.setPrice(15.00);
        request.setStockQuantity(5);
        request.setCategoryId(category.getId());

        String json = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(patch("/api/products/{id}/stock", id)
                        .param("quantity", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(25));
    }

    @Test
    void getByIdShouldReturnProduct() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Find Me");
        request.setSku("FND-001");
        request.setPrice(99.99);
        request.setStockQuantity(1);
        request.setCategoryId(category.getId());

        String json = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Find Me"));
    }
}
