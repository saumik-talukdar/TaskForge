package com.saumik.TaskForge.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByOrganizationId(UUID organizationId);

    boolean existsByOrganizationIdAndName(UUID orgId, String name);
}