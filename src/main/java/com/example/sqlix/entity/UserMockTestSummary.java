package com.example.sqlix.entity;

import org.hibernate.annotations.Immutable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "vw_user_mock_test_summary")
public class UserMockTestSummary {
    @Id @Column(name = "user_id") private UUID userId;
    @Column(name = "total_tests_taken") private Long totalTestsTaken;
    @Column(name = "average_score") private Double averageScore;
    @Column(name = "highest_score") private Integer highestScore;
    @Column(name = "pass_rate_percentage") private Double passRatePercentage;
    protected UserMockTestSummary() {}
    public UUID getUserId(){return userId;}
    public Long getTotalTestsTaken(){return totalTestsTaken;}
    public Double getAverageScore(){return averageScore;}
    public Integer getHighestScore(){return highestScore;}
    public Double getPassRatePercentage(){return passRatePercentage;}
}
