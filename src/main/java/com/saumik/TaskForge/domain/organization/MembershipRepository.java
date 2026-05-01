package com.saumik.TaskForge.domain.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    List<Membership> findByUserId(UUID userId);

    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    Optional<Membership> findByUserIdAndOrganizationId(UUID userId, UUID orgId);
}