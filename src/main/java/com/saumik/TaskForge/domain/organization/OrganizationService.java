package com.saumik.TaskForge.domain.organization;

import com.saumik.TaskForge.common.exception.AccessDeniedException;
import com.saumik.TaskForge.common.exception.BadRequestException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public void createOrganization(String name, UUID userId) {

        Organization org = Organization.builder()
                .name(name)
                .ownerId(userId)
                .build();

        organizationRepository.save(org);

        Membership membership = Membership.builder()
                .userId(userId)
                .organizationId(org.getId())
                .role(Role.ADMIN)
                .build();

        membershipRepository.save(membership);
    }

    public Page<Organization> getUserOrganizations(UUID userId, Pageable pageable) {
        return organizationRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public void joinOrganization(UUID orgId, UUID userId) {

        if (membershipRepository.existsByUserIdAndOrganizationId(userId, orgId)) {
            throw new BadRequestException("Already a member of this organization");
        }

        try {
            Membership membership = Membership.builder()
                    .userId(userId)
                    .organizationId(orgId)
                    .role(Role.MEMBER)
                    .build();

            membershipRepository.save(membership);

        } catch (Exception e) {
            // fallback safety for race condition
            throw new BadRequestException("Already a member of this organization");
        }
    }

    public Page<Membership> getMembers(UUID orgId, UUID userId, Pageable pageable) {

        // security check
        getMembership(userId, orgId);

        return membershipRepository.findByOrganizationId(orgId, pageable);
    }

    // helper
    private Membership getMembership(UUID userId, UUID orgId) {
        return membershipRepository
                .findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() ->
                        new AccessDeniedException("Not a member of this organization"));
    }
}