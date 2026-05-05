package com.saumik.TaskForge.domain.project;

import com.saumik.TaskForge.common.exception.AccessDeniedException;
import com.saumik.TaskForge.common.exception.BadRequestException;
import com.saumik.TaskForge.common.exception.NotFoundException;
import com.saumik.TaskForge.domain.organization.Membership;
import com.saumik.TaskForge.domain.organization.MembershipRepository;
import com.saumik.TaskForge.domain.organization.Role;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public void createProject(String name, String description,
                              UUID orgId, UUID userId) {

        Membership m = getMembership(userId, orgId);

        if (!isAdmin(m)) {
            throw new AccessDeniedException("Only admin can create projects");
        }

        boolean exists = projectRepository
                .existsByOrganizationIdAndName(orgId, name);

        if (exists) {
            throw new BadRequestException("Project name already exists");
        }

        Project project = Project.builder()
                .name(name)
                .description(description)
                .organizationId(orgId)
                .createdBy(userId)
                .build();

        projectRepository.save(project);
    }

    public Page<Project> getProjects(
            UUID orgId,
            UUID userId,
            String keyword,
            Instant from,
            Instant to,
            Pageable pageable
    ) {

        Membership m = getMembership(userId, orgId);

        Specification<Project> spec =
                ProjectSpecification.hasOrganization(orgId)
                        .and(ProjectSpecification.nameContains(keyword))
                        .and(ProjectSpecification.createdAfter(from))
                        .and(ProjectSpecification.createdBefore(to));

        return projectRepository.findAll(spec, pageable);
    }

    @Transactional
    public void updateProject(UUID projectId, UUID userId,
                              String name, String description) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        Membership m = getMembership(userId, project.getOrganizationId());

        if (!isAdmin(m)) {
            throw new AccessDeniedException("Only admin can update project");
        }

        if (name != null) project.setName(name);
        if (description != null) project.setDescription(description);
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));

        Membership m = getMembership(userId, project.getOrganizationId());

        if (!isAdmin(m)) {
            throw new AccessDeniedException("Only admin can delete project");
        }

        projectRepository.delete(project);
    }


    // helper

    private Membership getMembership(UUID userId, UUID orgId) {
        return membershipRepository
                .findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new AccessDeniedException("Not a member of this organization"));
    }

    private boolean isAdmin(Membership m) {
        return m.getRole() == Role.ADMIN;
    }
}