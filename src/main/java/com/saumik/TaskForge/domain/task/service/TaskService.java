package com.saumik.TaskForge.domain.task.service;

import com.saumik.TaskForge.common.exception.AccessDeniedException;
import com.saumik.TaskForge.common.exception.NotFoundException;
import com.saumik.TaskForge.domain.organization.entity.Membership;
import com.saumik.TaskForge.domain.organization.repository.MembershipRepository;
import com.saumik.TaskForge.domain.organization.enums.Role;
import com.saumik.TaskForge.domain.project.entity.Project;
import com.saumik.TaskForge.domain.project.repository.ProjectMembershipRepository;
import com.saumik.TaskForge.domain.project.repository.ProjectRepository;
import com.saumik.TaskForge.domain.project.enums.ProjectRole;
import com.saumik.TaskForge.domain.project.enums.ProjectVisibility;
import com.saumik.TaskForge.domain.task.enums.TaskPriority;
import com.saumik.TaskForge.domain.task.repository.TaskRepository;
import com.saumik.TaskForge.domain.task.specification.TaskSpecification;
import com.saumik.TaskForge.domain.task.enums.TaskStatus;
import com.saumik.TaskForge.domain.task.entity.Task;
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
public class TaskService {

    private final TaskRepository taskRepository;
    private final MembershipRepository membershipRepository;
    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectRepository projectRepository;


    @Transactional
    public void createTask(UUID orgId, UUID projectId,
                           UUID userId,
                           String title, String description,
                           TaskPriority priority, Instant dueDate) {

        // getAccessibleProject enforces org membership + project visibility
        Project project = getAccessibleProject(orgId, projectId, userId);

        Task task = Task.builder()
                .title(title)
                .description(description)
                .organizationId(orgId)
                .projectId(project.getId())
                .createdBy(userId)
                .updatedBy(userId)
                .status(TaskStatus.TODO)
                .priority(priority != null ? priority : TaskPriority.MEDIUM)
                .dueDate(dueDate)
                .build();

        taskRepository.save(task);
    }


    public Page<Task> getTasks(UUID orgId, UUID projectId, UUID userId,
                               TaskStatus status, UUID assigneeId,
                               TaskPriority priority, String keyword,
                               Instant from, Instant to, Pageable pageable) {

        // Access check — throws if user can't see the project
        getAccessibleProject(orgId, projectId, userId);

        Specification<Task> spec =
                TaskSpecification.hasOrganization(orgId)
                        .and(TaskSpecification.hasProject(projectId))
                        .and(TaskSpecification.hasStatus(status))
                        .and(TaskSpecification.hasPriority(priority))
                        .and(TaskSpecification.hasAssignee(assigneeId))
                        .and(TaskSpecification.titleContains(keyword))
                        .and(TaskSpecification.createdAfter(from))
                        .and(TaskSpecification.createdBefore(to));

        return taskRepository.findAll(spec, pageable);
    }


    @Transactional
    public void updateTask(UUID orgId, UUID projectId,
                           UUID taskId, UUID userId,
                           String title, String description, Instant dueDate) {

        Task task = getTaskInProject(orgId, projectId, taskId);
        Membership orgMembership = requireOrgMember(userId, orgId);

        boolean isOrgAdmin  = orgMembership.getRole() == Role.ADMIN;
        boolean isCreator   = task.getCreatedBy().equals(userId);
        boolean isAssignee  = userId.equals(task.getAssigneeId());
        boolean isManager   = isProjectManager(userId, projectId);

        if (!isOrgAdmin && !isManager && !isCreator && !isAssignee) {
            throw new AccessDeniedException("You are not allowed to update this task");
        }

        if (title != null)       task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (dueDate != null)     task.setDueDate(dueDate);
        task.setUpdatedBy(userId);
    }


    @Transactional
    public void updateStatus(UUID orgId, UUID projectId,
                             UUID taskId, UUID userId, TaskStatus status) {

        Task task = getTaskInProject(orgId, projectId, taskId);
        Membership orgMembership = requireOrgMember(userId, orgId);

        boolean isOrgAdmin = orgMembership.getRole() == Role.ADMIN;
        boolean isAssignee = userId.equals(task.getAssigneeId());
        boolean isCreator  = task.getCreatedBy().equals(userId);
        boolean isManager  = isProjectManager(userId, projectId);

        // Assignees, creators, managers, and org admins can all update status
        if (!isOrgAdmin && !isManager && !isAssignee && !isCreator) {
            throw new AccessDeniedException("You are not allowed to update this task's status");
        }

        task.setStatus(status);
        task.setUpdatedBy(userId);
    }


    @Transactional
    public void assignTask(UUID orgId, UUID projectId,
                           UUID taskId, UUID requesterId, UUID assigneeId) {

        Task task = getTaskInProject(orgId, projectId, taskId);
        Membership requesterMembership = requireOrgMember(requesterId, orgId);

        boolean isOrgAdmin = requesterMembership.getRole() == Role.ADMIN;
        boolean isManager  = isProjectManager(requesterId, projectId);

        // Self-assignment: any project-accessible member can assign themselves
        boolean isSelfAssign = requesterId.equals(assigneeId);

        if (!isOrgAdmin && !isManager && !isSelfAssign) {
            throw new AccessDeniedException(
                    "Only the project manager, org admin, or the assignee themselves can assign tasks");
        }

        if (assigneeId != null) {
            // Assignee must be an org member
            requireOrgMember(assigneeId, orgId);
        }

        task.setAssigneeId(assigneeId);  // null clears the assignment
        task.setUpdatedBy(requesterId);
    }

    @Transactional
    public void deleteTask(UUID orgId, UUID projectId,
                           UUID taskId, UUID userId) {

        Task task = getTaskInProject(orgId, projectId, taskId);
        Membership orgMembership = requireOrgMember(userId, orgId);

        boolean isOrgAdmin = orgMembership.getRole() == Role.ADMIN;
        boolean isCreator  = task.getCreatedBy().equals(userId);
        boolean isManager  = isProjectManager(userId, projectId);

        if (!isOrgAdmin && !isManager && !isCreator) {
            throw new AccessDeniedException("You are not allowed to delete this task");
        }

        taskRepository.delete(task);
    }

    /**
     * Fetches the project and enforces visibility rules — no dependency on ProjectService.
     * TaskService owns this check because tasks are leaves in the hierarchy; they
     * validate access by querying repos directly, not by calling up to a parent service.
     */
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
            throw new NotFoundException("Project not found");
        }
        return project;
    }

    private Task getTaskInProject(UUID orgId, UUID projectId, UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task not found"));

        // Validate path consistency — don't leak cross-project task existence
        if (!task.getOrganizationId().equals(orgId) || !task.getProjectId().equals(projectId)) {
            throw new NotFoundException("Task not found");
        }

        return task;
    }

    private Membership requireOrgMember(UUID userId, UUID orgId) {
        return membershipRepository
                .findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));
    }

    private boolean isProjectManager(UUID userId, UUID projectId) {
        return projectMembershipRepository
                .findByUserIdAndProjectId(userId, projectId)
                .map(m -> m.getRole() == ProjectRole.MANAGER)
                .orElse(false);
    }
}