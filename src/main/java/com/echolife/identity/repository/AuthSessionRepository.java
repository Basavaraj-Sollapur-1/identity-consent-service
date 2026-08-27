package com.echolife.identity.repository;

import com.echolife.identity.entity.AuthSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, UUID> {
    Optional<AuthSessionEntity> findByJti(UUID jti);
    Optional<AuthSessionEntity> findByJtiAndRevokedAtIsNull(UUID jti);
    List<AuthSessionEntity> findByUserIdAndRevokedAtIsNull(UUID userId);
    void deleteByExpiresAtBefore(Instant instant);
}
