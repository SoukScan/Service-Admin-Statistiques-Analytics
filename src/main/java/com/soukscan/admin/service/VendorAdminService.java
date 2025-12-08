package com.soukscan.admin.service;

import com.soukscan.admin.entity.VendorStats;
import com.soukscan.admin.repository.VendorStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Service for managing vendor operations via the Vendor microservice.
 * Includes fetching vendors, verifying, rejecting, suspending, and activating vendors.
 * Updates local VendorStats and logs admin actions.
 */
@Service
public class VendorAdminService {

    private final VendorStatsRepository vendorStatsRepo;
    private final AdminActionLogService adminActionLogService;
    private final WebClient vendorWebClient;

        public VendorAdminService(VendorStatsRepository vendorStatsRepo,
                                                          AdminActionLogService adminActionLogService,
                                                          @Qualifier("vendorWebClient") WebClient vendorWebClient) {
        this.vendorStatsRepo = vendorStatsRepo;
        this.adminActionLogService = adminActionLogService;
        this.vendorWebClient = vendorWebClient;
    }

    /**
     * Fetch all vendors from Vendor Service.
     */
    public Object fetchAllVendors() {
        return vendorWebClient.get()
                .uri("")
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Vendor-Service error fetching all vendors: " + ex.getMessage()
                )))
                .block();
    }

    /**
     * Fetch a specific vendor by ID from Vendor Service.
     */
    public Object fetchVendor(Long vendorId) {
        return vendorWebClient.get()
                .uri("/{id}", vendorId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Vendor-Service error fetching vendor " + vendorId + ": " + ex.getMessage()
                )))
                .block();
    }

    /**
     * Get pending vendors from Vendor Service.
     */
    public Object getPendingVendors() {
        return vendorWebClient.get()
                .uri("/pending")
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Vendor-Service error fetching pending vendors: " + ex.getMessage()
                )))
                .block();
    }

    /**
     * Verify a vendor via Vendor Service.
     * Updates VendorStats and logs the action.
     */
    public Object verifyVendor(Long vendorId, Long adminId) {
        Object vendorResponse = vendorWebClient.patch()
                .uri("/{id}/verify?adminId={adminId}", vendorId, adminId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Vendor-Service error during verification: " + ex.getMessage()
                )))
                .block();

        updateVendorStats(vendorId);
        adminActionLogService.logAction(
                adminId,
                "VENDOR_VERIFIED",
                "VENDOR",
                vendorId,
                "Vendor verified by admin"
        );

        return vendorResponse;
    }

    /**
     * Reject a vendor via Vendor Service.
     * Updates VendorStats and logs the action.
     */
    public Object rejectVendor(Long vendorId, Long adminId, String reason) {
        Object vendorResponse = vendorWebClient.patch()
                .uri("/{id}/reject?adminId={adminId}&reason={reason}", vendorId, adminId, reason)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Vendor-Service error during rejection: " + ex.getMessage()
                )))
                .block();

        updateVendorStats(vendorId);
        adminActionLogService.logAction(
                adminId,
                "VENDOR_REJECTED",
                "VENDOR",
                vendorId,
                "Vendor rejected. Reason: " + reason
        );

        return vendorResponse;
    }

    /**
     * Suspend a vendor via Vendor Service.
     * Updates VendorStats and logs the action.
     */
    public Object suspendVendor(Long vendorId, Long adminId, String reason) {
        Object vendorResponse = vendorWebClient.patch()
                .uri("/{id}/suspend?adminId={adminId}&reason={reason}", vendorId, adminId, reason)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Vendor-Service error during suspension: " + ex.getMessage()
                )))
                .block();

        updateVendorStats(vendorId);
        adminActionLogService.logAction(
                adminId,
                "VENDOR_SUSPENDED",
                "VENDOR",
                vendorId,
                "Vendor suspended. Reason: " + reason
        );

        return vendorResponse;
    }

    /**
     * Activate a vendor via Vendor Service.
     * Updates VendorStats and logs the action.
     */
    public Object activateVendor(Long vendorId, Long adminId) {
        Object vendorResponse = vendorWebClient.patch()
                .uri("/{id}/activate?adminId={adminId}", vendorId, adminId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Vendor-Service error during activation: " + ex.getMessage()
                )))
                .block();

        updateVendorStats(vendorId);
        adminActionLogService.logAction(
                adminId,
                "VENDOR_ACTIVATED",
                "VENDOR",
                vendorId,
                "Vendor activated by admin"
        );

        return vendorResponse;
    }

    /**
     * Internal method to update VendorStats locally.
     */
    private void updateVendorStats(Long vendorId) {
        VendorStats stats = vendorStatsRepo.findByVendorId(vendorId)
                .orElseGet(() -> {
                    VendorStats s = new VendorStats();
                    s.setVendorId(vendorId);
                    return vendorStatsRepo.save(s);
                });

        stats.setLastUpdated(LocalDateTime.now());
        vendorStatsRepo.save(stats);
    }
}
