package com.oauth_login.oauth_login.authentication.repository;

import com.oauth_login.oauth_login.authentication.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String username);

    Optional<UserEntity> findByEmail(String email);
    Boolean existsByEmail(String email);
}
