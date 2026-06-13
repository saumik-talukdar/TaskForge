package com.saumik.TaskForge.domain.project.service;

import com.saumik.TaskForge.common.exception.AccessDeniedException;
import com.saumik.TaskForge.common.exception.BadRequestException;
import com.saumik.TaskForge.common.exception.NotFoundException;
import com.saumik.TaskForge.domain.organization.entity.Membership;
import com.saumik.TaskForge.domain.organization.repository.MembershipRepository;
import com.saumik.TaskForge.domain.organization.enums.Role;
import com.saumik.TaskForge.domain.project.entity.Project;
import com.saumik.TaskForge.domain.project.entity.ProjectMembership;
import com.saumik.TaskForge.domain.project.enums.ProjectRole;
import com.saumik.TaskForge.domain.project.enums.ProjectVisibility;
import com.saumik.TaskForge.domain.project.repository.ProjectMembershipRepository;
import com.saumik.TaskForge.domain.project.repository.ProjectRepository;
import com.saumik.TaskForge.domain.project.specification.ProjectSpecification;
import com.saumik.TaskForge.domain.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final MembershipRepository membershipRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public void createProject(UUID orgId, UUID userId,
                              String name, String description,
                              ProjectVisibility visibility) {

        // Only org admins can create projects
        requireOrgAdmin(userId, orgId);

        if (projectRepository.existsByOrganizationIdAndName(orgId, name)) {
            throw new BadRequestException("A project with this name already exists");
        }

        Project project = Project.builder()
                .name(name)
                .description(description)
                .organizationId(orgId)
                .createdBy(userId)
                .managerId(userId)         // creator is the default manager
                .visibility(visibility != null ? visibility : ProjectVisibility.PUBLIC_TO_ORG)
                .build();

        projectRepository.save(project);

        // Creator gets a MANAGER membership row — they're a project member too
        projectMembershipRepository.save(
                ProjectMembership.builder()
                        .projectId(project.getId())
                        .userId(userId)
                        .role(ProjectRole.MANAGER)
                        .build()
        );
    }


    public Page<Project> getProjects(UUID orgId, UUID userId,
                                     String keyword, Instant from, Instant to,
                                     Pageable pageable) {

        Membership orgMembership = requireOrgMember(userId, orgId);
        boolean isOrgAdmin = orgMembership.getRole() == Role.ADMIN;

        Specification<Project> spec =
                ProjectSpecification.hasOrganization(orgId)
                        .and(ProjectSpecification.visibleTo(userId, isOrgAdmin))
                        .and(ProjectSpecification.nameContains(keyword))
                        .and(ProjectSpecification.createdAfter(from))
                        .and(ProjectSpecification.createdBefore(to));

        return projectRepository.findAll(spec, pageable);
    }

    public Page<ProjectMembership> getProjectMembers(UUID orgId, UUID projectId,
                                                     UUID requesterId, Pageable pageable) {

        Project project = getAccessibleProject(orgId, projectId, requesterId);
        return projectMembershipRepository.findByProjectId(project.getId(), pageable);
    }


    @Transactional
    public void updateProject(UUID orgId, UUID projectId, UUID userId,
                              String name, String description,
                              ProjectVisibility visibility) {

        Project project = getProjectInOrg(orgId, projectId);

        // Manager or org admin can update
        requireManagerOrOrgAdmin(userId, orgId, project);

        if (name != null) {
            if (projectRepository.existsByOrganizationIdAndName(orgId, name)
                    && !project.getName().equals(name)) {
                throw new BadRequestException("A project with this name already exists");
            }
            project.setName(name);
        }
        if (description != null) project.setDescription(description);
        if (visibility != null) project.setVisibility(visibility);
    }

    @Transactional
    public void transferManager(UUID orgId, UUID projectId,
                                UUID currentManagerId, UUID newManagerId) {

        Project project = getProjectInOrg(orgId, projectId);

        if (!project.getManagerId().equals(currentManagerId)) {
            throw new AccessDeniedException("Only the current project manager can transfer management");
        }

        if (currentManagerId.equals(newManagerId)) {
            throw new BadRequestException("You are already the manager");
        }

        // New manager must be an org member
        requireOrgMember(newManagerId, orgId);

        // Ensure new manager has a project membership row
        ProjectMembership newManagerMembership = projectMembershipRepository
                .findByUserIdAndProjectId(newManagerId, projectId)
                .orElse(null);

        if (newManagerMembership == null) {
            // Add them as a project member first, then elevate
            projectMembershipRepository.save(
                    ProjectMembership.builder()
                            .projectId(projectId)
                            .userId(newManagerId)
                            .role(ProjectRole.MANAGER)
                            .build()
            );
        } else {
            newManagerMembership.setRole(ProjectRole.MANAGER);
        }

        // Demote the current manager to MEMBER (keep them on the project)
        projectMembershipRepository
                .findByUserIdAndProjectId(currentManagerId, projectId)
                .ifPresent(m -> m.setRole(ProjectRole.MEMBER));

        project.setManagerId(newManagerId);
    }


    @Transactional
    public void addMember(UUID orgId, UUID projectId,
                          UUID requesterId, UUID targetUserId, ProjectRole role) {

        Project project = getProjectInOrg(orgId, projectId);

        // Only manager or org admin can add members
        requireManagerOrOrgAdmin(requesterId, orgId, project);

        // Target must be an org member
        requireOrgMember(targetUserId, orgId);

        if (projectMembershipRepository.existsByUserIdAndProjectId(targetUserId, projectId)) {
            throw new BadRequestException("User is already a project member");
        }

        // Cannot directly assign MANAGER via addMember — use transferManager
        if (role == ProjectRole.MANAGER) {
            throw new BadRequestException(
                    "Use the transfer-manager endpoint to assign a new manager");
        }

        projectMembershipRepository.save(
                ProjectMembership.builder()
                        .projectId(projectId)
                        .userId(targetUserId)
                        .role(role)
                        .build()
        );
    }

    @Transactional
    public void removeMember(UUID orgId, UUID projectId,
                             UUID requesterId, UUID targetUserId) {

        Project project = getProjectInOrg(orgId, projectId);

        requireManagerOrOrgAdmin(requesterId, orgId, project);

        if (project.getManagerId().equals(targetUserId)) {
            throw new BadRequestException(
                    "Cannot remove the project manager. Transfer management first.");
        }

        projectMembershipRepository
                .findByUserIdAndProjectId(targetUserId, projectId)
                .orElseThrow(() -> new NotFoundException("User is not a project member"));

        projectMembershipRepository.deleteByUserIdAndProjectId(targetUserId, projectId);
    }


    @Transactional
    public void deleteProject(UUID orgId, UUID projectId, UUID userId) {

        Project project = getProjectInOrg(orgId, projectId);

        requireManagerOrOrgAdmin(userId, orgId, project);

        // Cascade: tasks → project memberships → project
        taskRepository.deleteByProjectId(projectId);
        projectMembershipRepository.deleteByProjectId(projectId);
        projectRepository.delete(project);
    }

    // helper

    private Project getAccessibleProject(UUID orgId, UUID projectId, UUID userId) {

        Project project = getProjectInOrg(orgId, projectId);
        Membership orgMembership = requireOrgMember(userId, orgId);
        boolean isOrgAdmin = orgMembership.getRole() == Role.ADMIN;

        if (isOrgAdmin) return project;

        if (project.getVisibility() == ProjectVisibility.PUBLIC_TO_ORG) return project;

        // Private project — must have an explicit membership row
        if (!projectMembershipRepository.existsByUserIdAndProjectId(userId, projectId)) {
            throw new AccessDeniedException("You do not have access to this project");
        }

        return project;
    }

    private Project getProjectInOrg(UUID orgId, UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        if (!project.getOrganizationId().equals(orgId)) {
            throw new NotFoundException("Project not found");  // don't leak cross-org existence
        }
        return project;
    }

    private Membership requireOrgMember(UUID userId, UUID orgId) {
        return membershipRepository
                .findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));
    }

    private void requireOrgAdmin(UUID userId, UUID orgId) {
        Membership m = requireOrgMember(userId, orgId);
        if (m.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only org admins can perform this action");
        }
    }

    private void requireManagerOrOrgAdmin(UUID userId, UUID orgId, Project project) {
        Membership orgMembership = requireOrgMember(userId, orgId);

        boolean isOrgAdmin  = orgMembership.getRole() == Role.ADMIN;
        boolean isManager   = project.getManagerId().equals(userId);

        if (!isOrgAdmin && !isManager) {
            throw new AccessDeniedException(
                    "Only the project manager or an org admin can perform this action");
        }
    }
}