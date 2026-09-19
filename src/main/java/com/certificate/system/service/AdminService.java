package com.certificate.system.service;

import com.certificate.system.dto.admin.AdminDashboardDto;
import com.certificate.system.dto.admin.AdminRequestListDto;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.Notification;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.exception.ResourceNotFoundException;
import com.certificate.system.repository.CertificateRequestRepository;
import com.certificate.system.repository.NotificationRepository;
import com.certificate.system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    private final CertificateRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogService auditLogService;
    private final CertificateGenerationService certificateGenerationService;

    public AdminService(CertificateRequestRepository requestRepository,
                        UserRepository userRepository,
                        NotificationRepository notificationRepository,
                        AuditLogService auditLogService,
                        CertificateGenerationService certificateGenerationService) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.auditLogService = auditLogService;
        this.certificateGenerationService = certificateGenerationService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto buildDashboard() {
        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setTotalRequests(requestRepository.count());
        dto.setPendingRequests(requestRepository.countByStatus(RequestStatus.PENDING));
        dto.setUnderReviewRequests(requestRepository.countByStatus(RequestStatus.UNDER_REVIEW));
        dto.setApprovedRequests(requestRepository.countByStatus(RequestStatus.APPROVED));
        dto.setRejectedRequests(requestRepository.countByStatus(RequestStatus.REJECTED));
        dto.setTotalUsers(userRepository.count());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AdminRequestListDto> searchRequests(RequestStatus status, String keyword) {
        List<CertificateRequest> results;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (status != null && hasKeyword) {
            results = requestRepository.searchByStatusAndApplicantKeyword(status, keyword.trim());
        } else if (status != null) {
            results = requestRepository.findByStatusOrderByCreatedAtDesc(status);
        } else if (hasKeyword) {
            results = requestRepository.searchByApplicantKeyword(keyword.trim());
        } else {
            results = requestRepository.findAllByOrderByCreatedAtDesc();
        }

        return results.stream().map(this::toListDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CertificateRequest getRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
    }

    @Transactional
    public void markUnderReview(Long id, User admin) {
        CertificateRequest request = getRequestById(id);
        
        if (request.getStatus() != RequestStatus.PENDING) {
            // Already processing or processed, ignore quietly or throw.
            return;
        }

        request.setStatus(RequestStatus.UNDER_REVIEW);
        requestRepository.save(request);
        
        auditLogService.log("STATUS_UNDER_REVIEW", admin.getEmail(), "Marked request " + id + " as UNDER_REVIEW");
        
        createNotification(request.getUser(), "Your request for " + request.getCertificateType().getName() + " is now under review.");
    }

    @Transactional
    public void approveRequest(Long id, User admin) {
        CertificateRequest request = getRequestById(id);

        if (request.getStatus() == RequestStatus.APPROVED) {
            throw new InvalidRequestOperationException("Request is already approved.");
        }
        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new InvalidRequestOperationException("Cannot approve a cancelled request.");
        }
        if (request.getStatus() == RequestStatus.REJECTED) {
            throw new InvalidRequestOperationException("Cannot approve a rejected request.");
        }

        request.setStatus(RequestStatus.APPROVED);
        request.setRejectionReason(null);
        requestRepository.save(request);

        // Notify user
        createNotification(request.getUser(), "Your request for " + request.getCertificateType().getName() + " has been approved.");
        
        auditLogService.log("APPROVE_REQUEST", admin.getEmail(), "Approved request ID: " + id);

        // Call generation service (Phase 7 stub)
        certificateGenerationService.generateCertificate(request);
    }

    @Transactional
    public void rejectRequest(Long id, String reason, User admin) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidRequestOperationException("Rejection reason is mandatory.");
        }

        CertificateRequest request = getRequestById(id);
        
        if (request.getStatus() == RequestStatus.REJECTED) {
            throw new InvalidRequestOperationException("Request is already rejected.");
        }
        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new InvalidRequestOperationException("Cannot reject a cancelled request.");
        }
        if (request.getStatus() == RequestStatus.APPROVED) {
            throw new InvalidRequestOperationException("Cannot reject an already approved request.");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(reason.trim());
        requestRepository.save(request);

        createNotification(request.getUser(), "Your request for " + request.getCertificateType().getName() + " has been rejected. Reason: " + reason);
        
        auditLogService.log("REJECT_REQUEST", admin.getEmail(), "Rejected request ID: " + id + ". Reason: " + reason);
    }

    private void createNotification(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    private AdminRequestListDto toListDto(CertificateRequest r) {
        AdminRequestListDto dto = new AdminRequestListDto();
        dto.setId(r.getId());
        dto.setApplicantName(r.getUser().getFirstName() + " " + r.getUser().getLastName());
        dto.setApplicantEmail(r.getUser().getEmail());
        dto.setCertificateTypeName(r.getCertificateType().getName());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }
}

