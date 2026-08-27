package com.echolife.identity.repository;

import com.echolife.identity.entity.ConsentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ConsentHistoryRepository extends JpaRepository<ConsentHistoryEntity, UUID> {}
