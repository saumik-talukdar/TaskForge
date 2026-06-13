package com.saumik.TaskForge.domain.organization.service;

import com.saumik.TaskForge.common.email.EmailService;
import com.saumik.TaskForge.common.exception.AccessDeniedException;
import com.saumik.TaskForge.common.exception.BadRequestException;
import com.saumik.TaskForge.common.exception.NotFoundException;
import com.saumik.TaskForge.domain.organization.dto.MemberResponse;
import com.saumik.TaskForge.domain.organization.entity.Membership;
import com.saumik.TaskForge.domain.organization.entity.Organization;
import com.saumik.TaskForge.domain.organization.entity.OrganizationInvite;
import com.saumik.TaskForge.domain.organization.enums.Role;
import com.saumik.TaskForge.domain.organization.repository.MembershipRepository;
import com.saumik.TaskForge.domain.organization.repository.OrganizationInviteRepository;
import com.saumik.TaskForge.domain.organization.repository.OrganizationRepository;
import com.saumik.TaskForge.domain.project.repository.ProjectMembershipRepository;
import com.saumik.TaskForge.domain.project.repository.ProjectRepository;
import com.saumik.TaskForge.domain.task.repository.TaskRepository;
import com.saumik.TaskForge.domain.user.entity.User;
import com.saumik.TaskForge.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final OrganizationInviteRepository inviteRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectMembershipRepository projectMembershipRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;


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

    public Page<MemberResponse> getMembers(UUID orgId, UUID requesterId, Pageable pageable) {

        requireMember(requesterId, orgId);

        return membershipRepository.findMembersWithUserDetails(orgId, pageable);
    }


    @Transactional
    public void inviteMember(UUID orgId, UUID inviterId, String email, Role role) {

        requireAdmin(inviterId, orgId);

        String normalizedEmail = email.toLowerCase();

        // If the user already exists, check they're not already a member
        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent()) {
            boolean alreadyMember = membershipRepository.existsByUserIdAndOrganizationId(
                    existingUser.get().getId(), orgId
            );
            if (alreadyMember) {
                throw new BadRequestException("User is already a member");
            }
        }

        // Prevent duplicate pending invites
        if (inviteRepository.existsByOrganizationIdAndEmailAndAcceptedFalse(orgId, normalizedEmail)) {
            throw new BadRequestException("Pending invitation already exists for this email");
        }

        String token = UUID.randomUUID().toString();

        OrganizationInvite invite = OrganizationInvite.builder()
                .organizationId(orgId)
                .email(normalizedEmail)
                .role(role)
                .token(token)
                .accepted(false)
                .expiresAt(Instant.now().plusSeconds(86400))
                .build();

        inviteRepository.save(invite);

        String inviteLink = frontendUrl + "/accept-invite?token=" + token;

        emailService.sendEmail(
                normalizedEmail,
                "You've been invited to join an organization on TaskForge",
                """
                You have been invited to join an organization on TaskForge.

                Click the link below to accept (expires in 24 hours):

                %s

                If you don't have an account yet, register first and then use this link.
                """.formatted(inviteLink)
        );
    }

    @Transactional
    public void acceptInvite(String token, UUID userId) {

        OrganizationInvite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid invitation token"));

        if (invite.isAccepted()) {
            throw new BadRequestException("Invitation has already been accepted");
        }

        if (invite.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invitation has expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(invite.getEmail())) {
            throw new AccessDeniedException("This invitation was sent to a different email address");
        }

        if (membershipRepository.existsByUserIdAndOrganizationId(userId, invite.getOrganizationId())) {
            throw new BadRequestException("You are already a member of this organization");
        }

        membershipRepository.save(
                Membership.builder()
                        .userId(userId)
                        .organizationId(invite.getOrganizationId())
                        .role(invite.getRole())
                        .build()
        );

        // Delete after use — no need to keep accepted invites around
        inviteRepository.delete(invite);
    }


    @Transactional
    public void leaveOrganization(UUID orgId, UUID userId) {

        Membership membership = requireMember(userId, orgId);

        // The owner must transfer ownership before leaving
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        if (org.getOwnerId().equals(userId)) {
            throw new BadRequestException(
                    "You are the owner of this organization. Transfer ownership before leaving."
            );
        }

        long managedProjects = projectMembershipRepository.countManagedProjectsInOrg(userId, orgId);
        if (managedProjects > 0) {
            throw new BadRequestException(
                    "You are the manager of " + managedProjects +
                            " project(s) in this organization. Transfer management before leaving."
            );
        }

        projectMembershipRepository.deleteByUserIdAndOrganizationId(userId, orgId);
        membershipRepository.delete(membership);
    }

    @Transactional
    public void removeMember(UUID orgId, UUID memberId, UUID requesterId) {

        requireAdmin(requesterId, orgId);

        if (memberId.equals(requesterId)) {
            throw new BadRequestException("Use the leave organization endpoint to remove yourself");
        }

        Membership target = membershipRepository
                .findByUserIdAndOrganizationId(memberId, orgId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        // The owner cannot be removed — they must transfer ownership first
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        if (org.getOwnerId().equals(memberId)) {
            throw new BadRequestException(
                    "The organization owner cannot be removed. Transfer ownership first."
            );
        }

        long managedProjects = projectMembershipRepository.countManagedProjectsInOrg(memberId, orgId);
        if (managedProjects > 0) {
            throw new BadRequestException(
                    "This member manages " + managedProjects +
                            " project(s). Transfer management before removing them."
            );
        }

        projectMembershipRepository.deleteByUserIdAndOrganizationId(memberId, orgId);
        membershipRepository.delete(target);
    }

    @Transactional
    public void changeRole(UUID orgId, UUID memberId, UUID requesterId, Role newRole) {

        requireAdmin(requesterId, orgId);

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        Membership target = membershipRepository
                .findByUserIdAndOrganizationId(memberId, orgId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        if (target.getRole() == newRole) {
            throw new BadRequestException("Member already has this role");
        }

        // Protect the owner's admin role — use transferOwnership instead
        if (org.getOwnerId().equals(memberId) && newRole != Role.ADMIN) {
            throw new BadRequestException(
                    "Cannot demote the organization owner. Transfer ownership first."
            );
        }

        // Prevent removing the last admin (when demoting a non-owner admin)
        if (target.getRole() == Role.ADMIN && newRole == Role.MEMBER) {
            long adminCount = membershipRepository.countByOrganizationIdAndRole(orgId, Role.ADMIN);
            if (adminCount <= 1) {
                throw new BadRequestException("Organization must have at least one admin");
            }
        }

        target.setRole(newRole);
    }


    @Transactional
    public void transferOwnership(UUID orgId, UUID currentOwnerId, UUID newOwnerId, String password) {

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        // Only the current owner can transfer ownership
        if (!org.getOwnerId().equals(currentOwnerId)) {
            throw new AccessDeniedException("Only the organization owner can transfer ownership");
        }

        User currentOwner = userRepository.findById(currentOwnerId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(password, currentOwner.getPassword())) {
            throw new BadRequestException("Incorrect password");
        }

        if (currentOwnerId.equals(newOwnerId)) {
            throw new BadRequestException("You are already the owner");
        }

        // New owner must be a member of the org
        Membership newOwnerMembership = membershipRepository
                .findByUserIdAndOrganizationId(newOwnerId, orgId)
                .orElseThrow(() -> new BadRequestException("The new owner must be a member of this organization"));

        // Ensure the new owner becomes ADMIN
        newOwnerMembership.setRole(Role.ADMIN);

        // Update the org's ownerId
        org.setOwnerId(newOwnerId);
    }


    @Transactional
    public void deleteOrganization(UUID orgId, UUID requesterId, String password) {

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        // Only the owner can delete the organization
        if (!org.getOwnerId().equals(requesterId)) {
            throw new AccessDeniedException("Only the organization owner can delete it");
        }

        User user = userRepository.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Incorrect password");
        }

        taskRepository.deleteByOrganizationId(orgId);
        projectMembershipRepository.deleteByOrganizationId(orgId);  // before projects
        projectRepository.deleteByOrganizationId(orgId);
        membershipRepository.deleteByOrganizationId(orgId);
        inviteRepository.deleteByOrganizationId(orgId);
        organizationRepository.delete(org);
    }

    /**
     * Runs every hour and purges invite rows that have expired without being accepted.
     */
    @Scheduled(fixedRateString = "PT1H")
    @Transactional
    public void purgeExpiredInvites() {
        inviteRepository.deleteByExpiresAtBeforeAndAcceptedFalse(Instant.now());
    }

    // helper

    private Membership requireMember(UUID userId, UUID orgId) {
        return membershipRepository
                .findByUserIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this organization"));
    }

    private void requireAdmin(UUID userId, UUID orgId) {
        Membership m = requireMember(userId, orgId);
        if (m.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can perform this action");
        }
    }
}