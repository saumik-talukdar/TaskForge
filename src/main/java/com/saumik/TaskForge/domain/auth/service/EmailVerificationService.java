package com.saumik.TaskForge.domain.auth.service;

import com.saumik.TaskForge.common.email.EmailService;
import com.saumik.TaskForge.domain.auth.entity.EmailVerificationToken;
import com.saumik.TaskForge.domain.auth.repository.EmailVerificationTokenRepository;
import com.saumik.TaskForge.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public void sendVerificationEmail(User user) {

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verificationToken =
                EmailVerificationToken.builder()
                        .userId(user.getId())
                        .token(token)
                        .expiresAt(Instant.now().plusSeconds(86400))
                        .build();

        tokenRepository.save(verificationToken);

        String link = frontendUrl+"/api/v1/auth/verify-email?token=" + token;

        String body = """
            Welcome to TaskForge!

            Verify your email by clicking:

            %s
            """.formatted(link);

        emailService.sendEmail(
                user.getEmail(),
                "Verify your email",
                body
        );
    }
}
