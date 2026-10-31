package com.example.sqlix.service.learning;

import com.example.sqlix.entity.CourseCategory;
import com.example.sqlix.entity.Lesson;
import com.example.sqlix.entity.UserLessonProgress;
import com.example.sqlix.entity.UserLessonProgressId;
import com.example.sqlix.enums.LessonStatus;
import com.example.sqlix.repository.CourseCategoryRepository;
import com.example.sqlix.repository.UserLessonProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningServiceTest {
    @Mock CourseCategoryRepository courseCategoryRepository;
    @Mock UserLessonProgressRepository userLessonProgressRepository;
    @InjectMocks LearningService learningService;

    @Test
    void getCurriculumTreeMapsProgressAndSortsLessons() {
        UUID userId = UUID.randomUUID();

        CourseCategory basics = category("SQL Basics", 1);
        Lesson select = lesson("SELECT", "select", 1);
        Lesson where = lesson("WHERE", "where", 2);
        basics.setLessons(List.of(where, select));

        CourseCategory advanced = category("Advanced SQL", 2);
        Lesson joins = lesson("JOIN", "join", 1);
        advanced.setLessons(List.of(joins));

        UserLessonProgress completedProgress = new UserLessonProgress();
        completedProgress.setId(new UserLessonProgressId(userId, select.getId()));
        completedProgress.setStatus(LessonStatus.COMPLETED);

        when(courseCategoryRepository.findAllWithLessonsOrdered()).thenReturn(List.of(basics, advanced));
        when(userLessonProgressRepository.findAllByUserId(userId)).thenReturn(List.of(completedProgress));

        var result = learningService.getCurriculumTree(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("SQL Basics");
        assertThat(result.get(0).lessons())
                .extracting(lesson -> lesson.title())
                .containsExactly("SELECT", "WHERE");
        assertThat(result.get(0).lessons().get(0).status()).isEqualTo(LessonStatus.COMPLETED);
        assertThat(result.get(0).lessons().get(1).status()).isEqualTo(LessonStatus.NOT_STARTED);
        assertThat(result.get(1).lessons().getFirst().status()).isEqualTo(LessonStatus.NOT_STARTED);

        verify(courseCategoryRepository).findAllWithLessonsOrdered();
        verify(userLessonProgressRepository).findAllByUserId(userId);
    }

    private CourseCategory category(String name, int sortOrder) {
        CourseCategory category = new CourseCategory();
        category.setId(UUID.randomUUID());
        category.setName(name);
        category.setSortOrder(sortOrder);
        return category;
    }

    private Lesson lesson(String title, String slug, int sortOrder) {
        Lesson lesson = new Lesson();
        lesson.setId(UUID.randomUUID());
        lesson.setTitle(title);
        lesson.setSlug(slug);
        lesson.setSortOrder(sortOrder);
        return lesson;
    }
}
