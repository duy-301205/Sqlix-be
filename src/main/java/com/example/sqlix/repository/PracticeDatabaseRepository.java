package com.example.sqlix.repository;

import com.example.sqlix.entity.PracticeDatabase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PracticeDatabaseRepository extends JpaRepository<PracticeDatabase, UUID> {
    Optional<PracticeDatabase> findBySlug(String slug);
}
