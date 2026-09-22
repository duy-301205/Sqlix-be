package com.example.sqlix.configuration;

import com.example.sqlix.entity.User;
import com.example.sqlix.enums.UserOccupation;
import com.example.sqlix.enums.UserSystemRole;
import com.example.sqlix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ApplicationInitConfig {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Bean
    ApplicationRunner applicationRunner(
            @Value("${app.init-admin.username}") String adminUsername,
            @Value("${app.init-admin.email}") String adminEmail,
            @Value("${app.init-admin.password}") String adminPassword
    ) {
        return args -> {

            log.info("Starting application initialization...");

            initAdminUser(
                    adminUsername,
                    adminEmail,
                    adminPassword
            );

            log.info("Application initialization completed.");
        };
    }

    private void initAdminUser(
            String username,
            String email,
            String rawPassword
    ) {

        // Kiểm tra admin đã tồn tại chưa
        if (userRepository.existsBySystemRole(UserSystemRole.ADMIN)) {
            log.info("Admin account already exists.");
            return;
        }

        // Kiểm tra username đã được sử dụng chưa
        if (userRepository.existsByUsername(username)) {
            log.warn(
                    "Cannot initialize admin: username '{}' already exists.",
                    username
            );
            return;
        }

        // Kiểm tra email đã được sử dụng chưa
        if (userRepository.existsByEmail(email)) {
            log.warn(
                    "Cannot initialize admin: email '{}' already exists.",
                    email
            );
            return;
        }

        User admin = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .fullName("System Administrator")
                .occupation(UserOccupation.OTHER)
                .systemRole(UserSystemRole.ADMIN)
                .termsAccepted(true)
                .isActive(true)
                .build();

        userRepository.save(admin);

        log.info(
                "Admin account initialized successfully with username '{}'.",
                username
        );
    }
}
