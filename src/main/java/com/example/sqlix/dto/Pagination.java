package com.example.sqlix.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Pagination {
    Integer total;
    Integer per_page;
    Integer current_page;
    Integer last_page;
    Integer from;
    Integer to;
}
