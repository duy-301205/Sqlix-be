package com.example.sqlix.service.learning;

import com.example.sqlix.dto.response.CurriculumCategoryDto;
import com.example.sqlix.dto.response.LessonDetailResponse;
import com.example.sqlix.dto.response.LessonSectionDto;
import com.example.sqlix.dto.response.LessonSummaryDto;
import com.example.sqlix.entity.CourseCategory;
import com.example.sqlix.entity.Lesson;
import com.example.sqlix.entity.LessonSection;
import com.example.sqlix.entity.UserLessonProgress;
import com.example.sqlix.entity.UserLessonProgressId;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.LessonStatus;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.CourseCategoryRepository;
import com.example.sqlix.repository.LessonRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.repository.UserLessonProgressRepository;
import com.example.sqlix.service.dashboard.DashboardTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningService {
    private final CourseCategoryRepository courseCategoryRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final DashboardTrackingService dashboardTrackingService;

    @Transactional(readOnly = true)
    public List<CurriculumCategoryDto> getCurriculumTree(UUID userId) {
        List<CourseCategory> categories = courseCategoryRepository.findAllWithLessonsOrdered();
        Map<UUID, LessonStatus> statusByLessonId = userLessonProgressRepository.findAllByUserId(userId).stream()
                .collect(Collectors.toMap(
                        progress -> progress.getId().getLessonId(),
                        UserLessonProgress::getStatus));

        return categories.stream()
                .map(category -> toCategoryDto(category, statusByLessonId))
                .toList();
    }

    @Transactional(readOnly = true)
    public LessonDetailResponse getLessonDetail(UUID userId, String slug) {
        Lesson lesson = lessonRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
        UserLessonProgress progress = userLessonProgressRepository
                .findByUserIdAndLessonId(userId, lesson.getId())
                .orElse(null);

        List<LessonSectionDto> sections = lesson.getSections().stream()
                .sorted(Comparator.comparing(LessonSection::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(section -> new LessonSectionDto(
                        section.getId(),
                        section.getTitle(),
                        section.getContent(),
                        section.getSampleSql(),
                        section.getNoteBox(),
                        section.getSortOrder() == null ? 0 : section.getSortOrder()))
                .toList();

        return new LessonDetailResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getSlug(),
                lesson.getDescription(),
                lesson.getObjectives(),
                lesson.getPracticeTopicCode(),
                sections,
                progress != null && Boolean.TRUE.equals(progress.getIsBookmarked()),
                progress == null || progress.getStatus() == null
                        ? LessonStatus.NOT_STARTED
                        : progress.getStatus());
    }

    @Transactional
    public boolean toggleBookmark(UUID userId, UUID lessonId) {
        Lesson lesson = findLesson(lessonId);
        UserLessonProgress progress = userLessonProgressRepository
                .findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> newProgress(userId, lesson));

        boolean bookmarked = !Boolean.TRUE.equals(progress.getIsBookmarked());
        progress.setIsBookmarked(bookmarked);
        userLessonProgressRepository.saveAndFlush(progress);
        return bookmarked;
    }

    @Transactional
    public boolean markLessonCompleted(UUID userId, UUID lessonId) {
        Lesson lesson = findLesson(lessonId);
        UserLessonProgress progress = userLessonProgressRepository
                .findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> newProgress(userId, lesson));

        progress.setStatus(LessonStatus.COMPLETED);
        progress.setCompletedAt(Instant.now());
        userLessonProgressRepository.saveAndFlush(progress);
        dashboardTrackingService.logLessonCompleted(userId, lessonId, lesson.getTitle());
        return true;
    }

    private Lesson findLesson(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new AppException(ErrorCode.LESSON_NOT_FOUND));
    }

    private UserLessonProgress newProgress(UUID userId, Lesson lesson) {
        User user = userRepository.getReferenceById(userId);
        UserLessonProgress progress = new UserLessonProgress();
        progress.setId(new UserLessonProgressId(userId, lesson.getId()));
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setIsBookmarked(false);
        progress.setStatus(LessonStatus.NOT_STARTED);
        return progress;
    }

    private CurriculumCategoryDto toCategoryDto(
            CourseCategory category,
            Map<UUID, LessonStatus> statusByLessonId) {
        List<LessonSummaryDto> lessons = category.getLessons().stream()
                .sorted(Comparator.comparing(Lesson::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .map(lesson -> new LessonSummaryDto(
                        lesson.getId(),
                        lesson.getTitle(),
                        lesson.getSlug(),
                        lesson.getSortOrder() == null ? 0 : lesson.getSortOrder(),
                        statusByLessonId.getOrDefault(lesson.getId(), LessonStatus.NOT_STARTED)))
                .toList();

        return new CurriculumCategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder() == null ? 0 : category.getSortOrder(),
                lessons);
    }
}
