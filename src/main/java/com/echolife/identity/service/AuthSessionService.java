package com.echolife.identity.service;

import com.echolife.identity.entity.AuthSessionEntity;
import com.echolife.identity.entity.UserEntity;
import com.echolife.identity.repository.AuthSessionRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthSessionService {
    private final AuthSessionRepository sessions;

    public AuthSessionService(AuthSessionRepository sessions) { this.sessions = sessions; }

    @Transactional
    public void create(UserEntity user, UUID jti, Instant issuedAt, Instant expiresAt, boolean mfaVerified, String ipAddress, String userAgent) {
        AuthSessionEntity session = new AuthSessionEntity();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setJti(jti);
        session.setIssuedAt(issuedAt);
        session.setExpiresAt(expiresAt);
        session.setMfaVerified(mfaVerified);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        sessions.save(session);
    }

    @Transactional(readOnly = true)
    public AuthSessionEntity requireActive(Jwt jwt) {
        UUID jti = parseJti(jwt);
        AuthSessionEntity session = sessions.findByJtiAndRevokedAtIsNull(jti)
            .orElseThrow(() -> new IllegalArgumentException("AUTH_SESSION_NOT_FOUND"));
        Instant now = Instant.now();
        if (!session.getExpiresAt().isAfter(now)) throw new IllegalArgumentException("AUTH_SESSION_EXPIRED");
        if (!session.getUser().isActive()) throw new IllegalArgumentException("USER_NOT_ACTIVE");
        return session;
    }

    @Transactional
    public void revoke(Jwt jwt) {
        UUID jti = parseJti(jwt);
        sessions.findByJti(jti).ifPresent(s -> { s.setRevokedAt(Instant.now()); sessions.save(s); });
    }

    @Transactional
    public void revokeAll(UserEntity user) {
        sessions.findByUserIdAndRevokedAtIsNull(user.getId()).forEach(s -> s.setRevokedAt(Instant.now()));
    }

    @Transactional
    public void cleanupExpired() { sessions.deleteByExpiresAtBefore(Instant.now()); }

    private UUID parseJti(Jwt jwt) {
        try { return UUID.fromString(jwt.getId()); }
        catch (Exception ex) { throw new IllegalArgumentException("TOKEN_SESSION_ID_INVALID", ex); }
    }
}
