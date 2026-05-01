package com.saumik.TaskForge.domain.project;

public record CreateProjectRequest(
        String name,
        String description
) {}