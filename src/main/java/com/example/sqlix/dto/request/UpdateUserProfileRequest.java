package com.example.sqlix.dto.request;

import com.example.sqlix.enums.UserOccupation;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileRequest {
    @JsonProperty("full_name")
    @NotBlank
    @Size(min = 2, max = 100)
    private String fullName;

    @JsonProperty("avatar_url")
    @Size(max = 2048)
    private String avatarUrl;

    @Size(max = 500)
    private String bio;

    @JsonProperty("daily_target_minutes")
    @NotNull
    @Min(1)
    private Integer dailyTargetMinutes;

    @NotNull
    private UserOccupation occupation;
}
