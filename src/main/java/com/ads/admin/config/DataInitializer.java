package com.ads.admin.config;

import com.ads.admin.model.Admin;
import com.ads.admin.model.ContactSubmission;
import com.ads.admin.repository.AdminRepository;
import com.ads.admin.repository.ContactSubmissionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ContactSubmissionRepository contactSubmissionRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${admin.default.email:}")
    private String defaultAdminEmail;

    @Value("${admin.default.password:}")
    private String defaultAdminPassword;

    @Value("${admin.default.username:}")
    private String defaultAdminUsername;

    @Value("${admin.default.fullname:}")
    private String defaultAdminFullName;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdmin();
        // Only migrate if migration hasn't been done before
        migrateJsonDataToDatabase();
    }

    private void initializeDefaultAdmin() {
        // Only create default admin if environment variables are configured
        if (defaultAdminEmail != null && !defaultAdminEmail.isEmpty() &&
            defaultAdminPassword != null && !defaultAdminPassword.isEmpty()) {

            if (!adminRepository.existsByEmail(defaultAdminEmail)) {
                Admin defaultAdmin = new Admin();
                defaultAdmin.setUsername(defaultAdminUsername != null && !defaultAdminUsername.isEmpty()
                    ? defaultAdminUsername : "admin");
                defaultAdmin.setPassword(passwordEncoder.encode(defaultAdminPassword));
                defaultAdmin.setEmail(defaultAdminEmail);
                defaultAdmin.setFullName(defaultAdminFullName != null && !defaultAdminFullName.isEmpty()
                    ? defaultAdminFullName : "System Administrator");
                defaultAdmin.setEnabled(true);

                adminRepository.save(defaultAdmin);
                System.out.println("Default admin user created successfully with email: " + defaultAdminEmail);
            }
        } else {
            System.out.println("No default admin credentials configured. Please create an admin user manually or set environment variables:");
            System.out.println("  - ADMIN_DEFAULT_EMAIL");
            System.out.println("  - ADMIN_DEFAULT_PASSWORD");
            System.out.println("  - ADMIN_DEFAULT_USERNAME (optional)");
            System.out.println("  - ADMIN_DEFAULT_FULLNAME (optional)");
        }
    }


    private void migrateJsonDataToDatabase() {
        try {
            // Check if migration marker file exists
            File migrationMarker = new File("json_migration_completed.marker");
            if (migrationMarker.exists()) {
                System.out.println("JSON migration already completed previously. Skipping migration.");
                return;
            }

            // Check if database already has data (from previous migrations)
            if (contactSubmissionRepository.count() > 0) {
                System.out.println("Database already contains contact submissions, creating migration marker to prevent future migrations.");
                // Create marker file to prevent future migrations
                migrationMarker.createNewFile();
                return;
            }

            // Try to read from the project root directory first
            File jsonFile = new File("contact_submissions.json");
            ObjectMapper objectMapper = new ObjectMapper();

            if (jsonFile.exists()) {
                System.out.println("Found contact_submissions.json in project root, migrating data...");

                List<Map<String, Object>> jsonData = objectMapper.readValue(jsonFile,
                    new TypeReference<List<Map<String, Object>>>() {});

                for (Map<String, Object> item : jsonData) {
                    ContactSubmission submission = new ContactSubmission();
                    submission.setName((String) item.get("name"));
                    submission.setEmail((String) item.get("email"));
                    submission.setPhone((String) item.get("phone"));
                    submission.setService((String) item.get("service"));
                    submission.setMessage((String) item.get("message"));

                    // Parse date from JSON
                    String dateStr = (String) item.get("date");
                    if (dateStr != null) {
                        try {
                            // Handle ISO date format
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
                            submission.setDate(zonedDateTime.toLocalDateTime());
                        } catch (Exception e) {
                            // If parsing fails, use current time
                            submission.setDate(LocalDateTime.now());
                        }
                    } else {
                        submission.setDate(LocalDateTime.now());
                    }

                    contactSubmissionRepository.save(submission);
                }

                System.out.println("Successfully migrated " + jsonData.size() + " contact submissions to database.");

                // Create migration marker file to prevent future migrations
                migrationMarker.createNewFile();

                // Backup the original JSON file and remove it
                File backupFile = new File("contact_submissions_backup_" + System.currentTimeMillis() + ".json");
                if (jsonFile.renameTo(backupFile)) {
                    System.out.println("JSON file backed up as: " + backupFile.getName());
                    System.out.println("Migration completed. Future application restarts will use only PostgreSQL database.");
                }

            } else {
                System.out.println("No contact_submissions.json file found. Database will start empty.");
                // Create marker file to prevent future migration attempts
                migrationMarker.createNewFile();
            }

        } catch (Exception e) {
            System.err.println("Error migrating JSON data: " + e.getMessage());
            System.out.println("Database will start empty.");
        }
    }
}
