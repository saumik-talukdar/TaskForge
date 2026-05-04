package com.saumik.TaskForge.domain.task;

import com.saumik.TaskForge.common.response.PagedResponse;
import com.saumik.TaskForge.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;


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
                orgId,
                projectId,
                request.title(),
                request.description(),
                user.getId()
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
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Page<Task> taskPage = taskService.getTasks(
                orgId,
                projectId,
                user.getId(),
                status,
                assigneeId,
                priority,
                keyword,
                from,
                to,
                pageable
        );

        List<TaskResponse> data = taskPage.getContent().stream()
                .map(t -> new TaskResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getStatus().name()
                ))
                .toList();

        PagedResponse<TaskResponse> response =
                new PagedResponse<>(
                        data,
                        taskPage.getNumber(),
                        taskPage.getSize(),
                        taskPage.getTotalElements(),
                        taskPage.getTotalPages()
                );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<Void> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        taskService.updateTask(
                taskId,
                user.getId(),
                request.title(),
                request.description()
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateStatusRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        taskService.updateStatus(taskId, user.getId(), request.status());

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{taskId}/assign")
    public ResponseEntity<Void> assignTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        taskService.assignTask(
                taskId,
                user.getId(),
                request.assigneeId()
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID taskId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        taskService.deleteTask(taskId, user.getId());

        return ResponseEntity.noContent().build();
    }
}