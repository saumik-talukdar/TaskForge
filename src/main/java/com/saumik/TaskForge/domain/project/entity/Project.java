package com.saumik.TaskForge.domain.project.entity;

import com.saumik.TaskForge.domain.project.enums.ProjectVisibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "projects",
        indexes = {
                @Index(name = "idx_project_org",        columnList = "organization_id"),
                @Index(name = "idx_project_manager",     columnList = "manager_id"),
                @Index(name = "idx_project_org_name",    columnList = "organization_id, name", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * The current manager of this project. Exactly one per project.
     * Set to createdBy on creation; updated via transferManager only.
     */
    @Column(name = "manager_id", nullable = false)
    private UUID managerId;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectVisibility visibility = ProjectVisibility.PUBLIC_TO_ORG;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}