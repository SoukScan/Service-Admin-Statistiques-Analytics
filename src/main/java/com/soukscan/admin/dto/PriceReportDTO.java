package com.soukscan.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PriceReportDTO {

    @NotNull(message = "productId est obligatoire")
    private Long productId;

    @NotNull(message = "vendorId est obligatoire")
    private Long vendorId;

    @Min(value = 1, message = "price doit être > 0")
    private double price;

    @NotNull(message = "reportedBy est obligatoire")
    private Long reportedBy;

    // Getters & setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Long getReportedBy() { return reportedBy; }
    public void setReportedBy(Long reportedBy) { this.reportedBy = reportedBy; }
}
