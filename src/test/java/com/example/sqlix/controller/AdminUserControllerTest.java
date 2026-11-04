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
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
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
}
