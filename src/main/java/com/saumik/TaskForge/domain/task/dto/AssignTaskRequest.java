package com.saumik.TaskForge.domain.task.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignTaskRequest(

        @NotNull(message = "Assignee ID is required")
        UUID assigneeId
) {}

