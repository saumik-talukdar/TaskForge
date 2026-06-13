package com.saumik.TaskForge.domain.organization.dto;

import java.util.UUID;

public record OrganizationResponse(
        UUID id,
        String name
) {}