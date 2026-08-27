package com.echolife.identity.controller;

import com.echolife.identity.dto.MfaConfirmRequest;
import com.echolife.identity.dto.MfaDisableRequest;
import com.echolife.identity.dto.MfaEnrollResponse;
import com.echolife.identity.service.AuthService;
import com.echolife.identity.service.MfaEnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth/mfa")
public class MfaController {
    private final MfaEnrollmentService service;
    private final AuthService authService;
    public MfaController(MfaEnrollmentService service, AuthService authService){
        this.service=service;
        this.authService = authService;
    }

    @PostMapping("/enroll")
    public MfaEnrollResponse enroll(Authentication authentication){

        Jwt jwt=(Jwt)authentication.getPrincipal();
        return service.enroll(UUID.fromString(jwt.getSubject()), jwt);
    }

    @PostMapping("/confirm")
    public void confirm(Authentication authentication, @Valid @RequestBody MfaConfirmRequest request){
        System.out.println("Confirm mfa.............................");
        Jwt jwt=(Jwt)authentication.getPrincipal();
        service.confirm(UUID.fromString(jwt.getSubject()), request, jwt);
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disableMfa(
            Authentication authentication,
            @Valid @RequestBody MfaDisableRequest request) {
        System.out.println("Disable mfa.............................");

        Jwt jwt = (Jwt) authentication.getPrincipal();

        authService.disableMfa(
                UUID.fromString(jwt.getSubject()),
                request,
                jwt
        );

        return ResponseEntity.ok().build();
    }
}
