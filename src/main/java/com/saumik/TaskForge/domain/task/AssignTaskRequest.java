package com.saumik.TaskForge.domain.task;

import java.util.UUID;

public record AssignTaskRequest(
        UUID assigneeId
) {}
