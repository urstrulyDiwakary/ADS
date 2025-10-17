package com.ads.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    /**
     * Fallback H2 database for development when PostgreSQL is not available
     */
    @Bean
    @ConditionalOnProperty(
        name = "spring.datasource.url",
        havingValue = "jdbc:h2:mem:testdb",
        matchIfMissing = false
    )
    public DataSource h2DataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("testdb")
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("=================================================");
        logger.info("📊 Database Configuration:");
        logger.info("🔗 Database URL: {}", databaseUrl);
        logger.info("📝 Note: If PostgreSQL connection fails, check:");
        logger.info("   1. PostgreSQL service is running");
        logger.info("   2. Database 'ADS' exists");
        logger.info("   3. Username/password are correct");
        logger.info("=================================================");
    }
}
