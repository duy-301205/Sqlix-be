package com.example.sqlix.entity;

import com.example.sqlix.enums.TestSessionStatus;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "test_sessions")
@Getter
@Setter
@NoArgsConstructor
public class TestSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_id", nullable = false)
    private MockTest test;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "test_session_status")
    private TestSessionStatus status = TestSessionStatus.IN_PROGRESS;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;

    @Column(name = "duration_seconds", insertable = false, updatable = false)
    private Integer durationSeconds;

    @Column(name = "correct_count", insertable = false, updatable = false)
    private Integer correctCount;

    @Column(name = "total_questions", insertable = false, updatable = false)
    private Integer totalQuestions;

    @Column(name = "is_passed", insertable = false, updatable = false)
    private Boolean isPassed;

    @OneToMany(mappedBy = "session", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<TestSessionAnswer> answers = new ArrayList<>();
}
