package com.soukscan.admin.dto;

public class VendorStatsDTO {

    private Long vendorId;
    private int totalPriceReports;
    private int validPriceReports;
    private int rejectedPriceReports;
    private double veracityScore;

    private int moderationActions;

    // ---------- GETTERS ----------
    public Long getVendorId() {
        return vendorId;
    }

    public int getTotalPriceReports() {
        return totalPriceReports;
    }

    public int getValidPriceReports() {
        return validPriceReports;
    }

    public int getRejectedPriceReports() {
        return rejectedPriceReports;
    }

    public double getVeracityScore() {
        return veracityScore;
    }

    public int getModerationActions() {
        return moderationActions;
    }

    // ---------- SETTERS ----------
    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public void setTotalPriceReports(int totalPriceReports) {
        this.totalPriceReports = totalPriceReports;
    }

    public void setValidPriceReports(int validPriceReports) {
        this.validPriceReports = validPriceReports;
    }

    public void setRejectedPriceReports(int rejectedPriceReports) {
        this.rejectedPriceReports = rejectedPriceReports;
    }

    public void setVeracityScore(double veracityScore) {
        this.veracityScore = veracityScore;
    }

    public void setModerationActions(int moderationActions) {
        this.moderationActions = moderationActions;
    }
}
