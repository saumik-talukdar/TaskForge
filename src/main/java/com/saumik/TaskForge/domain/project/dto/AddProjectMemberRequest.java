package com.saumik.TaskForge.domain.project.dto;

import com.saumik.TaskForge.domain.project.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddProjectMemberRequest(

        @NotNull(message = "User id is required")
        UUID userId,

        @NotNull(message = "Project role is required")
        ProjectRole role
) {}