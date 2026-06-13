package com.saumik.TaskForge.domain.project.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferManagerRequest(

        @NotNull(message = "New manager id is required")
        UUID newManagerId
) {}