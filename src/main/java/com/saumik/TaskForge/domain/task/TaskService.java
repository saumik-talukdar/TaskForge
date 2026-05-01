package com.saumik.TaskForge.domain.task;

import com.saumik.TaskForge.domain.organization.Membership;
import com.saumik.TaskForge.domain.organization.MembershipRepository;
import com.saumik.TaskForge.domain.organization.Role;
import com.saumik.TaskForge.domain.project.Project;
import com.saumik.TaskForge.domain.project.ProjectRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final MembershipRepository membershipRepository;
    private final ProjectRepository projectRepository;

    // CREATE
    @Transactional
    public void createTask(UUID orgId, UUID projectId,
                           String title, String description,
                           UUID userId) {

        validateMembership(userId, orgId);
        validateProject(orgId, projectId);

        Task task = Task.builder()
                .title(title)
                .description(description)
                .organizationId(orgId)
                .projectId(projectId)
                .createdBy(userId)
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .build();

        taskRepository.save(task);
    }

    // GET
    public List<Task> getTasks(UUID orgId, UUID projectId, UUID userId) {

        validateMembership(userId, orgId);
        validateProject(orgId, projectId);

        return taskRepository.findByProjectId(projectId);
    }

    // UPDATE TASK DETAILS
    @Transactional
    public void updateTask(UUID taskId, UUID userId, String title, String description) {

        Task task = getTaskOrThrow(taskId);
        Membership m = getMembership(userId, task.getOrganizationId());

        boolean allowed =
                isAdmin(m) ||
                        task.getCreatedBy().equals(userId) ||
                        (task.getAssigneeId() != null && task.getAssigneeId().equals(userId));

        if (!allowed) {
            throw new RuntimeException("Not allowed to update task");
        }

        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(description);
    }


    // UPDATE STATUS
    @Transactional
    public void updateStatus(UUID taskId, UUID userId, TaskStatus status) {

        Task task = getTaskOrThrow(taskId);
        Membership m = getMembership(userId, task.getOrganizationId());

        boolean allowed =
                isAdmin(m) ||
                        (task.getAssigneeId() != null && task.getAssigneeId().equals(userId));

        if (!allowed) {
            throw new RuntimeException("Not allowed to update status");
        }

        task.setStatus(status);
    }

    // ASSIGN USER
    @Transactional
    public void assignTask(UUID taskId, UUID userId, UUID assigneeId) {

        Task task = getTaskOrThrow(taskId);
        Membership m = getMembership(userId, task.getOrganizationId());

        if (!isAdmin(m)) {
            throw new RuntimeException("Only admin can assign tasks");
        }

        validateMembership(assigneeId, task.getOrganizationId());

        task.setAssigneeId(assigneeId);
    }

    // DELETE
    @Transactional
    public void deleteTask(UUID taskId, UUID userId) {

        Task task = getTaskOrThrow(taskId);
        Membership m = getMembership(userId, task.getOrganizationId());

        boolean allowed =
                isAdmin(m) ||
                        task.getCreatedBy().equals(userId);

        if (!allowed) {
            throw new RuntimeException("Not allowed to delete task");
        }

        taskRepository.delete(task);
    }

    // ---------------- HELPERS ----------------

    private Task getTaskOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    private void validateMembership(UUID userId, UUID orgId) {
        boolean isMember = membershipRepository
                .existsByUserIdAndOrganizationId(userId, orgId);

        if (!isMember) {
            throw new RuntimeException("Unauthorized");
        }
    }

    private void validateProject(UUID orgId, UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOrganizationId().equals(orgId)) {
            throw new RuntimeException("Invalid project for organization");
        }
    }

    private Membership getMembership(UUID userId, UUID orgId) {
        return membershipRepository
                .findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new RuntimeException("Not a member"));
    }

    private boolean isAdmin(Membership m) {
        return m.getRole() == Role.ADMIN;
    }
}