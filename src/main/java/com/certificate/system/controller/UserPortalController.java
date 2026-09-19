package com.certificate.system.controller;

import com.certificate.system.dto.CertificateRequestDto;
import com.certificate.system.dto.CertificateRequestSubmitDto;
import com.certificate.system.dto.DashboardDto;
import com.certificate.system.dto.NotificationDto;
import com.certificate.system.dto.UserProfileDto;
import com.certificate.system.entity.User;
import com.certificate.system.exception.DuplicateRequestException;
import com.certificate.system.exception.FileStorageException;
import com.certificate.system.exception.InvalidRequestOperationException;
import com.certificate.system.service.FileStorageService;
import com.certificate.system.service.UserPortalService;
import com.certificate.system.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAuthority('ROLE_USER')")
public class UserPortalController {

    private static final Logger logger = LoggerFactory.getLogger(UserPortalController.class);

    private final UserService userService;
    private final UserPortalService userPortalService;
    private final FileStorageService fileStorageService;

    public UserPortalController(UserService userService, 
                                UserPortalService userPortalService,
                                FileStorageService fileStorageService) {
        this.userService = userService;
        this.userPortalService = userPortalService;
        this.fileStorageService = fileStorageService;
    }

    private User resolveUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    // ----------------------------------------------------------------
    // DASHBOARD
    // ----------------------------------------------------------------
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = resolveUser(userDetails);
        DashboardDto dashboard = userPortalService.buildDashboard(user);
        model.addAttribute("dashboard", dashboard);
        return "user/dashboard";
    }

    // ----------------------------------------------------------------
    // PROFILE
    // ----------------------------------------------------------------
    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UserProfileDto profileDto = userService.getProfileDto(userDetails.getUsername());
        model.addAttribute("profileDto", profileDto);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("profileDto") UserProfileDto profileDto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "user/profile";
        }
        userService.updateProfile(userDetails.getUsername(), profileDto);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        return "redirect:/user/profile";
    }

    // ----------------------------------------------------------------
    // REQUESTS
    // ----------------------------------------------------------------
    @GetMapping("/requests")
    public String listRequests(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = resolveUser(userDetails);
        List<CertificateRequestDto> requests = userPortalService.getRequestsForUser(user);
        model.addAttribute("requests", requests);
        return "user/requests";
    }

    @GetMapping("/requests/{id}")
    public String viewRequest(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User user = resolveUser(userDetails);
        CertificateRequestDto request = userPortalService.getRequestForUser(id, user);
        model.addAttribute("certRequest", request);
        return "user/request-detail";
    }

    @PostMapping("/requests/{id}/cancel")
    public String cancelRequest(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        try {
            userPortalService.cancelRequest(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Request cancelled successfully.");
        } catch (InvalidRequestOperationException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/requests/" + id;
    }

    @GetMapping("/requests/new")
    public String newRequestForm(Model model) {
        model.addAttribute("submitDto", new CertificateRequestSubmitDto());
        model.addAttribute("certificateTypes", userPortalService.getAvailableCertificateTypes());
        return "user/new-request";
    }

    @PostMapping("/requests/new")
    public String submitRequest(@AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("submitDto") CertificateRequestSubmitDto dto,
                                BindingResult result,
                                @RequestParam("document") MultipartFile document,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("certificateTypes", userPortalService.getAvailableCertificateTypes());
            return "user/new-request";
        }

        if (document.isEmpty()) {
            model.addAttribute("errorMessage", "Please upload a supporting document.");
            model.addAttribute("certificateTypes", userPortalService.getAvailableCertificateTypes());
            return "user/new-request";
        }

        try {
            String storedFilename = fileStorageService.storeFile(document);
            User user = resolveUser(userDetails);
            userPortalService.submitRequest(user, dto, storedFilename);
            redirectAttributes.addFlashAttribute("successMessage", "Certificate request submitted successfully.");
            return "redirect:/user/requests";
        } catch (FileStorageException | DuplicateRequestException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("certificateTypes", userPortalService.getAvailableCertificateTypes());
            return "user/new-request";
        }
    }

    // ----------------------------------------------------------------
    // NOTIFICATIONS
    // ----------------------------------------------------------------
    @GetMapping("/notifications")
    public String listNotifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = resolveUser(userDetails);
        List<NotificationDto> notifications = userPortalService.getNotificationsForUser(user);
        model.addAttribute("notifications", notifications);
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        model.addAttribute("unreadCount", unreadCount);
        return "user/notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllRead(@AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        User user = resolveUser(userDetails);
        userPortalService.markAllNotificationsRead(user);
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/user/notifications";
    }

    // ----------------------------------------------------------------
    // CERTIFICATES
    // ----------------------------------------------------------------
    @GetMapping("/certificates")
    public String listCertificates(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = resolveUser(userDetails);
        List<CertificateRequestDto> approved = userPortalService.getRequestsForUser(user)
                .stream()
                .filter(CertificateRequestDto::isHasCertificate)
                .toList();
        model.addAttribute("certificates", approved);
        return "user/certificates";
    }
}
