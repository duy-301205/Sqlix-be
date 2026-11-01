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
@Table(name = "user_exercise_progress")
@Getter
@Setter
@NoArgsConstructor
public class UserExerciseProgress {
    @EmbeddedId
    private UserExerciseProgressId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("exerciseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private PracticeExercise exercise;

    @Column(name = "highest_score", nullable = false)
    private Integer highestScore = 0;

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed = false;

    @Column(name = "last_attempt_at", nullable = false)
    private Instant lastAttemptAt;
}
