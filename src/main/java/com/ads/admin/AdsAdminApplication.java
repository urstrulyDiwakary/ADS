package com.ads.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class AdsAdminApplication {

    private static final Logger logger = LoggerFactory.getLogger(AdsAdminApplication.class);

    @Autowired
    private Environment environment;

    @Autowired(required = false)
    private DataSource dataSource;

    public static void main(String[] args) {
        try {
            logger.info("Starting ADS Admin Application...");
            SpringApplication.run(AdsAdminApplication.class, args);
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.err.println("=================================================");
            System.err.println("❌ Application failed to start!");
            System.err.println("=================================================");

            if (e.getMessage() != null && (
                e.getMessage().contains("Connection") ||
                e.getMessage().contains("database") ||
                e.getMessage().contains("SQLException"))) {
                System.err.println("📊 Database Connection Issue Detected:");
                System.err.println("   - Ensure PostgreSQL is running on your system");
                System.err.println("   - Check if database 'ADS' exists");
                System.err.println("   - Verify username/password in application.properties");
                System.err.println("   - Default: localhost:5432/ADS with user 'postgres'");
            } else if (e.getMessage() != null && e.getMessage().contains("Port")) {
                System.err.println("🔌 Port Issue Detected:");
                System.err.println("   - Port 8080 may already be in use");
                System.err.println("   - Stop other applications or change PORT in environment");
            } else {
                System.err.println("Error: " + e.getMessage());
            }
            System.err.println("=================================================");
            System.exit(1);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = environment.getProperty("server.port", "8080");
        String profile = String.join(",", environment.getActiveProfiles());
        if (profile.isEmpty()) {
            profile = "default";
        }

        // Test database connection
        testDatabaseConnection();

        logger.info("=================================================");
        logger.info("🚀 ADS Admin Application Started Successfully!");
        logger.info("🌐 Server running on: http://localhost:{}", port);
        logger.info("📊 Active Profile: {}", profile);
        logger.info("🔐 Admin Panel: http://localhost:{}/admin/login", port);
        logger.info("🏠 Home Page: http://localhost:{}/", port);
        logger.info("=================================================");
    }

    private void testDatabaseConnection() {
        if (dataSource != null) {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(3)) {
                    logger.info("✅ Database connection successful!");
                    logger.info("📊 Database: {}", connection.getMetaData().getDatabaseProductName());
                } else {
                    logger.warn("⚠️ Database connection validation failed!");
                }
            } catch (SQLException e) {
                logger.error("❌ Database connection test failed: {}", e.getMessage());
                logger.error("🔧 Please check your database configuration and ensure PostgreSQL is running.");
            }
        } else {
            logger.warn("⚠️ DataSource is not configured!");
        }
    }
}
