package com.example.sqlix.enums;

import lombok.Getter;

@Getter
public enum UserSystemRole {
    USER("user"),
    INSTRUCTOR("instructor"),
    ADMIN("admin");

    private final String value;

    UserSystemRole(String value) {
        this.value = value;
    }
}
