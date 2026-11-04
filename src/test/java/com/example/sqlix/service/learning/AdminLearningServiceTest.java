package com.example.sqlix.service.learning;

import com.example.sqlix.dto.request.CourseCategoryRequest;
import com.example.sqlix.dto.request.LessonAdminRequest;
import com.example.sqlix.dto.request.LessonSectionAdminRequest;
import com.example.sqlix.entity.CourseCategory;
import com.example.sqlix.entity.Lesson;
import com.example.sqlix.entity.LessonSection;
import com.example.sqlix.repository.CourseCategoryRepository;
import com.example.sqlix.repository.LessonRepository;
import com.example.sqlix.repository.LessonSectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminLearningServiceTest {
    @Mock CourseCategoryRepository categoryRepository;
    @Mock LessonRepository lessonRepository;
    @Mock LessonSectionRepository sectionRepository;
    @InjectMocks AdminLearningService service;

    @Test
    void createsAndUpdatesCategory() {
        CourseCategory category = new CourseCategory();
        category.setId(UUID.randomUUID());
        when(categoryRepository.save(any(CourseCategory.class))).thenAnswer(invocation -> {
            CourseCategory saved = invocation.getArgument(0);
            saved.setId(category.getId());
            return saved;
        });

        UUID createdId = service.createCategory(new CourseCategoryRequest("Basics", "Intro", 1));
        when(categoryRepository.findByIdForUpdate(createdId)).thenReturn(Optional.of(category));
        UUID updatedId = service.updateCategory(createdId, new CourseCategoryRequest("SQL Basics", null, 2));

        assertThat(updatedId).isEqualTo(createdId);
        assertThat(category.getName()).isEqualTo("SQL Basics");
        assertThat(category.getSortOrder()).isEqualTo(2);
    }

    @Test
    void createsLessonWithCategoryAndJsonObjectives() {
        UUID categoryId = UUID.randomUUID();
        CourseCategory category = new CourseCategory();
        category.setId(categoryId);
        when(lessonRepository.existsBySlug("select-intro")).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(invocation -> {
            Lesson lesson = invocation.getArgument(0);
            lesson.setId(UUID.randomUUID());
            return lesson;
        });

        UUID lessonId = service.createLesson(new LessonAdminRequest(categoryId, "SELECT", "select-intro",
                "Intro", List.of("Read rows"), "BASIC_SELECT", 1));

        assertThat(lessonId).isNotNull();
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void createsUpdatesAndDeletesLessonSection() {
        UUID lessonId = UUID.randomUUID();
        UUID sectionId = UUID.randomUUID();
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        LessonSection section = new LessonSection();
        section.setId(sectionId);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(sectionRepository.save(any(LessonSection.class))).thenAnswer(invocation -> {
            LessonSection saved = invocation.getArgument(0);
            saved.setId(sectionId);
            return saved;
        });
        LessonSectionAdminRequest request = new LessonSectionAdminRequest("Intro", "Content", "SELECT 1",
                Map.of("type", "tip", "text", "Read carefully"), 0);

        UUID createdId = service.createSection(lessonId, request);
        when(sectionRepository.findById(sectionId)).thenReturn(Optional.of(section));
        service.updateSection(sectionId, request);
        service.deleteSection(sectionId);

        assertThat(createdId).isEqualTo(sectionId);
        assertThat(section.getNoteBox()).containsEntry("type", "tip");
        verify(sectionRepository).delete(section);
    }

    @Test
    void deletesCategoryAndLesson() {
        UUID categoryId = UUID.randomUUID();
        CourseCategory category = new CourseCategory();
        category.setId(categoryId);
        when(categoryRepository.findByIdForUpdate(categoryId)).thenReturn(Optional.of(category));
        service.deleteCategory(categoryId);

        UUID lessonId = UUID.randomUUID();
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        service.deleteLesson(lessonId);

        verify(categoryRepository).delete(category);
        verify(lessonRepository).delete(lesson);
    }
}
