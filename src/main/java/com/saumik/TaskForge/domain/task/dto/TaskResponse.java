package com.saumik.TaskForge.domain.task.dto;

import com.saumik.TaskForge.domain.task.enums.TaskPriority;
import com.saumik.TaskForge.domain.task.enums.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UUID assigneeId,
        UUID createdBy,
        UUID updatedBy,
        Instant dueDate,
        Instant createdAt,
        Instant updatedAt
) {}