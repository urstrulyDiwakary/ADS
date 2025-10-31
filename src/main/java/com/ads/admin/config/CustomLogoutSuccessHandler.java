package com.ads.admin.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);

    public CustomLogoutSuccessHandler() {
        setDefaultTargetUrl("/admin/login?logout=true");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        // Log the logout event
        if (authentication != null) {
            String email = authentication.getName();
            logger.info("User logged out successfully: {}", email);
        } else {
            logger.info("Logout request processed (no active authentication)");
        }

        // Ensure session is fully invalidated before redirect
        HttpSession session = request.getSession(false);
        if (session != null) {
            try {
                session.invalidate();
                logger.info("Session invalidated successfully");
            } catch (IllegalStateException e) {
                // Session already invalidated by Spring Security
                logger.debug("Session was already invalidated");
            }
        }

        // Clear any security context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        // Send redirect response
        String targetUrl = determineTargetUrl(request, response);
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        return getDefaultTargetUrl();
    }
}

