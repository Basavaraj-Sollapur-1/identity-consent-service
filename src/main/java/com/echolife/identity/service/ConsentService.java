package com.echolife.identity.service;

import com.echolife.identity.dto.ConsentRequest;
import com.echolife.identity.entity.ConsentEntity;
import com.echolife.identity.entity.UserEntity;
import com.echolife.identity.repository.ConsentRepository;
import com.echolife.identity.repository.ConsentHistoryRepository;
import com.echolife.identity.entity.ConsentHistoryEntity;
import com.echolife.identity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ConsentService {
    private final UserRepository users;
    private final ConsentRepository consents;
    private final ConsentHistoryRepository history;

    public ConsentService(UserRepository users, ConsentRepository consents, ConsentHistoryRepository history) {
        this.users = users;
        this.consents = consents;
        this.history = history;
    }

    @Transactional
    public void save(UUID userId, ConsentRequest request) {
        UserEntity user = users.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));
        String personaId = request.personaId().trim();
        if (personaId.isEmpty()) {
            throw new IllegalArgumentException("PERSONA_ID_REQUIRED");
        }
        ConsentEntity consent = consents.findByUserIdAndPersonaId(userId, personaId)
            .orElseGet(() -> {
                ConsentEntity entity = new ConsentEntity();
                entity.setUser(user);
                entity.setPersonaId(personaId);
                return entity;
            });
        consent.setInteractiveAllowed(request.interactiveAllowed());
        consent.setVoiceAllowed(request.voiceAllowed());
        consent.setAvatarAllowed(request.avatarAllowed());
        consent.setTextAllowed(request.textAllowed());
        consent.setWithdrawn(false);
        consent.setUpdatedAt(Instant.now());
        ConsentEntity saved = consents.saveAndFlush(consent);
        ConsentHistoryEntity snapshot = new ConsentHistoryEntity();
        snapshot.setUserId(userId);
        snapshot.setPersonaId(saved.getPersonaId());
        snapshot.setVersion(saved.getVersion());
        snapshot.setInteractiveAllowed(saved.isInteractiveAllowed());
        snapshot.setVoiceAllowed(saved.isVoiceAllowed());
        snapshot.setAvatarAllowed(saved.isAvatarAllowed());
        snapshot.setTextAllowed(saved.isTextAllowed());
        snapshot.setWithdrawn(saved.isWithdrawn());
        snapshot.setChangedAt(saved.getUpdatedAt());
        history.save(snapshot);
    }
}
