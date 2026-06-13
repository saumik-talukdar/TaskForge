package com.saumik.TaskForge.domain.project.repository;

import com.saumik.TaskForge.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProjectRepository extends
        JpaRepository<Project, UUID>,
        JpaSpecificationExecutor<Project> {

    boolean existsByOrganizationIdAndName(UUID orgId, String name);

    @Modifying
    @Query("DELETE FROM Project p WHERE p.organizationId = :orgId")
    void deleteByOrganizationId(@Param("orgId") UUID orgId);
}