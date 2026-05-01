package com.saumik.TaskForge.domain.auth;

import com.saumik.TaskForge.domain.user.User;
import com.saumik.TaskForge.domain.user.UserRepository;
import com.saumik.TaskForge.security.jwt.JwtService;
import com.saumik.TaskForge.security.refresh.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Transactional
    public AuthResponse register(RegistrationRequest request){
        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getPasswordVersion()
        );
        String refreshToken = refreshTokenService.create(
                user.getId()
        );
        return new AuthResponse(accessToken,refreshToken);
    }


    public AuthResponse login(LoginRequest request){
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = (User) auth.getPrincipal();

        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getPasswordVersion()
        );
        String refreshToken = refreshTokenService.create(
                user.getId()
        );
        return new AuthResponse(accessToken,refreshToken);
    }


    public AuthResponse refresh(RefreshRequest request){

        UUID userId = refreshTokenService.validate(request.getRefreshToken());

        refreshTokenService.logout(request.getRefreshToken());
        User user = userRepository.findById(userId)
                        .orElseThrow();
        String refreshToken = refreshTokenService.create(userId);
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getPasswordVersion()
        );
        return new AuthResponse(accessToken,refreshToken);
    }
}
