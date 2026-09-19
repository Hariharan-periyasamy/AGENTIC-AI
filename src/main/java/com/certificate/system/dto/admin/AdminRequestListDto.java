package com.certificate.system.dto.admin;

import com.certificate.system.entity.enums.RequestStatus;
import java.time.LocalDateTime;

public class AdminRequestListDto {
    private Long id;
    private String applicantName;
    private String applicantEmail;
    private String certificateTypeName;
    private RequestStatus status;
    private LocalDateTime createdAt;
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getApplicantEmail() { return applicantEmail; }
    public void setApplicantEmail(String applicantEmail) { this.applicantEmail = applicantEmail; }

    public String getCertificateTypeName() { return certificateTypeName; }
    public void setCertificateTypeName(String certificateTypeName) { this.certificateTypeName = certificateTypeName; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
