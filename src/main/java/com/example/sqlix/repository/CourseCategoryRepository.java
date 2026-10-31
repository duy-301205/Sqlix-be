package com.example.sqlix.repository;

import com.example.sqlix.entity.CourseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseCategoryRepository extends JpaRepository<CourseCategory, UUID> {
}
