package com.echolife.identity.controller;

import com.echolife.identity.dto.SessionAccessRequest;
import com.echolife.identity.dto.SessionAccessResponse;
import com.echolife.identity.service.SessionAccessService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/v1/internal")
public class GovernanceController {
    private final SessionAccessService access;
    private final byte[] serviceKey;

    public GovernanceController(SessionAccessService access, @Value("${echolife.internal.service-key}") String serviceKey) {
        this.access = access;
        this.serviceKey = serviceKey.getBytes(StandardCharsets.UTF_8);
        if (serviceKey.isBlank()) throw new IllegalStateException("echolife.internal.service-key must not be blank");
    }

    @PostMapping("/session-access-check")
    public SessionAccessResponse check(@Valid @RequestBody SessionAccessRequest request,
                                       @RequestHeader(value = "X-Internal-Service-Key", required = false) String suppliedKey) {
        if (suppliedKey == null || !MessageDigest.isEqual(serviceKey, suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new AccessDeniedException("INTERNAL_SERVICE_UNAUTHORIZED");
        }
        return access.check(request);
    }
}
