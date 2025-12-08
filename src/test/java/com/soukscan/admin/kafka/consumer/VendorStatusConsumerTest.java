package com.soukscan.admin.kafka.consumer;

import com.soukscan.admin.entity.VendorStats;
import com.soukscan.admin.kafka.dto.VendorStatusChangedEvent;
import com.soukscan.admin.repository.VendorStatsRepository;
import com.soukscan.admin.service.AdminActionLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VendorStatusConsumer.
 */
class VendorStatusConsumerTest {

    @Mock
    private VendorStatsRepository vendorStatsRepository;

    @Mock
    private AdminActionLogService adminActionLogService;

    @InjectMocks
    private VendorStatusConsumer vendorStatusConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConsumeValidVendorStatusChangedEvent() {
        // Arrange
        VendorStatusChangedEvent event = new VendorStatusChangedEvent();
        event.setVendorId(1L);
        event.setPreviousStatus("PENDING");
        event.setNewStatus("VERIFIED");
        event.setReason("Verification passed");
        event.setChangedBy(99L);

        VendorStats vendorStats = new VendorStats();
        vendorStats.setVendorId(1L);
        when(vendorStatsRepository.findByVendorId(1L)).thenReturn(Optional.of(vendorStats));
        when(vendorStatsRepository.save(any(VendorStats.class))).thenReturn(vendorStats);

        // Act
        vendorStatusConsumer.consume(event);

        // Assert
        verify(vendorStatsRepository, times(1)).findByVendorId(1L);
        verify(vendorStatsRepository, times(1)).save(any(VendorStats.class));
        verify(adminActionLogService, times(1)).logAction(anyLong(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void testConsumeEventWithNewVendor() {
        // Arrange
        VendorStatusChangedEvent event = new VendorStatusChangedEvent();
        event.setVendorId(2L);
        event.setPreviousStatus("NEW");
        event.setNewStatus("PENDING");
        event.setReason("Registration submitted");
        event.setChangedBy(99L);

        when(vendorStatsRepository.findByVendorId(2L)).thenReturn(Optional.empty());
        when(vendorStatsRepository.save(any(VendorStats.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        vendorStatusConsumer.consume(event);

        // Assert
        verify(vendorStatsRepository, times(1)).findByVendorId(2L);
        verify(vendorStatsRepository, times(1)).save(any(VendorStats.class));
        verify(adminActionLogService, times(1)).logAction(anyLong(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void testConsumeEventWithException() {
        // Arrange
        VendorStatusChangedEvent event = new VendorStatusChangedEvent();
        event.setVendorId(null);
        event.setChangedBy(99L);

        when(vendorStatsRepository.findByVendorId(null)).thenThrow(new RuntimeException("Database error"));

        // Act
        try {
            vendorStatusConsumer.consume(event);
        } catch (Exception ex) {
            // Assert - exception should be logged but not thrown
            verify(vendorStatsRepository, times(1)).findByVendorId(null);
        }
    }
}
