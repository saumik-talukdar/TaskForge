package com.saumik.TaskForge.domain.organization.entity;

import com.saumik.TaskForge.domain.organization.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "memberships",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_org",
                columnNames = {"user_id", "organization_id"}
        ),
        indexes = {
                @Index(name = "idx_membership_user",    columnList = "user_id"),
                @Index(name = "idx_membership_org",     columnList = "organization_id"),
                @Index(name = "idx_membership_org_role",columnList = "organization_id, role")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    public void onCreate() {
        this.joinedAt = Instant.now();
    }
}