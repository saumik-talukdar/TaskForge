package com.saumik.TaskForge.domain.task.specification;

import com.saumik.TaskForge.domain.task.entity.Task;
import com.saumik.TaskForge.domain.task.enums.TaskPriority;
import com.saumik.TaskForge.domain.task.enums.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class TaskSpecification {

    public static Specification<Task> hasOrganization(UUID orgId) {
        return (root, query, cb) ->
                cb.equal(root.get("organizationId"), orgId);
    }

    public static Specification<Task> hasProject(UUID projectId) {
        return (root, query, cb) ->
                cb.equal(root.get("projectId"), projectId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return status == null
                ? Specification.unrestricted()
                : (root, query, cb) ->
                cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return priority == null
                ? Specification.unrestricted()
                : (root, query, cb) ->
                cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasAssignee(UUID assigneeId) {
        return assigneeId == null
                ? Specification.unrestricted()
                : (root, query, cb) ->
                cb.equal(root.get("assigneeId"), assigneeId);
    }

    public static Specification<Task> titleContains(String keyword) {
        return (keyword == null || keyword.isBlank())
                ? Specification.unrestricted()
                : (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("title")),
                        "%" + keyword.toLowerCase() + "%"
                );
    }

    public static Specification<Task> createdAfter(Instant from) {
        return from == null
                ? Specification.unrestricted()
                : (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Task> createdBefore(Instant to) {
        return to == null
                ? Specification.unrestricted()
                : (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}