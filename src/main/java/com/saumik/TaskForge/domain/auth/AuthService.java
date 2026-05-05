package com.saumik.TaskForge.domain.auth;

import com.saumik.TaskForge.common.exception.BadRequestException;
import com.saumik.TaskForge.common.exception.NotFoundException;
import com.saumik.TaskForge.domain.user.User;
import com.saumik.TaskForge.domain.user.UserRepository;
import com.saumik.TaskForge.security.jwt.JwtService;
import com.saumik.TaskForge.security.refresh.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // REGISTER
    @Transactional
    public AuthResponse register(RegistrationRequest request){

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

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getPasswordVersion()
        );

        String refreshToken = refreshTokenService.create(user.getId());

        return new AuthResponse(accessToken, refreshToken);
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
            throw new BadRequestException("Email not verified");
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
        refreshTokenService.logout(request.refreshToken());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String newRefreshToken = refreshTokenService.create(userId);

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getPasswordVersion()
        );

        return new AuthResponse(accessToken, newRefreshToken);
    }
}