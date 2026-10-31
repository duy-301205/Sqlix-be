package com.example.sqlix.repository;

import com.example.sqlix.entity.UserDailyMetric;
import com.example.sqlix.entity.UserDailyMetricId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDailyMetricRepository extends JpaRepository<UserDailyMetric, UserDailyMetricId> {
}
