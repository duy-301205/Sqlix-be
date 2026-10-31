package com.example.sqlix.repository;

import com.example.sqlix.entity.UserRecentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRecentActivityRepository extends JpaRepository<UserRecentActivity, UUID> {
    List<UserRecentActivity> findTop5ByUser_IdOrderByCreatedAtDesc(UUID userId);
}
