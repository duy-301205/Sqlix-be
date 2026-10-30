package com.example.sqlix.service.authentication;

import com.example.sqlix.entity.OAuthAccount;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.OAuthProvider;
import com.example.sqlix.repository.OAuthAccountRepository;
import com.example.sqlix.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OAuthAuthenticationServiceTest {
    @Test void googleFirstLoginCreatesAccountAndSecondLoginUsesExistingLink() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        UserRepository users = mock(UserRepository.class);
        OAuthAccountRepository accounts = mock(OAuthAccountRepository.class);
        RefreshTokenService refresh = mock(RefreshTokenService.class);
        JwtService jwt = mock(JwtService.class);
        OAuthAuthenticationService service = new OAuthAuthenticationService(builder, users, accounts, refresh, jwt);

        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).email("oauth@example.com").username("google_123")
                .emailVerified(true).isActive(true).build();
        server.expect(org.springframework.test.web.client.ExpectedCount.times(2), requestTo("https://www.googleapis.com/oauth2/v3/userinfo"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"sub":"google-sub-123","email":"oauth@example.com","email_verified":true,"name":"OAuth User"}
                        """, org.springframework.http.MediaType.APPLICATION_JSON));
        when(accounts.findByProviderAndProviderUserId(OAuthProvider.GOOGLE, "google-sub-123"))
                .thenReturn(Optional.empty(), Optional.of(OAuthAccount.builder().user(user).build()));
        when(users.findByEmail("oauth@example.com")).thenReturn(Optional.empty());
        when(users.existsByUsername(anyString())).thenReturn(false);
        when(users.saveAndFlush(any(User.class))).thenReturn(user);
        when(refresh.createRefreshToken(user, "agent", "ip")).thenReturn("refresh-1", "refresh-2");
        when(jwt.generateAccessToken(user)).thenReturn("access-1", "access-2");

        var first = service.login(OAuthProvider.GOOGLE, "provider-token", "agent", "ip");
        var second = service.login(OAuthProvider.GOOGLE, "provider-token", "agent", "ip");
        assertEquals(id, first.getUserId());
        assertEquals(id, second.getUserId());
        verify(users, times(1)).saveAndFlush(any(User.class));
        verify(accounts).saveAndFlush(argThat(a -> a.getProvider() == OAuthProvider.GOOGLE
                && a.getProviderUserId().equals("google-sub-123")));
        server.verify();
    }
}
