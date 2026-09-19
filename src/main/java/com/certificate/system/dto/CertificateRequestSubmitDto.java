package com.certificate.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CertificateRequestSubmitDto {

    @NotNull(message = "Please select a certificate type")
    private Long certificateTypeId;

    @NotBlank(message = "Purpose is required")
    @Size(max = 500, message = "Purpose cannot exceed 500 characters")
    private String purpose;

    @Size(max = 1000, message = "Remarks cannot exceed 1000 characters")
    private String remarks;

    // File handled separately via MultipartFile in controller
    public Long getCertificateTypeId() { return certificateTypeId; }
    public void setCertificateTypeId(Long certificateTypeId) { this.certificateTypeId = certificateTypeId; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
