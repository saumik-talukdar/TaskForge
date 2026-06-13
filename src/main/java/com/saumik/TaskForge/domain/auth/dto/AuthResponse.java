package com.saumik.TaskForge.domain.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}