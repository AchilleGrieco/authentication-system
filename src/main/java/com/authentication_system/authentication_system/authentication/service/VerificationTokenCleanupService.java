package com.authentication_system.authentication_system.authentication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.authentication_system.authentication_system.authentication.model.VerificationToken;
import com.authentication_system.authentication_system.authentication.repository.VerificationTokenRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.verification-token.cleanup-enabled", havingValue = "true", matchIfMissing = true)
public class VerificationTokenCleanupService {


    private final VerificationTokenRepository verificationTokenRepository;
    
    @Value("${app.verification-token.expiration-hours:24}")
    private int expirationHours;
    
    @Value("${app.verification-token.max-per-email:3}")
    private int maxTokensPerEmail;

    @Autowired
    public VerificationTokenCleanupService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    /**
     * Scheduled task to remove expired verification tokens
     * Cron expression is configured in application.properties
     */
    @Scheduled(cron = "${app.verification-token.cleanup-cron:0 0 * * * *}")
    @Transactional
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        
        try {
            int deletedTokens = verificationTokenRepository.deleteByExpiryDateBefore(now);
            
            if (deletedTokens > 0) {
            }
        } catch (Exception e) {
        }
    }

    /**
     * Calculate expiration date for new tokens
     */
    public LocalDateTime calculateExpirationDate() {
        return LocalDateTime.now().plusHours(expirationHours);
    }

    /**
     * Check if new token can be generated for an email
     */
    public boolean canGenerateTokenForEmail(String email) {
        LocalDateTime now = LocalDateTime.now();
        List<VerificationToken> activeTokens = verificationTokenRepository
            .findActiveTokensByEmail(email, now);

        if (activeTokens.size() >= maxTokensPerEmail) {
            return false;
        }
        
        return true;
    }

    /**
     * Manual cleanup method
     */
    @Transactional
    public int removeTokensOlderThan(LocalDateTime olderThan) {
        try {
            int deletedTokens = verificationTokenRepository.deleteByExpiryDateBefore(olderThan);
            return deletedTokens;
        } catch (Exception e) {
            return 0;
        }
    }
}
