package com.soukscan.admin.dto;

/**
 * Global statistics DTO for admin dashboard.
 * Contains aggregated metrics across the platform.
 */
public class GlobalStatsDTO {

    private long totalUsers;
    private long totalVendors;
    private long totalPriceReports;
    private long totalModerationReports;
    private long totalModerationActions;
    private long totalWarnings;
    private long totalBlockedUsers;

    // Getters and setters

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

    public long getTotalPriceReports() {
        return totalPriceReports;
    }

    public void setTotalPriceReports(long totalPriceReports) {
        this.totalPriceReports = totalPriceReports;
    }

    public long getTotalModerationReports() {
        return totalModerationReports;
    }

    public void setTotalModerationReports(long totalModerationReports) {
        this.totalModerationReports = totalModerationReports;
    }

    public long getTotalModerationActions() {
        return totalModerationActions;
    }

    public void setTotalModerationActions(long totalModerationActions) {
        this.totalModerationActions = totalModerationActions;
    }

    public long getTotalWarnings() {
        return totalWarnings;
    }

    public void setTotalWarnings(long totalWarnings) {
        this.totalWarnings = totalWarnings;
    }

    public long getTotalBlockedUsers() {
        return totalBlockedUsers;
    }

    public void setTotalBlockedUsers(long totalBlockedUsers) {
        this.totalBlockedUsers = totalBlockedUsers;
    }
}
