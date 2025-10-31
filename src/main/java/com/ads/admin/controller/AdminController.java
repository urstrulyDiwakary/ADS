package com.ads.admin.controller;

import com.ads.admin.model.Admin;
import com.ads.admin.service.ContactSubmissionService;
import com.ads.admin.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private ContactSubmissionService contactSubmissionService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        logger.info("Admin login page accessed");

        // Force CSRF token generation on first visit to login page
        // This ensures the token is available for the first login attempt
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Add to model to ensure Thymeleaf can access it
            model.addAttribute("_csrf", csrfToken);
            logger.debug("CSRF token loaded: {}", csrfToken.getToken());
        }

        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        logger.info("Dashboard accessed - Authentication: {}", authentication != null ? authentication.getName() : "null");

        // Spring Security handles authentication - no need to check session manually
        // Add any necessary model attributes for the dashboard
        model.addAttribute("pageTitle", "Admin Dashboard");
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            // Get the full admin details to show full name
            Admin admin = adminService.findByEmail(authentication.getName());
            if (admin != null) {
                model.addAttribute("adminFullName", admin.getFullName());
                logger.info("Admin dashboard loaded for: {}", admin.getFullName());
            }
        }
        return "admin/dashboard";
    }

    @GetMapping("/submissions")
    public String submissions(Authentication authentication, Model model) {
        logger.info("Submissions page accessed - Authentication: {}", authentication != null ? authentication.getName() : "null");

        // Spring Security handles authentication
        // Add submissions data to model
        model.addAttribute("submissions", contactSubmissionService.getAllSubmissions());
        model.addAttribute("pageTitle", "Contact Submissions");
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        return "admin/submissions";
    }
}
