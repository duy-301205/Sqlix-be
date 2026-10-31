package com.example.sqlix.dto.response;

import com.example.sqlix.enums.SqlTopicCode;

import java.math.BigDecimal;

public record TopicMasteryResponse(SqlTopicCode topicCode, BigDecimal masteryPercentage) {
}
