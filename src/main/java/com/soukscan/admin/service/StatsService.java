package com.soukscan.admin.service;

import com.soukscan.admin.dto.GlobalStatsDTO;
import com.soukscan.admin.dto.UserStatsDTO;
import com.soukscan.admin.dto.VendorStatsDTO;
import com.soukscan.admin.entity.UserStats;
import com.soukscan.admin.entity.VendorStats;
import com.soukscan.admin.repository.*;
import org.springframework.stereotype.Service;

/**
 * Service for computing and retrieving statistics across the admin service.
 * Provides global stats, user stats, and vendor stats.
 */
@Service
public class StatsService {

    private final ModerationReportRepository reportRepo;
    private final VendorStatsRepository vendorStatsRepo;
    private final UserStatsRepository userStatsRepo;
    private final PriceReportRepository priceReportRepo;
    private final ModerationActionRepository actionRepo;

    public StatsService(
            ModerationReportRepository reportRepo,
            VendorStatsRepository vendorStatsRepo,
            UserStatsRepository userStatsRepo,
            PriceReportRepository priceReportRepo,
            ModerationActionRepository actionRepo
    ) {
        this.reportRepo = reportRepo;
        this.vendorStatsRepo = vendorStatsRepo;
        this.userStatsRepo = userStatsRepo;
        this.priceReportRepo = priceReportRepo;
        this.actionRepo = actionRepo;
    }

    /**
     * Get global statistics for the admin dashboard.
     */
    public GlobalStatsDTO getGlobalStats() {
        GlobalStatsDTO dto = new GlobalStatsDTO();

        dto.setTotalUsers(userStatsRepo.count());
        dto.setTotalVendors(vendorStatsRepo.count());
        dto.setTotalPriceReports(priceReportRepo.count());
        dto.setTotalModerationReports(reportRepo.count());
        dto.setTotalModerationActions((long) actionRepo.findAll().size());
        dto.setTotalWarnings(
                userStatsRepo.findAll()
                        .stream()
                        .mapToInt(UserStats::getWarningCount)
                        .sum()
        );

        // You can add more aggregated metrics here
        dto.setTotalBlockedUsers(0L);  // Placeholder; depends on your blocking mechanism

        return dto;
    }

    /**
     * Get statistics for a specific user.
     */
    public UserStatsDTO getUserStats(Long userId) {
        UserStats userStats = userStatsRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User stats not found for userId: " + userId));

        return mapUserStatsToDTO(userStats);
    }

    /**
     * Get statistics for a specific vendor.
     */
    public VendorStatsDTO getVendorStats(Long vendorId) {
        VendorStats vendorStats = vendorStatsRepo.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor stats not found for vendorId: " + vendorId));

        return mapVendorStatsToDTO(vendorStats);
    }

    /**
     * Map UserStats entity to UserStatsDTO.
     */
    private UserStatsDTO mapUserStatsToDTO(UserStats stats) {
        UserStatsDTO dto = new UserStatsDTO();
        dto.setUserId(stats.getUserId());
        dto.setTotalReportsSubmitted(stats.getTotalReportsSubmitted());
        dto.setTotalValidReports(stats.getTotalValidReports());
        dto.setTotalRejectedReports(stats.getTotalRejectedReports());
        dto.setWarningCount(stats.getWarningCount());

        // Calculate accuracy score if there are reports
        if (stats.getTotalReportsSubmitted() > 0) {
            double accuracy = (double) stats.getTotalValidReports() / stats.getTotalReportsSubmitted() * 100;
            dto.setAccuracyScore(accuracy);
        } else {
            dto.setAccuracyScore(0.0);
        }

        return dto;
    }

    /**
     * Map VendorStats entity to VendorStatsDTO.
     */
    private VendorStatsDTO mapVendorStatsToDTO(VendorStats stats) {
        VendorStatsDTO dto = new VendorStatsDTO();
        dto.setVendorId(stats.getVendorId());
        dto.setTotalPriceReports(stats.getTotalReportsAgainstVendor());
        dto.setValidPriceReports(stats.getTotalApprovedReports());
        dto.setRejectedPriceReports(stats.getTotalRejectedReports());
        dto.setVeracityScore(stats.getTrustScore());
        dto.setModerationActions(0);  // Placeholder; can be extended

        return dto;
    }
}
