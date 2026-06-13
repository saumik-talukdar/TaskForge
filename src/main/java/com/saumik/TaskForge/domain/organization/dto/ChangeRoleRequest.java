package com.saumik.TaskForge.domain.organization.dto;

import com.saumik.TaskForge.domain.organization.enums.Role;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(

        @NotNull(message = "Role is required")
        Role role
) {}