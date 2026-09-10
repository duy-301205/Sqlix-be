package com.example.sqlix.dto.response;

import com.example.sqlix.dto.Pagination;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    int code = 0;
    String message;
    T result;
    String[] _block;
    Pagination pagination;

    @Builder.Default
    Integer cursor_pagination = null;

    @Builder.Default
    Integer key = null;
}
