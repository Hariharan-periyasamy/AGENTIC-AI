package com.certificate.system.repository;
import com.certificate.system.entity.CertificateType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CertificateTypeRepository extends JpaRepository<CertificateType, Long> {
    Optional<CertificateType> findByName(String name);
}
