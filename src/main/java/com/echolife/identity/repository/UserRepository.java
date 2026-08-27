package com.echolife.identity.repository;
import com.echolife.identity.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface UserRepository extends JpaRepository<UserEntity,UUID>{ Optional<UserEntity> findByEmailIgnoreCase(String email); }
