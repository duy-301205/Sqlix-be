package com.example.sqlix.repository;

import com.example.sqlix.entity.CourseCategory;
import com.example.sqlix.entity.Lesson;
import com.example.sqlix.entity.LessonSection;
import com.example.sqlix.entity.User;
import com.example.sqlix.entity.UserLessonProgress;
import com.example.sqlix.entity.UserLessonProgressId;
import com.example.sqlix.enums.LessonStatus;
import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.enums.UserSystemRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class Module3RepositoryTest {
    @Autowired private UserRepository userRepository;
    @Autowired private CourseCategoryRepository courseCategoryRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private LessonSectionRepository lessonSectionRepository;
    @Autowired private UserLessonProgressRepository userLessonProgressRepository;

    private User user;
    private CourseCategory category;
    private Lesson lesson;

    @BeforeEach
    void setUpForeignKeyFixtures() {
        user = User.builder()
                .username("module3_" + UUID.randomUUID().toString().substring(0, 8))
                .email(UUID.randomUUID() + "@example.com")
                .fullName("Module 3 Test")
                .occupation(UserOccupation.OTHER)
                .systemRole(UserSystemRole.USER)
                .build();
        user = userRepository.saveAndFlush(user);

        category = new CourseCategory();
        category.setName("SQL Basics");
        category.setDescription("Introductory SQL course");
        category = courseCategoryRepository.saveAndFlush(category);

        lesson = new Lesson();
        lesson.setCategory(category);
        lesson.setTitle("SELECT basics");
        lesson.setSlug("select-basics-" + UUID.randomUUID());
        lesson.setDescription("Learn to select rows");
        lesson.setObjectives(List.of("Understand SELECT", "Read table rows"));
        lesson.setPracticeTopicCode("BASIC_SELECT");
        lesson = lessonRepository.saveAndFlush(lesson);
    }

    @Test
    void savesAndFindsLessonWithJsonbObjectives() {
        Lesson reloaded = lessonRepository.findById(lesson.getId()).orElseThrow();

        assertThat(reloaded.getObjectives()).containsExactly("Understand SELECT", "Read table rows");
        assertThat(reloaded.getCategory().getId()).isEqualTo(category.getId());
        assertThat(lessonRepository.findBySlug(lesson.getSlug())).contains(reloaded);
    }

    @Test
    void savesAndFindsLessonSectionWithJsonbNoteBox() {
        LessonSection section = new LessonSection();
        section.setLesson(lesson);
        section.setTitle("What is SELECT?");
        section.setContent("SELECT reads data from a table.");
        section.setSampleSql("SELECT * FROM users;");
        section.setNoteBox(Map.of("type", "tip", "text", "Use LIMIT while exploring data."));
        section = lessonSectionRepository.saveAndFlush(section);

        LessonSection reloaded = lessonSectionRepository.findById(section.getId()).orElseThrow();

        assertThat(reloaded.getNoteBox()).isEqualTo(section.getNoteBox());
        assertThat(reloaded.getLesson().getId()).isEqualTo(lesson.getId());
        assertThat(reloaded.getSampleSql()).isEqualTo("SELECT * FROM users;");
    }

    @Test
    void savesAndFindsUserLessonProgressByCompositeKeyAndForeignKeys() {
        UserLessonProgressId id = new UserLessonProgressId(user.getId(), lesson.getId());
        UserLessonProgress progress = new UserLessonProgress();
        progress.setId(id);
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setIsBookmarked(true);
        progress.setStatus(LessonStatus.COMPLETED);
        progress.setCompletedAt(Instant.parse("2026-10-31T10:00:00Z"));
        userLessonProgressRepository.saveAndFlush(progress);

        UserLessonProgress reloaded = userLessonProgressRepository.findById(id).orElseThrow();
        UserLessonProgress foundByForeignKeys = userLessonProgressRepository
                .findByUserIdAndLessonId(user.getId(), lesson.getId()).orElseThrow();

        assertThat(reloaded.getId()).isEqualTo(id);
        assertThat(reloaded.getUser().getId()).isEqualTo(user.getId());
        assertThat(reloaded.getLesson().getId()).isEqualTo(lesson.getId());
        assertThat(reloaded.getIsBookmarked()).isTrue();
        assertThat(reloaded.getStatus()).isEqualTo(LessonStatus.COMPLETED);
        assertThat(foundByForeignKeys.getId()).isEqualTo(id);
    }
}
