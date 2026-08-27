package com.echolife.identity.controller;

import com.echolife.identity.dto.*;
import com.echolife.identity.entity.UserEntity;
import com.echolife.identity.service.AuthService;
import com.echolife.identity.service.AuthSessionService;
import com.echolife.identity.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    private final UserRepository users;
    private final AuthSessionService authSessions;

    public AuthController(AuthService service, UserRepository users, AuthSessionService authSessions) {
        this.service = service;
        this.users = users;
        this.authSessions = authSessions;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest r) {
        service.register(r);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r,
                               jakarta.servlet.http.HttpServletRequest request) {
        return service.login(r, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @PostMapping("/mfa/verify")
    public LoginResponse mfa(@Valid @RequestBody MfaVerifyRequest r,
                             jakarta.servlet.http.HttpServletRequest request) {
        System.out.println("Verify mfa........................................");
        return service.verifyMfa(r, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UserEntity user = users.findById(UUID.fromString(jwt.getSubject()))
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        var session = authSessions.requireActive(jwt);
        return new CurrentUserResponse(
            user.getId().toString(),
            user.getEmail(),
            user.getName(),
            user.getRole(),
            session.isMfaVerified(),
            user.isActive(),
            jwt.getExpiresAt()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authSessions.revoke((Jwt) authentication.getPrincipal());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UserEntity user = users.findById(UUID.fromString(jwt.getSubject()))
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        authSessions.revokeAll(user);
        return ResponseEntity.noContent().build();
    }
}
