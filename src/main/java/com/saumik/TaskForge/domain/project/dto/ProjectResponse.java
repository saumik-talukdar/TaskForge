package com.saumik.TaskForge.domain.project.dto;

import com.saumik.TaskForge.domain.project.enums.ProjectVisibility;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(

        UUID id,
        String name,
        String description,
        UUID managerId,
        ProjectVisibility visibility,
        Instant createdAt,
        Instant updatedAt
) {}