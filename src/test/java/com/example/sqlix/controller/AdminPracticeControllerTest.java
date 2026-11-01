package com.example.sqlix.controller;

import com.example.sqlix.dto.request.DatabaseRequest;
import com.example.sqlix.dto.request.ExerciseRequest;
import com.example.sqlix.entity.PracticeDatabase;
import com.example.sqlix.entity.PracticeExercise;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.service.practice.AdminPracticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminPracticeControllerTest {
    @Mock AdminPracticeService adminPracticeService;
    @InjectMocks AdminPracticeController adminPracticeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminPracticeController).build();
    }

    @Test
    void createDatabaseReturnsCreated() throws Exception {
        UUID id = UUID.randomUUID();
        PracticeDatabase database = new PracticeDatabase();
        database.setId(id);
        when(adminPracticeService.createDatabase(any(DatabaseRequest.class))).thenReturn(database);

        mockMvc.perform(post("/api/admin/practice/databases")
                        .contentType("application/json")
                        .content(databaseJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result").value(id.toString()));

        verify(adminPracticeService).createDatabase(any(DatabaseRequest.class));
    }

    @Test
    void updateDatabaseReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        PracticeDatabase database = new PracticeDatabase();
        database.setId(id);
        when(adminPracticeService.updateDatabase(eq(id), any(DatabaseRequest.class))).thenReturn(database);

        mockMvc.perform(put("/api/admin/practice/databases/{id}", id)
                        .contentType("application/json")
                        .content(databaseJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(id.toString()));

        verify(adminPracticeService).updateDatabase(eq(id), any(DatabaseRequest.class));
    }

    @Test
    void deleteDatabaseReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/practice/databases/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));

        verify(adminPracticeService).deleteDatabase(id);
    }

    @Test
    void createExerciseReturnsCreated() throws Exception {
        UUID databaseId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        PracticeExercise exercise = new PracticeExercise();
        exercise.setId(exerciseId);
        when(adminPracticeService.createExercise(eq(databaseId), any(ExerciseRequest.class))).thenReturn(exercise);

        mockMvc.perform(post("/api/admin/practice/databases/{dbId}/exercises", databaseId)
                        .contentType("application/json")
                        .content(exerciseJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.result").value(exerciseId.toString()));

        verify(adminPracticeService).createExercise(eq(databaseId), any(ExerciseRequest.class));
    }

    @Test
    void updateExerciseReturnsOk() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        PracticeExercise exercise = new PracticeExercise();
        exercise.setId(exerciseId);
        when(adminPracticeService.updateExercise(eq(exerciseId), any(ExerciseRequest.class))).thenReturn(exercise);

        mockMvc.perform(put("/api/admin/practice/exercises/{exerciseId}", exerciseId)
                        .contentType("application/json")
                        .content(exerciseJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(exerciseId.toString()));

        verify(adminPracticeService).updateExercise(eq(exerciseId), any(ExerciseRequest.class));
    }

    @Test
    void deleteExerciseReturnsOk() throws Exception {
        UUID exerciseId = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/practice/exercises/{exerciseId}", exerciseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(true));

        verify(adminPracticeService).deleteExercise(exerciseId);
    }

    private String databaseJson() {
        return """
                {"name":"Shop DB","slug":"shop-db","description":"A shop database",
                 "difficulty":"MEDIUM","iconUrl":"shop.png","schemaData":{"tables":["products"]},
                 "initSqlScript":"CREATE TABLE products (id INT);","tablesCount":1,"exercisesCount":0}
                """;
    }

    private String exerciseJson() {
        return """
                {"title":"Find products","description":"Read products","difficulty":"EASY",
                 "topics":"SELECT","maxScore":100,"requirements":["Read products"],
                 "expectedColumns":["id","name"],"solutionQuery":"SELECT id, name FROM products;",
                 "sortOrder":1}
                """;
    }
}
