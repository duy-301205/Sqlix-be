package com.example.sqlix.repository;

import com.example.sqlix.entity.OAuthAccount;
import com.example.sqlix.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, UUID> {
    Optional<OAuthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);
    boolean existsByUser_IdAndProvider(UUID userId, OAuthProvider provider);
}
