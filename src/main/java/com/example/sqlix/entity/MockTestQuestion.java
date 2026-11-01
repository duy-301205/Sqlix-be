package com.example.sqlix.entity;

import com.example.sqlix.enums.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "mock_test_questions")
@Getter
@Setter
@NoArgsConstructor
public class MockTestQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private MockTest test;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "question_type", nullable = false, columnDefinition = "question_type")
    private QuestionType questionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "database_id")
    private PracticeDatabase database;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "points", nullable = false)
    private Integer points = 100;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "requirements", columnDefinition = "jsonb")
    private List<String> requirements;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_columns", columnDefinition = "jsonb")
    private List<String> expectedColumns;

    @Column(name = "solution_query", columnDefinition = "text")
    private String solutionQuery;

    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mc_options", columnDefinition = "jsonb")
    private List<Map<String, Object>> mcOptions;

    @Column(name = "mc_correct_option", length = 50)
    private String mcCorrectOption;

    @Column(name = "explanation", columnDefinition = "text")
    private String explanation;
}
