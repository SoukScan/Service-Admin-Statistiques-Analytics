package com.soukscan.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ModerationReportDTO {

    @NotNull(message = "targetId est obligatoire")
    private Long targetId;

    @NotBlank(message = "targetType est obligatoire")
    private String targetType;  // "vendor", "user", "product", etc.

    @NotBlank(message = "reason est obligatoire")
    private String reason;

    @NotNull(message = "reportedBy est obligatoire")
    private Long reportedBy;

    // Getters & setters
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Long getReportedBy() { return reportedBy; }
    public void setReportedBy(Long reportedBy) { this.reportedBy = reportedBy; }
}
