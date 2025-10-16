package com.ads.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ads.admin.model.ContactSubmission;
import com.ads.admin.repository.ContactSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PublicController {

    @Autowired
    private ContactSubmissionRepository contactSubmissionRepository;

    // Home page - serves HOME.html
    @GetMapping("/")
    public String home() {
        return "redirect:/HOME.html";
    }

    @GetMapping("/home")
    public String homeAlias() {
        return "redirect:/HOME.html";
    }

    // About page
    @GetMapping("/about")
    public String about() {
        return "redirect:/about.html";
    }

    // Contact page
    @GetMapping("/contact")
    public String contact() {
        return "redirect:/CONTACT.HTML";
    }

    // Services page
    @GetMapping("/services")
    public String services() {
        return "redirect:/SERVICES.HTML";
    }

    // Portfolio page
    @GetMapping("/portfolio")
    public String portfolio() {
        return "redirect:/PORTFOLIO.HTML";
    }

    // Jobs page
    @GetMapping("/jobs")
    public String jobs() {
        return "redirect:/jobs.html";
    }

    // Privacy page
    @GetMapping("/privacy")
    public String privacy() {
        return "redirect:/PRIVACY.html";
    }

    // Terms page
    @GetMapping("/terms")
    public String terms() {
        return "redirect:/TERMS.html";
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
            response.put("message", "Sorry, there was an error submitting your message. Please try again.");
        }

        return response;
    }
}
