package com.ads.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
            .authorizeHttpRequests((authz) -> authz
                // Allow public access to frontend files and static resources
                .requestMatchers("/", "/home", "/about", "/contact", "/services",
                               "/portfolio", "/jobs", "/privacy", "/terms").permitAll()
                .requestMatchers("/submit-contact").permitAll()
                .requestMatchers("/images/**", "/css/**", "/js/**", "/fonts/**").permitAll()

                // Allow all static files (case insensitive patterns)
                .requestMatchers("/*.html", "/*.HTML", "/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif").permitAll()
                .requestMatchers("/static/**").permitAll()

                // Specific static files that are being redirected to
                .requestMatchers("/HOME.html", "/about.html", "/CONTACT.HTML", "/SERVICES.HTML",
                               "/PORTFOLIO.HTML", "/jobs.html", "/PRIVACY.html", "/TERMS.html").permitAll()

                // Allow access to login page WITHOUT authentication
                .requestMatchers("/admin/login").permitAll()

                // Allow API endpoints for authenticated users
                .requestMatchers("/api/admins/**").hasRole("ADMIN")
                .requestMatchers("/api/contact/**").hasRole("ADMIN")
                .requestMatchers("/api/dashboard/**").hasRole("ADMIN")

                // Secure admin routes (except login which is already permitted above)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Allow all other requests to be public (this is the key fix)
                .anyRequest().permitAll()
            )
            .sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .formLogin((form) -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .usernameParameter("usermail")
                .passwordParameter("password")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/admin/login?error=true")
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf((csrf) -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/submit-contact", "/api/**")
            );

        return http.build();
    }
}
