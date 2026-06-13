package com.saumik.TaskForge.domain.organization.repository;

import com.saumik.TaskForge.domain.organization.entity.Membership;
import com.saumik.TaskForge.domain.organization.enums.Role;
import com.saumik.TaskForge.domain.organization.dto.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    Page<Membership> findByOrganizationId(UUID orgId, Pageable pageable);

    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);

    Optional<Membership> findByUserIdAndOrganizationId(UUID userId, UUID orgId);

    long countByOrganizationIdAndRole(UUID organizationId, Role role);


    @Modifying
    @Query("DELETE FROM Membership m WHERE m.organizationId = :orgId")
    void deleteByOrganizationId(@Param("orgId") UUID orgId);


    /**
     * Returns member details joined with User so the response includes name + email.
     * Without @Query, Spring Data would try to parse "MembersWithUserDetails"
     * as a property path and throw PropertyReferenceException at startup.
     */
    @Query("""
        SELECT new com.saumik.TaskForge.domain.organization.dto.MemberResponse(
            m.userId, u.fullName, u.email, m.role
        )
        FROM Membership m
        JOIN User u ON u.id = m.userId
        WHERE m.organizationId = :orgId
    """)
    Page<MemberResponse> findMembersWithUserDetails(@Param("orgId") UUID orgId, Pageable pageable);
}