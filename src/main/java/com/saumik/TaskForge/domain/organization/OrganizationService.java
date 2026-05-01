package com.saumik.TaskForge.domain.organization;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<Organization> getUserOrganizations(UUID userId) {

        List<Membership> memberships = membershipRepository.findByUserId(userId);

        List<UUID> orgIds = memberships.stream()
                .map(Membership::getOrganizationId)
                .toList();

        return organizationRepository.findAllById(orgIds);
    }

    @Transactional
    public void joinOrganization(UUID orgId, UUID userId) {

        boolean exists = membershipRepository
                .existsByUserIdAndOrganizationId(userId, orgId);

        if (exists) {
            throw new RuntimeException("Already a member of this organization");
        }

        Membership membership = Membership.builder()
                .userId(userId)
                .organizationId(orgId)
                .role(Role.MEMBER)
                .build();

        membershipRepository.save(membership);
    }
}