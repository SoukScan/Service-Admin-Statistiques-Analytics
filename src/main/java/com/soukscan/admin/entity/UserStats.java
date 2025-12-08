package com.soukscan.admin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private int totalReportsSubmitted;
    private int totalValidReports;
    private int totalRejectedReports;
    private int warningCount;

    private LocalDateTime lastActivity;

    @PrePersist
    public void onCreate() {
        lastActivity = LocalDateTime.now();
    }

    // ----------------------
    // Getters & Setters
    // ----------------------

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getTotalReportsSubmitted() {
        return totalReportsSubmitted;
    }
    public void setTotalReportsSubmitted(int totalReportsSubmitted) {
        this.totalReportsSubmitted = totalReportsSubmitted;
    }

    public int getTotalValidReports() {
        return totalValidReports;
    }
    public void setTotalValidReports(int totalValidReports) {
        this.totalValidReports = totalValidReports;
    }

    public int getTotalRejectedReports() {
        return totalRejectedReports;
    }
    public void setTotalRejectedReports(int totalRejectedReports) {
        this.totalRejectedReports = totalRejectedReports;
    }

    public int getWarningCount() {
        return warningCount;
    }
    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }
    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }
}
