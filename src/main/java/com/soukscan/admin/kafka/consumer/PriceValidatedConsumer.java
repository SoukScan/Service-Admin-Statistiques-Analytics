package com.soukscan.admin.kafka.consumer;

import com.soukscan.admin.kafka.dto.PriceValidatedEvent;
import com.soukscan.admin.service.PriceReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for price-validated events.
 * Updates price report validation status.
 */
@Service
public class PriceValidatedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PriceValidatedConsumer.class);

    private final PriceReportService priceReportService;

    @Autowired
    public PriceValidatedConsumer(PriceReportService priceReportService) {
        this.priceReportService = priceReportService;
    }

    @KafkaListener(topics = "${kafka.topics.priceValidated}", 
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PriceValidatedEvent event) {
        logger.info("Received price validated event: eventId={}, priceReportId={}, status={}, validatedBy={}",
                event.getEventId(), event.getPriceReportId(), event.getStatus(), event.getValidatedBy());
        try {
            // Update price report validation status
            priceReportService.updateStatus(event.getPriceReportId(), event.getStatus());
            logger.info("Price report validation processed successfully: eventId={}, reportId={}, status={}",
                    event.getEventId(), event.getPriceReportId(), event.getStatus());
        } catch (Exception ex) {
            logger.error("Error processing price validated event: eventId={}, reportId={}, error={}",
                    event.getEventId(), event.getPriceReportId(), ex.getMessage(), ex);
        }
    }
}
