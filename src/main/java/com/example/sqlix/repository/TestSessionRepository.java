package com.example.sqlix.repository;

import com.example.sqlix.entity.TestSession;
import com.example.sqlix.enums.TestSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.sqlix.enums.DifficultyLevel;

import java.util.Optional;
import java.util.UUID;

public interface TestSessionRepository extends JpaRepository<TestSession, UUID> {
    long countByStatus(TestSessionStatus status);


    @Query(value = "select total_tests_taken as \"totalTestsTaken\", average_score as \"averageScore\", highest_score as \"highestScore\", pass_rate_percentage as \"passRatePercentage\" from vw_user_mock_test_summary where user_id = :userId", nativeQuery = true)
    Optional<UserMockTestSummaryProjection> findHistorySummaryByUserId(@Param("userId") UUID userId);

    @Query(value = "select s from TestSession s join fetch s.test t where s.user.id = :userId and s.status in (com.example.sqlix.enums.TestSessionStatus.COMPLETED, com.example.sqlix.enums.TestSessionStatus.TIMEOUT) and (:keyword is null or lower(t.title) like lower(concat('%', :keyword, '%'))) and (:passed is null or s.isPassed = :passed) and (:difficulty is null or t.difficulty = :difficulty)",
           countQuery = "select count(s) from TestSession s join s.test t where s.user.id = :userId and s.status in (com.example.sqlix.enums.TestSessionStatus.COMPLETED, com.example.sqlix.enums.TestSessionStatus.TIMEOUT) and (:keyword is null or lower(t.title) like lower(concat('%', :keyword, '%'))) and (:passed is null or s.isPassed = :passed) and (:difficulty is null or t.difficulty = :difficulty)")
    Page<TestSession> findHistory(@Param("userId") UUID userId, @Param("keyword") String keyword,
            @Param("passed") Boolean passed, @Param("difficulty") DifficultyLevel difficulty, Pageable pageable);

    @Query("select session from TestSession session where session.id = :sessionId and session.user.id = :userId")
    Optional<TestSession> findByIdAndUserId(
            @Param("sessionId") UUID sessionId,
            @Param("userId") UUID userId);
}
