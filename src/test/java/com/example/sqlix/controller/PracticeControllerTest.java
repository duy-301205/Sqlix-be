package com.example.sqlix.controller;

import com.example.sqlix.dto.response.DatabaseCatalogDto;
import com.example.sqlix.dto.response.DatabaseDetailDto;
import com.example.sqlix.dto.response.ExerciseSummaryDto;
import com.example.sqlix.dto.response.QueryResultDto;
import com.example.sqlix.dto.response.SubmissionResultDto;
import com.example.sqlix.dto.response.WorkspaceDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.DifficultyLevel;
import com.example.sqlix.enums.SubmissionStatus;
import com.example.sqlix.service.practice.PracticeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PracticeControllerTest {
    @Mock PracticeService practiceService;
    @InjectMocks PracticeController practiceController;

    private MockMvc mockMvc;
    private UUID userId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(practiceController).build();
        userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("practice-user").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void databaseCatalogReturnsOk() throws Exception {
        UUID databaseId = UUID.randomUUID();
        when(practiceService.getDatabaseCatalog()).thenReturn(List.of(new DatabaseCatalogDto(
                databaseId, "Shop DB", "shop-db", "Shop schema", DifficultyLevel.MEDIUM,
                null, 4, 8, new BigDecimal("4.80"))));

        mockMvc.perform(get("/api/practice/databases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result[0].slug").value("shop-db"));

        verify(practiceService).getDatabaseCatalog();
    }

    @Test
    void databaseDetailReturnsOk() throws Exception {
        UUID databaseId = UUID.randomUUID();
        when(practiceService.getDatabaseDetail(userId, "shop-db")).thenReturn(new DatabaseDetailDto(
                databaseId, "Shop DB", "shop-db", "Shop schema", DifficultyLevel.MEDIUM,
                null, Map.of("tables", List.of("products")), 1, 1, new BigDecimal("4.80"),
                List.of(new ExerciseSummaryDto(UUID.randomUUID(), "Find products", DifficultyLevel.EASY,
                        "SELECT", 100, false))));

        mockMvc.perform(get("/api/practice/databases/shop-db"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.schemaData.tables[0]").value("products"))
                .andExpect(jsonPath("$.result.exercises[0].isPassed").value(false));

        verify(practiceService).getDatabaseDetail(userId, "shop-db");
    }

    @Test
    void exerciseWorkspaceReturnsOk() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        when(practiceService.getWorkspace(userId, exerciseId)).thenReturn(new WorkspaceDto(
                exerciseId, UUID.randomUUID(), "Shop DB", "shop-db", "Find products", "Select products",
                DifficultyLevel.EASY, "SELECT", 100, List.of("Read products"), List.of("id", "name"),
                Map.of("tables", List.of("products")), "SELECT * FROM products;"));

        mockMvc.perform(get("/api/practice/exercises/{exerciseId}", exerciseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.requirements[0]").value("Read products"))
                .andExpect(jsonPath("$.result.lastSubmittedQuery").value("SELECT * FROM products;"));

        verify(practiceService).getWorkspace(userId, exerciseId);
    }

    @Test
    void runQueryReturnsOk() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        when(practiceService.runQuery(exerciseId, "SELECT 1"))
                .thenReturn(new QueryResultDto(List.of("?column?"), List.of(List.of(1)), null));

        mockMvc.perform(post("/api/practice/exercises/{exerciseId}/run", exerciseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"SELECT 1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.columns[0]").value("?column?"))
                .andExpect(jsonPath("$.result.rows[0][0]").value(1));

        verify(practiceService).runQuery(exerciseId, "SELECT 1");
    }

    @Test
    void submitCodeReturnsOk() throws Exception {
        UUID exerciseId = UUID.randomUUID();
        when(practiceService.submitCode(userId, exerciseId, "SELECT 1"))
                .thenReturn(new SubmissionResultDto(SubmissionStatus.PASSED, 100, "Correct",
                        new QueryResultDto(List.of("?column?"), List.of(List.of(1)), null)));

        mockMvc.perform(post("/api/practice/exercises/{exerciseId}/submit", exerciseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\":\"SELECT 1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.status").value("PASSED"))
                .andExpect(jsonPath("$.result.score").value(100));

        verify(practiceService).submitCode(userId, exerciseId, "SELECT 1");
    }
}
