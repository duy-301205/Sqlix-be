package com.example.sqlix.repository;

import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.PracticeExercise;
import com.example.sqlix.enums.DifficultyLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PracticeRepositoryTest {
    @Autowired private PracticeDatabaseRepository databaseRepository;
    @Autowired private PracticeExerciseRepository exerciseRepository;

    @Test
    void savesAndReadsPracticeDatabaseSchemaJsonb() {
        PracticeDatabase database = new PracticeDatabase();
        database.setName("Shop Database");
        database.setSlug("shop-db");
        database.setDifficulty(DifficultyLevel.MEDIUM);
        database.setSchemaData(Map.of(
                "tables", List.of(Map.of("name", "products", "columns", List.of("id", "price"))),
                "relationships", List.of()));
        database.setInitSqlScript("CREATE TABLE products (id INT, price NUMERIC);");

        PracticeDatabase saved = databaseRepository.saveAndFlush(database);
        PracticeDatabase reloaded = databaseRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getSchemaData()).isEqualTo(database.getSchemaData());
        assertThat(databaseRepository.findBySlug("shop-db")).contains(reloaded);
    }

    @Test
    void savesAndReadsPracticeExerciseJsonbFields() {
        PracticeDatabase database = new PracticeDatabase();
        database.setName("Shop Database");
        database.setSlug("shop-exercise-db");
        database.setSchemaData(Map.of("tables", List.of("products")));
        database.setInitSqlScript("CREATE TABLE products (id INT);");
        database = databaseRepository.saveAndFlush(database);

        PracticeExercise exercise = new PracticeExercise();
        exercise.setDatabase(database);
        exercise.setTitle("Find products");
        exercise.setDescription("Select product fields");
        exercise.setDifficulty(DifficultyLevel.EASY);
        exercise.setTopics("SELECT");
        exercise.setRequirements(List.of("Read from products", "Return every product"));
        exercise.setExpectedColumns(List.of("id", "name"));
        exercise.setSolutionQuery("SELECT id, name FROM products;");

        PracticeExercise saved = exerciseRepository.saveAndFlush(exercise);
        PracticeExercise reloaded = exerciseRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getRequirements()).containsExactly("Read from products", "Return every product");
        assertThat(reloaded.getExpectedColumns()).containsExactly("id", "name");
        assertThat(reloaded.getDatabase().getId()).isEqualTo(database.getId());
    }
}
