package com.ads.service.config;

import com.ads.service.service.AdminService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Autowired
    private AdminService adminService;

    public CustomAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/admin/dashboard");
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        logger.info("User logged in successfully: {}", email);

        // Get or create session BEFORE redirect
        HttpSession session = request.getSession(true);
        logger.info("Session ID: {}", session.getId());

        // Update last login time BEFORE redirect (but catch any exceptions)
        try {
            adminService.updateLastLogin(email);
            logger.info("Updated last login time for admin: {}", email);
        } catch (Exception e) {
            logger.error("Error updating last login time for {}: {}", email, e.getMessage());
            // Don't throw exception - allow login to proceed
        }

        // Use the parent's redirect logic which properly handles saved requests
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

