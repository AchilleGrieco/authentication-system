package com.oauth_login.oauth_login.authentication.repository;

import com.oauth_login.oauth_login.authentication.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    
    List<VerificationToken> findAllByExpiryDateBefore(LocalDateTime date);
    
    /**
     * Delete all verification tokens that have expired before the given date
     * 
     * @param date Cutoff date for token expiration
     * @return Number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiryDate < :date")
    int deleteByExpiryDateBefore(@Param("date") LocalDateTime date);

    /**
     * Find tokens by user email that are not yet expired
     * 
     * @param email User's email
     * @param currentTime Current time to compare against expiry
     * @return List of non-expired verification tokens
     */
    @Query("SELECT t FROM VerificationToken t JOIN t.user u WHERE u.email = :email AND t.expiryDate > :currentTime")
    List<VerificationToken> findActiveTokensByEmail(
        @Param("email") String email, 
        @Param("currentTime") LocalDateTime currentTime
    );
}
