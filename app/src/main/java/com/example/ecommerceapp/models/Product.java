package com.example.ecommerceapp.models;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private int stock;
    private String imageUrl;
    private double rating;
    private String sku;
    private String warranty;
    private double discount;
    private boolean isNew;
    private boolean isHot;
    private boolean isFeatured;

    public Product() {
        this.rating = 4.5;
        this.warranty = "12 tháng";
        this.discount = 0;
        this.isNew = false;
        this.isHot = false;
        this.isFeatured = false;
    }

    public Product(int id, String name, String description, double price,
                   String category, int stock, String imageUrl) {
        this();
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.sku = "PRD-" + String.format("%04d", id);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { 
        this.id = id; 
        if (this.sku == null || this.sku.isEmpty()) {
            this.sku = "PRD-" + String.format("%04d", id);
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = Math.max(0, Math.min(5, rating)); }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getWarranty() { return warranty; }
    public void setWarranty(String warranty) { this.warranty = warranty; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = Math.max(0, Math.min(100, discount)); }

    public boolean isNew() { return isNew; }
    public void setNew(boolean isNew) { this.isNew = isNew; }

    public boolean isHot() { return isHot; }
    public void setHot(boolean isHot) { this.isHot = isHot; }

    public boolean isFeatured() { return isFeatured; }
    public void setFeatured(boolean isFeatured) { this.isFeatured = isFeatured; }

    // Utility methods
    public double getFinalPrice() {
        return price * (1 - discount / 100);
    }

    public String getFormattedSku() {
        return sku != null && !sku.isEmpty() ? sku : "PRD-" + String.format("%04d", id);
    }
}
