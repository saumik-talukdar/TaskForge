package com.saumik.TaskForge.domain.organization.controller;

import com.saumik.TaskForge.common.response.PagedResponse;
import com.saumik.TaskForge.domain.organization.dto.*;
import com.saumik.TaskForge.domain.organization.entity.Organization;
import com.saumik.TaskForge.domain.organization.service.OrganizationService;
import com.saumik.TaskForge.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orgs")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<Void> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        organizationService.createOrganization(request.name(), user.getId());
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<PagedResponse<OrganizationResponse>> getMyOrganizations(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Page<Organization> orgPage = organizationService.getUserOrganizations(user.getId(), pageable);

        List<OrganizationResponse> data = orgPage.getContent().stream()
                .map(o -> new OrganizationResponse(o.getId(), o.getName()))
                .toList();

        return ResponseEntity.ok(new PagedResponse<>(
                data,
                orgPage.getNumber(),
                orgPage.getSize(),
                orgPage.getTotalElements(),
                orgPage.getTotalPages()
        ));
    }

    @DeleteMapping("/{orgId}")
    public ResponseEntity<Void> deleteOrganization(
            @PathVariable UUID orgId,
            @Valid @RequestBody DeleteOrganizationRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        organizationService.deleteOrganization(orgId, user.getId(), request.password());
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{orgId}/transfer-ownership")
    public ResponseEntity<Void> transferOwnership(
            @PathVariable UUID orgId,
            @Valid @RequestBody TransferOwnershipRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        organizationService.transferOwnership(
                orgId,
                user.getId(),
                request.newOwnerId(),
                request.password()
        );
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{orgId}/members")
    public ResponseEntity<PagedResponse<MemberResponse>> getMembers(
            @PathVariable UUID orgId,
            @PageableDefault(size = 10, sort = "joinedAt", direction = Sort.Direction.ASC)
            Pageable pageable,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Page<MemberResponse> memberPage =
                organizationService.getMembers(orgId, user.getId(), pageable);

        return ResponseEntity.ok(new PagedResponse<>(
                memberPage.getContent(),
                memberPage.getNumber(),
                memberPage.getSize(),
                memberPage.getTotalElements(),
                memberPage.getTotalPages()
        ));
    }

    @DeleteMapping("/{orgId}/leave")
    public ResponseEntity<Void> leaveOrganization(
            @PathVariable UUID orgId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        organizationService.leaveOrganization(orgId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{orgId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID orgId,
            @PathVariable UUID memberId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        organizationService.removeMember(orgId, memberId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{orgId}/members/{memberId}/role")
    public ResponseEntity<Void> changeRole(
            @PathVariable UUID orgId,
            @PathVariable UUID memberId,
            @Valid @RequestBody ChangeRoleRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        organizationService.changeRole(orgId, memberId, user.getId(), request.role());
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{orgId}/invites")
    public ResponseEntity<Void> inviteMember(
            @PathVariable UUID orgId,
            @Valid @RequestBody InviteMemberRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        organizationService.inviteMember(orgId, user.getId(), request.email(), request.role());
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/invites/accept")
    public ResponseEntity<Void> acceptInvite(
            @Valid @RequestBody AcceptInviteRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        organizationService.acceptInvite(request.token(), user.getId());
        return ResponseEntity.noContent().build();
    }
}