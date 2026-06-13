package com.saumik.TaskForge.domain.task.controller;

import com.saumik.TaskForge.common.response.PagedResponse;
import com.saumik.TaskForge.domain.task.enums.TaskPriority;
import com.saumik.TaskForge.domain.task.enums.TaskStatus;
import com.saumik.TaskForge.domain.task.dto.*;
import com.saumik.TaskForge.domain.task.entity.Task;
import com.saumik.TaskForge.domain.task.service.TaskService;
import com.saumik.TaskForge.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Void> createTask(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        taskService.createTask(
                orgId, projectId, user.getId(),
                request.title(), request.description(),
                request.priority(), request.dueDate()
        );
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TaskResponse>> getTasks(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Page<Task> page = taskService.getTasks(
                orgId, projectId, user.getId(),
                status, assigneeId, priority, keyword, from, to, pageable
        );

        List<TaskResponse> data = page.getContent().stream()
                .map(t -> new TaskResponse(
                        t.getId(), t.getTitle(), t.getDescription(),
                        t.getStatus(), t.getPriority(),
                        t.getAssigneeId(), t.getCreatedBy(), t.getUpdatedBy(),
                        t.getDueDate(), t.getCreatedAt(), t.getUpdatedAt()
                ))
                .toList();

        return ResponseEntity.ok(new PagedResponse<>(
                data, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages()
        ));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<Void> updateTask(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        taskService.updateTask(
                orgId, projectId, taskId, user.getId(),
                request.title(), request.description(), request.dueDate()
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        taskService.updateStatus(orgId, projectId, taskId, user.getId(), request.status());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/assign")
    public ResponseEntity<Void> assignTask(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        taskService.assignTask(orgId, projectId, taskId, user.getId(), request.assigneeId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        taskService.deleteTask(orgId, projectId, taskId, user.getId());
        return ResponseEntity.noContent().build();
    }
}