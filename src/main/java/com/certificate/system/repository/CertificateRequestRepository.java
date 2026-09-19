package com.certificate.system.repository;

import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.CertificateType;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CertificateRequestRepository extends JpaRepository<CertificateRequest, Long> {

    List<CertificateRequest> findByUserOrderByCreatedAtDesc(User user);

    List<CertificateRequest> findByStatus(RequestStatus status);

    List<CertificateRequest> findAllByOrderByCreatedAtDesc();

    // SECURITY: user can only fetch their own request by id
    Optional<CertificateRequest> findByIdAndUser(Long id, User user);

    @Query("SELECT COUNT(r) FROM CertificateRequest r WHERE r.user = :user AND r.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") RequestStatus status);

    // Duplicate prevention: check if user already has PENDING or UNDER_REVIEW request for same type
    @Query("SELECT COUNT(r) FROM CertificateRequest r " +
           "WHERE r.user = :user AND r.certificateType = :type " +
           "AND r.status IN ('PENDING', 'UNDER_REVIEW')")
    long countActiveDuplicates(@Param("user") User user, @Param("type") CertificateType type);

    // Admin searches
    List<CertificateRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);

    @Query("SELECT r FROM CertificateRequest r " +
           "WHERE LOWER(r.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY r.createdAt DESC")
    List<CertificateRequest> searchByApplicantKeyword(@Param("keyword") String keyword);

    @Query("SELECT r FROM CertificateRequest r " +
           "WHERE r.status = :status AND (" +
           "LOWER(r.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) )" +
           "ORDER BY r.createdAt DESC")
    List<CertificateRequest> searchByStatusAndApplicantKeyword(@Param("status") RequestStatus status, @Param("keyword") String keyword);
    
    long countByStatus(RequestStatus status);
}
