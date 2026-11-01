package com.example.sqlix.service.practice;

import com.example.sqlix.dto.request.DatabaseRequest;
import com.example.sqlix.dto.request.ExerciseRequest;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.PracticeExercise;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.repository.PracticeDatabaseRepository;
import com.example.sqlix.repository.PracticeExerciseRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPracticeServiceTest {
    @Mock PracticeDatabaseRepository databaseRepository;
    @Mock PracticeExerciseRepository exerciseRepository;
    @InjectMocks AdminPracticeService adminPracticeService;

    @Test
    void createsDatabaseFromRequest() {
        DatabaseRequest request = databaseRequest("shop-db");
        when(databaseRepository.existsBySlug("shop-db")).thenReturn(false);
        when(databaseRepository.save(org.mockito.ArgumentMatchers.any(PracticeDatabase.class)))
                .thenAnswer(invocation -> {
                    PracticeDatabase saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        PracticeDatabase created = adminPracticeService.createDatabase(request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getSlug()).isEqualTo("shop-db");
        assertThat(created.getSchemaData()).isEqualTo(request.schemaData());
        verify(databaseRepository).existsBySlug("shop-db");
    }

    @Test
    void updatesDatabaseFields() {
        UUID id = UUID.randomUUID();
        PracticeDatabase database = database(id, 3);
        DatabaseRequest request = databaseRequest("shop-db-updated");
        when(databaseRepository.findById(id)).thenReturn(Optional.of(database));
        when(databaseRepository.existsBySlugAndIdNot(request.slug(), id)).thenReturn(false);
        when(databaseRepository.save(database)).thenReturn(database);

        PracticeDatabase updated = adminPracticeService.updateDatabase(id, request);

        assertThat(updated.getSlug()).isEqualTo("shop-db-updated");
        assertThat(updated.getExercisesCount()).isEqualTo(request.exercisesCount());
        verify(databaseRepository).save(database);
    }

    @Test
    void deletesDatabase() {
        UUID id = UUID.randomUUID();
        PracticeDatabase database = database(id, 2);
        when(databaseRepository.findById(id)).thenReturn(Optional.of(database));

        adminPracticeService.deleteDatabase(id);

        verify(databaseRepository).delete(database);
    }

    @Test
    void createsExerciseAndIncrementsDatabaseExerciseCount() {
        UUID databaseId = UUID.randomUUID();
        PracticeDatabase database = database(databaseId, 4);
        ExerciseRequest request = exerciseRequest("New exercise");
        when(databaseRepository.findByIdForUpdate(databaseId)).thenReturn(Optional.of(database));
        when(exerciseRepository.save(org.mockito.ArgumentMatchers.any(PracticeExercise.class)))
                .thenAnswer(invocation -> {
                    PracticeExercise saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        PracticeExercise created = adminPracticeService.createExercise(databaseId, request);

        assertThat(created.getDatabase()).isSameAs(database);
        assertThat(created.getTitle()).isEqualTo("New exercise");
        assertThat(database.getExercisesCount()).isEqualTo(5);
        verify(databaseRepository).save(database);
    }

    @Test
    void updatesExercise() {
        UUID exerciseId = UUID.randomUUID();
        PracticeExercise exercise = new PracticeExercise();
        exercise.setId(exerciseId);
        ExerciseRequest request = exerciseRequest("Updated exercise");
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(exerciseRepository.save(exercise)).thenReturn(exercise);

        PracticeExercise updated = adminPracticeService.updateExercise(exerciseId, request);

        assertThat(updated.getTitle()).isEqualTo("Updated exercise");
        assertThat(updated.getExpectedColumns()).containsExactly("id", "name");
        verify(exerciseRepository).save(exercise);
    }

    @Test
    void deletesExerciseAndDecrementsDatabaseExerciseCount() {
        UUID exerciseId = UUID.randomUUID();
        PracticeDatabase database = database(UUID.randomUUID(), 3);
        PracticeExercise exercise = new PracticeExercise();
        exercise.setId(exerciseId);
        exercise.setDatabase(database);
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise));
        when(databaseRepository.findByIdForUpdate(database.getId())).thenReturn(Optional.of(database));

        adminPracticeService.deleteExercise(exerciseId);

        assertThat(database.getExercisesCount()).isEqualTo(2);
        verify(exerciseRepository).delete(exercise);
        verify(databaseRepository).save(database);
    }

    private DatabaseRequest databaseRequest(String slug) {
        return new DatabaseRequest("Shop DB", slug, "Database description", DifficultyLevel.MEDIUM,
                "shop.png", Map.of("tables", List.of("products")),
                "CREATE TABLE products (id INT);", 1, 2);
    }

    private ExerciseRequest exerciseRequest(String title) {
        return new ExerciseRequest(title, "Exercise description", DifficultyLevel.EASY, "SELECT", 100,
                List.of("Read products"), List.of("id", "name"),
                "SELECT id, name FROM products;", 1);
    }

    private PracticeDatabase database(UUID id, int exercisesCount) {
        PracticeDatabase database = new PracticeDatabase();
        database.setId(id);
        database.setName("Shop DB");
        database.setSlug("shop-db");
        database.setDifficulty(DifficultyLevel.MEDIUM);
        database.setSchemaData(Map.of("tables", List.of("products")));
        database.setInitSqlScript("CREATE TABLE products (id INT);");
        database.setTablesCount(1);
        database.setExercisesCount(exercisesCount);
        database.setRating(new BigDecimal("4.80"));
        return database;
    }
}
