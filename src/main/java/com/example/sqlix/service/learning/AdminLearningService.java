package com.example.sqlix.service.learning;

import com.example.sqlix.dto.request.CourseCategoryRequest;
import com.example.sqlix.dto.request.LessonAdminRequest;
import com.example.sqlix.dto.request.LessonSectionAdminRequest;
import com.example.sqlix.entity.CourseCategory;
import com.example.sqlix.entity.Lesson;
import com.example.sqlix.entity.LessonSection;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.CourseCategoryRepository;
import com.example.sqlix.repository.LessonRepository;
import com.example.sqlix.repository.LessonSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminLearningService {
    private final CourseCategoryRepository categoryRepository;
    private final LessonRepository lessonRepository;
    private final LessonSectionRepository sectionRepository;

    @Transactional
    public UUID createCategory(CourseCategoryRequest request) {
        CourseCategory category = new CourseCategory();
        applyCategory(category, request);
        return categoryRepository.save(category).getId();
    }

    @Transactional
    public UUID updateCategory(UUID categoryId, CourseCategoryRequest request) {
        CourseCategory category = categoryRepository.findByIdForUpdate(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        applyCategory(category, request);
        return categoryRepository.save(category).getId();
    }

    @Transactional
    public void deleteCategory(UUID categoryId) {
        CourseCategory category = categoryRepository.findByIdForUpdate(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }

    @Transactional
    public UUID createLesson(LessonAdminRequest request) {
        String slug = request.slug().trim();
        if (lessonRepository.existsBySlug(slug)) {
            throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
        }
        Lesson lesson = new Lesson();
        applyLesson(lesson, request);
        return lessonRepository.save(lesson).getId();
    }

    @Transactional
    public UUID updateLesson(UUID lessonId, LessonAdminRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        if (lessonRepository.existsBySlugAndIdNot(request.slug().trim(), lessonId)) {
            throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
        }
        applyLesson(lesson, request);
        return lessonRepository.save(lesson).getId();
    }

    @Transactional
    public void deleteLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        lessonRepository.delete(lesson);
    }

    @Transactional
    public UUID createSection(UUID lessonId, LessonSectionAdminRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        LessonSection section = new LessonSection();
        section.setLesson(lesson);
        applySection(section, request);
        return sectionRepository.save(section).getId();
    }

    @Transactional
    public UUID updateSection(UUID sectionId, LessonSectionAdminRequest request) {
        LessonSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_SECTION_NOT_FOUND));
        applySection(section, request);
        return sectionRepository.save(section).getId();
    }

    @Transactional
    public void deleteSection(UUID sectionId) {
        LessonSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_SECTION_NOT_FOUND));
        sectionRepository.delete(section);
    }

    private void applyCategory(CourseCategory category, CourseCategoryRequest request) {
        category.setName(request.name().trim());
        category.setDescription(request.description());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private void applyLesson(Lesson lesson, LessonAdminRequest request) {
        CourseCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        lesson.setCategory(category);
        lesson.setTitle(request.title().trim());
        lesson.setSlug(request.slug().trim());
        lesson.setDescription(request.description());
        lesson.setObjectives(request.objectives());
        lesson.setPracticeTopicCode(request.practiceTopicCode());
        lesson.setSortOrder(request.sortOrder());
    }

    private void applySection(LessonSection section, LessonSectionAdminRequest request) {
        section.setTitle(request.title().trim());
        section.setContent(request.content());
        section.setSampleSql(request.sampleSql());
        section.setNoteBox(request.noteBox());
        section.setSortOrder(request.sortOrder());
    }
}
