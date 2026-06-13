package com.saumik.TaskForge.domain.project.dto;

import com.saumik.TaskForge.domain.project.enums.ProjectRole;

import java.time.Instant;
import java.util.UUID;

public record ProjectMemberResponse(

        UUID userId,
        ProjectRole role,
        Instant joinedAt
) {}