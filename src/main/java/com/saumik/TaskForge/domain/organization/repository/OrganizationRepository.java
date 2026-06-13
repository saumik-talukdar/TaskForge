package com.saumik.TaskForge.domain.organization.repository;

import com.saumik.TaskForge.domain.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    @Query("""
        SELECT o FROM Organization o
        JOIN Membership m ON m.organizationId = o.id
        WHERE m.userId = :userId
    """)
    Page<Organization> findByUserId(UUID userId, Pageable pageable);
}