package com.example.sqlix.service.practice;

import com.example.sqlix.dto.response.DatabaseCatalogDto;
import com.example.sqlix.dto.response.DatabaseDetailDto;
import com.example.sqlix.dto.response.ExerciseSummaryDto;
import com.example.sqlix.dto.response.WorkspaceDto;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.PracticeExercise;
import com.example.sqlix.entity.UserExerciseProgress;
import com.example.sqlix.entity.UserExerciseSubmission;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.PracticeDatabaseRepository;
import com.example.sqlix.repository.PracticeExerciseRepository;
import com.example.sqlix.repository.UserExerciseProgressRepository;
import com.example.sqlix.repository.UserExerciseSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PracticeService {
    private final PracticeDatabaseRepository databaseRepository;
    private final PracticeExerciseRepository exerciseRepository;
    private final UserExerciseProgressRepository progressRepository;
    private final UserExerciseSubmissionRepository submissionRepository;

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
