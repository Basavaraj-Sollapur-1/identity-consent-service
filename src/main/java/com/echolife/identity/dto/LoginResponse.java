package com.echolife.identity.dto;

import java.time.Instant;

public record LoginResponse(
    boolean mfaRequired,
    String mfaToken,
    String accessToken,
    String userId,
    String role,
    Instant accessTokenExpiresAt
) {}
