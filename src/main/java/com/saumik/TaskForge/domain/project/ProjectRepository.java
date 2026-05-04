package com.saumik.TaskForge.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends
        JpaRepository<Project, UUID>,
        JpaSpecificationExecutor<Project> {

    List<Project> findByOrganizationId(UUID organizationId);

    boolean existsByOrganizationIdAndName(UUID orgId, String name);
}