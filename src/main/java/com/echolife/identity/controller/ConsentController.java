package com.echolife.identity.controller;

import com.echolife.identity.dto.ConsentRequest;
import com.echolife.identity.service.AuthSessionService;
import com.echolife.identity.service.ConsentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consents")
public class ConsentController {
    private final ConsentService service;
    private final AuthSessionService authSessions;

    public ConsentController(ConsentService service, AuthSessionService authSessions) {
        this.service = service;
        this.authSessions = authSessions;
    }

    @PostMapping
    public void upsert(Authentication authentication, @Valid @RequestBody ConsentRequest request) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        authSessions.requireActive(jwt);
        service.save(UUID.fromString(jwt.getSubject()), request);
    }
}
