package com.saumik.TaskForge.domain.task.dto;

import com.saumik.TaskForge.domain.task.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

        @NotNull(message = "Status is required")
        TaskStatus status
) {}
