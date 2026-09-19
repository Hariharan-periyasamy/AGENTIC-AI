package com.certificate.system.controller;

import com.certificate.system.repository.AuditLogRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/audit")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminAuditController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public String listAuditLogs(Model model) {
        // Fetching top 200 logs for demo purposes. Pagination would be better for production.
        model.addAttribute("logs", auditLogRepository.findAll());
        return "admin/audit-logs";
    }
}
