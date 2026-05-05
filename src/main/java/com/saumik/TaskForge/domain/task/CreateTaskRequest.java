package com.saumik.TaskForge.domain.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(

        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Title too long")
        String title,

        @Size(max = 2000, message = "Description too long")
        String description
) {}