package com.ads.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ads.admin.model.ContactSubmission;
import com.ads.admin.repository.ContactSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PublicController {

    @Autowired
    private ContactSubmissionRepository contactSubmissionRepository;

    // Home page - serves HOME.html directly
    @GetMapping("/")
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

    @GetMapping("/home")
    @ResponseBody
    public ResponseEntity<Resource> homeAlias() {
        return home();
    }

    // About page
    @GetMapping("/about")
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

    // Contact page
    @GetMapping("/contact")
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

    // Services page
    @GetMapping("/services")
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

    // Portfolio page
    @GetMapping("/portfolio")
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

    // Jobs page
    @GetMapping("/jobs")
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

    // Privacy page
    @GetMapping("/privacy")
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

    // Terms page
    @GetMapping("/terms")
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
            ContactSubmission submission = new ContactSubmission();
            submission.setName(name);
            submission.setEmail(email);
            submission.setPhone(phone);
            submission.setService(service);
            submission.setMessage(message);
            submission.setDate(LocalDateTime.now());

            contactSubmissionRepository.save(submission);

            response.put("success", true);
            response.put("message", "Thank you for your message! We'll get back to you soon.");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Sorry, there was an error processing your request. Please try again.");
        }

        return response;
    }
}
