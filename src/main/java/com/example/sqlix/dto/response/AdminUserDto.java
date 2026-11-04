package com.example.sqlix.dto.response;

import com.example.sqlix.enums.UserSystemRole;

import java.time.Instant;
import java.util.UUID;

public record AdminUserDto(
        UUID id,
        String username,
        String email,
        String fullName,
        UserSystemRole role,
        boolean active,
        boolean emailVerified,
        Instant createdAt) {
}
