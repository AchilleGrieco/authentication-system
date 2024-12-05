package com.oauth_login.oauth_login.authentication.repository;

import com.oauth_login.oauth_login.authentication.model.RefreshToken;
import com.oauth_login.oauth_login.authentication.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    
    @Modifying
    int deleteByUser(UserEntity user);
}
