package com.example.sqlix.repository;

import com.example.sqlix.entity.PracticeDatabase;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PracticeDatabaseRepository extends JpaRepository<PracticeDatabase, UUID> {
    Optional<PracticeDatabase> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select database from PracticeDatabase database where database.id = :id")
    Optional<PracticeDatabase> findByIdForUpdate(@Param("id") UUID id);
}
