package com.example.sqlix.service.practice;

import com.example.sqlix.dto.request.DatabaseRequest;
import com.example.sqlix.dto.request.ExerciseRequest;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.PracticeExercise;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.PracticeDatabaseRepository;
import com.example.sqlix.repository.PracticeExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminPracticeService {
    private final PracticeDatabaseRepository databaseRepository;
    private final PracticeExerciseRepository exerciseRepository;

    @Transactional
    public PracticeDatabase createDatabase(DatabaseRequest request) {
        if (databaseRepository.existsBySlug(request.slug())) {
            throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
        }
        PracticeDatabase database = new PracticeDatabase();
        applyDatabaseRequest(database, request);
        return databaseRepository.save(database);
    }

    @Transactional
    public PracticeDatabase updateDatabase(UUID id, DatabaseRequest request) {
        PracticeDatabase database = databaseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DATABASE_CATALOG_NOT_FOUND));
        if (databaseRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
        }
        applyDatabaseRequest(database, request);
        return databaseRepository.save(database);
    }

    @Transactional
    public void deleteDatabase(UUID id) {
        PracticeDatabase database = databaseRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DATABASE_CATALOG_NOT_FOUND));
        databaseRepository.delete(database);
    }

    @Transactional
    public PracticeExercise createExercise(UUID databaseId, ExerciseRequest request) {
        PracticeDatabase database = databaseRepository.findByIdForUpdate(databaseId)
                .orElseThrow(() -> new AppException(ErrorCode.DATABASE_CATALOG_NOT_FOUND));
        PracticeExercise exercise = new PracticeExercise();
        exercise.setDatabase(database);
        applyExerciseRequest(exercise, request);
        PracticeExercise savedExercise = exerciseRepository.save(exercise);

        database.setExercisesCount(valueOrZero(database.getExercisesCount()) + 1);
        databaseRepository.save(database);
        return savedExercise;
    }

    @Transactional
    public PracticeExercise updateExercise(UUID exerciseId, ExerciseRequest request) {
        PracticeExercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));
        applyExerciseRequest(exercise, request);
        return exerciseRepository.save(exercise);
    }

    @Transactional
    public void deleteExercise(UUID exerciseId) {
        PracticeExercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new AppException(ErrorCode.EXERCISE_NOT_FOUND));
        PracticeDatabase database = databaseRepository.findByIdForUpdate(exercise.getDatabase().getId())
                .orElseThrow(() -> new AppException(ErrorCode.DATABASE_CATALOG_NOT_FOUND));
        database.setExercisesCount(Math.max(0, valueOrZero(database.getExercisesCount()) - 1));
        exerciseRepository.delete(exercise);
        databaseRepository.save(database);
    }

    private void applyDatabaseRequest(PracticeDatabase database, DatabaseRequest request) {
        database.setName(request.name());
        database.setSlug(request.slug());
        database.setDescription(request.description());
        database.setDifficulty(request.difficulty());
        database.setIconUrl(request.iconUrl());
        database.setSchemaData(request.schemaData());
        database.setInitSqlScript(request.initSqlScript());
        database.setTablesCount(request.tablesCount());
        database.setExercisesCount(request.exercisesCount());
    }

    private void applyExerciseRequest(PracticeExercise exercise, ExerciseRequest request) {
        exercise.setTitle(request.title());
        exercise.setDescription(request.description());
        exercise.setDifficulty(request.difficulty());
        exercise.setTopics(request.topics());
        exercise.setMaxScore(request.maxScore());
        exercise.setRequirements(request.requirements());
        exercise.setExpectedColumns(request.expectedColumns());
        exercise.setSolutionQuery(request.solutionQuery());
        exercise.setSortOrder(request.sortOrder());
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }
}
