package com.echolife.identity.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consent_history", indexes = {
    @Index(name = "idx_consent_history_user_persona", columnList = "user_id,persona_id,changed_at")
})
public class ConsentHistoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name="user_id", nullable=false) private UUID userId;
    @Column(name="persona_id", nullable=false, length=120) private String personaId;
    @Column(name="version", nullable=false) private int version;
    @Column(name="interactive_allowed", nullable=false) private boolean interactiveAllowed;
    @Column(name="voice_allowed", nullable=false) private boolean voiceAllowed;
    @Column(name="avatar_allowed", nullable=false) private boolean avatarAllowed;
    @Column(name="text_allowed", nullable=false) private boolean textAllowed;
    @Column(name="withdrawn", nullable=false) private boolean withdrawn;
    @Column(name="changed_at", nullable=false) private Instant changedAt;

    public UUID getId(){return id;}
    public UUID getUserId(){return userId;} public void setUserId(UUID v){userId=v;}
    public String getPersonaId(){return personaId;} public void setPersonaId(String v){personaId=v;}
    public int getVersion(){return version;} public void setVersion(int v){version=v;}
    public boolean isInteractiveAllowed(){return interactiveAllowed;} public void setInteractiveAllowed(boolean v){interactiveAllowed=v;}
    public boolean isVoiceAllowed(){return voiceAllowed;} public void setVoiceAllowed(boolean v){voiceAllowed=v;}
    public boolean isAvatarAllowed(){return avatarAllowed;} public void setAvatarAllowed(boolean v){avatarAllowed=v;}
    public boolean isTextAllowed(){return textAllowed;} public void setTextAllowed(boolean v){textAllowed=v;}
    public boolean isWithdrawn(){return withdrawn;} public void setWithdrawn(boolean v){withdrawn=v;}
    public Instant getChangedAt(){return changedAt;} public void setChangedAt(Instant v){changedAt=v;}
}
