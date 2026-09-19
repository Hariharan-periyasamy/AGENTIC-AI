package com.certificate.system.dto.admin;

import jakarta.validation.constraints.NotBlank;

public class AdminRequestReviewDto {
    
    // Only populated when rejecting
    private String rejectionReason;

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}
