package com.example.sqlix.service.practice;

import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.PracticeExercise;
import com.example.sqlix.entity.UserExerciseProgress;
import com.example.sqlix.entity.UserExerciseProgressId;
import com.example.sqlix.entity.UserExerciseSubmission;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.repository.PracticeDatabaseRepository;
import com.example.sqlix.repository.PracticeExerciseRepository;
import com.example.sqlix.repository.UserExerciseProgressRepository;
import com.example.sqlix.repository.UserExerciseSubmissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PracticeServiceTest {
    @Mock PracticeDatabaseRepository databaseRepository;
    @Mock PracticeExerciseRepository exerciseRepository;
    @Mock UserExerciseProgressRepository progressRepository;
    @Mock UserExerciseSubmissionRepository submissionRepository;
    @InjectMocks PracticeService practiceService;

    @Test
    void mapsDatabaseCatalogWithoutExposingSchemaOrInitSql() {
        PracticeDatabase database = database();
        when(databaseRepository.findAll()).thenReturn(List.of(database));

        var catalog = practiceService.getDatabaseCatalog();

        assertThat(catalog).hasSize(1);
        assertThat(catalog.getFirst().name()).isEqualTo("Shop DB");
        assertThat(catalog.getFirst().difficulty()).isEqualTo(DifficultyLevel.MEDIUM);
        assertThat(catalog.getFirst().tablesCount()).isEqualTo(1);
        assertThat(catalog.getFirst().exercisesCount()).isEqualTo(2);
    }

    @Test
    void mapsDatabaseDetailAndUserExerciseProgress() {
        UUID userId = UUID.randomUUID();
        PracticeDatabase database = database();
        PracticeExercise passed = exercise(database, "Passed task", 1);
        PracticeExercise fresh = exercise(database, "New task", 2);
        when(databaseRepository.findBySlug(database.getSlug())).thenReturn(Optional.of(database));
        when(exerciseRepository.findAllByDatabase_IdOrderBySortOrderAsc(database.getId()))
                .thenReturn(List.of(passed, fresh));
        UserExerciseProgress progress = new UserExerciseProgress();
        progress.setId(new UserExerciseProgressId(userId, passed.getId()));
        progress.setIsPassed(true);
        when(progressRepository.findAllForUserAndExercises(userId, List.of(passed.getId(), fresh.getId())))
                .thenReturn(List.of(progress));

        var detail = practiceService.getDatabaseDetail(userId, database.getSlug());

        assertThat(detail.schemaData()).isEqualTo(database.getSchemaData());
        assertThat(detail.exercises()).extracting(item -> item.isPassed()).containsExactly(true, false);
    }

    @Test
    void mapsWorkspaceAndLatestSubmittedQuery() {
        UUID userId = UUID.randomUUID();
        PracticeDatabase database = database();
        PracticeExercise exercise = exercise(database, "Find products", 1);
        UserExerciseSubmission submission = new UserExerciseSubmission();
        submission.setSubmittedQuery("SELECT * FROM products;");
        when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
        when(submissionRepository.findFirstByUser_IdAndExercise_IdOrderByCreatedAtDesc(userId, exercise.getId()))
                .thenReturn(Optional.of(submission));

        var workspace = practiceService.getWorkspace(userId, exercise.getId());

        assertThat(workspace.exerciseId()).isEqualTo(exercise.getId());
        assertThat(workspace.schemaData()).isEqualTo(database.getSchemaData());
        assertThat(workspace.requirements()).containsExactly("Read products");
        assertThat(workspace.expectedColumns()).containsExactly("id", "name");
        assertThat(workspace.lastSubmittedQuery()).isEqualTo("SELECT * FROM products;");
    }

    private PracticeDatabase database() {
        PracticeDatabase database = new PracticeDatabase();
        database.setId(UUID.randomUUID());
        database.setName("Shop DB");
        database.setSlug("shop-db");
        database.setDifficulty(DifficultyLevel.MEDIUM);
        database.setSchemaData(Map.of("tables", List.of("products")));
        database.setTablesCount(1);
        database.setExercisesCount(2);
        database.setRating(new BigDecimal("4.80"));
        return database;
    }

    private PracticeExercise exercise(PracticeDatabase database, String title, int sortOrder) {
        PracticeExercise exercise = new PracticeExercise();
        exercise.setId(UUID.randomUUID());
        exercise.setDatabase(database);
        exercise.setTitle(title);
        exercise.setDifficulty(DifficultyLevel.EASY);
        exercise.setTopics("SELECT");
        exercise.setMaxScore(100);
        exercise.setRequirements(List.of("Read products"));
        exercise.setExpectedColumns(List.of("id", "name"));
        exercise.setSolutionQuery("SELECT id, name FROM products;");
        exercise.setSortOrder(sortOrder);
        return exercise;
    }
}
