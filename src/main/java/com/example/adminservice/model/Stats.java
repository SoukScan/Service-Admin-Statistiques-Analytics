package com.example.adminservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "stats")
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date = LocalDate.now();
    private long totalUsers;
    private long totalVendors;
    private long verifiedVendors;
    private long pendingVerifications;
    private long totalReports;
    private long anomaliesDetected;

    // --- Getters et Setters ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getTotalUsers() {
        return totalUsers;
    }
    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalVendors() {
        return totalVendors;
    }
    public void setTotalVendors(long totalVendors) {
        this.totalVendors = totalVendors;
    }

    public long getVerifiedVendors() {
        return verifiedVendors;
    }
    public void setVerifiedVendors(long verifiedVendors) {
        this.verifiedVendors = verifiedVendors;
    }

    public long getPendingVerifications() {
        return pendingVerifications;
    }
    public void setPendingVerifications(long pendingVerifications) {
        this.pendingVerifications = pendingVerifications;
    }

    public long getTotalReports() {
        return totalReports;
    }
    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

    public long getAnomaliesDetected() {
        return anomaliesDetected;
    }
    public void setAnomaliesDetected(long anomaliesDetected) {
        this.anomaliesDetected = anomaliesDetected;
    }
}
