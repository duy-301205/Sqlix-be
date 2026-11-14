package com.example.sqlix.config;

import com.example.sqlix.enums.DifficultyLevel;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DifficultyLevelConverter implements Converter<String, DifficultyLevel> {
    @Override
    public DifficultyLevel convert(String source) {
        return DifficultyLevel.fromString(source);
    }
}
