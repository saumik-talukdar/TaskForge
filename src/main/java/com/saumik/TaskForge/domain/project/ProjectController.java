package com.saumik.TaskForge.domain.project;

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
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs/{orgId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<Void> createProject(
            @PathVariable UUID orgId,
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        projectService.createProject(
                request.name(),
                request.description(),
                orgId,
                user.getId()
        );

        return ResponseEntity.status(201).build(); // CREATED
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProjectResponse>> getProjects(
            @PathVariable UUID orgId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Page<Project> projectPage = projectService.getProjects(
                orgId,
                user.getId(),
                keyword,
                from,
                to,
                pageable
        );

        List<ProjectResponse> data = projectPage.getContent().stream()
                .map(p -> new ProjectResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription()
                ))
                .toList();

        PagedResponse<ProjectResponse> response =
                new PagedResponse<>(
                        data,
                        projectPage.getNumber(),
                        projectPage.getSize(),
                        projectPage.getTotalElements(),
                        projectPage.getTotalPages()
                );

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{projectId}")
    public ResponseEntity<Void> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        projectService.updateProject(
                projectId,
                user.getId(),
                request.name(),
                request.description()
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        projectService.deleteProject(projectId, user.getId());

        return ResponseEntity.noContent().build();
    }
}