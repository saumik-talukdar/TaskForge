package com.saumik.TaskForge.domain.task;

import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String status
) {}