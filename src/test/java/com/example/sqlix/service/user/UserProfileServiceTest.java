package com.example.sqlix.service.user;

import com.example.sqlix.dto.request.UpdateUserProfileRequest;
import com.example.sqlix.entity.User;
import com.example.sqlix.entity.UserProfile;
import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {
    @Mock UserRepository users;
    @InjectMocks UserProfileService service;

    @Test void readsCurrentUserAndProfileFields() {
        UserProfile profile = UserProfile.builder().currentStreak(4).longestStreak(10).dailyTargetMinutes(25).build();
        User user = User.builder().id(UUID.randomUUID()).fullName("SQLix User").username("sqlix")
                .email("user@example.com").systemRole(UserSystemRole.USER).profile(profile).build();
        when(users.findByEmailWithProfile("user@example.com")).thenReturn(Optional.of(user));
        var response = service.getCurrentUser("USER@example.com");
        assertEquals("SQLix User", response.getFullName());
        assertEquals(4, response.getCurrentStreak());
        assertEquals(10, response.getLongestStreak());
        assertEquals(25, response.getDailyTargetMinutes());
    }

    @Test void updatesUserAndProfileFields() {
        UUID id = UUID.randomUUID();
        UserProfile profile = UserProfile.builder().userId(id).currentStreak(1).longestStreak(3).dailyTargetMinutes(15).build();
        User user = User.builder().id(id).fullName("Old Name").email("user@example.com").profile(profile).build();
        when(users.findByEmailWithProfile("user@example.com")).thenReturn(Optional.of(user));
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setFullName(" New Name "); request.setAvatarUrl(" https://example.com/avatar.png ");
        request.setBio(" Updated bio "); request.setDailyTargetMinutes(30); request.setOccupation(UserOccupation.PROFESSIONAL);
        var response = service.updateCurrentUser("user@example.com", request);
        assertEquals("New Name", user.getFullName());
        assertEquals("Updated bio", profile.getBio());
        assertEquals(30, profile.getDailyTargetMinutes());
        assertEquals("https://example.com/avatar.png", user.getAvatarUrl());
        assertEquals(UserOccupation.PROFESSIONAL, user.getOccupation());
        assertEquals(30, response.getDailyTargetMinutes());
        verify(users).flush();
    }
}
