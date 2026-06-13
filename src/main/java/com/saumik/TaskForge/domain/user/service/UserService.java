package com.saumik.TaskForge.domain.user.service;

import com.saumik.TaskForge.common.exception.BadRequestException;
import com.saumik.TaskForge.common.exception.NotFoundException;
import com.saumik.TaskForge.domain.user.entity.User;
import com.saumik.TaskForge.domain.user.repository.UserRepository;
import com.saumik.TaskForge.security.refresh.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;


    @Transactional
    public void changePassword(
            UUID userId,
            String currentPassword,
            String newPassword
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean matches = passwordEncoder.matches(
                currentPassword,
                user.getPassword()
        );

        if (!matches) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        user.setPasswordVersion(
                user.getPasswordVersion() + 1
        );

        refreshTokenService.logoutAll(userId);
    }

    public void logout(UUID userId,String refreshToken) {
        refreshTokenService.logout(userId,refreshToken);
    }

    public void logoutAll(UUID userId) {
        refreshTokenService.logoutAll(userId);
    }
}
