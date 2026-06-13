package com.saumik.TaskForge.domain.project.dto;

import com.saumik.TaskForge.domain.project.enums.ProjectVisibility;

public record UpdateProjectVisibilityRequest(
        ProjectVisibility visibility
) {}