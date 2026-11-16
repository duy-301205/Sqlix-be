package com.example.sqlix.dto.request;

import com.example.sqlix.enums.UserSystemRole;
import jakarta.validation.constraints.*;

public record AdminUserCreateRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^\\S+$") String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull UserSystemRole role,
        @NotNull Boolean active) {
}
