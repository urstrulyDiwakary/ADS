package com.ads.admin.service;

import com.ads.admin.model.ContactSubmission;
import com.ads.admin.repository.ContactSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContactSubmissionService {

    @Autowired
    private ContactSubmissionRepository contactSubmissionRepository;

    public ContactSubmission saveSubmission(ContactSubmission submission) {
        if (submission.getDate() == null) {
            submission.setDate(LocalDateTime.now());
        }
        return contactSubmissionRepository.save(submission);
    }

    public List<ContactSubmission> getAllSubmissions() {
        return contactSubmissionRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .toList();
    }

    public ContactSubmission getSubmissionById(Long id) {
        return contactSubmissionRepository.findById(id).orElse(null);
    }

    public boolean deleteSubmission(Long id) {
        try {
            if (contactSubmissionRepository.existsById(id)) {
                contactSubmissionRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total submissions count
        long totalSubmissions = contactSubmissionRepository.count();
        stats.put("totalSubmissions", totalSubmissions);

        // Get all submissions and calculate stats
        List<ContactSubmission> allSubmissions = getAllSubmissions();

        // Service breakdown
        Map<String, Long> serviceBreakdown = new HashMap<>();
        for (ContactSubmission submission : allSubmissions) {
            String service = submission.getService() != null ? submission.getService() : "Other";
            serviceBreakdown.put(service, serviceBreakdown.getOrDefault(service, 0L) + 1);
        }
        stats.put("serviceBreakdown", serviceBreakdown);

        // Recent submissions (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentSubmissions = allSubmissions.stream()
                .mapToLong(sub -> sub.getDate().isAfter(thirtyDaysAgo) ? 1 : 0)
                .sum();
        stats.put("recentSubmissions", recentSubmissions);

        // This week submissions
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long weeklySubmissions = allSubmissions.stream()
                .mapToLong(sub -> sub.getDate().isAfter(weekAgo) ? 1 : 0)
                .sum();
        stats.put("weeklySubmissions", weeklySubmissions);

        return stats;
    }

    public List<ContactSubmission> searchByService(String service) {
        List<ContactSubmission> allSubmissions = getAllSubmissions();
        return allSubmissions.stream()
                .filter(submission -> submission.getService() != null &&
                        submission.getService().toLowerCase().contains(service.toLowerCase()))
                .toList();
    }

    public List<ContactSubmission> searchByName(String name) {
        List<ContactSubmission> allSubmissions = getAllSubmissions();
        return allSubmissions.stream()
                .filter(submission -> submission.getName() != null &&
                        submission.getName().toLowerCase().contains(name.toLowerCase()))
                .toList();
    }

    public List<ContactSubmission> getSubmissionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<ContactSubmission> allSubmissions = getAllSubmissions();
        return allSubmissions.stream()
                .filter(submission -> {
                    LocalDateTime submissionDate = submission.getDate();
                    return submissionDate.isAfter(startDate) && submissionDate.isBefore(endDate);
                })
                .toList();
    }
}
