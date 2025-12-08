package com.soukscan.admin.kafka.dto;

import java.time.LocalDateTime;

/**
 * Event triggered when a price is reported.
 */
public class PriceReportedEvent extends KafkaEvent {
    private Long priceReportId;
    private Long productId;
    private Long reporterId;
    private Double reportedPrice;
    private String reason;

    public PriceReportedEvent() {
        super();
    }

    public PriceReportedEvent(Long priceReportId, Long productId, Long reporterId, 
                             Double reportedPrice, String reason) {
        super();
        this.priceReportId = priceReportId;
        this.productId = productId;
        this.reporterId = reporterId;
        this.reportedPrice = reportedPrice;
        this.reason = reason;
    }

    public Long getPriceReportId() { return priceReportId; }
    public void setPriceReportId(Long priceReportId) { this.priceReportId = priceReportId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getReporterId() { return reporterId; }
    public void setReporterId(Long reporterId) { this.reporterId = reporterId; }

    public Double getReportedPrice() { return reportedPrice; }
    public void setReportedPrice(Double reportedPrice) { this.reportedPrice = reportedPrice; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
