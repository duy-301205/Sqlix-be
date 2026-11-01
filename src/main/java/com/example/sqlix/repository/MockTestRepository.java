package com.example.sqlix.repository;

import com.example.sqlix.entity.MockTest;
import com.example.sqlix.enums.DifficultyLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MockTestRepository extends JpaRepository<MockTest, UUID> {
    Optional<MockTest> findBySlug(String slug);

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
