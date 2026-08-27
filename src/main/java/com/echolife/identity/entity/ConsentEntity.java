package com.echolife.identity.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="consents", uniqueConstraints=@UniqueConstraint(columnNames={"user_id","persona_id"}))
public class ConsentEntity {
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id") private UserEntity user;
  @Column(name="persona_id", nullable=false, length=120) private String personaId;
  @Column(nullable=false) private boolean interactiveAllowed;
  @Column(nullable=false) private boolean voiceAllowed;
  @Column(nullable=false) private boolean avatarAllowed;
  @Column(nullable=false) private boolean textAllowed;
  @Column(nullable=false) private boolean withdrawn;
  @Version @Column(nullable=false) private int version;
  @Column(nullable=false) private Instant updatedAt;
  public UUID getId(){return id;}
  public UserEntity getUser(){return user;} public void setUser(UserEntity v){user=v;}
  public String getPersonaId(){return personaId;} public void setPersonaId(String v){personaId=v;}
  public boolean isInteractiveAllowed(){return interactiveAllowed;} public void setInteractiveAllowed(boolean v){interactiveAllowed=v;}
  public boolean isVoiceAllowed(){return voiceAllowed;} public void setVoiceAllowed(boolean v){voiceAllowed=v;}
  public boolean isAvatarAllowed(){return avatarAllowed;} public void setAvatarAllowed(boolean v){avatarAllowed=v;}
  public boolean isTextAllowed(){return textAllowed;} public void setTextAllowed(boolean v){textAllowed=v;}
  public boolean isWithdrawn(){return withdrawn;} public void setWithdrawn(boolean v){withdrawn=v;}
  public int getVersion(){return version;} public void setVersion(int v){version=v;}
  public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
}
