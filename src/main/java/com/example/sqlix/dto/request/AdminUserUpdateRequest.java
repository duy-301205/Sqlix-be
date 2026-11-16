package com.example.sqlix.dto.request;

import com.example.sqlix.enums.UserSystemRole;
import jakarta.validation.constraints.*;

public record AdminUserUpdateRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^\\S+$") String username,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull UserSystemRole role,
        @NotNull Boolean active) {
}
