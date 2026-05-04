package com.saumik.TaskForge.domain.organization;

import java.util.UUID;

public record MemberResponse(
        UUID userId,
        String role
) {}