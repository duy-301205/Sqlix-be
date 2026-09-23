package com.example.sqlix.dto.response;

import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.enums.UserSystemRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegisterResponse {
    UUID id;
    String username;
    String email;
    String fullName;
    UserOccupation occupation;
    UserSystemRole systemRole;
    Instant createdAt;
}
