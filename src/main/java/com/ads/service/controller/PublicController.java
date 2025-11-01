package com.ads.service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ads.service.model.ContactSubmission;
import com.ads.service.repository.ContactSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    @Autowired
    private ContactSubmissionRepository contactSubmissionRepository;

    // Home page mappings - handle both with and without .html extension
    @GetMapping({"/", "/home", "/home.html"})
    @ResponseBody
    public ResponseEntity<Resource> home() {
        try {
            Resource resource = new ClassPathResource("static/HOME.html");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // About page mappings
    @GetMapping({"/about", "/about.html"})
    @ResponseBody
    public ResponseEntity<Resource> about() {
        try {
            Resource resource = new ClassPathResource("static/about.html");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Contact page mappings
    @GetMapping({"/contact", "/contact.html"})
    @ResponseBody
    public ResponseEntity<Resource> contact() {
        try {
            Resource resource = new ClassPathResource("static/CONTACT.HTML");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Services page mappings
    @GetMapping({"/services", "/services.html"})
    @ResponseBody
    public ResponseEntity<Resource> services() {
        try {
            Resource resource = new ClassPathResource("static/SERVICES.HTML");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Portfolio page mappings
    @GetMapping({"/portfolio", "/portfolio.html"})
    @ResponseBody
    public ResponseEntity<Resource> portfolio() {
        try {
            Resource resource = new ClassPathResource("static/PORTFOLIO.HTML");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Jobs page mappings
    @GetMapping({"/jobs", "/jobs.html"})
    @ResponseBody
    public ResponseEntity<Resource> jobs() {
        try {
            Resource resource = new ClassPathResource("static/jobs.html");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Privacy page mappings
    @GetMapping({"/privacy", "/privacy.html"})
    @ResponseBody
    public ResponseEntity<Resource> privacy() {
        try {
            Resource resource = new ClassPathResource("static/PRIVACY.html");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Terms page mappings
    @GetMapping({"/terms", "/terms.html"})
    @ResponseBody
    public ResponseEntity<Resource> terms() {
        try {
            Resource resource = new ClassPathResource("static/TERMS.html");
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Handle contact form submission
    @PostMapping("/submit-contact")
    @ResponseBody
    public Map<String, Object> submitContact(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("service") String service,
            @RequestParam("message") String message) {

        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("Received contact form submission from: {} ({})", name, email);

            // Validate inputs
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }

            ContactSubmission submission = new ContactSubmission();
            submission.setName(name.trim());
            submission.setEmail(email.trim());
            submission.setPhone(phone != null ? phone.trim() : "");
            submission.setService(service != null ? service.trim() : "General Inquiry");
            submission.setMessage(message != null ? message.trim() : "");
            submission.setDate(LocalDateTime.now());

            contactSubmissionRepository.save(submission);

            logger.info("Contact submission saved successfully with ID: {}", submission.getId());

            response.put("success", true);
            response.put("message", "Thank you for your message! We'll get back to you soon.");

        } catch (IllegalArgumentException e) {
            logger.warn("Validation error in contact form: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing contact form submission", e);
            response.put("success", false);
            response.put("message", "Sorry, there was an error processing your request. Please try again or contact us directly.");
        }

        return response;
    }
}
