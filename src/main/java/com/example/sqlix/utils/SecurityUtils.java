package com.example.sqlix.utils;

import com.example.sqlix.exception.AppException;
import com.example.sqlix.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class SecurityUtils {

    private SecurityUtils() {}

    public static Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return (Jwt) authentication.getPrincipal();
    }

    public static UUID getCurrentUserId() {
        Jwt jwt = getCurrentJwt();
        String userId = jwt.getClaimAsString("userId");
        if (userId == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        return UUID.fromString(userId);
    }

    public static String getCurrentUserRole() {
        Jwt jwt = getCurrentJwt();

        // Tránh lỗi khi không có JWT (User chưa đăng nhập)
        if (jwt == null) {
            return null; // Hoặc trả về "GUEST" / "USER" tùy hệ thống của bạn
        }

        String scope = jwt.getClaimAsString("scope");

        // Tránh lỗi khi token không có claim "scope"
        if (scope != null) {
            return scope.replaceFirst("^ROLE_", "").toUpperCase();
        }

        return null;
    }

    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return role != null && "ADMIN".equalsIgnoreCase(role);
    }

    public static String getCurrentUserEmail() {
        Jwt jwt = getCurrentJwt();
        return jwt.getClaimAsString("contactEmail");
    }

    public static String getCurrentUserFullName() {
        Jwt jwt = getCurrentJwt();
        return jwt.getClaimAsString("fullName");
    }
}
