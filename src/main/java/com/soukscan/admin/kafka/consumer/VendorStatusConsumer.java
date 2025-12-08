package com.soukscan.admin.kafka.consumer;

import com.soukscan.admin.entity.VendorStats;
import com.soukscan.admin.kafka.dto.VendorStatusChangedEvent;
import com.soukscan.admin.repository.VendorStatsRepository;
import com.soukscan.admin.service.AdminActionLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Kafka consumer for vendor-status-changed events.
 * Updates vendor statistics when vendor status changes.
 */
@Service
public class VendorStatusConsumer {

    private static final Logger logger = LoggerFactory.getLogger(VendorStatusConsumer.class);

    private final VendorStatsRepository vendorStatsRepository;
    private final AdminActionLogService adminActionLogService;

    @Autowired
    public VendorStatusConsumer(VendorStatsRepository vendorStatsRepository,
                                AdminActionLogService adminActionLogService) {
        this.vendorStatsRepository = vendorStatsRepository;
        this.adminActionLogService = adminActionLogService;
    }

    @KafkaListener(topics = "${kafka.topics.vendorStatusChanged}", 
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(VendorStatusChangedEvent event) {
        logger.info("Received vendor status changed event: eventId={}, vendorId={}, previousStatus={}, newStatus={}",
                event.getEventId(), event.getVendorId(), event.getPreviousStatus(), event.getNewStatus());
        try {
            // Update vendor stats
            VendorStats vendorStats = vendorStatsRepository.findByVendorId(event.getVendorId())
                    .orElseGet(() -> {
                        VendorStats newStats = new VendorStats();
                        newStats.setVendorId(event.getVendorId());
                        return newStats;
                    });

            vendorStats.setLastUpdated(LocalDateTime.now());
            vendorStatsRepository.save(vendorStats);

            // Log admin action
            adminActionLogService.logAction(
                    event.getChangedBy(),
                    "VENDOR_STATUS_CHANGED",
                    "VENDOR",
                    event.getVendorId(),
                    String.format("Status changed from %s to %s. Reason: %s", 
                            event.getPreviousStatus(), event.getNewStatus(), event.getReason())
            );

            logger.info("Vendor status update processed successfully: eventId={}, vendorId={}, newStatus={}",
                    event.getEventId(), event.getVendorId(), event.getNewStatus());
        } catch (Exception ex) {
            logger.error("Error processing vendor status changed event: eventId={}, vendorId={}, error={}",
                    event.getEventId(), event.getVendorId(), ex.getMessage(), ex);
        }
    }
}
