package com.saumik.TaskForge.domain.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}