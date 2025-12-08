package com.soukscan.admin.controller;

import com.soukscan.admin.dto.ModerationActionDTO;
import com.soukscan.admin.dto.ModerationReportDTO;
import com.soukscan.admin.entity.ModerationAction;
import com.soukscan.admin.entity.ModerationReport;
import com.soukscan.admin.service.ModerationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for moderation management operations.
 * Provides endpoints to handle user reports, approve/reject moderation actions, warn users, and block accounts.
 *
 * All endpoints require JWT Bearer token authentication with ROLE_ADMIN or ROLE_MODERATOR.
 */
@RestController
@RequestMapping("/admin/moderation")
@Tag(name = "Admin - Moderation", description = "Moderation and user action management endpoints for admins")
@SecurityRequirement(name = "bearerAuth")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    // ---- GET ALL PENDING REPORTS ----

    /**
     * Retrieve all pending moderation reports.
     * @return List of pending reports
     */
    @GetMapping("/reports/pending")
    @Operation(summary = "Get pending reports", description = "Fetch all pending moderation reports awaiting admin action")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pending reports retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<List<ModerationReport>> getPendingReports() {
        return ResponseEntity.ok(moderationService.getPendingReports());
    }

    // ---- APPROVE REPORT ----

    /**
     * Approve a moderation report.
     * @param reportId Report ID
     * @param adminId Admin ID performing the action
     * @param dto Optional moderation action details
     * @return Created moderation action
     */
    @PostMapping("/reports/{reportId}/approve")
    @Operation(summary = "Approve moderation report", description = "Approve a moderation report and record the admin action taken")
    @Parameter(name = "reportId", description = "Moderation Report ID", required = true, example = "456")
    @Parameter(name = "adminId", description = "Admin ID performing the action", required = true, example = "101")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report approved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid action"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ModerationAction> approveReport(
            @PathVariable Long reportId,
            @RequestParam Long adminId,
            @RequestBody(required = false) ModerationActionDTO dto) {

        return ResponseEntity.ok(moderationService.approveReport(reportId, adminId, dto));
    }

    // ---- REJECT REPORT ----

    /**
     * Reject a moderation report.
     * @param reportId Report ID
     * @param adminId Admin ID performing the action
     * @param dto Optional moderation action details
     * @return Created moderation action
     */
    @PostMapping("/reports/{reportId}/reject")
    @Operation(summary = "Reject moderation report", description = "Reject a moderation report and record the admin action")
    @Parameter(name = "reportId", description = "Moderation Report ID", required = true)
    @Parameter(name = "adminId", description = "Admin ID performing the action", required = true)
    public ResponseEntity<ModerationAction> rejectReport(
            @PathVariable Long reportId,
            @RequestParam Long adminId,
            @RequestBody(required = false) ModerationActionDTO dto) {

        return ResponseEntity.ok(moderationService.rejectReport(reportId, adminId, dto));
    }

    // ---- SEND WARNING ----

    /**
     * Send a warning to a user.
     * @param reportId Report ID
     * @param adminId Admin ID performing the action
     * @param dto Moderation action details
     * @return Created warning action
     */
    @PostMapping("/reports/{reportId}/warn")
    @Operation(summary = "Warn user", description = "Send a warning to a user and record the admin action")
    @Parameter(name = "reportId", description = "Moderation Report ID", required = true)
    @Parameter(name = "adminId", description = "Admin ID performing the action", required = true)
    public ResponseEntity<ModerationAction> warnUser(
            @PathVariable Long reportId,
            @RequestParam Long adminId,
            @RequestBody ModerationActionDTO dto) {

        return ResponseEntity.ok(moderationService.warnUser(reportId, adminId, dto));
    }

    // ---- BLOCK USER ----

    /**
     * Block a user account.
     * @param userId User ID to block
     * @param adminId Admin ID performing the action
     * @param dto Moderation action details
     * @return Created block action
     */
    @PostMapping("/users/{userId}/block")
    @Operation(summary = "Block user", description = "Block a user account and record the admin action")
    @Parameter(name = "userId", description = "User ID to block", required = true)
    @Parameter(name = "adminId", description = "Admin ID performing the action", required = true)
    public ResponseEntity<ModerationAction> blockUser(
            @PathVariable Long userId,
            @RequestParam Long adminId,
            @RequestBody ModerationActionDTO dto) {

        return ResponseEntity.ok(moderationService.blockUser(userId, adminId, dto));
    }

    // ---- MODERATION ACTION LOGS ----

    /**
     * Retrieve all moderation actions history.
     * @return List of all moderation actions
     */
    @GetMapping("/actions")
    @Operation(summary = "Get moderation actions history", description = "Fetch all moderation actions performed by admins")
    public ResponseEntity<List<ModerationAction>> getActionsHistory() {
        return ResponseEntity.ok(moderationService.getAllActions());
    }
}
