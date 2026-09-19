package com.certificate.system.controller;

import com.certificate.system.dto.publicapi.CertificateVerificationDto;
import com.certificate.system.entity.Certificate;
import com.certificate.system.repository.CertificateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/certificates")
public class VerificationApiController {

    private final CertificateRepository certificateRepository;

    public VerificationApiController(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @GetMapping("/verify/{code}")
    public ResponseEntity<CertificateVerificationDto> verifyCertificate(@PathVariable String code) {
        if (code == null || code.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Certificate> opt = certificateRepository.findByUuidOrCertificateNumber(code.trim());
        CertificateVerificationDto dto = new CertificateVerificationDto();

        if (opt.isPresent()) {
            Certificate cert = opt.get();
            dto.setValid(true);
            dto.setCertificateNumber(cert.getCertificateNumber());
            dto.setHolderName(cert.getCertificateRequest().getUser().getFirstName() + " " + cert.getCertificateRequest().getUser().getLastName());
            dto.setCertificateType(cert.getCertificateRequest().getCertificateType().getName());
            dto.setIssueDate(cert.getIssuedAt());
            dto.setIssuingAuthority("Digital Certificate Authority");
            dto.setVerificationCode(cert.getUuid());
            return ResponseEntity.ok(dto);
        } else {
            dto.setValid(false);
            return ResponseEntity.ok(dto);
        }
    }
}
