package com.example.sqlix.entity;

import com.example.sqlix.enums.DifficultyLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "practice_databases")
@Getter
@Setter
@NoArgsConstructor
public class PracticeDatabase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "difficulty", nullable = false, columnDefinition = "difficulty_level")
    private DifficultyLevel difficulty = DifficultyLevel.MEDIUM;

    @Column(name = "icon_url", columnDefinition = "text")
    private String iconUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> schemaData;

    @Column(name = "init_sql_script", nullable = false, columnDefinition = "text")
    private String initSqlScript;

    @Column(name = "tables_count", nullable = false)
    private Integer tablesCount = 0;

    @Column(name = "exercises_count", nullable = false)
    private Integer exercisesCount = 0;

    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating = new BigDecimal("5.00");

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "database")
    private List<PracticeExercise> exercises = new ArrayList<>();
}
