package com.soukscan.admin.kafka.producer;

import com.soukscan.admin.kafka.dto.PriceValidatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * Kafka producer service for publishing events to Kafka topics.
 */
@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.priceValidated}")
    private String priceValidatedTopic;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send a price validated event to Kafka.
     * @param event the price validated event
     */
    public void sendPriceValidated(PriceValidatedEvent event) {
        logger.info("Publishing price validated event: eventId={}, priceReportId={}, status={}",
                event.getEventId(), event.getPriceReportId(), event.getStatus());
        try {
            Message<PriceValidatedEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, priceValidatedTopic)
                    .setHeader("eventId", event.getEventId())
                    .build();

            kafkaTemplate.send(message);
            logger.debug("Price validated event sent successfully: eventId={}", event.getEventId());
        } catch (Exception ex) {
            logger.error("Error publishing price validated event: eventId={}, error={}",
                    event.getEventId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to publish price validated event", ex);
        }
    }
}
