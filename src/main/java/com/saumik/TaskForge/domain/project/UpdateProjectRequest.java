package com.saumik.TaskForge.domain.project;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(

        @Size(min = 1, max = 150, message = "Project name must be between 1 and 150 characters")
        String name,

        @Size(max = 1000, message = "Description too long")
        String description
) {}