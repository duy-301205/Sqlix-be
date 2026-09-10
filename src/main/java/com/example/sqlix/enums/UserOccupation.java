package com.example.sqlix.enums;

import lombok.Getter;

@Getter
public enum UserOccupation {
    STUDENT("student"),
    PROFESSIONAL("professional"),
    OTHER("other");

    private final String value;

    UserOccupation(String value) {
        this.value = value;
    }
}
