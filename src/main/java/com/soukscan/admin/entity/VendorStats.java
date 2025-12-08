package com.soukscan.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_stats")
public class VendorStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long vendorId;

    private int totalReportsAgainstVendor;
    private int totalApprovedReports;
    private int totalRejectedReports;
    private double trustScore; // score de confiance

    private LocalDateTime lastUpdated;

    @PrePersist
    public void onCreate() {
        lastUpdated = LocalDateTime.now();
        trustScore = 100; // vendeur commence avec 100%
    }

    @PreUpdate
    public void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public int getTotalReportsAgainstVendor() { return totalReportsAgainstVendor; }
    public void setTotalReportsAgainstVendor(int value) { this.totalReportsAgainstVendor = value; }

    public int getTotalApprovedReports() { return totalApprovedReports; }
    public void setTotalApprovedReports(int value) { this.totalApprovedReports = value; }

    public int getTotalRejectedReports() { return totalRejectedReports; }
    public void setTotalRejectedReports(int value) { this.totalRejectedReports = value; }

    public double getTrustScore() { return trustScore; }
    public void setTrustScore(double trustScore) { this.trustScore = trustScore; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
