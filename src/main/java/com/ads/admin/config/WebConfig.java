package com.ads.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configure static resource handling
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600); // Cache for 1 hour in production

        // Specifically handle images
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(3600);

        // Handle CSS, JS, and other assets
        registry.addResourceHandler("/css/**", "/js/**", "/fonts/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/", "classpath:/static/fonts/")
                .setCachePeriod(3600);
    }
}
