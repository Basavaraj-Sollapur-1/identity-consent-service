package com.echolife.identity.repository;
import com.echolife.identity.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ConsentRepository extends JpaRepository<ConsentEntity,UUID>{ Optional<ConsentEntity> findByUserIdAndPersonaId(UUID userId,String personaId); }
