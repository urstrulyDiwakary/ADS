package com.ads.service.service;

import com.ads.service.model.Admin;
import com.ads.service.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AdminService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + email));

        // Since we don't have roles in our Admin model, we'll use a default ADMIN role
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        // Don't update lastLogin here - it can interfere with authentication
        // Update it after successful login in the controller if needed

        return new User(admin.getEmail(), admin.getPassword(), admin.getEnabled(), true, true, true, authorities);
    }

    public Admin createAdmin(String username, String password, String email, String fullName) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEmail(email);
        admin.setFullName(fullName);
        admin.setEnabled(true);
        return adminRepository.save(admin);
    }

    public Admin createAdmin(String username, String password) {
        return createAdmin(username, password, username + "@ads.com", "Admin User");
    }

    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }

    public Admin findByUsername(String username) {
        return adminRepository.findByUsername(username).orElse(null);
    }

    public Admin findByEmail(String email) {
        return adminRepository.findByEmail(email).orElse(null);
    }

    public List<Admin> findAllAdmins() {
        return adminRepository.findAll();
    }

    @Transactional
    public Admin updateAdmin(Admin admin) {
        return adminRepository.save(admin);
    }

    @Transactional
    public void updateLastLogin(String email) {
        adminRepository.findByEmail(email).ifPresent(admin -> {
            admin.setLastLogin(LocalDateTime.now());
            adminRepository.save(admin);
        });
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    public Admin enableAdmin(Long id) {
        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin != null) {
            admin.setEnabled(true);
            return adminRepository.save(admin);
        }
        return null;
    }

    public Admin disableAdmin(Long id) {
        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin != null) {
            admin.setEnabled(false);
            return adminRepository.save(admin);
        }
        return null;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Admin admin = findByUsername(username);
        if (admin != null && passwordEncoder.matches(oldPassword, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminRepository.save(admin);
            return true;
        }
        return false;
    }

    // New methods for Admin Management CRUD
    public Admin findById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public Admin createAdminFull(Admin admin) {
        // Encode password before saving
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        // Ensure createdDate is set (should be set by constructor, but double-check)
        if (admin.getCreatedDate() == null) {
            admin.setCreatedDate(LocalDateTime.now());
        }
        return adminRepository.save(admin);
    }

    public Admin updateAdminDetails(Long id, Admin updatedAdmin) {
        Admin existingAdmin = adminRepository.findById(id).orElse(null);
        if (existingAdmin != null) {
            existingAdmin.setUsername(updatedAdmin.getUsername());
            existingAdmin.setFullName(updatedAdmin.getFullName());
            existingAdmin.setEmail(updatedAdmin.getEmail());
            existingAdmin.setEnabled(updatedAdmin.getEnabled());
            // Don't update password here
            return adminRepository.save(existingAdmin);
        }
        return null;
    }

    public boolean updatePassword(Long id, String newPassword) {
        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin != null) {
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminRepository.save(admin);
            return true;
        }
        return false;
    }

    public boolean deleteAdminById(Long id) {
        if (adminRepository.existsById(id)) {
            adminRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean toggleAdminStatus(Long id) {
        Admin admin = adminRepository.findById(id).orElse(null);
        if (admin != null) {
            admin.setEnabled(!admin.getEnabled());
            adminRepository.save(admin);
            return true;
        }
        return false;
    }
}
