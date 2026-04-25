package com.specsheetcentral.controller;

import com.specsheetcentral.dto.ProductRequest;
import com.specsheetcentral.dto.ProductResponse;
import com.specsheetcentral.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void getAllShouldReturnProducts() throws Exception {
        ProductResponse resp = new ProductResponse();
        resp.setId(1L);
        resp.setName("Arduino Uno");
        resp.setSku("ARD-UNO");
        resp.setPrice(24.99);
        resp.setStockQuantity(10);
        resp.setCategoryName("Microcontrollers");

        when(productService.findAll(any(), any(), any(), any(), any())).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Arduino Uno"));
    }

    @Test
    void getByIdShouldReturnProduct() throws Exception {
        ProductResponse resp = new ProductResponse();
        resp.setId(1L);
        resp.setName("Arduino Uno");

        when(productService.findById(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/products/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Arduino Uno"));
    }

    @Test
    void createShouldReturnCreatedProduct() throws Exception {
        ProductResponse resp = new ProductResponse();
        resp.setId(1L);
        resp.setName("Arduino Uno");
        resp.setSku("ARD-UNO");
        resp.setPrice(24.99);
        resp.setStockQuantity(10);
        resp.setCategoryName("Microcontrollers");

        when(productService.create(any(ProductRequest.class))).thenReturn(resp);

        ProductRequest request = new ProductRequest();
        request.setName("Arduino Uno");
        request.setSku("ARD-UNO");
        request.setPrice(24.99);
        request.setStockQuantity(10);
        request.setCategoryId(1L);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Arduino Uno"));
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void updateStockShouldReturnUpdatedProduct() throws Exception {
        ProductResponse resp = new ProductResponse();
        resp.setId(1L);
        resp.setStockQuantity(25);

        when(productService.updateStock(eq(1L), eq(25))).thenReturn(resp);

        mockMvc.perform(patch("/api/products/1/stock?quantity=25"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stockQuantity").value(25));
    }
}
