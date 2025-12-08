package com.soukscan.admin.kafka.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base Kafka event DTO.
 * Common fields for all Kafka events.
 */
public abstract class KafkaEvent {
    protected String eventId;
    protected LocalDateTime timestamp;

    public KafkaEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
