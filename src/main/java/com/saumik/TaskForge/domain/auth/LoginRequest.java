package com.saumik.TaskForge.domain.auth;


public record LoginRequest (
    String email,
    String password
){}
