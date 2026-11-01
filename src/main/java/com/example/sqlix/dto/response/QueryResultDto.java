package com.example.sqlix.dto.response;

import java.util.List;

public record QueryResultDto(
        List<String> columns,
        List<List<Object>> rows,
        String errorMessage) {

    public static QueryResultDto error(String message) {
        return new QueryResultDto(List.of(), List.of(), message);
    }
}
