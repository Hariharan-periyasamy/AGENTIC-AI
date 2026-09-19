package com.certificate.system.dto;

import com.certificate.system.entity.enums.RequestStatus;

import java.time.LocalDateTime;

public class CertificateRequestDto {

    private Long id;
    private String certificateTypeName;
    private RequestStatus status;
    private String purpose;
    private String remarks;
    private String supportingDocumentPath;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean hasCertificate;
    private String certificateUuid;
    private boolean cancellable; // true only when status == PENDING

    // ---- Getters / Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCertificateTypeName() { return certificateTypeName; }
    public void setCertificateTypeName(String v) { this.certificateTypeName = v; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getSupportingDocumentPath() { return supportingDocumentPath; }
    public void setSupportingDocumentPath(String v) { this.supportingDocumentPath = v; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String v) { this.rejectionReason = v; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isHasCertificate() { return hasCertificate; }
    public void setHasCertificate(boolean hasCertificate) { this.hasCertificate = hasCertificate; }

    public String getCertificateUuid() { return certificateUuid; }
    public void setCertificateUuid(String certificateUuid) { this.certificateUuid = certificateUuid; }

    public boolean isCancellable() { return cancellable; }
    public void setCancellable(boolean cancellable) { this.cancellable = cancellable; }
}
