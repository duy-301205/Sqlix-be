package com.example.sqlix.controller;

import com.example.sqlix.dto.request.MockTestQuestionRequest;
import com.example.sqlix.dto.request.MockTestRequest;
import com.example.sqlix.entity.MockTest;
import com.example.sqlix.entity.MockTestQuestion;
import com.example.sqlix.service.mocktest.AdminMockTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
class AdminMockTestControllerTest {
    @Mock AdminMockTestService adminMockTestService;
    @InjectMocks AdminMockTestController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createMockTestAndQuestionReturnCreated() throws Exception {
        UUID testId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        MockTest test = new MockTest();
        test.setId(testId);
        MockTestQuestion question = new MockTestQuestion();
        question.setId(questionId);
        when(adminMockTestService.createMockTest(any(MockTestRequest.class))).thenReturn(test);
        when(adminMockTestService.createQuestion(eq(testId), any(MockTestQuestionRequest.class))).thenReturn(question);

        mockMvc.perform(post("/api/admin/mock-tests").contentType("application/json").content(testJson()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result").value(testId.toString()));
        mockMvc.perform(post("/api/admin/mock-tests/{testId}/questions", testId)
                        .contentType("application/json").content(questionJson()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.result").value(questionId.toString()));
    }

    @Test
    void updateAndDeleteMockTestAndQuestionReturnOk() throws Exception {
        UUID testId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        MockTest test = new MockTest();
        test.setId(testId);
        MockTestQuestion question = new MockTestQuestion();
        question.setId(questionId);
        when(adminMockTestService.updateMockTest(eq(testId), any(MockTestRequest.class))).thenReturn(test);
        when(adminMockTestService.updateQuestion(eq(questionId), any(MockTestQuestionRequest.class)))
                .thenReturn(question);

        mockMvc.perform(put("/api/admin/mock-tests/{id}", testId)
                        .contentType("application/json").content(testJson()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value(testId.toString()));
        mockMvc.perform(put("/api/admin/mock-test-questions/{questionId}", questionId)
                        .contentType("application/json").content(questionJson()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value(questionId.toString()));
        mockMvc.perform(delete("/api/admin/mock-tests/{id}", testId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value(true));
        mockMvc.perform(delete("/api/admin/mock-test-questions/{questionId}", questionId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value(true));

        verify(adminMockTestService).deleteMockTest(testId);
        verify(adminMockTestService).deleteQuestion(questionId);
    }

    private String testJson() {
        return """
                {"title":"SQL Basics","slug":"sql-basics","description":"Intro",
                 "difficulty":"EASY","durationMinutes":30,"coverImageUrl":"cover.png"}
                """;
    }

    private String questionJson() {
        return """
                {"questionType":"MULTIPLE_CHOICE","content":"Choose one","points":10,"sortOrder":1,
                 "mcOptions":[{"id":"A","text":"SELECT 1"}],"mcCorrectOption":"A",
                 "explanation":"A is correct"}
                """;
    }
}
