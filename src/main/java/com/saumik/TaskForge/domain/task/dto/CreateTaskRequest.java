package com.saumik.TaskForge.domain.task.dto;

import com.saumik.TaskForge.domain.task.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateTaskRequest(

        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Title too long")
        String title,

        @Size(max = 2000, message = "Description too long")
        String description,

        @NotNull(message = "Task priority is required")
        TaskPriority priority,

        @NotNull(message = "Due date is required")
        Instant dueDate
) {}