package com.saumik.TaskForge.domain.auth.service;

import com.saumik.TaskForge.common.email.EmailService;
import com.saumik.TaskForge.common.exception.*;
import com.saumik.TaskForge.domain.auth.entity.EmailVerificationToken;
import com.saumik.TaskForge.domain.auth.repository.EmailVerificationTokenRepository;
import com.saumik.TaskForge.domain.auth.entity.PasswordResetToken;
import com.saumik.TaskForge.domain.auth.repository.PasswordResetTokenRepository;
import com.saumik.TaskForge.domain.auth.dto.AuthResponse;
import com.saumik.TaskForge.domain.auth.dto.LoginRequest;
import com.saumik.TaskForge.domain.auth.dto.RefreshRequest;
import com.saumik.TaskForge.domain.auth.dto.RegistrationRequest;
import com.saumik.TaskForge.domain.user.entity.User;
import com.saumik.TaskForge.domain.user.repository.UserRepository;
import com.saumik.TaskForge.security.jwt.JwtService;
import com.saumik.TaskForge.security.refresh.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // REGISTER
    @Transactional
    public void register(RegistrationRequest request){

        String email = request.email().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new BadRequestException("Email already in use");
        }

        var user = User.builder()
                .fullName(request.fullName())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);

        emailVerificationService.sendVerificationEmail(user);

    }

    // LOGIN
    public AuthResponse login(LoginRequest request){

        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email().toLowerCase(),
                        request.password()
                )
        );

        User user = (User) auth.getPrincipal();

        if (!user.isActive()) {
            throw new BadRequestException("Account is disabled");
        }
        if (!user.isEmailVerified()) {
            throw new ForbiddenException(
                    "Email is not verified"
            );
        }
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getPasswordVersion()
        );

        String refreshToken = refreshTokenService.create(user.getId());

        return new AuthResponse(accessToken, refreshToken);
    }

    // REFRESH
    public AuthResponse refresh(RefreshRequest request){

        UUID userId;

        try {
            userId = refreshTokenService.validate(request.refreshToken());
        } catch (Exception e) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        // invalidate old token (rotation)
        refreshTokenService.logout(userId,request.refreshToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String newRefreshToken = refreshTokenService.create(userId);

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getPasswordVersion()
        );

        return new AuthResponse(accessToken, newRefreshToken);
    }

    @Transactional
    public void forgotPassword(String email) {

        User user = userRepository
                .findByEmail(email.toLowerCase())
                .orElse(null);

        if(user == null){
            return;
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .userId(user.getId())
                        .token(token)
                        .expiresAt(Instant.now().plusSeconds(900))
                        .build();

        tokenRepository.save(resetToken);

//        System.out.println(
//                "RESET TOKEN: " + token
//        );

        String link = frontendUrl + "/reset-password?token=" + token;

        String body = """
        Reset your password using the link below:

        %s
        """.formatted(link);

        emailService.sendEmail(
                user.getEmail(),
                "Reset your password",
                body
        );
    }


    @Transactional
    public void resetPassword(
            String token,
            String newPassword
    ) {

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidTokenException("Invalid token"));


        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new TokenExpiredException("Token expired");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() ->
                        new NotFoundException("User not found"));

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        user.setPasswordVersion(
                user.getPasswordVersion() + 1
        );

        refreshTokenService.logoutAll(user.getId());
        tokenRepository.delete(resetToken);
    }

    @Transactional
    public void verifyEmail(String token) {

        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new InvalidTokenException("Invalid token"));


        if (verificationToken.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new TokenExpiredException(
                    "Verification token expired"
            );
        }

        User user = userRepository.findById(
                verificationToken.getUserId()
        ).orElseThrow(() ->
                new NotFoundException("User not found"));

        user.setEmailVerified(true);
        emailVerificationTokenRepository.delete(verificationToken);
    }
}