package com.example.sqlix.repository;

import com.example.sqlix.entity.UserProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from UserProfile p where p.userId = :userId")
    java.util.Optional<UserProfile> findByIdForUpdate(@Param("userId") UUID userId);
}
