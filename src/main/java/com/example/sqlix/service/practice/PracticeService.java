package com.example.sqlix.service.practice;

import com.example.sqlix.dto.response.DatabaseCatalogDto;
import com.example.sqlix.dto.response.DatabaseDetailDto;
import com.example.sqlix.dto.response.ExerciseSummaryDto;
import com.example.sqlix.dto.response.QueryResultDto;
import com.example.sqlix.dto.response.SubmissionResultDto;
import com.example.sqlix.dto.response.WorkspaceDto;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.PracticeExercise;
import com.example.sqlix.entity.UserExerciseProgress;
import com.example.sqlix.entity.UserExerciseProgressId;
import com.example.sqlix.entity.UserExerciseSubmission;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.SqlTopicCode;
import com.example.sqlix.enums.SubmissionStatus;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.PracticeDatabaseRepository;
import com.example.sqlix.repository.PracticeExerciseRepository;
import com.example.sqlix.repository.UserExerciseProgressRepository;
import com.example.sqlix.repository.UserExerciseSubmissionRepository;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.service.dashboard.DashboardTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class PracticeService {
    private final PracticeDatabaseRepository databaseRepository;
    private final PracticeExerciseRepository exerciseRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final UserExerciseSubmissionRepository submissionRepository;
    private final SqlExecutionService sqlExecutionService;
    private final UserRepository userRepository;
    private final DashboardTrackingService dashboardTrackingService;

    private static final int RUN_RESULT_LIMIT = 50;
    private static final int SUBMISSION_RESULT_LIMIT = 10_001;
    private static final int MAX_GRADED_ROWS = 10_000;

    @Transactional(readOnly = true)
    public List<DatabaseCatalogDto> getDatabaseCatalog() {
        return databaseRepository.findAll().stream()
                .map(this::toCatalogDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public DatabaseDetailDto getDatabaseDetail(UUID userId, String slug) {
        PracticeDatabase database = databaseRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(ErrorCode.DATABASE_CATALOG_NOT_FOUND));
        List<PracticeExercise> exercises = exerciseRepository
                .findAllByDatabase_IdOrderBySortOrderAsc(database.getId());
        Map<UUID, Boolean> passedByExerciseId = loadProgressByExercise(userId, exercises);

        List<ExerciseSummaryDto> exerciseSummaries = exercises.stream()
                .map(exercise -> toExerciseSummary(exercise,
                        passedByExerciseId.getOrDefault(exercise.getId(), false)))
                .toList();

        return new DatabaseDetailDto(
                database.getId(),
                database.getName(),
                database.getSlug(),
                database.getDescription(),
                database.getDifficulty(),
                database.getIconUrl(),
                database.getSchemaData(),
                valueOrZero(database.getTablesCount()),
                valueOrZero(database.getExercisesCount()),
                database.getRating(),
                exerciseSummaries);
    }

    @Transactional(readOnly = true)
    public WorkspaceDto getWorkspace(UUID userId, UUID exerciseId) {
        PracticeExercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));
        PracticeDatabase database = exercise.getDatabase();
        String lastSubmittedQuery = submissionRepository
                .findFirstByUser_IdAndExercise_IdOrderByCreatedAtDesc(userId, exerciseId)
                .map(UserExerciseSubmission::getSubmittedQuery)
                .orElse(null);

        return new WorkspaceDto(
                exercise.getId(),
                database.getId(),
                database.getName(),
                database.getSlug(),
                exercise.getTitle(),
                exercise.getDescription(),
                exercise.getDifficulty(),
                exercise.getTopics(),
                valueOrZero(exercise.getMaxScore()),
                exercise.getRequirements(),
                exercise.getExpectedColumns(),
                database.getSchemaData(),
                lastSubmittedQuery);
    }

    @Transactional(readOnly = true)
    public QueryResultDto runQuery(UUID exerciseId, String query) {
        PracticeExercise exercise = findExercise(exerciseId);
        return sqlExecutionService.executeSelect(
                exercise.getDatabase().getInitSqlScript(), query, RUN_RESULT_LIMIT);
    }

    @Transactional
    public SubmissionResultDto submitCode(UUID userId, UUID exerciseId, String query) {
        PracticeExercise exercise = findExercise(exerciseId);
        String initSqlScript = exercise.getDatabase().getInitSqlScript();
        QueryResultDto actualResult = sqlExecutionService.executeSelect(
                initSqlScript, query, SUBMISSION_RESULT_LIMIT);
        QueryResultDto expectedResult = null;
        SubmissionStatus status;
        String feedback;
        int score = 0;

        if (hasError(actualResult)) {
            status = SubmissionStatus.ERROR;
            feedback = actualResult.errorMessage();
        } else {
            expectedResult = sqlExecutionService.executeSelect(
                    initSqlScript, exercise.getSolutionQuery(), SUBMISSION_RESULT_LIMIT);
            if (hasError(expectedResult)) {
                status = SubmissionStatus.ERROR;
                feedback = "Không thể chạy đáp án chuẩn cho bài tập này.";
            } else if (actualResult.rows().size() > MAX_GRADED_ROWS
                    || expectedResult.rows().size() > MAX_GRADED_ROWS) {
                status = SubmissionStatus.ERROR;
                feedback = "Kết quả quá lớn để chấm tự động.";
            } else {
                feedback = compareResults(actualResult, expectedResult);
                if (feedback == null) {
                    status = SubmissionStatus.PASSED;
                    score = valueOrZero(exercise.getMaxScore());
                    feedback = "Kết quả chính xác.";
                } else {
                    status = SubmissionStatus.FAILED;
                }
            }
        }

        UserExerciseSubmission submission = new UserExerciseSubmission();
        submission.setUser(userRepository.getReferenceById(userId));
        submission.setExercise(exercise);
        submission.setSubmittedQuery(query);
        submission.setScore(score);
        submission.setStatus(status);
        submission.setFeedback(feedback);
        submissionRepository.saveAndFlush(submission);

        UserExerciseProgressId progressId = new UserExerciseProgressId(userId, exerciseId);
        UserExerciseProgress progress = progressRepository.findByIdForUpdate(progressId)
                .orElseGet(() -> newExerciseProgress(userId, exercise));
        int previousHighestScore = valueOrZero(progress.getHighestScore());
        progress.setHighestScore(Math.max(previousHighestScore, score));
        progress.setIsPassed(Boolean.TRUE.equals(progress.getIsPassed()) || status == SubmissionStatus.PASSED);
        progress.setLastAttemptAt(Instant.now());
        progressRepository.saveAndFlush(progress);

        if (status == SubmissionStatus.PASSED || score > previousHighestScore) {
            dashboardTrackingService.logExerciseSolved(
                    userId, toSqlTopicCode(exercise.getTopics()), exerciseId, exercise.getTitle(), score);
        }

        return new SubmissionResultDto(status, score, feedback, actualResult);
    }

    private PracticeExercise findExercise(UUID exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));
    }

    private UserExerciseProgress newExerciseProgress(UUID userId, PracticeExercise exercise) {
        UserExerciseProgress progress = new UserExerciseProgress();
        progress.setId(new UserExerciseProgressId(userId, exercise.getId()));
        progress.setUser(userRepository.getReferenceById(userId));
        progress.setExercise(exercise);
        progress.setHighestScore(0);
        progress.setIsPassed(false);
        return progress;
    }

    private boolean hasError(QueryResultDto result) {
        return result == null || result.errorMessage() != null;
    }

    private String compareResults(QueryResultDto actual, QueryResultDto expected) {
        if (actual.columns().size() != expected.columns().size()) {
            return "Số lượng cột không khớp.";
        }
        if (!actual.columns().equals(expected.columns())) {
            return "Tên hoặc thứ tự cột không khớp.";
        }
        if (actual.rows().size() != expected.rows().size()) {
            return "Số lượng dòng không khớp.";
        }
        if (!rowMultiset(actual.rows()).equals(rowMultiset(expected.rows()))) {
            return "Nội dung dữ liệu trả về không khớp.";
        }
        return null;
    }

    private Map<List<Object>, Integer> rowMultiset(List<List<Object>> rows) {
        Map<List<Object>, Integer> counts = new HashMap<>();
        for (List<Object> row : rows) {
            List<Object> normalized = row.stream().map(this::normalizeValue).toList();
            counts.merge(normalized, 1, Integer::sum);
        }
        return counts;
    }

    private Object normalizeValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.stripTrailingZeros();
        }
        if (value instanceof Number number) {
            try {
                return new BigDecimal(number.toString()).stripTrailingZeros();
            } catch (NumberFormatException ignored) {
                return value;
            }
        }
        return value;
    }

    private SqlTopicCode toSqlTopicCode(String topics) {
        if (topics == null || topics.isBlank()) {
            return null;
        }
        String normalized = topics.toUpperCase(java.util.Locale.ROOT).replace(' ', '_');
        if (normalized.contains("WINDOW")) return SqlTopicCode.WINDOW_FUNCTIONS;
        if (normalized.contains("SUBQUER")) return SqlTopicCode.SUBQUERIES;
        if (normalized.contains("GROUP")) return SqlTopicCode.GROUP_BY;
        if (normalized.contains("JOIN")) return SqlTopicCode.JOINS;
        if (normalized.contains("AGGREGAT") || normalized.contains("COUNT") || normalized.contains("SUM")) {
            return SqlTopicCode.AGGREGATION;
        }
        if (normalized.contains("INDEX")) return SqlTopicCode.INDEXING;
        if (normalized.contains("SELECT") || normalized.contains("WHERE")) return SqlTopicCode.BASIC_SELECT;
        return null;
    }

    private Map<UUID, Boolean> loadProgressByExercise(UUID userId, List<PracticeExercise> exercises) {
        if (exercises.isEmpty()) {
            return Map.of();
        }
        List<UUID> exerciseIds = exercises.stream().map(PracticeExercise::getId).toList();
        return progressRepository.findAllForUserAndExercises(userId, exerciseIds).stream()
                .collect(Collectors.toMap(
                        progress -> progress.getId().getExerciseId(),
                        progress -> Boolean.TRUE.equals(progress.getIsPassed())));
    }

    private DatabaseCatalogDto toCatalogDto(PracticeDatabase database) {
        return new DatabaseCatalogDto(
                database.getId(), database.getName(), database.getSlug(), database.getDescription(),
                database.getDifficulty(), database.getIconUrl(), valueOrZero(database.getTablesCount()),
                valueOrZero(database.getExercisesCount()), database.getRating());
    }

    private ExerciseSummaryDto toExerciseSummary(PracticeExercise exercise, boolean isPassed) {
        return new ExerciseSummaryDto(
                exercise.getId(), exercise.getTitle(), exercise.getDifficulty(), exercise.getTopics(),
                valueOrZero(exercise.getMaxScore()), isPassed);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
