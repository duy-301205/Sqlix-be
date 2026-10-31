package com.example.sqlix.repository;

import com.example.sqlix.entity.UserTopicMastery;
import com.example.sqlix.entity.UserTopicMasteryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.UUID;

public interface UserTopicMasteryRepository extends JpaRepository<UserTopicMastery, UserTopicMasteryId> {
    @Query("select m from UserTopicMastery m where m.id.userId = :userId")
    List<UserTopicMastery> findAllByUserId(@Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from UserTopicMastery m where m.id = :id")
    java.util.Optional<UserTopicMastery> findByIdForUpdate(@Param("id") UserTopicMasteryId id);
}
