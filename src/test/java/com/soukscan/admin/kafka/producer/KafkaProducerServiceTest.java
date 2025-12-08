package com.soukscan.admin.kafka.producer;

import com.soukscan.admin.kafka.dto.PriceValidatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KafkaProducerService.
 */
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(kafkaProducerService, "priceValidatedTopic", "price.validated");
    }

    @Test
    void testSendPriceValidatedEventSuccess() {
        // Arrange
        PriceValidatedEvent event = new PriceValidatedEvent();
        event.setPriceReportId(1L);
        event.setStatus("VALID");
        event.setValidatedBy("ADMIN_001");
        event.setComment("Price validation passed");

        when(kafkaTemplate.send(any(Message.class))).thenReturn(null);

        // Act
        kafkaProducerService.sendPriceValidated(event);

        // Assert
        verify(kafkaTemplate, times(1)).send(any(Message.class));
    }

    @Test
    void testSendPriceValidatedEventWithNullEvent() {
        // Act & Assert
        try {
            kafkaProducerService.sendPriceValidated(null);
        } catch (NullPointerException ex) {
            verify(kafkaTemplate, never()).send(any(Message.class));
        }
    }

    @Test
    void testSendPriceValidatedEventKafkaError() {
        // Arrange
        PriceValidatedEvent event = new PriceValidatedEvent();
        event.setPriceReportId(1L);
        event.setStatus("INVALID");

        when(kafkaTemplate.send(any(Message.class))).thenThrow(new RuntimeException("Kafka error"));

        // Act & Assert
        try {
            kafkaProducerService.sendPriceValidated(event);
        } catch (RuntimeException ex) {
            verify(kafkaTemplate, times(1)).send(any(Message.class));
        }
    }
}
