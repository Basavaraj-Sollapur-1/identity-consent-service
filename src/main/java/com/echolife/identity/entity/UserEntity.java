package com.echolife.identity.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
  @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
  @Column(nullable=false) private String name;
  @Column(nullable=false, unique=true) private String email;
  @Column(nullable=false) private String passwordHash;
  @Column(nullable=false) private LocalDate dateOfBirth;
  @Column(nullable=false) private String role;
  @Column(nullable=false) private boolean mfaEnabled;
  @Column(length=512) private String mfaSecret;
  @Column(nullable=false) private boolean guardianApproved;
  @Column(nullable=false) private boolean active;
  @Column(nullable=false) private Instant createdAt;
  @Column(nullable=false, length=20) private String preferredLanguage;
  public UUID getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
  public String getEmail(){return email;} public void setEmail(String v){email=v;}
  public String getPasswordHash(){return passwordHash;} public void setPasswordHash(String v){passwordHash=v;}
  public LocalDate getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(LocalDate v){dateOfBirth=v;}
  public String getRole(){return role;} public void setRole(String v){role=v;}
  public boolean isMfaEnabled(){return mfaEnabled;} public void setMfaEnabled(boolean v){mfaEnabled=v;}
  public String getMfaSecret(){return mfaSecret;} public void setMfaSecret(String v){mfaSecret=v;}
  public boolean isGuardianApproved(){return guardianApproved;} public void setGuardianApproved(boolean v){guardianApproved=v;}
  public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
  public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
  public String getPreferredLanguage(){return preferredLanguage;} public void setPreferredLanguage(String v){preferredLanguage=v;}
}
