package com.ads.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;

    public SecurityConfig(UserDetailsService userDetailsService,
                         BCryptPasswordEncoder passwordEncoder,
                         CustomAuthenticationSuccessHandler authenticationSuccessHandler,
                         CustomLogoutSuccessHandler logoutSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationSuccessHandler = authenticationSuccessHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Allow public access to frontend files and static resources
                .requestMatchers("/", "/home", "/about", "/contact", "/services",
                               "/portfolio", "/jobs", "/privacy", "/terms").permitAll()

                // Allow access to routes with .html extensions (matching navigation links)
                .requestMatchers("/home.html", "/about.html", "/contact.html", "/services.html",
                               "/portfolio.html", "/jobs.html", "/privacy.html", "/terms.html").permitAll()

                .requestMatchers("/submit-contact").permitAll()
                .requestMatchers("/images/**", "/css/**", "/js/**", "/fonts/**").permitAll()

                // Allow all static files (case insensitive patterns)
                .requestMatchers("/*.html", "/*.HTML", "/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif").permitAll()
                .requestMatchers("/static/**").permitAll()

                // Specific static files that are being served
                .requestMatchers("/HOME.html", "/about.html", "/CONTACT.HTML", "/SERVICES.HTML",
                               "/PORTFOLIO.HTML", "/jobs.html", "/PRIVACY.html", "/TERMS.html").permitAll()

                // Allow access to login page WITHOUT authentication
                .requestMatchers("/admin/login").permitAll()

                // Allow actuator endpoints for health checks
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")

                // Allow API endpoints for authenticated users
                .requestMatchers("/api/admins/**").hasRole("ADMIN")
                .requestMatchers("/api/contact/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/**").hasRole("ADMIN")

                // Secure admin routes (except login which is already permitted above)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Allow all other requests to be public
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .usernameParameter("usermail")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler)
                .failureUrl("/admin/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers("/submit-contact", "/admin/logout") // Disable CSRF for public contact form and logout
            )
            .addFilterBefore(new CsrfCookieFilter(), CsrfFilter.class)
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            );

        return http.build();
    }
}
