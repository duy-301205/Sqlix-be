package com.example.sqlix.repository;

import com.example.sqlix.entity.MockTestQuestion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MockTestQuestionRepository extends JpaRepository<MockTestQuestion, UUID> {
    @EntityGraph(attributePaths = "database")
    List<MockTestQuestion> findAllByTest_IdOrderBySortOrderAsc(UUID testId);

    @EntityGraph(attributePaths = "database")
    Optional<MockTestQuestion> findById(UUID id);
}
