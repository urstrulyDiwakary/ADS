package com.ads.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class AdsAdminApplication {

    private static final Logger logger = LoggerFactory.getLogger(AdsAdminApplication.class);

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        try {
            SpringApplication.run(AdsAdminApplication.class, args);
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.err.println("Application failed to start. Check your database connection.");
            System.err.println("Error: " + e.getMessage());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = environment.getProperty("server.port", "8080");
        String profile = String.join(",", environment.getActiveProfiles());
        if (profile.isEmpty()) {
            profile = "default";
        }

        logger.info("=================================================");
        logger.info("🚀 ADS Admin Application Started Successfully!");
        logger.info("🌐 Server running on: http://localhost:{}", port);
        logger.info("📊 Active Profile: {}", profile);
        logger.info("🔐 Admin Panel: http://localhost:{}/admin/login", port);
        logger.info("=================================================");
    }
}
