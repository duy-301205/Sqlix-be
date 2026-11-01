package com.example.sqlix.repository;

import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.QuestionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MockTestRepositoryTest {
    @Autowired private MockTestRepository mockTestRepository;
    @Autowired private MockTestQuestionRepository questionRepository;
    @Autowired private PracticeDatabaseRepository practiceDatabaseRepository;

    @Test
    void persistsMockTestQuestionJsonbFields() {
        PracticeDatabase practiceDatabase = new PracticeDatabase();
        practiceDatabase.setName("Mock test sample DB");
        practiceDatabase.setSlug("mock-test-sample-db");
        practiceDatabase.setSchemaData(Map.of("tables", List.of("products")));
        practiceDatabase.setInitSqlScript("CREATE TABLE products (id INT, name TEXT);");
        practiceDatabase = practiceDatabaseRepository.saveAndFlush(practiceDatabase);

        MockTest test = new MockTest();
        test.setTitle("SQL Basics Mock Test");
        test.setSlug("sql-basics-mock-test");
        test.setDifficulty(DifficultyLevel.EASY);
        test.setDurationMinutes(30);
        test = mockTestRepository.saveAndFlush(test);

        MockTestQuestion question = new MockTestQuestion();
        question.setTest(test);
        question.setQuestionType(QuestionType.SQL_QUERY);
        question.setDatabase(practiceDatabase);
        question.setContent("Return all product names.");
        question.setRequirements(List.of("Read from products", "Return the name column"));
        question.setExpectedColumns(List.of("name"));
        question.setSolutionQuery("SELECT name FROM products;");
        question.setMaxRetries(3);
        question.setMcOptions(List.of(Map.of("id", "A", "text", "SELECT *")));

        MockTestQuestion saved = questionRepository.saveAndFlush(question);
        MockTestQuestion reloaded = questionRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getRequirements()).containsExactly("Read from products", "Return the name column");
        assertThat(reloaded.getExpectedColumns()).containsExactly("name");
        assertThat(reloaded.getMcOptions()).containsExactly(Map.of("id", "A", "text", "SELECT *"));
        assertThat(reloaded.getDatabase().getId()).isEqualTo(practiceDatabase.getId());
        assertThat(reloaded.getQuestionType()).isEqualTo(QuestionType.SQL_QUERY);
    }
}
