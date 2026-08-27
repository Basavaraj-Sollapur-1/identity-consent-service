package com.echolife.identity.dto;

import java.time.Instant;

public record CurrentUserResponse(
    String userId,
    String email,
    String name,
    String role,
    boolean mfaVerified,
    boolean active,
    Instant tokenExpiresAt
) {}
