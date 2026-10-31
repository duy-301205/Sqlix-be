package com.example.sqlix.service.learning;

import com.example.sqlix.entity.CourseCategory;
import com.example.sqlix.entity.Lesson;
import com.example.sqlix.entity.LessonSection;
import com.example.sqlix.entity.User;
import com.example.sqlix.entity.UserLessonProgress;
import com.example.sqlix.entity.UserLessonProgressId;
import com.example.sqlix.enums.LessonStatus;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.CourseCategoryRepository;
import com.example.sqlix.repository.LessonRepository;
import com.example.sqlix.repository.UserLessonProgressRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.service.dashboard.DashboardTrackingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LearningServiceTest {
    @Mock CourseCategoryRepository courseCategoryRepository;
    @Mock UserLessonProgressRepository userLessonProgressRepository;
    @Mock LessonRepository lessonRepository;
    @Mock UserRepository userRepository;
    @Mock DashboardTrackingService dashboardTrackingService;
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

    @Test
    void getLessonDetailMapsLessonSectionsAndExistingUserProgress() {
        UUID userId = UUID.randomUUID();
        Lesson lesson = lesson("SELECT", "select", 1);
        lesson.setDescription("Read rows from a table");
        lesson.setObjectives(List.of("Understand SELECT"));
        lesson.setPracticeTopicCode("BASIC_SELECT");

        LessonSection laterSection = section(lesson, "Filtering", 2);
        LessonSection firstSection = section(lesson, "Introduction", 1);
        lesson.setSections(List.of(laterSection, firstSection));

        UserLessonProgress progress = new UserLessonProgress();
        progress.setId(new UserLessonProgressId(userId, lesson.getId()));
        progress.setIsBookmarked(true);
        progress.setStatus(LessonStatus.COMPLETED);

        when(lessonRepository.findBySlug("select")).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId()))
                .thenReturn(Optional.of(progress));

        var response = learningService.getLessonDetail(userId, "select");

        assertThat(response.id()).isEqualTo(lesson.getId());
        assertThat(response.objectives()).containsExactly("Understand SELECT");
        assertThat(response.isBookmarked()).isTrue();
        assertThat(response.status()).isEqualTo(LessonStatus.COMPLETED);
        assertThat(response.sections()).extracting(section -> section.title())
                .containsExactly("Introduction", "Filtering");
        assertThat(response.sections().getFirst().noteBox()).containsEntry("type", "tip");
    }

    @Test
    void getLessonDetailDefaultsProgressWhenUserHasNotStartedLesson() {
        UUID userId = UUID.randomUUID();
        Lesson lesson = lesson("SELECT", "select", 1);
        when(lessonRepository.findBySlug("select")).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId()))
                .thenReturn(Optional.empty());

        var response = learningService.getLessonDetail(userId, "select");

        assertThat(response.isBookmarked()).isFalse();
        assertThat(response.status()).isEqualTo(LessonStatus.NOT_STARTED);
    }

    @Test
    void getLessonDetailThrowsLessonNotFoundForUnknownSlug() {
        UUID userId = UUID.randomUUID();
        when(lessonRepository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> learningService.getLessonDetail(userId, "missing"))
                .isInstanceOfSatisfying(AppException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LESSON_NOT_FOUND));
    }

    @Test
    void toggleBookmarkCreatesProgressWhenItDoesNotExist() {
        UUID userId = UUID.randomUUID();
        Lesson lesson = lesson("SELECT", "select", 1);
        User user = User.builder().id(userId).build();
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId()))
                .thenReturn(Optional.empty());
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        boolean bookmarked = learningService.toggleBookmark(userId, lesson.getId());

        ArgumentCaptor<UserLessonProgress> captor = ArgumentCaptor.forClass(UserLessonProgress.class);
        verify(userLessonProgressRepository).saveAndFlush(captor.capture());
        assertThat(bookmarked).isTrue();
        assertThat(captor.getValue().getId().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getId().getLessonId()).isEqualTo(lesson.getId());
        assertThat(captor.getValue().getIsBookmarked()).isTrue();
        assertThat(captor.getValue().getStatus()).isEqualTo(LessonStatus.NOT_STARTED);
    }

    @Test
    void toggleBookmarkReversesExistingProgress() {
        UUID userId = UUID.randomUUID();
        Lesson lesson = lesson("SELECT", "select", 1);
        UserLessonProgress progress = new UserLessonProgress();
        progress.setId(new UserLessonProgressId(userId, lesson.getId()));
        progress.setIsBookmarked(true);
        progress.setStatus(LessonStatus.COMPLETED);
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId()))
                .thenReturn(Optional.of(progress));

        boolean bookmarked = learningService.toggleBookmark(userId, lesson.getId());

        assertThat(bookmarked).isFalse();
        assertThat(progress.getIsBookmarked()).isFalse();
        assertThat(progress.getStatus()).isEqualTo(LessonStatus.COMPLETED);
        verify(userLessonProgressRepository, times(1)).saveAndFlush(progress);
    }

    @Test
    void markLessonCompletedSavesProgressAndTracksLessonCompletion() {
        UUID userId = UUID.randomUUID();
        Lesson lesson = lesson("SELECT", "select", 1);
        User user = User.builder().id(userId).build();
        when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByUserIdAndLessonId(userId, lesson.getId()))
                .thenReturn(Optional.empty());
        when(userRepository.getReferenceById(userId)).thenReturn(user);

        boolean completed = learningService.markLessonCompleted(userId, lesson.getId());

        ArgumentCaptor<UserLessonProgress> captor = ArgumentCaptor.forClass(UserLessonProgress.class);
        verify(userLessonProgressRepository).saveAndFlush(captor.capture());
        assertThat(completed).isTrue();
        assertThat(captor.getValue().getStatus()).isEqualTo(LessonStatus.COMPLETED);
        assertThat(captor.getValue().getCompletedAt()).isNotNull();
        verify(dashboardTrackingService, times(1))
                .logLessonCompleted(userId, lesson.getId(), "SELECT");
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

    private LessonSection section(Lesson lesson, String title, int sortOrder) {
        LessonSection section = new LessonSection();
        section.setId(UUID.randomUUID());
        section.setLesson(lesson);
        section.setTitle(title);
        section.setContent(title + " content");
        section.setNoteBox(Map.of("type", "tip", "text", "Helpful note"));
        section.setSortOrder(sortOrder);
        return section;
    }
}
