package com.saumik.TaskForge.domain.project.controller;

import com.saumik.TaskForge.common.response.PagedResponse;
import com.saumik.TaskForge.domain.project.dto.*;
import com.saumik.TaskForge.domain.project.entity.Project;
import com.saumik.TaskForge.domain.project.entity.ProjectMembership;
import com.saumik.TaskForge.domain.project.service.ProjectService;
import com.saumik.TaskForge.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
                orgId, user.getId(),
                request.name(), request.description(), request.visibility()
        );
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProjectResponse>> getProjects(
            @PathVariable UUID orgId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Page<Project> page = projectService.getProjects(orgId, user.getId(), keyword, from, to, pageable);

        List<ProjectResponse> data = page.getContent().stream()
                .map(p -> new ProjectResponse(
                        p.getId(), p.getName(), p.getDescription(),
                        p.getManagerId(), p.getVisibility(),
                        p.getCreatedAt(), p.getUpdatedAt()
                ))
                .toList();

        return ResponseEntity.ok(new PagedResponse<>(
                data, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages()
        ));
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<Void> updateProject(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        projectService.updateProject(
                orgId, projectId, user.getId(),
                request.name(), request.description(), request.visibility()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        projectService.deleteProject(orgId, projectId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/transfer-manager")
    public ResponseEntity<Void> transferManager(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @Valid @RequestBody TransferManagerRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        projectService.transferManager(orgId, projectId, user.getId(), request.newManagerId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<PagedResponse<ProjectMemberResponse>> getProjectMembers(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @PageableDefault(size = 20, sort = "joinedAt", direction = Sort.Direction.ASC) Pageable pageable,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Page<ProjectMembership> page =
                projectService.getProjectMembers(orgId, projectId, user.getId(), pageable);

        List<ProjectMemberResponse> data = page.getContent().stream()
                .map(m -> new ProjectMemberResponse(m.getUserId(), m.getRole(), m.getJoinedAt()))
                .toList();

        return ResponseEntity.ok(new PagedResponse<>(
                data, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages()
        ));
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        projectService.addMember(orgId, projectId, user.getId(), request.userId(), request.role());
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID orgId,
            @PathVariable UUID projectId,
            @PathVariable UUID memberId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        projectService.removeMember(orgId, projectId, user.getId(), memberId);
        return ResponseEntity.noContent().build();
    }
}