package com.certificate.system.service;

import com.certificate.system.dto.CertificateRequestDto;
import com.certificate.system.dto.CertificateRequestSubmitDto;
import com.certificate.system.dto.DashboardDto;
import com.certificate.system.dto.NotificationDto;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.CertificateType;
import com.certificate.system.entity.Notification;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.exception.DuplicateRequestException;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.exception.ResourceNotFoundException;
import com.certificate.system.repository.CertificateRepository;
import com.certificate.system.repository.CertificateRequestRepository;
import com.certificate.system.repository.CertificateTypeRepository;
import com.certificate.system.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserPortalService {

    private static final Logger logger = LoggerFactory.getLogger(UserPortalService.class);

    private final CertificateRequestRepository requestRepository;
    private final CertificateRepository certificateRepository;
    private final NotificationRepository notificationRepository;
    private final CertificateTypeRepository typeRepository;
    private final AuditLogService auditLogService;

    public UserPortalService(CertificateRequestRepository requestRepository,
                             CertificateRepository certificateRepository,
                             NotificationRepository notificationRepository,
                             CertificateTypeRepository typeRepository,
                             AuditLogService auditLogService) {
        this.requestRepository = requestRepository;
        this.certificateRepository = certificateRepository;
        this.notificationRepository = notificationRepository;
        this.typeRepository = typeRepository;
        this.auditLogService = auditLogService;
    }

    // ----------------------------------------------------------------
    // Dashboard
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public DashboardDto buildDashboard(User user) {
        DashboardDto dto = new DashboardDto();
        dto.setFullName(user.getFirstName() + " " + user.getLastName());
        dto.setTotalRequests(requestRepository.findByUserOrderByCreatedAtDesc(user).size());
        dto.setPendingRequests(requestRepository.countByUserAndStatus(user, RequestStatus.PENDING));
        dto.setApprovedRequests(requestRepository.countByUserAndStatus(user, RequestStatus.APPROVED));
        dto.setRejectedRequests(requestRepository.countByUserAndStatus(user, RequestStatus.REJECTED));
        dto.setUnreadNotifications(notificationRepository.countByUserAndReadFalse(user));
        return dto;
    }

    // ----------------------------------------------------------------
    // Requests 
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<CertificateRequestDto> getRequestsForUser(User user) {
        return requestRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CertificateRequestDto getRequestForUser(Long requestId, User user) {
        CertificateRequest request = requestRepository.findByIdAndUser(requestId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found or access denied."));
        return toRequestDto(request);
    }

    @Transactional
    public CertificateRequest submitRequest(User user, CertificateRequestSubmitDto dto, String storedFilename) {
        CertificateType type = typeRepository.findById(dto.getCertificateTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid certificate type selected."));

        long duplicateCount = requestRepository.countActiveDuplicates(user, type);
        if (duplicateCount > 0) {
            throw new DuplicateRequestException("You already have an active request for this certificate type.");
        }

        CertificateRequest req = new CertificateRequest();
        req.setUser(user);
        req.setCertificateType(type);
        req.setStatus(RequestStatus.PENDING);
        req.setPurpose(dto.getPurpose());
        req.setRemarks(dto.getRemarks());
        req.setSupportingDocumentPath(storedFilename);

        CertificateRequest saved = requestRepository.save(req);
        
        auditLogService.log("SUBMIT_REQUEST", user.getEmail(), "Submitted request ID: " + saved.getId() + " for type: " + type.getName());
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Your request for " + type.getName() + " has been submitted successfully.");
        notificationRepository.save(notification);
        return saved;
    }

    @Transactional
    public void cancelRequest(Long requestId, User user) {
        CertificateRequest request = requestRepository.findByIdAndUser(requestId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found or access denied."));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestOperationException("Only PENDING requests can be cancelled.");
        }

        request.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(request);

        auditLogService.log("CANCEL_REQUEST", user.getEmail(), "Cancelled request ID: " + request.getId());
    }

    private CertificateRequestDto toRequestDto(CertificateRequest r) {
        CertificateRequestDto dto = new CertificateRequestDto();
        dto.setId(r.getId());
        dto.setCertificateTypeName(r.getCertificateType().getName());
        dto.setStatus(r.getStatus());
        dto.setPurpose(r.getPurpose());
        dto.setRemarks(r.getRemarks());
        dto.setSupportingDocumentPath(r.getSupportingDocumentPath());
        dto.setRejectionReason(r.getRejectionReason());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        
        dto.setCancellable(r.getStatus() == RequestStatus.PENDING);

        certificateRepository.findByCertificateRequest(r).ifPresent(cert -> {
            dto.setHasCertificate(true);
            dto.setCertificateUuid(cert.getUuid());
        });

        return dto;
    }

    // ----------------------------------------------------------------
    // Notifications 
    // ----------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotificationsForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toNotificationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAllNotificationsRead(User user) {
        notificationRepository.markAllReadForUser(user);
        logger.info("Marked all notifications as read for user: {}", user.getEmail());
    }

    private NotificationDto toNotificationDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<CertificateType> getAvailableCertificateTypes() {
        return typeRepository.findAll();
    }
}

