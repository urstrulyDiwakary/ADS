package com.ads.admin.controller;

import com.ads.admin.model.Admin;
import com.ads.admin.service.ContactSubmissionService;
import com.ads.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ContactSubmissionService contactSubmissionService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // Spring Security handles authentication - no need to check session manually
        // Add any necessary model attributes for the dashboard
        model.addAttribute("pageTitle", "Admin Dashboard");
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
            // Get the full admin details to show full name
            Admin admin = adminService.findByEmail(authentication.getName());
            if (admin != null) {
                model.addAttribute("adminFullName", admin.getFullName());
            }
        }
        return "admin/dashboard";
    }

    @GetMapping("/submissions")
    public String submissions(Authentication authentication, Model model) {
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
