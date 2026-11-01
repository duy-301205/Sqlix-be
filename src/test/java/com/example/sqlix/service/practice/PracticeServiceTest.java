package com.example.sqlix.service.practice;

import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.PracticeExercise;
import com.example.sqlix.entity.User;
import com.example.sqlix.entity.UserExerciseProgress;
import com.example.sqlix.entity.UserExerciseProgressId;
import com.example.sqlix.entity.UserExerciseSubmission;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.SubmissionStatus;
import com.example.sqlix.dto.response.QueryResultDto;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.service.dashboard.DashboardTrackingService;
import com.example.sqlix.service.practice.SqlExecutionService;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PracticeServiceTest {
    @Mock PracticeDatabaseRepository databaseRepository;
    @Mock PracticeExerciseRepository exerciseRepository;
    @Mock UserExerciseProgressRepository progressRepository;
    @Mock UserExerciseSubmissionRepository submissionRepository;
    @Mock SqlExecutionService sqlExecutionService;
    @Mock UserRepository userRepository;
    @Mock DashboardTrackingService dashboardTrackingService;
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

    @Test
    void submitCodePassesWhenColumnsAndRowsMatchRegardlessOfRowOrder() {
        UUID userId = UUID.randomUUID();
        PracticeDatabase database = database();
        PracticeExercise exercise = exercise(database, "Find products", 1);
        UUID exerciseId = exercise.getId();
        User user = User.builder().id(userId).build();
        QueryResultDto actual = new QueryResultDto(List.of("id", "price"), List.of(
                List.of(2, new BigDecimal("20.0")), List.of(1, new BigDecimal("10.00"))), null);
        QueryResultDto expected = new QueryResultDto(List.of("id", "price"), List.of(
                List.of(1, new BigDecimal("10.0")), List.of(2, new BigDecimal("20.00"))), null);
        UserExerciseProgressId progressId = new UserExerciseProgressId(userId, exerciseId);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(sqlExecutionService.executeSelect(database.getInitSqlScript(), "SELECT id, price FROM products", 10_001))
                .thenReturn(actual);
        when(sqlExecutionService.executeSelect(database.getInitSqlScript(), exercise.getSolutionQuery(), 10_001))
                .thenReturn(expected);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(progressRepository.findByIdForUpdate(progressId)).thenReturn(Optional.empty());

        var result = practiceService.submitCode(userId, exerciseId, "SELECT id, price FROM products");

        assertThat(result.status()).isEqualTo(SubmissionStatus.PASSED);
        assertThat(result.score()).isEqualTo(100);
        assertThat(result.actualResult()).isEqualTo(actual);
        verify(submissionRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(submission ->
                submission.getStatus() == SubmissionStatus.PASSED && submission.getScore() == 100));
        verify(progressRepository).saveAndFlush(org.mockito.ArgumentMatchers.argThat(progress ->
                progress.getHighestScore() == 100 && Boolean.TRUE.equals(progress.getIsPassed())));
        verify(dashboardTrackingService).logExerciseSolved(userId,
                com.example.sqlix.enums.SqlTopicCode.BASIC_SELECT, exerciseId, "Find products", 100);
    }

    @Test
    void submitCodeFailsWhenReturnedRowsDoNotMatch() {
        UUID userId = UUID.randomUUID();
        PracticeDatabase database = database();
        PracticeExercise exercise = exercise(database, "Find products", 1);
        UUID exerciseId = exercise.getId();
        User user = User.builder().id(userId).build();
        QueryResultDto actual = new QueryResultDto(List.of("id"), List.of(List.of(5)), null);
        QueryResultDto expected = new QueryResultDto(List.of("id"), List.of(List.of(1)), null);
        UserExerciseProgressId progressId = new UserExerciseProgressId(userId, exerciseId);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(sqlExecutionService.executeSelect(database.getInitSqlScript(), "SELECT id FROM products", 10_001))
                .thenReturn(actual);
        when(sqlExecutionService.executeSelect(database.getInitSqlScript(), exercise.getSolutionQuery(), 10_001))
                .thenReturn(expected);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(progressRepository.findByIdForUpdate(progressId)).thenReturn(Optional.empty());

        var result = practiceService.submitCode(userId, exerciseId, "SELECT id FROM products");

        assertThat(result.status()).isEqualTo(SubmissionStatus.FAILED);
        assertThat(result.score()).isZero();
        assertThat(result.feedback()).isEqualTo("Nội dung dữ liệu trả về không khớp.");
        verify(dashboardTrackingService, never()).logExerciseSolved(eq(userId),
                org.mockito.ArgumentMatchers.any(), eq(exerciseId), eq("Find products"), eq(0));
    }

    @Test
    void submitCodeReturnsErrorWhenUserQueryHasSqlError() {
        UUID userId = UUID.randomUUID();
        PracticeDatabase database = database();
        PracticeExercise exercise = exercise(database, "Find products", 1);
        UUID exerciseId = exercise.getId();
        User user = User.builder().id(userId).build();
        QueryResultDto actual = QueryResultDto.error("Syntax error near FROM");
        UserExerciseProgressId progressId = new UserExerciseProgressId(userId, exerciseId);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(sqlExecutionService.executeSelect(database.getInitSqlScript(), "SELEC broken", 10_001))
                .thenReturn(actual);
        when(userRepository.getReferenceById(userId)).thenReturn(user);
        when(progressRepository.findByIdForUpdate(progressId)).thenReturn(Optional.empty());

        var result = practiceService.submitCode(userId, exerciseId, "SELEC broken");

        assertThat(result.status()).isEqualTo(SubmissionStatus.ERROR);
        assertThat(result.score()).isZero();
        assertThat(result.feedback()).isEqualTo("Syntax error near FROM");
        verify(sqlExecutionService, never()).executeSelect(database.getInitSqlScript(), exercise.getSolutionQuery(), 10_001);
        verify(dashboardTrackingService, never()).logExerciseSolved(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt());
    }

    private PracticeDatabase database() {
        PracticeDatabase database = new PracticeDatabase();
        database.setId(UUID.randomUUID());
        database.setName("Shop DB");
        database.setSlug("shop-db");
        database.setDifficulty(DifficultyLevel.MEDIUM);
        database.setSchemaData(Map.of("tables", List.of("products")));
        database.setInitSqlScript("CREATE TABLE products (id INT, price NUMERIC);");
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
