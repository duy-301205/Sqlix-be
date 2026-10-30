package com.example.sqlix.dto.response;

import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.enums.UserSystemRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CurrentUserResponse {
    private UUID id;
    @JsonProperty("full_name") private String fullName;
    private String username;
    private String email;
    @JsonProperty("avatar_url") private String avatarUrl;
    private UserSystemRole role;
    private UserOccupation occupation;
    @JsonProperty("current_streak") private Integer currentStreak;
    @JsonProperty("longest_streak") private Integer longestStreak;
    @JsonProperty("daily_target_minutes") private Integer dailyTargetMinutes;
    private String bio;
}
