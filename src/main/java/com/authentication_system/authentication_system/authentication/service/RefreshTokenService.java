package com.authentication_system.authentication_system.authentication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.authentication_system.authentication_system.authentication.model.RefreshToken;
import com.authentication_system.authentication_system.authentication.model.UserEntity;
import com.authentication_system.authentication_system.authentication.repository.RefreshTokenRepository;
import com.authentication_system.authentication_system.authentication.repository.UserRepository;
import com.authentication_system.authentication_system.authentication.security.SecurityConstants;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            
        // Force delete any existing tokens using native query
        refreshTokenRepository.deleteByUserIdNative(userId);
        
        // Create new refresh token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(SecurityConstants.REFRESH_TOKEN_EXPIRATION));
        refreshToken.setToken(UUID.randomUUID().toString());

        try {
            refreshToken = refreshTokenRepository.save(refreshToken);
            return refreshToken;
        } catch (Exception e) {
            throw e;
        }
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
