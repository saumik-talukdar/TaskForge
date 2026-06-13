package com.saumik.TaskForge.domain.project.specification;

import com.saumik.TaskForge.domain.project.entity.Project;
import com.saumik.TaskForge.domain.project.entity.ProjectMembership;
import com.saumik.TaskForge.domain.project.enums.ProjectVisibility;
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

    /**
     * Visibility filter — the core access control gate for project listing.
     *
     * Rules:
     *  - Org admins / org owner → see everything (no filter applied)
     *  - Regular members       → see PUBLIC_TO_ORG projects
     *                            + PRIVATE projects where they have a ProjectMembership row
     *
     * The subquery checks the project_memberships table directly so we don't
     * need to join the table at the service layer.
     */
    public static Specification<Project> visibleTo(UUID userId, boolean isOrgAdmin) {
        if (isOrgAdmin) {
            return Specification.unrestricted();
        }

        return (root, query, cb) -> {
            // Subquery: does a ProjectMembership row exist for (userId, project.id)?
            var subquery = query.subquery(Long.class);
            var pm = subquery.from(ProjectMembership.class);
            subquery.select(cb.literal(1L))
                    .where(
                            cb.equal(pm.get("userId"), userId),
                            cb.equal(pm.get("projectId"), root.get("id"))
                    );

            return cb.or(
                    cb.equal(root.get("visibility"), ProjectVisibility.PUBLIC_TO_ORG),
                    cb.exists(subquery)
            );
        };
    }
}