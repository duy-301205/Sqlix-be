package com.example.sqlix.service.user;

import com.example.sqlix.dto.request.AdminUserRoleRequest;
import com.example.sqlix.dto.request.AdminUserStatusRequest;
import com.example.sqlix.dto.request.AdminUserCreateRequest;
import com.example.sqlix.dto.request.AdminUserUpdateRequest;
import com.example.sqlix.dto.response.AdminUserDto;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import com.example.sqlix.repository.UserRepository;
import com.example.sqlix.repository.UserSqlActivityProjection;
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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional
    public AdminUserDto createUser(AdminUserCreateRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User user = User.builder().username(username).email(email).fullName(request.fullName().trim())
                .passwordHash(passwordEncoder.encode(request.password())).systemRole(request.role())
                .isActive(request.active()).emailVerified(true).build();
        // The existing PostgreSQL trigger creates the profile for the new account.
        return toDto(saveAndFlush(user));
    }

    @Transactional
    public AdminUserDto updateUser(UUID id, AdminUserUpdateRequest request) {
        User user = findForUpdate(id);
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsernameAndIdNot(username, id) || userRepository.existsByEmailAndIdNot(email, id)) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        user.setFullName(request.fullName().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setSystemRole(request.role());
        user.setIsActive(request.active());
        return toDto(saveAndFlush(user));
    }

    private User saveAndFlush(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (org.springframework.dao.DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.DATA_CONSTRAINT_VIOLATION);
        }
    }

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

        Page<User> users = userRepository.findAll(spec, pageable);
        if (users.isEmpty()) return users.map(user -> toDto(user, null));
        // Fetch activity for the current page in one query; never multiply counts by joining histories.
        var activities = userRepository.findSqlActivityByUserIds(users.map(User::getId).getContent()).stream()
                .collect(java.util.stream.Collectors.toMap(UserSqlActivityProjection::getUserId, activity -> activity));
        return users.map(user -> toDto(user, activities.get(user.getId())));
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

    @Transactional
    public void deleteUser(UUID userId) {
        User user = findForUpdate(userId);
        if (user.getSystemRole() == UserSystemRole.ADMIN) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ADMIN);
        }
        userRepository.delete(user);
    }

    private User findForUpdate(UUID id) {
        return userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private AdminUserDto toDto(User user) {
        var activity = userRepository.findSqlActivityByUserIds(List.of(user.getId())).stream().findFirst().orElse(null);
        return toDto(user, activity);
    }

    private AdminUserDto toDto(User user, UserSqlActivityProjection activity) {
        return new AdminUserDto(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(),
                user.getSystemRole(), Boolean.TRUE.equals(user.getIsActive()),
                Boolean.TRUE.equals(user.getEmailVerified()), user.getCreatedAt(),
                activity == null ? 0 : activity.getTotalExercisesSolved(),
                activity == null ? 0 : activity.getTestsTaken());
    }
}
