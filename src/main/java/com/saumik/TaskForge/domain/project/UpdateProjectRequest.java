package com.saumik.TaskForge.domain.project;

public record UpdateProjectRequest(
        String name,
        String description
) {}