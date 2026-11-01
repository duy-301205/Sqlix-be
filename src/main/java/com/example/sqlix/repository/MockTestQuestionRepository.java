package com.example.sqlix.repository;

import com.example.sqlix.entity.MockTestQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MockTestQuestionRepository extends JpaRepository<MockTestQuestion, UUID> {
    @EntityGraph(attributePaths = "database")
    List<MockTestQuestion> findAllByTest_IdOrderBySortOrderAsc(UUID testId);

    @EntityGraph(attributePaths = "database")
    Optional<MockTestQuestion> findById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select question from MockTestQuestion question where question.id = :id")
    Optional<MockTestQuestion> findForUpdateById(@Param("id") UUID id);
}
