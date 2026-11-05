package com.example.sqlix.repository;

import com.example.sqlix.entity.UserMockTestSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserMockTestSummaryRepository extends JpaRepository<UserMockTestSummary, UUID> {}
