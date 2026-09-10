package com.example.sqlix.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile {

    @Id
    @Column(name = "user_id")
    UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    Integer currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    @Builder.Default
    Integer longestStreak = 0;

    @Column(name = "last_active_date")
    LocalDate lastActiveDate;

    @Column(name = "daily_target_minutes", nullable = false)
    @Builder.Default
    Integer dailyTargetMinutes = 15;

    @Column(name = "bio", columnDefinition = "TEXT")
    String bio;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;
}
