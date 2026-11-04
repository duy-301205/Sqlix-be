package com.example.sqlix.controller;

import com.example.sqlix.dto.Pagination;
import com.example.sqlix.dto.request.AdminUserRoleRequest;
import com.example.sqlix.dto.request.AdminUserStatusRequest;
import com.example.sqlix.dto.response.AdminUserDto;
import com.example.sqlix.dto.response.ApiResponse;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.service.user.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<List<AdminUserDto>> searchUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserSystemRole role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminUserDto> result = adminUserService.searchUsers(search, role, active, page, size);
        int total = Math.toIntExact(result.getTotalElements());
        int from = total == 0 ? 0 : result.getNumber() * result.getSize() + 1;
        int to = total == 0 ? 0 : from + result.getNumberOfElements() - 1;
        return ApiResponse.<List<AdminUserDto>>builder().code(200).result(result.getContent())
                .pagination(Pagination.builder().total(total).per_page(result.getSize())
                        .current_page(result.getNumber() + 1).last_page(result.getTotalPages())
                        .from(from).to(to).build()).build();
    }

    @PutMapping("/{userId}/status")
    public ApiResponse<AdminUserDto> updateStatus(
            @PathVariable UUID userId, @Valid @RequestBody AdminUserStatusRequest request) {
        return ApiResponse.<AdminUserDto>builder().code(200)
                .result(adminUserService.updateStatus(userId, request)).build();
    }

    @PutMapping("/{userId}/role")
    public ApiResponse<AdminUserDto> updateRole(
            @PathVariable UUID userId, @Valid @RequestBody AdminUserRoleRequest request) {
        return ApiResponse.<AdminUserDto>builder().code(200)
                .result(adminUserService.updateRole(userId, request)).build();
    }
}
