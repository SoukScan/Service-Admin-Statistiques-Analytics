package com.soukscan.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data Transfer Object for vendor verification documents.
 * Used to check document existence and metadata when approving/rejecting vendors.
 * The actual document file is stored and managed by Vendor-Service.
 */
@Schema(description = "Vendor verification document metadata")
public class VendorDocumentDTO {

    @Schema(description = "Vendor ID who owns the document", example = "12345")
    private Long vendorId;

    @Schema(description = "Document file name", example = "business_license_12345.pdf")
    private String fileName;

    @Schema(description = "Document file type/MIME type", example = "application/pdf")
    private String contentType;

    @Schema(description = "Document file size in bytes", example = "1024576")
    private Long fileSize;

    @Schema(description = "Document upload date/time in ISO 8601 format", example = "2025-12-10T14:30:00Z")
    private String uploadedAt;

    @Schema(description = "Document verification status", example = "PENDING")
    private String verificationStatus;

    @Schema(description = "Signed URL for downloading the document (if provided by Vendor-Service)", example = "https://vendor-storage.example.com/docs/12345?signature=xyz&expires=1702219800")
    private String downloadUrl;

    @Schema(description = "URL path on Vendor-Service to fetch the document", example = "/vendors/12345/document")
    private String documentPath;

    public VendorDocumentDTO() {
    }

    public VendorDocumentDTO(Long vendorId, String fileName, String contentType, Long fileSize, String uploadedAt) {
        this.vendorId = vendorId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedAt = uploadedAt;
    }

    // Getters and Setters
    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    @Override
    public String toString() {
        return "VendorDocumentDTO{" +
                "vendorId=" + vendorId +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", fileSize=" + fileSize +
                ", uploadedAt='" + uploadedAt + '\'' +
                ", verificationStatus='" + verificationStatus + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
}
