package com.saumik.TaskForge.domain.organization.dto;

import com.saumik.TaskForge.domain.organization.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MemberResponse {
    private final UUID userId;
    private final String fullName;
    private final String email;
    private final Role role;
}