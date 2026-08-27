package com.echolife.identity.dto;

public record SessionAccessResponse(
    boolean allowed,
    String reason,
    String userId,
    String role,
    boolean ageEligible,
    boolean consented,
    boolean personaAllowed,
    String[] effectiveModes,
    String[] effectiveChannels,
    int policyVersion
) {}
