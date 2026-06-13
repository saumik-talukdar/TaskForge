package com.saumik.TaskForge.domain.project.dto;

import com.saumik.TaskForge.domain.project.enums.ProjectRole;

public record ChangeProjectRoleRequest(
        ProjectRole role
) {}