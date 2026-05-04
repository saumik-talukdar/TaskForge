package com.saumik.TaskForge.common.response;

import java.util.List;

public record PagedResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}