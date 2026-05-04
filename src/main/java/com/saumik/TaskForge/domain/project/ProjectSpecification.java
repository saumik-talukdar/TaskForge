package com.saumik.TaskForge.domain.project;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public class ProjectSpecification {

    public static Specification<Project> hasOrganization(UUID orgId) {
        return (root, query, cb) ->
                cb.equal(root.get("organizationId"), orgId);
    }

    public static Specification<Project> nameContains(String keyword) {
        return (keyword == null || keyword.isBlank())
                ? Specification.unrestricted()
                : (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("name")),
                        "%" + keyword.toLowerCase() + "%"
                );
    }

    public static Specification<Project> createdAfter(Instant from) {
        return from == null
                ? Specification.unrestricted()
                : (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Project> createdBefore(Instant to) {
        return to == null
                ? Specification.unrestricted()
                : (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}