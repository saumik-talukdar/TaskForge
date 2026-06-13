package com.saumik.TaskForge.domain.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(

        @NotBlank(message = "Organization name is required")
        @Size(max = 150, message = "Organization name must be at most 150 characters")
        String name
) {}