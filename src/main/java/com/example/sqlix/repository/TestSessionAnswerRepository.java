package com.example.sqlix.repository;

import com.example.sqlix.entity.TestSessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestSessionAnswerRepository extends JpaRepository<TestSessionAnswer, UUID> {
    Optional<TestSessionAnswer> findBySession_IdAndQuestion_Id(UUID sessionId, UUID questionId);

    List<TestSessionAnswer> findAllBySession_Id(UUID sessionId);
}
