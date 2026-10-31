package com.example.sqlix.repository;

import com.example.sqlix.entity.UserTopicMastery;
import com.example.sqlix.entity.UserTopicMasteryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTopicMasteryRepository extends JpaRepository<UserTopicMastery, UserTopicMasteryId> {
}
