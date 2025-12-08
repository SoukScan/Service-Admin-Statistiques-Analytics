package com.soukscan.admin.kafka.dto;

/**
 * Event triggered when a price report is validated.
 */
public class PriceValidatedEvent extends KafkaEvent {
    private Long priceReportId;
    private String status; // VALID, INVALID
    private String validatedBy;
    private String comment;

    public PriceValidatedEvent() {
        super();
    }

    public PriceValidatedEvent(Long priceReportId, String status, String validatedBy, String comment) {
        super();
        this.priceReportId = priceReportId;
        this.status = status;
        this.validatedBy = validatedBy;
        this.comment = comment;
    }

    public Long getPriceReportId() { return priceReportId; }
    public void setPriceReportId(Long priceReportId) { this.priceReportId = priceReportId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getValidatedBy() { return validatedBy; }
    public void setValidatedBy(String validatedBy) { this.validatedBy = validatedBy; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
