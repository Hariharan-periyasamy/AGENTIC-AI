package com.certificate.system.controller;

import com.certificate.system.dto.admin.AdminRequestReviewDto;
import com.certificate.system.entity.CertificateRequest;
import com.certificate.system.entity.User;
import com.certificate.system.entity.enums.RequestStatus;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.service.AdminService;
import com.certificate.system.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    private User resolveUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", adminService.buildDashboard());
        return "admin/dashboard";
    }

    @GetMapping("/requests")
    public String listRequests(@RequestParam(required = false) RequestStatus status,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        model.addAttribute("requests", adminService.searchRequests(status, keyword));
        model.addAttribute("statuses", RequestStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        return "admin/requests";
    }

    @GetMapping("/requests/{id}")
    public String viewRequest(@PathVariable Long id, 
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        // Mark as UNDER_REVIEW automatically if it's currently PENDING and admin opens it
        adminService.markUnderReview(id, resolveUser(userDetails));

        CertificateRequest request = adminService.getRequestById(id);
        model.addAttribute("certRequest", request);
        model.addAttribute("reviewDto", new AdminRequestReviewDto());
        return "admin/request-detail";
    }

    @PostMapping("/requests/{id}/approve")
    public String approveRequest(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            adminService.approveRequest(id, resolveUser(userDetails));
            redirectAttributes.addFlashAttribute("successMessage", "Request approved successfully.");
        } catch (InvalidRequestOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/requests/" + id;
    }

    @PostMapping("/requests/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @ModelAttribute AdminRequestReviewDto reviewDto,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            adminService.rejectRequest(id, reviewDto.getRejectionReason(), resolveUser(userDetails));
            redirectAttributes.addFlashAttribute("successMessage", "Request rejected.");
        } catch (InvalidRequestOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/requests/" + id;
    }
}
