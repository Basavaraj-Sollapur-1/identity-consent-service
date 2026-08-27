package com.echolife.identity.service;

import com.echolife.identity.dto.*;
import com.echolife.identity.entity.UserEntity;
import com.echolife.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final TotpService totp;
    private final JwtDecoder decoder;
    private final AuthSessionService authSessions;
    private final LoginRateLimitService rateLimit;
    private final SecretEncryptionService encryption;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt, TotpService totp,
                       @Qualifier("mfaChallengeJwtDecoder") JwtDecoder decoder, AuthSessionService authSessions, LoginRateLimitService rateLimit, SecretEncryptionService encryption) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.totp = totp;
        this.decoder = decoder;
        this.authSessions = authSessions;
        this.rateLimit = rateLimit;
        this.encryption = encryption;
    }

    public void register(RegisterRequest r) {
        String email = normalizeEmail(r.email());
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("EMAIL_ALREADY_EXISTS");
        }
        if (r.dateOfBirth().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("DATE_OF_BIRTH_INVALID");
        }

        UserEntity u = new UserEntity();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setDateOfBirth(r.dateOfBirth());
        u.setPreferredLanguage(r.preferredLanguage().trim());
        u.setRole("FAMILY_MEMBER");
        u.setMfaEnabled(false);
        u.setGuardianApproved(false);
        u.setActive(true);
        u.setCreatedAt(Instant.now());
        try {
            users.save(u);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("EMAIL_ALREADY_EXISTS", ex);
        }
    }

    public LoginResponse login(LoginRequest r, String ipAddress, String userAgent) {
        String email = normalizeEmail(r.email());
        rateLimit.check(email);

        UserEntity user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !user.isActive() || !encoder.matches(r.password(), user.getPasswordHash())) {
            rateLimit.recordFailure(email);
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }

        rateLimit.clear(email);

        if (user.isMfaEnabled()) {
            String challenge = jwt.issueMfaChallenge(user.getId(), user.getRole());
            return new LoginResponse(true, challenge, null, user.getId().toString(), user.getRole(), null);
        }

        JwtService.IssuedToken issued = jwt.issue(user.getId(), user.getRole(), true);
        authSessions.create(user, issued.jti(), issued.issuedAt(), issued.expiresAt(), true, ipAddress, userAgent);
        return new LoginResponse(false, null, issued.value(), user.getId().toString(), user.getRole(), issued.expiresAt());
    }

    public LoginResponse verifyMfa(MfaVerifyRequest r, String ipAddress, String userAgent) {
        final Jwt challenge;
        try {
            challenge = decoder.decode(r.mfaToken());
        } catch (org.springframework.security.oauth2.jwt.JwtException ex) {
            throw new IllegalArgumentException("INVALID_MFA_CHALLENGE", ex);
        }
        if (!"mfa_challenge".equals(challenge.getClaimAsString("type"))) {
            throw new IllegalArgumentException("INVALID_MFA_CHALLENGE");
        }
        UUID id;
        try { id = UUID.fromString(challenge.getSubject()); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("INVALID_MFA_CHALLENGE", ex); }
        String rateKey = id.toString();
        rateLimit.checkMfa(rateKey, ipAddress);
        UserEntity user = users.findById(id).orElseThrow(() -> new IllegalArgumentException("MFA_USER_NOT_FOUND"));
        if (!user.isActive() || !user.isMfaEnabled()) {
            rateLimit.recordMfaFailure(rateKey, ipAddress);
            throw new IllegalArgumentException("INVALID_MFA_CODE");
        }
        String stored = user.getMfaSecret();
        String secret;
        try {
            secret = encryption.isEncrypted(stored) ? encryption.decrypt(stored) : stored;
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException("INVALID_MFA_SECRET", ex);
        }
        if (!totp.verify(secret, r.code())) {
            rateLimit.recordMfaFailure(rateKey, ipAddress);
            throw new IllegalArgumentException("INVALID_MFA_CODE");
        }
        if (!rateLimit.consumeMfaChallenge(challenge.getId())) {
            throw new IllegalArgumentException("MFA_CHALLENGE_ALREADY_USED");
        }
        rateLimit.clearMfa(rateKey, ipAddress);
        if (stored != null && !encryption.isEncrypted(stored)) {
            user.setMfaSecret(encryption.encrypt(secret));
            users.save(user);
        }
        JwtService.IssuedToken issued = jwt.issue(user.getId(), user.getRole(), true);
        authSessions.create(user, issued.jti(), issued.issuedAt(), issued.expiresAt(), true, ipAddress, userAgent);
        return new LoginResponse(false, null, issued.value(), user.getId().toString(), user.getRole(), issued.expiresAt());
    }


    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
    public void disableMfa(
            UUID userId,
            MfaDisableRequest request,
            Jwt jwt) {

        // User must have a valid authenticated session
        authSessions.requireActive(jwt);

        UserEntity user = users.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("USER_NOT_FOUND"));

        // MFA must currently be enabled
        if (!user.isMfaEnabled()) {
            throw new IllegalArgumentException("MFA_NOT_ENABLED");
        }

        // Verify current password
        if (!encoder.matches(
                request.password(),
                user.getPasswordHash())) {

            throw new IllegalArgumentException("INVALID_PASSWORD");
        }

        // Read encrypted MFA secret
        String storedSecret = user.getMfaSecret();

        if (storedSecret == null || storedSecret.isBlank()) {
            throw new IllegalArgumentException("MFA_SECRET_NOT_FOUND");
        }

        String secret;

        try {
            secret = encryption.isEncrypted(storedSecret)
                    ? encryption.decrypt(storedSecret)
                    : storedSecret;
        } catch (IllegalStateException ex) {
            throw new IllegalArgumentException("INVALID_MFA_SECRET");
        }

        // Verify current TOTP code
        if (!totp.verify(secret, request.code())) {
            throw new IllegalArgumentException("INVALID_MFA_CODE");
        }

        // Disable MFA
        user.setMfaEnabled(false);

        // Remove the TOTP secret
        user.setMfaSecret(null);

        users.save(user);
    }
}
