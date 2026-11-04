package com.example.sqlix.repository;

import com.example.sqlix.entity.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CourseCategoryRepository extends JpaRepository<CourseCategory, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select category from CourseCategory category where category.id = :id")
    java.util.Optional<CourseCategory> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select distinct category
            from CourseCategory category
            left join fetch category.lessons lesson
            order by category.sortOrder asc, lesson.sortOrder asc
            """)
    java.util.List<CourseCategory> findAllWithLessonsOrdered();
}
