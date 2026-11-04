package com.example.sqlix.dto.request;

import com.example.sqlix.enums.UserSystemRole;
import jakarta.validation.constraints.NotNull;

public record AdminUserRoleRequest(@NotNull UserSystemRole role) {
}
