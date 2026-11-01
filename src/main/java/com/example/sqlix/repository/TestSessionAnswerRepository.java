package com.example.sqlix.repository;

import com.example.sqlix.entity.TestSessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestSessionAnswerRepository extends JpaRepository<TestSessionAnswer, UUID> {
}
