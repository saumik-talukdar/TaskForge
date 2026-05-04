package com.saumik.TaskForge.domain.organization;

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

        organizationService.createOrganization(
                request.name(),
                user.getId()
        );

        return ResponseEntity.status(201).build(); // FIXED
    }

    @GetMapping
    public ResponseEntity<PagedResponse<OrganizationResponse>> getMyOrganizations(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Page<Organization> orgPage =
                organizationService.getUserOrganizations(user.getId(), pageable);

        List<OrganizationResponse> data = orgPage.getContent().stream()
                .map(o -> new OrganizationResponse(o.getId(), o.getName()))
                .toList();

        return ResponseEntity.ok(
                new PagedResponse<>(
                        data,
                        orgPage.getNumber(),
                        orgPage.getSize(),
                        orgPage.getTotalElements(),
                        orgPage.getTotalPages()
                )
        );
    }

    @PostMapping("/{orgId}/join")
    public ResponseEntity<Void> joinOrganization(
            @PathVariable UUID orgId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        organizationService.joinOrganization(orgId, user.getId());

        return ResponseEntity.noContent().build(); // FIXED
    }

    @GetMapping("/{orgId}/members")
    public ResponseEntity<PagedResponse<MemberResponse>> getMembers(
            @PathVariable UUID orgId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        Page<Membership> memberPage =
                organizationService.getMembers(orgId, user.getId(), pageable);

        List<MemberResponse> data = memberPage.getContent().stream()
                .map(m -> new MemberResponse(
                        m.getUserId(),
                        m.getRole().name()
                ))
                .toList();

        return ResponseEntity.ok(
                new PagedResponse<>(
                        data,
                        memberPage.getNumber(),
                        memberPage.getSize(),
                        memberPage.getTotalElements(),
                        memberPage.getTotalPages()
                )
        );
    }
}