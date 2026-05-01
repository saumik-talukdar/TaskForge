package com.saumik.TaskForge.domain.task;

public record UpdateTaskRequest(
        String title,
        String description
) {}