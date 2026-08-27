package com.echolife.identity.service;

import com.echolife.identity.dto.MfaConfirmRequest;
import com.echolife.identity.dto.MfaEnrollResponse;
import com.echolife.identity.entity.UserEntity;
import com.echolife.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@Service
public class MfaEnrollmentService {
    private final UserRepository users;
    private final TotpService totp;
    private final AuthSessionService authSessions;
    private final SecretEncryptionService encryption;

    public MfaEnrollmentService(UserRepository users, TotpService totp, AuthSessionService authSessions, SecretEncryptionService encryption) {
        this.users = users;
        this.totp = totp;
        this.authSessions = authSessions;
        this.encryption = encryption;
    }

    @Transactional
    public MfaEnrollResponse enroll(UUID userId, Jwt jwt) {
        authSessions.requireActive(jwt);
        UserEntity user = users.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        String secret = totp.generateSecret();
        user.setMfaSecret(encryption.encrypt(secret));
        user.setMfaEnabled(false);
        users.save(user);
        return new MfaEnrollResponse(
            "otpauth://totp/EchoLife:" + user.getEmail()
                + "?secret=" + secret + "&issuer=EchoLife"
        );
    }

    @Transactional
    public void confirm(UUID userId, MfaConfirmRequest request, Jwt jwt) {
        authSessions.requireActive(jwt);
        UserEntity user = users.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        String stored = user.getMfaSecret();
        String secret = encryption.isEncrypted(stored) ? encryption.decrypt(stored) : stored;
        if (secret == null || !totp.verify(secret, request.code())) {
            throw new IllegalArgumentException("INVALID_MFA_CODE");
        }
        if (!encryption.isEncrypted(stored)) {
            user.setMfaSecret(encryption.encrypt(secret));
        }
        user.setMfaEnabled(true);
        users.save(user);
    }


}
