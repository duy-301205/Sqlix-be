package com.example.sqlix.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DifficultyLevel {
    EASY,
    MEDIUM,
    HARD;

    @JsonCreator
    public static DifficultyLevel fromString(String value) {
        if (value == null || value.isBlank()) {
            return MEDIUM;
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "BEGINNER", "EASY", "CO_BAN", "CƠ BẢN", "DỄ", "DE" -> EASY;
            case "INTERMEDIATE", "MEDIUM", "TRUNG_BINH", "TRUNG BÌNH" -> MEDIUM;
            case "ADVANCED", "HARD", "NANG_CAO", "NÂNG CAO", "KHÓ", "KHO" -> HARD;
            default -> {
                try {
                    yield DifficultyLevel.valueOf(normalized);
                } catch (IllegalArgumentException e) {
                    yield MEDIUM;
                }
            }
        };
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
