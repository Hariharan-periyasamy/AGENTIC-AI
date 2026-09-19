package com.certificate.system.entity;

import com.certificate.system.entity.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_requests", indexes = {
        @Index(name = "idx_req_user_id",   columnList = "user_id"),
        @Index(name = "idx_req_status",    columnList = "status"),
        @Index(name = "idx_req_type_id",   columnList = "type_id")
})
@Getter @Setter
public class CertificateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private CertificateType certificateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    // Phase 5 additions — purpose and remarks required for submission
    @Column(nullable = false, length = 500)
    private String purpose;

    @Column(length = 1000)
    private String remarks;

    // CONTRACT field name: supportingDocumentPath
    @Column(name = "supporting_document_path")
    private String supportingDocumentPath;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
