package com.saumik.TaskForge.domain.user.controller;

import com.saumik.TaskForge.domain.auth.dto.RefreshRequest;
import com.saumik.TaskForge.domain.user.service.UserService;
import com.saumik.TaskForge.domain.user.dto.ChangePasswordRequest;
import com.saumik.TaskForge.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        userService.changePassword(
                user.getId(),
                request.currentPassword(),
                request.newPassword()
        );

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody RefreshRequest request,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        userService.logout(
                user.getId(),
                request.refreshToken()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();

        userService.logoutAll(
                user.getId()
        );

        return ResponseEntity.noContent().build();
    }
}
