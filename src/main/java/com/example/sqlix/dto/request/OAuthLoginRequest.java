package com.example.sqlix.dto.request;

import com.example.sqlix.enums.OAuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OAuthLoginRequest {
    @NotNull
    private OAuthProvider provider;
    @NotBlank
    private String accessToken;
}
