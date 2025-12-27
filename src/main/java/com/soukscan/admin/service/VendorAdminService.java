package com.soukscan.admin.service;

import com.soukscan.admin.dto.VendorDocumentDTO;
import com.soukscan.admin.entity.VendorStats;
import com.soukscan.admin.repository.VendorStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Service for managing vendor operations via the Vendor microservice.
 * Includes fetching vendors, verifying, rejecting, suspending, and activating vendors.
 * Updates local VendorStats and logs admin actions.
 * Verifies vendor documents before approving/rejecting and publishes Kafka events.
 */
@Service
public class VendorAdminService {

    private static final Logger logger = LoggerFactory.getLogger(VendorAdminService.class);

    private final VendorStatsRepository vendorStatsRepo;
    private final AdminActionLogService adminActionLogService;
    private final VendorDocumentService vendorDocumentService;
    private final VendorEventPublisherService vendorEventPublisherService;
    private final WebClient vendorWebClient;

    public VendorAdminService(VendorStatsRepository vendorStatsRepo,
                              AdminActionLogService adminActionLogService,
                              VendorDocumentService vendorDocumentService,
                              VendorEventPublisherService vendorEventPublisherService,
                              @Qualifier("vendorWebClient") WebClient vendorWebClient) {
        this.vendorStatsRepo = vendorStatsRepo;
        this.adminActionLogService = adminActionLogService;
        this.vendorDocumentService = vendorDocumentService;
        this.vendorEventPublisherService = vendorEventPublisherService;
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
     * Checks that a verification document exists.
     * Updates VendorStats and logs the action.
     * Publishes vendor status change event to Kafka.
     */
    public Object verifyVendor(Long vendorId, Long adminId) {
        logger.info("Verifying vendor {}", vendorId);

        // Check that verification document exists
        VendorDocumentDTO document = vendorDocumentService.getDocumentOrThrow(vendorId);
        logger.info("Verification document found for vendor {}: {}", vendorId, document.getFileName());

        // Call Vendor Service to update status
        Object vendorResponse = vendorWebClient.patch()
                .uri("/{id}/verify?adminId={adminId}", vendorId, adminId)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Vendor-Service error during verification: " + ex.getMessage()
                )))
                .block();

        updateVendorStats(vendorId);

        // Log admin action
        adminActionLogService.logAction(
                adminId,
                "VENDOR_VERIFIED",
                "VENDOR",
                vendorId,
                "Vendor verified by admin. Document: " + document.getFileName()
        );

        // Publish Kafka event
        vendorEventPublisherService.publishVendorVerificationEvent(vendorId, "VENDOR_APPROVED", adminId, document);

        logger.info("Vendor {} verified successfully", vendorId);
        return vendorResponse;
    }

    /**
     * Reject a vendor via Vendor Service.
     * Checks that a verification document exists for review.
     * Updates VendorStats and logs the action.
     * Publishes vendor status change event to Kafka.
     */
    public Object rejectVendor(Long vendorId, Long adminId, String reason) {
        logger.info("Rejecting vendor {} with reason: {}", vendorId, reason);

        // Check that verification document exists for review
        VendorDocumentDTO document = null;
        try {
            document = vendorDocumentService.getDocumentOrThrow(vendorId);
            logger.info("Verification document found for vendor {}: {}", vendorId, document.getFileName());
        } catch (VendorDocumentService.DocumentNotFoundException ex) {
            logger.warn("No verification document found for vendor {} - proceeding with rejection", vendorId);
            // Allow rejection even without document for edge cases
        }

        // Call Vendor Service to update status
        Object vendorResponse = vendorWebClient.patch()
                .uri("/{id}/reject?adminId={adminId}&reason={reason}", vendorId, adminId, reason)
                .retrieve()
                .bodyToMono(Object.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Vendor-Service error during rejection: " + ex.getMessage()
                )))
                .block();

        updateVendorStats(vendorId);

        // Log admin action
        String docInfo = document != null ? document.getFileName() : "no document";
        adminActionLogService.logAction(
                adminId,
                "VENDOR_REJECTED",
                "VENDOR",
                vendorId,
                "Vendor rejected. Reason: " + reason + ". Document: " + docInfo
        );

        // Publish Kafka event
        vendorEventPublisherService.publishVendorVerificationEvent(vendorId, "VENDOR_REJECTED", adminId, document);

        logger.info("Vendor {} rejected successfully", vendorId);
        return vendorResponse;
    }

    /**
     * Suspend a vendor via Vendor Service.
     * Updates VendorStats and logs the action.
     * Publishes vendor status change event to Kafka.
     */
    public Object suspendVendor(Long vendorId, Long adminId, String reason) {
        logger.info("Suspending vendor {} with reason: {}", vendorId, reason);

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

        // Publish Kafka event
        vendorEventPublisherService.publishVendorStatusChanged(vendorId, "SUSPENDED", adminId, reason);

        logger.info("Vendor {} suspended successfully", vendorId);
        return vendorResponse;
    }

    /**
     * Activate a vendor via Vendor Service.
     * Updates VendorStats and logs the action.
     * Publishes vendor status change event to Kafka.
     */
    public Object activateVendor(Long vendorId, Long adminId) {
        logger.info("Activating vendor {}", vendorId);

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

        // Publish Kafka event
        vendorEventPublisherService.publishVendorStatusChanged(vendorId, "ACTIVATED", adminId, null);

        logger.info("Vendor {} activated successfully", vendorId);
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
