package com.soukscan.admin.kafka.dto;

/**
 * Event triggered when a vendor status changes.
 */
public class VendorStatusChangedEvent extends KafkaEvent {
    private Long vendorId;
    private String previousStatus;
    private String newStatus; // PENDING, VERIFIED, REJECTED, SUSPENDED, ACTIVE
    private String reason;
    private Long changedBy;

    public VendorStatusChangedEvent() {
        super();
    }

    public VendorStatusChangedEvent(Long vendorId, String previousStatus, String newStatus, 
                                   String reason, Long changedBy) {
        super();
        this.vendorId = vendorId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedBy = changedBy;
    }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getChangedBy() { return changedBy; }
    public void setChangedBy(Long changedBy) { this.changedBy = changedBy; }
}
