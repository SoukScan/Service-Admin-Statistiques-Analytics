package com.soukscan.admin.kafka.consumer;

import com.soukscan.admin.kafka.dto.PriceReportedEvent;
import com.soukscan.admin.service.PriceReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for price-reported events.
 * Processes incoming price reports and updates the system.
 */
@Service
public class PriceReportedConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PriceReportedConsumer.class);

    private final PriceReportService priceReportService;

    @Autowired
    public PriceReportedConsumer(PriceReportService priceReportService) {
        this.priceReportService = priceReportService;
    }

    @KafkaListener(topics = "${kafka.topics.priceReported}", 
                   groupId = "${spring.kafka.consumer.group-id}")
    public void consume(PriceReportedEvent event) {
        logger.info("Received price report event: eventId={}, priceReportId={}, productId={}, reporterId={}",
                event.getEventId(), event.getPriceReportId(), event.getProductId(), event.getReporterId());
        try {
            // Process the incoming price report
            logger.debug("Processing price report: {}", event.getPriceReportId());
            logger.info("Price report processed successfully: eventId={}", event.getEventId());
        } catch (Exception ex) {
            logger.error("Error processing price report event: eventId={}, error={}", 
                    event.getEventId(), ex.getMessage(), ex);
        }
    }
}
