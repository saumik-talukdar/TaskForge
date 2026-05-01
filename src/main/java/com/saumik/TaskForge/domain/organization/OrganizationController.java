package com.saumik.TaskForge.domain.organization;

import com.saumik.TaskForge.domain.user.User;
import lombok.RequiredArgsConstructor;
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
            @RequestBody CreateOrganizationRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        organizationService.createOrganization(
                request.name(),
                user.getId()
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getMyOrganizations(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        List<Organization> orgs =
                organizationService.getUserOrganizations(user.getId());

        List<OrganizationResponse> response = orgs.stream()
                .map(o -> new OrganizationResponse(o.getId(), o.getName()))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orgId}/join")
    public ResponseEntity<Void> joinOrganization(
            @PathVariable UUID orgId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        organizationService.joinOrganization(orgId, user.getId());

        return ResponseEntity.ok().build();
    }
}