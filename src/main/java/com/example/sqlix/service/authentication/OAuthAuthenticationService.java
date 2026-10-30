package com.example.sqlix.service.authentication;

import com.example.sqlix.dto.response.LoginResponse;
import com.example.sqlix.entity.OAuthAccount;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.OAuthProvider;
import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.OAuthAccountRepository;
import com.example.sqlix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthAuthenticationService {
    private final RestClient.Builder restClientBuilder;
    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @Transactional
    public LoginResponse login(OAuthProvider provider, String accessToken, String userAgent, String ipAddress) {
        ProviderIdentity identity = verifyProviderToken(provider, accessToken);
        OAuthAccount existingAccount = oauthAccountRepository
                .findByProviderAndProviderUserId(provider, identity.id()).orElse(null);
        User user;
        if (existingAccount != null) {
            user = existingAccount.getUser();
        } else {
            String email = identity.email().trim().toLowerCase(java.util.Locale.ROOT);
            user = userRepository.findByEmail(email).orElseGet(() -> createUser(identity, provider, email));
            try {
                oauthAccountRepository.saveAndFlush(OAuthAccount.builder()
                        .user(user).provider(provider).providerUserId(identity.id()).build());
            } catch (DataIntegrityViolationException ex) {
                throw new AppException(ErrorCode.OAUTH_ACCOUNT_LINKED_OTHER);
            }
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        String accessJwt = jwtService.generateAccessToken(user);
        String refresh = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);
        return LoginResponse.builder().userId(user.getId()).accessToken(accessJwt).refreshToken(refresh).build();
    }

    private ProviderIdentity verifyProviderToken(OAuthProvider provider, String token) {
        try {
            RestClient client = restClientBuilder.build();
            if (provider == OAuthProvider.GOOGLE) {
                Map<?, ?> claims = client.get().uri("https://www.googleapis.com/oauth2/v3/userinfo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token).retrieve().body(Map.class);
                if (claims == null || !Boolean.TRUE.equals(claims.get("email_verified"))) throw new IllegalArgumentException("Unverified Google email");
                return new ProviderIdentity(claims.get("sub").toString(), claims.get("email").toString(),
                        claims.get("name") == null ? claims.get("email").toString() : claims.get("name").toString());
            }

            Map<?, ?> profile = client.get().uri("https://api.github.com/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json").retrieve().body(Map.class);
            List<?> emails = client.get().uri("https://api.github.com/user/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json").retrieve().body(List.class);
            if (profile == null || emails == null) throw new IllegalArgumentException("Missing GitHub identity");
            String email = emails.stream().filter(Map.class::isInstance).map(Map.class::cast)
                    .filter(row -> Boolean.TRUE.equals(row.get("primary")) && Boolean.TRUE.equals(row.get("verified")))
                    .map(row -> row.get("email").toString()).findFirst().orElseThrow(() -> new IllegalArgumentException("No verified GitHub email"));
            Object name = profile.get("name");
            return new ProviderIdentity(profile.get("id").toString(), email,
                    name == null || name.toString().isBlank() ? email : name.toString());
        } catch (Exception ex) {
            throw new AppException(ErrorCode.OAUTH_TOKEN_INVALID);
        }
    }

    private User createUser(ProviderIdentity identity, OAuthProvider provider, String normalizedEmail) {
        String prefix = (provider.name().toLowerCase() + "_" + identity.id()).replaceAll("[^a-zA-Z0-9_]", "_");
        String username = prefix.substring(0, Math.min(prefix.length(), 50));
        if (userRepository.existsByUsername(username)) username = (provider.name().toLowerCase() + "_" + UUID.randomUUID()).substring(0, 50);
        try {
            return userRepository.saveAndFlush(User.builder().username(username).email(normalizedEmail)
                    .passwordHash(null).fullName(identity.name().substring(0, Math.min(100, identity.name().length())))
                    .occupation(UserOccupation.OTHER).systemRole(UserSystemRole.USER).emailVerified(true)
                    .termsAccepted(false).isActive(true).build());
        } catch (DataIntegrityViolationException ex) {
            throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
        }
    }

    private record ProviderIdentity(String id, String email, String name) {}
}
