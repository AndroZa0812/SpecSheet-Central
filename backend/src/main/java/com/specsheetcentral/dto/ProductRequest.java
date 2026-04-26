package com.specsheetcentral.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public class ProductRequest {
    private String name;
    private String sku;
    private Double price;
    private Double costPrice;
    private Integer stockQuantity;
    private Long categoryId;
    private String manufacturer;
    private String imageUrl;
    private String description;
    private String datasheetUrl;
    private Map<String, String> specs;
    private Integer lowStockThreshold;
    private MultipartFile datasheetFile;
    private boolean clearDatasheet;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getCostPrice() { return costPrice; }
    public void setCostPrice(Double costPrice) { this.costPrice = costPrice; }
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDatasheetUrl() { return datasheetUrl; }
    public void setDatasheetUrl(String datasheetUrl) { this.datasheetUrl = datasheetUrl; }
    public Map<String, String> getSpecs() { return specs; }
    public void setSpecs(Map<String, String> specs) { this.specs = specs; }
    public Integer getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(Integer lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    public MultipartFile getDatasheetFile() { return datasheetFile; }
    public void setDatasheetFile(MultipartFile datasheetFile) { this.datasheetFile = datasheetFile; }
    public boolean isClearDatasheet() { return clearDatasheet; }
    public void setClearDatasheet(boolean clearDatasheet) { this.clearDatasheet = clearDatasheet; }
}
