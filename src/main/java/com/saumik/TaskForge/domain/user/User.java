package com.saumik.TaskForge.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }

)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false,length = 255, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String password;
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;
    @Column(name = "password_version", nullable = false)
    private Long passwordVersion = 0L;
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true; // later change
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = true; // later change
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate(){
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.email = this.email.toLowerCase();
    }

    @PreUpdate
    void onUpdate(){
        this.updatedAt = Instant.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }
}
