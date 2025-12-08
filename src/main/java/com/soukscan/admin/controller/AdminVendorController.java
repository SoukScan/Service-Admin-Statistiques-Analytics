package com.soukscan.admin.controller;

import com.soukscan.admin.service.VendorAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin vendor management operations.
 * Provides endpoints to list, retrieve, and manage vendor status (verify, reject, suspend, activate).
 *
 * All endpoints require JWT Bearer token authentication with ROLE_ADMIN or ROLE_MODERATOR.
 */
@RestController
@RequestMapping("/admin/vendors")
@Tag(name = "Admin - Vendors", description = "Vendor management endpoints for admins")
@SecurityRequirement(name = "bearerAuth")
public class AdminVendorController {

    private final VendorAdminService vendorAdminService;

    public AdminVendorController(VendorAdminService vendorAdminService) {
        this.vendorAdminService = vendorAdminService;
    }

    // ---- GET VENDORS FROM Vendor-Service ----

    /**
     * Fetch all vendors from the Vendor microservice.
     * @return List of all vendors
     */
    @GetMapping
    @Operation(summary = "Get all vendors", description = "Fetch all vendors from Vendor-Service with pagination and filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of vendors retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<Object> getAllVendors() {
        return ResponseEntity.ok(vendorAdminService.fetchAllVendors());
    }

    /**
     * Fetch a vendor by ID from the Vendor microservice.
     * @param id Vendor ID
     * @return Vendor details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get vendor by ID", description = "Fetch a specific vendor details from Vendor-Service")
    @Parameter(name = "id", description = "Vendor ID (unique identifier)", required = true, example = "12345")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vendor details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Vendor not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Object> getVendorById(@PathVariable Long id) {
        return ResponseEntity.ok(vendorAdminService.fetchVendor(id));
    }

    // ---- VERIFY ----

    /**
     * Verify a vendor (approve registration).
     * @param id Vendor ID
     * @param adminId Admin user ID
     * @return Updated vendor status
     */
    @PostMapping("/{id}/verify")
    @Operation(summary = "Verify vendor", description = "Approve vendor registration")
    @Parameter(name = "id", description = "Vendor ID", required = true)
    @Parameter(name = "adminId", description = "Admin ID performing the action", required = true)
    public ResponseEntity<Object> verifyVendor(
            @PathVariable Long id,
            @RequestParam Long adminId) {

        return ResponseEntity.ok(vendorAdminService.verifyVendor(id, adminId));
    }

    // ---- REJECT ----

    /**
     * Reject a vendor (deny registration).
     * @param id Vendor ID
     * @param adminId Admin user ID
     * @param reason Rejection reason
     * @return Rejection confirmation
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject vendor", description = "Reject vendor registration with a reason")
    @Parameter(name = "id", description = "Vendor ID", required = true)
    @Parameter(name = "adminId", description = "Admin ID performing the action", required = true)
    @Parameter(name = "reason", description = "Rejection reason", required = true)
    public ResponseEntity<Object> rejectVendor(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestParam String reason) {

        return ResponseEntity.ok(vendorAdminService.rejectVendor(id, adminId, reason));
    }

    // ---- SUSPEND ----

    /**
     * Suspend a vendor account (temporary deactivation).
     * @param id Vendor ID
     * @param adminId Admin user ID
     * @param reason Suspension reason
     * @return Suspension confirmation
     */
    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend vendor", description = "Temporarily suspend vendor account")
    @Parameter(name = "id", description = "Vendor ID", required = true)
    @Parameter(name = "adminId", description = "Admin ID performing the action", required = true)
    @Parameter(name = "reason", description = "Suspension reason", required = true)
    public ResponseEntity<Object> suspendVendor(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestParam String reason) {

        return ResponseEntity.ok(vendorAdminService.suspendVendor(id, adminId, reason));
    }

    // ---- ACTIVATE ----

    /**
     * Activate (re-enable) a vendor account.
     * @param id Vendor ID
     * @param adminId Admin user ID
     * @return Activation confirmation
     */
    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate vendor", description = "Re-enable suspended vendor account")
    @Parameter(name = "id", description = "Vendor ID", required = true)
    @Parameter(name = "adminId", description = "Admin ID performing the action", required = true)
    public ResponseEntity<Object> activateVendor(
            @PathVariable Long id,
            @RequestParam Long adminId) {

        return ResponseEntity.ok(vendorAdminService.activateVendor(id, adminId));
    }
}
