package com.saumik.TaskForge.domain.project.entity;

import com.saumik.TaskForge.domain.project.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "project_memberships",
        indexes = {
                @Index(name = "idx_pm_user_project",  columnList = "user_id, project_id", unique = true),
                @Index(name = "idx_pm_project",        columnList = "project_id"),
                @Index(name = "idx_pm_user",           columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    public void onCreate() {
        this.joinedAt = Instant.now();
    }
}