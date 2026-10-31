package com.example.sqlix.repository;

import com.example.sqlix.entity.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CourseCategoryRepository extends JpaRepository<CourseCategory, UUID> {
    @Query("""
            select distinct category
            from CourseCategory category
            left join fetch category.lessons lesson
            order by category.sortOrder asc, lesson.sortOrder asc
            """)
    java.util.List<CourseCategory> findAllWithLessonsOrdered();
}
