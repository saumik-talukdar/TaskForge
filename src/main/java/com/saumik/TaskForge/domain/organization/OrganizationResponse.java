package com.saumik.TaskForge.domain.organization;

import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name
) {}