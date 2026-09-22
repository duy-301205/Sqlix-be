package com.example.sqlix.repository;

import com.example.sqlix.entity.User;
import com.example.sqlix.enums.UserSystemRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsBySystemRole(UserSystemRole systemRole);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
