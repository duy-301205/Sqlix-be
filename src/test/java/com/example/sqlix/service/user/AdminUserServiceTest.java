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

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks AdminUserService service;

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
}
