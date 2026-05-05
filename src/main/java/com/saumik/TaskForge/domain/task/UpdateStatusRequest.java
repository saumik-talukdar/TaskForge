package com.saumik.TaskForge.domain.task;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(

        @NotNull(message = "Status is required")
        TaskStatus status
) {}
