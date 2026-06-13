package com.saumik.TaskForge.domain.organization.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteOrganizationRequest(

        @NotBlank(message = "Password is required")
        String password
) {}