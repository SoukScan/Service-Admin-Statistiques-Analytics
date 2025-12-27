package com.soukscan.admin.service;

import com.soukscan.admin.dto.VendorDocumentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for publishing vendor-related Kafka events.
 * Publishes events to the "vendor.status.changed" topic whenever a vendor's status changes.
 */
@Service
public class VendorEventPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(VendorEventPublisherService.class);
    private static final String VENDOR_STATUS_TOPIC = "vendor.status.changed";
    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public VendorEventPublisherService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish a vendor status change event to Kafka.
     *
     * @param vendorId The ID of the vendor
     * @param status The new status (e.g., APPROVED, REJECTED, SUSPENDED, ACTIVATED)
     * @param adminId The admin user ID who initiated the change
     * @param reason Optional reason for the status change
     */
    public void publishVendorStatusChanged(Long vendorId, String status, Long adminId, String reason) {
        Map<String, Object> event = new HashMap<>();
        event.put("vendorId", vendorId);
        event.put("status", status);
        event.put("adminId", adminId);
        event.put("reason", reason);
        event.put("timestamp", LocalDateTime.now().format(ISO_DATE_TIME));
        event.put("eventType", "VENDOR_STATUS_CHANGED");

        try {
            kafkaTemplate.send(VENDOR_STATUS_TOPIC, vendorId.toString(), event);
            logger.info("Published vendor status event: vendorId={}, status={}, adminId={}", vendorId, status, adminId);
        } catch (Exception ex) {
            logger.error("Failed to publish vendor status event for vendor {}: {}", vendorId, ex.getMessage());
            // Log error but don't throw — status change already happened
        }
    }

    /**
     * Publish a vendor verification event (approval/rejection with document).
     *
     * @param vendorId The ID of the vendor
     * @param actionType VENDOR_APPROVED or VENDOR_REJECTED
     * @param adminId The admin user ID
     * @param document The verification document metadata (optional)
     */
    public void publishVendorVerificationEvent(Long vendorId, String actionType, Long adminId, VendorDocumentDTO document) {
        Map<String, Object> event = new HashMap<>();
        event.put("vendorId", vendorId);
        event.put("actionType", actionType);
        event.put("adminId", adminId);
        if (document != null) {
            event.put("documentName", document.getFileName());
            event.put("documentUploadedAt", document.getUploadedAt());
        }
        event.put("timestamp", LocalDateTime.now().format(ISO_DATE_TIME));
        event.put("eventType", "VENDOR_VERIFICATION");

        try {
            kafkaTemplate.send(VENDOR_STATUS_TOPIC, vendorId.toString(), event);
            logger.info("Published vendor verification event: vendorId={}, actionType={}, adminId={}", vendorId, actionType, adminId);
        } catch (Exception ex) {
            logger.error("Failed to publish vendor verification event for vendor {}: {}", vendorId, ex.getMessage());
            // Log error but don't throw
        }
    }
}
