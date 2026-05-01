package com.saumik.TaskForge.domain.project;

import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String description
) {}
