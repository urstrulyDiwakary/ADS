package com.ads.admin.repository;

import com.ads.admin.model.ContactSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactSubmissionRepository extends JpaRepository<ContactSubmission, Long> {

    List<ContactSubmission> findByOrderByDateDesc();

    @Query("SELECT c.service, COUNT(c) FROM ContactSubmission c GROUP BY c.service")
    List<Object[]> findServiceStats();

    @Query("SELECT COUNT(c) FROM ContactSubmission c WHERE c.date >= :startDate")
    Long countSubmissionsAfterDate(LocalDateTime startDate);

    List<ContactSubmission> findByServiceContainingIgnoreCase(String service);

    List<ContactSubmission> findByNameContainingIgnoreCase(String name);
}
