package com.saumik.TaskForge.domain.task;

import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(

        @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
        String title,

        @Size(max = 2000, message = "Description too long")
        String description
) {}