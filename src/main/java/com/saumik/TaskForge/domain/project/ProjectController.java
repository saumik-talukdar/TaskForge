package com.saumik.TaskForge.domain.project;

import com.saumik.TaskForge.domain.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @PathVariable UUID orgId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        List<Project> projects =
                projectService.getProjects(orgId, user.getId());

        List<ProjectResponse> response = projects.stream()
                .map(p -> new ProjectResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription()
                ))
                .toList();

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