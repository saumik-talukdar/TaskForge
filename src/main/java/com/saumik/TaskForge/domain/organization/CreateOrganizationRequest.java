package com.saumik.TaskForge.domain.organization;

import jakarta.validation.constraints.NotBlank;

public record CreateOrganizationRequest(
        @NotBlank(message = "Organization name is required")
        String name
) {}