package com.example.sqlix.service.learning;

import com.example.sqlix.dto.response.CurriculumCategoryDto;
import com.example.sqlix.dto.response.LessonSummaryDto;
import com.example.sqlix.entity.CourseCategory;
import com.example.sqlix.entity.Lesson;
import com.example.sqlix.entity.UserLessonProgress;
import com.example.sqlix.enums.LessonStatus;
import com.example.sqlix.repository.CourseCategoryRepository;
import com.example.sqlix.repository.UserLessonProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningService {
    private final CourseCategoryRepository courseCategoryRepository;
    private final UserLessonProgressRepository userLessonProgressRepository;

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
