package com.ads.admin.controller;

import com.ads.admin.model.ContactSubmission;
import com.ads.admin.model.Admin;
import com.ads.admin.service.ContactSubmissionService;
import com.ads.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ContactSubmissionService contactSubmissionService;

    @Autowired
    private AdminService adminService;

    @GetMapping("/contact/summary")
    public ResponseEntity<Map<String, Object>> getContactSummary() {
        try {
            Map<String, Object> summary = new HashMap<>();
            Map<String, Object> stats = contactSubmissionService.getDashboardStats();

            // Get total count
            long totalCount = (Long) stats.get("totalSubmissions");
            summary.put("count", totalCount);

            // Get service statistics
            Map<String, Long> serviceBreakdown = (Map<String, Long>) stats.get("serviceBreakdown");
            summary.put("serviceStats", serviceBreakdown);

            // Get recent submissions count
            summary.put("recentSubmissions", stats.get("recentSubmissions"));
            summary.put("weeklySubmissions", stats.get("weeklySubmissions"));

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load summary data");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/contact/submissions")
    public ResponseEntity<List<ContactSubmission>> getAllSubmissions() {
        try {
            List<ContactSubmission> submissions = contactSubmissionService.getAllSubmissions();
            return ResponseEntity.ok(submissions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/contact/submissions/{id}")
    public ResponseEntity<ContactSubmission> getSubmissionById(@PathVariable Long id) {
        try {
            ContactSubmission submission = contactSubmissionService.getSubmissionById(id);
            if (submission != null) {
                return ResponseEntity.ok(submission);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/contact/submissions/{id}")
    public ResponseEntity<Map<String, Object>> deleteSubmission(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = contactSubmissionService.deleteSubmission(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Submission deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Submission not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting submission");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/contact/search")
    public ResponseEntity<List<ContactSubmission>> searchSubmissions(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String name) {
        try {
            List<ContactSubmission> results;

            if (service != null && !service.trim().isEmpty()) {
                results = contactSubmissionService.searchByService(service);
            } else if (name != null && !name.trim().isEmpty()) {
                results = contactSubmissionService.searchByName(name);
            } else {
                results = contactSubmissionService.getAllSubmissions();
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = contactSubmissionService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load dashboard statistics");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ==================== ADMIN MANAGEMENT API ENDPOINTS ====================

    @GetMapping("/admins")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        try {
            List<Admin> admins = adminService.findAllAdmins();
            // Remove password from response
            admins.forEach(admin -> admin.setPassword(null));
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/admins/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable Long id) {
        try {
            Admin admin = adminService.findById(id);
            if (admin != null) {
                admin.setPassword(null); // Don't send password
                return ResponseEntity.ok(admin);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/admins")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody Admin admin) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate input
            if (admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username is required");
                return ResponseEntity.badRequest().body(response);
            }
            if (admin.getEmail() == null || admin.getEmail().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            if (admin.getPassword() == null || admin.getPassword().length() < 8) {
                response.put("success", false);
                response.put("message", "Password must be at least 8 characters");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if username or email already exists
            if (adminService.existsByUsername(admin.getUsername())) {
                response.put("success", false);
                response.put("message", "Username already exists");
                return ResponseEntity.badRequest().body(response);
            }

            Admin createdAdmin = adminService.createAdminFull(admin);
            createdAdmin.setPassword(null); // Don't send password back
            response.put("success", true);
            response.put("message", "Admin created successfully");
            response.put("admin", createdAdmin);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating admin: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/admins/{id}")
    public ResponseEntity<Map<String, Object>> updateAdmin(@PathVariable Long id, @RequestBody Admin admin) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin updatedAdmin = adminService.updateAdminDetails(id, admin);
            if (updatedAdmin != null) {
                updatedAdmin.setPassword(null);
                response.put("success", true);
                response.put("message", "Admin updated successfully");
                response.put("admin", updatedAdmin);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Admin not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating admin: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/admins/{id}/password")
    public ResponseEntity<Map<String, Object>> changeAdminPassword(@PathVariable Long id, @RequestBody Map<String, String> passwordData) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newPassword = passwordData.get("newPassword");

            if (newPassword == null || newPassword.length() < 8) {
                response.put("success", false);
                response.put("message", "Password must be at least 8 characters");
                return ResponseEntity.badRequest().body(response);
            }

            boolean updated = adminService.updatePassword(id, newPassword);
            if (updated) {
                response.put("success", true);
                response.put("message", "Password changed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Admin not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error changing password: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Map<String, Object>> deleteAdmin(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = adminService.deleteAdminById(id);
            if (deleted) {
                response.put("success", true);
                response.put("message", "Admin deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Admin not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting admin: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/admins/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleAdminStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean toggled = adminService.toggleAdminStatus(id);
            if (toggled) {
                Admin admin = adminService.findById(id);
                admin.setPassword(null);
                response.put("success", true);
                response.put("message", "Admin status updated successfully");
                response.put("admin", admin);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Admin not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error toggling admin status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }


    // JSON API endpoint for contact form submissions
    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> submitContactJson(@RequestBody Map<String, String> contactData) {
        Map<String, Object> response = new HashMap<>();

        try {
            ContactSubmission submission = new ContactSubmission();
            submission.setName(contactData.get("name"));
            submission.setEmail(contactData.get("email"));
            submission.setPhone(contactData.get("phone"));
            submission.setService(contactData.get("service"));
            submission.setMessage(contactData.get("message"));
            submission.setDate(java.time.LocalDateTime.now());

            contactSubmissionService.saveSubmission(submission);

            response.put("success", true);
            response.put("message", "Thank you for your message! We'll get back to you soon.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing your request. Please try again later.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
