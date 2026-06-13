package com.saumik.TaskForge.domain.organization.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferOwnershipRequest(

        @NotNull(message = "New owner ID is required")
        UUID newOwnerId,

        @NotNull(message = "Password is required")
        String password
) {}