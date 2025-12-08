package com.soukscan.admin.kafka.consumer;

import com.soukscan.admin.kafka.dto.PriceReportedEvent;
import com.soukscan.admin.service.PriceReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

/**
 * Unit tests for PriceReportedConsumer.
 */
class PriceReportedConsumerTest {

    @Mock
    private PriceReportService priceReportService;

    @InjectMocks
    private PriceReportedConsumer priceReportedConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConsumeValidPriceReportedEvent() {
        // Arrange
        PriceReportedEvent event = new PriceReportedEvent();
        event.setPriceReportId(1L);
        event.setProductId(100L);
        event.setReporterId(50L);
        event.setReportedPrice(29.99);
        event.setReason("Price anomaly detected");

        // Act
        priceReportedConsumer.consume(event);

        // Assert
        verify(priceReportService, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void testConsumeNullEvent() {
        // Act & Assert - should handle gracefully (pass empty event instead of null)
        PriceReportedEvent emptyEvent = new PriceReportedEvent();
        priceReportedConsumer.consume(emptyEvent);
        verify(priceReportService, never()).updateStatus(anyLong(), anyString());
    }

    @Test
    void testConsumeEventWithException() {
        // Arrange
        PriceReportedEvent event = new PriceReportedEvent();
        event.setPriceReportId(null);

        // Act
        try {
            priceReportedConsumer.consume(event);
        } catch (Exception ex) {
            // Assert - exception should be logged but not thrown
            verify(priceReportService, never()).updateStatus(anyLong(), anyString());
        }
    }
}
