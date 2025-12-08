package com.soukscan.admin.dto;

/**
 * Data Transfer Object for Product.
 * Used for exchanging product information between admin service and product service.
 */
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private String category;
    private Double price;
    private String currency;
    private Boolean active;

    // Constructors

    public ProductDTO() {
    }

    public ProductDTO(Long id, String name, String description, String category,
                      Double price, String currency, Boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.currency = currency;
        this.active = active;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
