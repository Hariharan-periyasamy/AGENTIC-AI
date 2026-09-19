package com.certificate.system.repository;

import com.certificate.system.entity.Certificate;
import com.certificate.system.entity.CertificateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByUuid(String uuid);
    Optional<Certificate> findByCertificateRequest(CertificateRequest request);
    Optional<Certificate> findByCertificateNumber(String certificateNumber);
    
    @Query("SELECT c FROM Certificate c WHERE c.uuid = :code OR c.certificateNumber = :code")
    Optional<Certificate> findByUuidOrCertificateNumber(@Param("code") String code);
}
