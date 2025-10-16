package com.ads.admin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes
 * Run this class to generate a BCrypt hash for manual database insertion
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Generate hash for password: admin123
        String password = "admin123";
        String hashedPassword = encoder.encode(password);

        System.out.println("=========================================");
        System.out.println("BCrypt Password Hash Generator");
        System.out.println("=========================================");
        System.out.println("Original Password: " + password);
        System.out.println("BCrypt Hash: " + hashedPassword);
        System.out.println("=========================================");
        System.out.println("\nSQL Insert Statement:");
        System.out.println("INSERT INTO public.admins");
        System.out.println("    (enabled, created_date, last_login, username, full_name, email, password)");
        System.out.println("VALUES");
        System.out.println("    (true, now(), NULL, 'admin', 'Administrator', 'admin@gmail.com',");
        System.out.println("     '" + hashedPassword + "');");
        System.out.println("=========================================");
    }
}

