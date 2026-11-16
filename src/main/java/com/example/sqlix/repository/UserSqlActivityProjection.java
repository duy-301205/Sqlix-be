package com.example.sqlix.repository;

import java.util.UUID;

public interface UserSqlActivityProjection {
    UUID getUserId();
    long getTotalExercisesSolved();
    long getTestsTaken();
}
