package com.example.sqlix.repository;

import com.example.sqlix.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    @Query("""
        SELECT t FROM EmailVerificationToken t
        WHERE t.user.id = :userId
          AND t.usedAt IS NULL
        ORDER BY t.createdAt DESC
    """)
    java.util.List<EmailVerificationToken> findUnusedTokens(
            @Param("userId") UUID userId
    );

    Optional<EmailVerificationToken>
    findTopByUser_IdOrderByCreatedAtDesc(UUID userId);
}
