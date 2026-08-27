package com.echolife.identity.service;

import com.echolife.identity.dto.*;
import com.echolife.identity.entity.*;
import com.echolife.identity.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
public class SessionAccessService {
    private final UserRepository users;
    private final ConsentRepository consents;

    @Value("${echolife.age.minimum-session-age:13}")
    private int minimumAge;

    public SessionAccessService(UserRepository users, ConsentRepository consents) {
        this.users = users;
        this.consents = consents;
    }

    public SessionAccessResponse check(SessionAccessRequest request) {
        UUID userId;
        try {
            userId = UUID.fromString(request.userId());
        } catch (IllegalArgumentException e) {
            return denied(request.userId(), "USER_ID_INVALID", null, false, false, false);
        }

        UserEntity user = users.findById(userId).orElse(null);
        if (user == null || !user.isActive()) {
            return denied(request.userId(), "USER_NOT_ACTIVE", user == null ? null : user.getRole(), false, false, false);
        }

        int age = Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();
        if (age < minimumAge) {
            return new SessionAccessResponse(false, "AGE_RESTRICTED", request.userId(), user.getRole(), false, false, false, new String[0], new String[0], 1);
        }

        if ("CHILD".equalsIgnoreCase(user.getRole()) && !user.isGuardianApproved()) {
            return new SessionAccessResponse(false, "GUARDIAN_REQUIRED", request.userId(), user.getRole(), true, false, false, new String[0], new String[0], 1);
        }

        String personaId = request.personaId().trim();
        ConsentEntity consent = consents.findByUserIdAndPersonaId(user.getId(), personaId).orElse(null);
        if (consent == null) {
            return new SessionAccessResponse(false, "CONSENT_DENIED", request.userId(), user.getRole(), true, false, false, new String[0], new String[0], 1);
        }
        if (consent.isWithdrawn() || !consent.isInteractiveAllowed()) {
            return new SessionAccessResponse(false, "CONSENT_DENIED", request.userId(), user.getRole(), true, false, true, new String[0], new String[0], consent.getVersion());
        }

        List<String> channels = new ArrayList<>();
        if (consent.isTextAllowed()) channels.add("TEXT");
        if (consent.isVoiceAllowed()) channels.add("VOICE");
        if (consent.isAvatarAllowed()) channels.add("AVATAR");

        String input = request.inputChannel().toUpperCase(Locale.ROOT);
        if (!channels.contains(input)) {
            return new SessionAccessResponse(false, "INPUT_CHANNEL_NOT_CONSENTED", request.userId(), user.getRole(), true, true, true, modesFor(consent), channels.toArray(String[]::new), consent.getVersion());
        }

        String output = request.outputChannel().toUpperCase(Locale.ROOT);
        if (!channels.contains(output)) {
            return new SessionAccessResponse(false, "OUTPUT_CHANNEL_NOT_CONSENTED", request.userId(), user.getRole(), true, true, true, modesFor(consent), channels.toArray(String[]::new), consent.getVersion());
        }

        String mode = request.mode().toUpperCase(Locale.ROOT);
        String[] effectiveModes = modesFor(consent);
        if (!allowedMode(mode, effectiveModes)) {
            return new SessionAccessResponse(false, "MODE_NOT_ALLOWED", request.userId(), user.getRole(), true, true, true, effectiveModes, channels.toArray(String[]::new), consent.getVersion());
        }

        return new SessionAccessResponse(true, "OK", request.userId(), user.getRole(), true, true, true, effectiveModes, channels.toArray(String[]::new), consent.getVersion());
    }

    private String[] modesFor(ConsentEntity c) {
        if (c.isInteractiveAllowed()) {
            return new String[]{"BLESSING", "STORY", "ADVICE", "CHECK_IN", "REFLECTION"};
        }
        return new String[0];
    }

    private boolean allowedMode(String mode, String[] effectiveModes) {
        return Set.of(effectiveModes).contains(mode);
    }

    private SessionAccessResponse denied(String userId, String reason, String role, boolean age, boolean consent, boolean persona) {
        return new SessionAccessResponse(false, reason, userId, role, age, consent, persona, new String[0], new String[0], 1);
    }
}
