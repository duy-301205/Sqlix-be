package com.example.sqlix.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_daily_metrics")
@Getter
@Setter
@NoArgsConstructor
public class UserDailyMetric {
    @EmbeddedId
    private UserDailyMetricId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "study_time_seconds", nullable = false)
    private Integer studyTimeSeconds;

    @Column(name = "lessons_completed", nullable = false)
    private Integer lessonsCompleted;

    @Column(name = "exercises_solved", nullable = false)
    private Integer exercisesSolved;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;
}
