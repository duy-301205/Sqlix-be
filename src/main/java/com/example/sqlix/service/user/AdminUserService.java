package com.example.sqlix.service.user;

import com.example.sqlix.dto.request.AdminUserRoleRequest;
import com.example.sqlix.dto.request.AdminUserStatusRequest;
import com.example.sqlix.dto.response.AdminUserDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;

        @Transactional(readOnly = true)
    public Page<AdminUserDto> searchUsers(String search, UserSystemRole role, Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("username")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("fullName")), pattern)
                ));
            }
            if (role != null) {
                predicates.add(cb.equal(root.get("systemRole"), role));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("isActive"), active));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable).map(this::toDto);
    }

    @Transactional
    public AdminUserDto updateStatus(UUID userId, AdminUserStatusRequest request) {
        User user = findForUpdate(userId);
        user.setIsActive(request.active());
        return toDto(userRepository.save(user));
    }

    @Transactional
    public AdminUserDto updateRole(UUID userId, AdminUserRoleRequest request) {
        User user = findForUpdate(userId);
        user.setSystemRole(request.role());
        return toDto(userRepository.save(user));
    }

    private User findForUpdate(UUID id) {
        return userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private AdminUserDto toDto(User user) {
        return new AdminUserDto(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                user.getSystemRole(), Boolean.TRUE.equals(user.getIsActive()),
                Boolean.TRUE.equals(user.getEmailVerified()), user.getCreatedAt());
    }
}
