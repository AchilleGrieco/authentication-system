package com.authentication_system.authentication_system.authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.authentication_system.authentication_system.authentication.model.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String username);

    Optional<UserEntity> findByEmail(String email);
    Boolean existsByEmail(String email);
}
