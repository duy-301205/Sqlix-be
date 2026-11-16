package com.example.sqlix.service.user;

import com.example.sqlix.dto.request.AdminUserRoleRequest;
import com.example.sqlix.dto.request.AdminUserStatusRequest;
import com.example.sqlix.entity.User;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {
    @Mock UserRepository userRepository;
    @Mock org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @InjectMocks AdminUserService service;

    @Test
    void createPersistsAnEncodedPasswordAndSelectedRole() {
        when(passwordEncoder.encode("SamplePass123")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        var result = service.createUser(new com.example.sqlix.dto.request.AdminUserCreateRequest(
                " New User ", "newuser", "NEW@EXAMPLE.COM", "SamplePass123", UserSystemRole.INSTRUCTOR, true));
        var capture = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(capture.capture());
        assertThat(capture.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(capture.getValue().getEmailVerified()).isTrue();
        assertThat(result.fullName()).isEqualTo("New User");
        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.role()).isEqualTo(UserSystemRole.INSTRUCTOR);
    }

    @Test
    void editPersistsAllFieldsAndPreservesPassword() {
        User user = user(UUID.randomUUID());
        user.setPasswordHash("existing-hash");
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        var result = service.updateUser(user.getId(), new com.example.sqlix.dto.request.AdminUserUpdateRequest(
                "Edited User", "edited", "edited@example.com", UserSystemRole.INSTRUCTOR, false));
        verify(userRepository).saveAndFlush(user);
        assertThat(result.fullName()).isEqualTo("Edited User");
        assertThat(result.username()).isEqualTo("edited");
        assertThat(result.email()).isEqualTo("edited@example.com");
        assertThat(result.role()).isEqualTo(UserSystemRole.INSTRUCTOR);
        assertThat(result.active()).isFalse();
        assertThat(user.getPasswordHash()).isEqualTo("existing-hash");
    }

    @Test
    void editRejectsDuplicateEmailWithoutSavingOrChangingOtherFields() {
        User user = user(UUID.randomUUID());
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndIdNot("taken@example.com", user.getId())).thenReturn(true);
        assertThatThrownBy(() -> service.updateUser(user.getId(),
                new com.example.sqlix.dto.request.AdminUserUpdateRequest(
                        "Changed", "edited", "taken@example.com", UserSystemRole.ADMIN, false)))
                .isInstanceOf(AppException.class);
        verify(userRepository, never()).saveAndFlush(any());
        assertThat(user.getFullName()).isEqualTo("Learner");
    }

    @Test
    void returnsActivityCountsPerUserForOnePage() {
        User first = user(UUID.randomUUID());
        User second = user(UUID.randomUUID());
        var activity = org.mockito.Mockito.mock(com.example.sqlix.repository.UserSqlActivityProjection.class);
        when(activity.getUserId()).thenReturn(first.getId());
        when(activity.getTotalExercisesSolved()).thenReturn(3L);
        when(activity.getTestsTaken()).thenReturn(5L);
        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(first, second)));
        when(userRepository.findSqlActivityByUserIds(java.util.List.of(first.getId(), second.getId())))
                .thenReturn(java.util.List.of(activity));

        var result = service.searchUsers(null, null, null, 0, 20).getContent();
        assertThat(result.getFirst().totalExercisesSolved()).isEqualTo(3);
        assertThat(result.getFirst().testsTaken()).isEqualTo(5);
        assertThat(result.get(1).totalExercisesSolved()).isZero();
        assertThat(result.get(1).testsTaken()).isZero();
        verify(userRepository).findSqlActivityByUserIds(java.util.List.of(first.getId(), second.getId()));
    }

    @Test
    void statusUpdateKeepsSqlActivityCounts() {
        User user = user(UUID.randomUUID());
        var activity = org.mockito.Mockito.mock(com.example.sqlix.repository.UserSqlActivityProjection.class);
        when(activity.getTotalExercisesSolved()).thenReturn(2L);
        when(activity.getTestsTaken()).thenReturn(4L);
        when(userRepository.findByIdForUpdate(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findSqlActivityByUserIds(java.util.List.of(user.getId())))
                .thenReturn(java.util.List.of(activity));
        var result = service.updateStatus(user.getId(), new AdminUserStatusRequest(false));
        assertThat(result.totalExercisesSolved()).isEqualTo(2);
        assertThat(result.testsTaken()).isEqualTo(4);
        assertThat(result.active()).isFalse();
    }

    @Test
    void locksAndUnlocksAccount() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        when(userRepository.findByIdForUpdate(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var result = service.updateStatus(id, new AdminUserStatusRequest(false));

        assertThat(result.active()).isFalse();
        assertThat(user.getIsActive()).isFalse();
    }

    @Test
    void changesUserRole() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        when(userRepository.findByIdForUpdate(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        var result = service.updateRole(id, new AdminUserRoleRequest(UserSystemRole.INSTRUCTOR));

        assertThat(result.role()).isEqualTo(UserSystemRole.INSTRUCTOR);
    }

    private User user(UUID id) {
        return User.builder().id(id).username("learner").email("learner@example.com")
                .fullName("Learner").systemRole(UserSystemRole.USER).isActive(true)
                .emailVerified(true).build();
    }

    @Test void deletesPersistedUser() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        when(userRepository.findByIdForUpdate(id)).thenReturn(Optional.of(user));
        service.deleteUser(id);
        verify(userRepository).delete(user);
    }

    @Test void cannotDeleteAdmin() {
        UUID id = UUID.randomUUID();
        User user = user(id);
        user.setSystemRole(UserSystemRole.ADMIN);
        when(userRepository.findByIdForUpdate(id)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service.deleteUser(id)).isInstanceOfSatisfying(AppException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.CANNOT_DELETE_ADMIN));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test void missingUserCannotReportSuccessfulDeletion() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdForUpdate(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.deleteUser(id)).isInstanceOfSatisfying(AppException.class,
                error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND));
        verify(userRepository, never()).delete(any(User.class));
    }
}
