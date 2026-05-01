package com.saumik.TaskForge.domain.auth;

public record RefreshRequest (
    String accessToken,
    String refreshToken
){}
