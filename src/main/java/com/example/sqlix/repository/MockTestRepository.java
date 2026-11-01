package com.example.sqlix.repository;

import com.example.sqlix.entity.MockTest;
import com.example.sqlix.enums.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;

public interface MockTestRepository extends JpaRepository<MockTest, UUID> {
    Optional<MockTest> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select test from MockTest test where test.id = :id")
    Optional<MockTest> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select test from MockTest test
            where (:search is null or lower(test.title) like lower(concat('%', :search, '%'))
                   or lower(test.slug) like lower(concat('%', :search, '%')))
              and (:difficulty is null or test.difficulty = :difficulty)
            """)
    Page<MockTest> searchCatalog(
            @Param("search") String search,
            @Param("difficulty") DifficultyLevel difficulty,
            Pageable pageable);
}
