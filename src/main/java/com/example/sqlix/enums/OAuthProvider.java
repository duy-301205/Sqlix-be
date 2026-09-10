package com.example.sqlix.enums;

import lombok.Getter;

@Getter
public enum OAuthProvider {
    GOOGLE("google"),
    GITHUB("github");

    private final String value;

    OAuthProvider(String value) {
        this.value = value;
    }
}
