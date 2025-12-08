package com.soukscan.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ModerationActionDTO {

    @NotNull(message = "adminId est obligatoire")
    private Long adminId;

    @NotNull(message = "reportId est obligatoire")
    private Long reportId;

    @NotBlank(message = "actionType est obligatoire")
    private String actionType; // approve / reject / warn / block

    @NotBlank(message = "comment est obligatoire")
    private String comment;

    // Getters & setters
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
