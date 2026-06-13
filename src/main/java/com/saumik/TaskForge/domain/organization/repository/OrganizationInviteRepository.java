package com.saumik.TaskForge.domain.organization.repository;

import com.saumik.TaskForge.domain.organization.entity.OrganizationInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationInviteRepository
        extends JpaRepository<OrganizationInvite, UUID> {

    Optional<OrganizationInvite> findByToken(String token);

    boolean existsByOrganizationIdAndEmailAndAcceptedFalse(
            UUID organizationId,
            String email
    );

    void deleteByExpiresAtBeforeAndAcceptedFalse(Instant now);

    void deleteByOrganizationId(UUID orgId);
}