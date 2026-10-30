package com.example.sqlix.repository;

import com.example.sqlix.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshToken r JOIN FETCH r.user WHERE r.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("""
        UPDATE RefreshToken r
        SET r.revokedAt = :now
        WHERE r.user.id = :userId
        AND r.revokedAt IS NULL
    """)
    int revokeAllByUserId(
            @Param("userId") UUID userId,
            @Param("now") Instant now
    );
}
