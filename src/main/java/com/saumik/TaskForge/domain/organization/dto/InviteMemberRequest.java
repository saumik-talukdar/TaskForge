package com.saumik.TaskForge.domain.organization.dto;

import com.saumik.TaskForge.domain.organization.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record InviteMemberRequest(

        @Email(message = "Invalid email")
        String email,

        @NotNull(message = "Role is required")
        Role role
) {}