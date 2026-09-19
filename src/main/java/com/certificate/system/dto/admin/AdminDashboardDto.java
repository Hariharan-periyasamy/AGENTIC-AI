package com.certificate.system.dto.admin;

public class AdminDashboardDto {
    private long totalRequests;
    private long pendingRequests;
    private long underReviewRequests;
    private long approvedRequests;
    private long rejectedRequests;
    private long totalUsers;

    public long getTotalRequests() { return totalRequests; }
    public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

    public long getPendingRequests() { return pendingRequests; }
    public void setPendingRequests(long pendingRequests) { this.pendingRequests = pendingRequests; }

    public long getUnderReviewRequests() { return underReviewRequests; }
    public void setUnderReviewRequests(long underReviewRequests) { this.underReviewRequests = underReviewRequests; }

    public long getApprovedRequests() { return approvedRequests; }
    public void setApprovedRequests(long approvedRequests) { this.approvedRequests = approvedRequests; }

    public long getRejectedRequests() { return rejectedRequests; }
    public void setRejectedRequests(long rejectedRequests) { this.rejectedRequests = rejectedRequests; }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
}
