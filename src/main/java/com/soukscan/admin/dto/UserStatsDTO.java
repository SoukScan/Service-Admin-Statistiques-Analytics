package com.soukscan.admin.dto;

public class UserStatsDTO {

    private Long userId;
    private int totalReportsSubmitted;
    private int totalValidReports;
    private int totalRejectedReports;
    private int warningCount;
    private double accuracyScore;

    // ---------- GETTERS ----------
    public Long getUserId() {
        return userId;
    }

    public int getTotalReportsSubmitted() {
        return totalReportsSubmitted;
    }

    public int getTotalValidReports() {
        return totalValidReports;
    }

    public int getTotalRejectedReports() {
        return totalRejectedReports;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public double getAccuracyScore() {
        return accuracyScore;
    }

    // ---------- SETTERS ----------
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTotalReportsSubmitted(int totalReportsSubmitted) {
        this.totalReportsSubmitted = totalReportsSubmitted;
    }

    public void setTotalValidReports(int totalValidReports) {
        this.totalValidReports = totalValidReports;
    }

    public void setTotalRejectedReports(int totalRejectedReports) {
        this.totalRejectedReports = totalRejectedReports;
    }

    public void setWarningCount(int warningCount) {
        this.warningCount = warningCount;
    }

    public void setAccuracyScore(double accuracyScore) {
        this.accuracyScore = accuracyScore;
    }
}
