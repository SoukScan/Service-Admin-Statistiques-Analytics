package com.soukscan.admin.controller;

import com.soukscan.admin.dto.GlobalStatsDTO;
import com.soukscan.admin.dto.UserStatsDTO;
import com.soukscan.admin.dto.VendorStatsDTO;
import com.soukscan.admin.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for statistics and metrics endpoints.
 * Provides endpoints to retrieve global, user, and vendor statistics.
 *
 * All endpoints require JWT Bearer token authentication with ROLE_ADMIN or ROLE_MODERATOR.
 */
@RestController
@RequestMapping("/admin/stats")
@Tag(name = "Admin - Statistics", description = "Statistics and metrics endpoints for admins")
@SecurityRequirement(name = "bearerAuth")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // ---- GLOBAL STATS ----

    /**
     * Retrieve global platform statistics.
     * @return Global statistics aggregated across all users, vendors, and reports
     */
    @GetMapping("/global")
    @Operation(summary = "Get global statistics", description = "Retrieve aggregated statistics for the entire platform including total users, vendors, reports, and moderation actions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Global statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<GlobalStatsDTO> getGlobalStats() {
        return ResponseEntity.ok(statsService.getGlobalStats());
    }

    // ---- USER STATS ----

    /**
     * Retrieve statistics for a specific user.
     * @param userId User ID
     * @return User-specific statistics including reports and warnings
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user statistics", description = "Retrieve statistics for a specific user")
    @Parameter(name = "userId", description = "User ID", required = true)
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable Long userId) {
        return ResponseEntity.ok(statsService.getUserStats(userId));
    }

    // ---- VENDOR STATS ----

    /**
     * Retrieve statistics for a specific vendor.
     * @param vendorId Vendor ID
     * @return Vendor-specific statistics including trust score and rating
     */
    @GetMapping("/vendors/{vendorId}")
    @Operation(summary = "Get vendor statistics", description = "Retrieve statistics for a specific vendor")
    @Parameter(name = "vendorId", description = "Vendor ID", required = true)
    public ResponseEntity<VendorStatsDTO> getVendorStats(@PathVariable Long vendorId) {
        return ResponseEntity.ok(statsService.getVendorStats(vendorId));
    }
}
