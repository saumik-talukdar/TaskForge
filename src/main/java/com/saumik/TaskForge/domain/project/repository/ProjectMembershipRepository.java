package com.saumik.TaskForge.domain.project.repository;

import com.saumik.TaskForge.domain.project.entity.ProjectMembership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, UUID> {

    Optional<ProjectMembership> findByUserIdAndProjectId(UUID userId, UUID projectId);

    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);

    Page<ProjectMembership> findByProjectId(UUID projectId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM ProjectMembership pm WHERE pm.projectId = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);

    @Modifying
    @Query("DELETE FROM ProjectMembership pm WHERE pm.userId = :userId AND pm.projectId = :projectId")
    void deleteByUserIdAndProjectId(@Param("userId") UUID userId, @Param("projectId") UUID projectId);

    /**
     * How many projects in this org does this user manage?
     * Used to block leaving/removal when the user still owns projects.
     */
    @Query("""
        SELECT COUNT(pm) FROM ProjectMembership pm
        JOIN Project p ON p.id = pm.projectId
        WHERE pm.userId = :userId
        AND p.organizationId = :orgId
        AND pm.role = com.saumik.TaskForge.domain.project.enums.ProjectRole.MANAGER
    """)
    long countManagedProjectsInOrg(@Param("userId") UUID userId, @Param("orgId") UUID orgId);

    /**
     * Remove all project memberships for a user across every project in the given org.
     * Called when a user leaves or is removed from the org.
     */
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM ProjectMembership pm
        WHERE pm.userId = :userId
        AND pm.projectId IN (
            SELECT p.id FROM Project p WHERE p.organizationId = :orgId
        )
    """)
    void deleteByUserIdAndOrganizationId(@Param("userId") UUID userId, @Param("orgId") UUID orgId);

    /**
     * Remove all project memberships for every project in an org.
     * Called during full org deletion to avoid FK violations.
     */
    @Modifying
    @Transactional
    @Query("""
        DELETE FROM ProjectMembership pm
        WHERE pm.projectId IN (
            SELECT p.id FROM Project p WHERE p.organizationId = :orgId
        )
    """)
    void deleteByOrganizationId(@Param("orgId") UUID orgId);
}