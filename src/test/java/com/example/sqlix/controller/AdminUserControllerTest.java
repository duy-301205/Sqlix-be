package com.example.sqlix.controller;

import com.example.sqlix.dto.response.AdminUserDto;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.service.user.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.exception.GlobalExceptionHandler;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {
    @Mock AdminUserService service;
    @InjectMocks AdminUserController controller;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createsAndEditsUsingPersistedAdminEndpoints() throws Exception {
        UUID id = UUID.randomUUID();
        var saved = new AdminUserDto(id, "newuser", "new@example.com", "New User",
                UserSystemRole.USER, true, true, null, 2, 5);
        when(service.createUser(any())).thenReturn(saved);
        when(service.updateUser(eq(id), any())).thenReturn(saved);
        String body = "{\"username\":\"newuser\",\"email\":\"new@example.com\",\"fullName\":\"New User\",\"password\":\"SamplePass123\",\"role\":\"USER\",\"active\":true}";
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users")
                .contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.id").value(id.toString()));
        mvc.perform(put("/api/admin/users/{userId}", id).contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.testsTaken").value(5));
        verify(service).createUser(any());
        verify(service).updateUser(eq(id), any());
    }

    @Test
    void rejectsInvalidAdminInputs() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/admin/users")
                .contentType("application/json")
                .content("{\"username\":\"newuser\",\"email\":\"bad-email\",\"password\":\"123\",\"fullName\":\"New\",\"role\":\"USER\",\"active\":true}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/admin/users/{id}/role", UUID.randomUUID()).contentType("application/json")
                .content("{\"role\":\"INVALID\"}"))
                .andExpect(status().isBadRequest());
        org.mockito.Mockito.verifyNoInteractions(service);
    }

    @Test
    void listsUsersWithPaginationAndFilters() throws Exception {
        UUID id = UUID.randomUUID();
        AdminUserDto user = new AdminUserDto(id, "learner", "learner@example.com", "Learner",
                UserSystemRole.USER, true, true, null);
        when(service.searchUsers(eq("learner"), eq(UserSystemRole.USER), eq(true), eq(0), eq(20)))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/admin/users").param("search", "learner").param("role", "USER")
                        .param("active", "true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result[0].id").value(id.toString()))
                .andExpect(jsonPath("$.pagination.total").value(1));
    }

    @Test
    void updatesStatusAndRole() throws Exception {
        UUID id = UUID.randomUUID();
        AdminUserDto inactive = new AdminUserDto(id, "learner", "learner@example.com", "Learner",
                UserSystemRole.USER, false, true, null);
        AdminUserDto admin = new AdminUserDto(id, "learner", "learner@example.com", "Learner",
                UserSystemRole.ADMIN, false, true, null);
        when(service.updateStatus(eq(id), any())).thenReturn(inactive);
        when(service.updateRole(eq(id), any())).thenReturn(admin);

        mvc.perform(put("/api/admin/users/{userId}/status", id).contentType("application/json")
                        .content("{\"active\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.active").value(false));
        mvc.perform(put("/api/admin/users/{userId}/role", id).contentType("application/json")
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.result.role").value("ADMIN"));
    }

    @Test void deletePersistsBeforeReportingSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(delete("/api/admin/users/{userId}", id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        verify(service).deleteUser(id);
    }

    @Test void deleteReturnsNotFoundForMissingUser() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AppException(ErrorCode.USER_NOT_FOUND)).when(service).deleteUser(id);
        mvc.perform(delete("/api/admin/users/{userId}", id)).andExpect(status().isNotFound());
    }

    @Test void deleteRejectsAdminAccount() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new AppException(ErrorCode.CANNOT_DELETE_ADMIN)).when(service).deleteUser(id);
        mvc.perform(delete("/api/admin/users/{userId}", id)).andExpect(status().isForbidden());
    }
}
