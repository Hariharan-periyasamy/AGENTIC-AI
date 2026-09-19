package com.certificate.system.controller;

import com.certificate.system.entity.Certificate;
import com.certificate.system.repository.CertificateRepository;
import com.certificate.system.service.AuditLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
@RequestMapping("/verify")
public class VerificationController {

    @Value("${app.certificate.dir:uploads/certificates}")
    private String certificateDir;

    private final CertificateRepository certificateRepository;
    private final AuditLogService auditLogService;

    public VerificationController(CertificateRepository certificateRepository, AuditLogService auditLogService) {
        this.certificateRepository = certificateRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String searchForm() {
        return "public/verify-search";
    }

    @PostMapping("/search")
    public String processSearch(@RequestParam("code") String code, RedirectAttributes redirectAttributes) {
        if (code == null || code.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid certificate number or verification code.");
            return "redirect:/verify";
        }
        return "redirect:/verify/" + code.trim();
    }

    @GetMapping("/{code}")
    public String verifyCertificate(@PathVariable String code, Model model) {
        Optional<Certificate> opt = certificateRepository.findByUuidOrCertificateNumber(code);
        if (opt.isPresent()) {
            model.addAttribute("valid", true);
            model.addAttribute("certificate", opt.get());
        } else {
            model.addAttribute("valid", false);
            model.addAttribute("searchedCode", code);
        }
        return "public/verify";
    }

    @GetMapping("/download/{uuid}")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable String uuid) {
        Certificate cert = certificateRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        try {
            Path file = Paths.get(certificateDir).resolve(cert.getPdfUrl()).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                auditLogService.log("CERTIFICATE_DOWNLOAD", "PUBLIC", "Downloaded certificate " + cert.getCertificateNumber());
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cert.getPdfUrl() + "\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            } else {
                throw new RuntimeException("Could not read file");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file", e);
        }
    }
}

