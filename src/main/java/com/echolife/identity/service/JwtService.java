package com.echolife.identity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final long accessTokenTtlSeconds;
    private final long mfaChallengeTtlSeconds;

    public JwtService(
            JwtEncoder encoder,
            @Value("${echolife.jwt.issuer}") String issuer,
            @Value("${echolife.jwt.access-token-ttl-seconds:900}") long accessTokenTtlSeconds,
            @Value("${echolife.jwt.mfa-challenge-ttl-seconds:300}") long mfaChallengeTtlSeconds) {

        this.encoder = encoder;
        this.issuer = issuer;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.mfaChallengeTtlSeconds = mfaChallengeTtlSeconds;

        // Temporary debugging
        System.out.println("========================================");
        System.out.println("JWT ISSUER = [" + issuer + "]");
        System.out.println("JWT ISSUER LENGTH = " + issuer.length());
        System.out.println("========================================");
    }

    // =========================================================
    // ACCESS TOKEN
    // =========================================================

    public IssuedToken issue(
            UUID userId,
            String role,
            boolean mfaVerified) {

        Instant now = Instant.now();
        Instant expires = now.plusSeconds(accessTokenTtlSeconds);
        UUID jti = UUID.randomUUID();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .id(jti.toString())
                .issuedAt(now)
                .expiresAt(expires)
                .claim("role", role)
                .claim("mfa", mfaVerified)
                .audience(List.of("echolife-session"))
                .build();

        String token = encoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(SignatureAlgorithm.RS256).build(),
                        claims
                )
        ).getTokenValue();

        return new IssuedToken(
                token,
                jti,
                now,
                expires
        );
    }

    // =========================================================
    // MFA CHALLENGE TOKEN
    // =========================================================

    public String issueMfaChallenge(
            UUID userId,
            String role) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(userId.toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(
                        now.plusSeconds(mfaChallengeTtlSeconds)
                )
                .claim("role", role)
                .claim("type", "mfa_challenge")
                .audience(List.of("echolife-identity"))
                .build();

        return encoder.encode(
                JwtEncoderParameters.from(
                        JwsHeader.with(SignatureAlgorithm.RS256).build(),
                        claims
                )
        ).getTokenValue();
    }

    // =========================================================
    // ISSUED TOKEN RECORD
    // =========================================================

    public record IssuedToken(
            String value,
            UUID jti,
            Instant issuedAt,
            Instant expiresAt
    ) {
    }
}