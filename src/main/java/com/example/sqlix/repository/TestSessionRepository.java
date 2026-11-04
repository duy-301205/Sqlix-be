package com.example.sqlix.repository;

import com.example.sqlix.entity.TestSession;
import com.example.sqlix.enums.TestSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TestSessionRepository extends JpaRepository<TestSession, UUID> {
    long countByStatus(TestSessionStatus status);

    @Query("select session from TestSession session where session.id = :sessionId and session.user.id = :userId")
    Optional<TestSession> findByIdAndUserId(
            @Param("sessionId") UUID sessionId,
            @Param("userId") UUID userId);
}
