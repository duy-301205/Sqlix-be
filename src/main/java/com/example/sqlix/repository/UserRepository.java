package com.example.sqlix.repository;

import com.example.sqlix.entity.User;
import com.example.sqlix.enums.UserSystemRole;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    @Query(value = """
            select u.id as "userId",
                   (select count(*) from user_exercise_progress p
                    where p.user_id = u.id and p.is_passed = true) as "totalExercisesSolved",
                   (select count(*) from test_sessions s
                    where s.user_id = u.id and s.status in ('COMPLETED', 'TIMEOUT')) as "testsTaken"
            from users u where u.id in (:userIds)
            """, nativeQuery = true)
    java.util.List<UserSqlActivityProjection> findSqlActivityByUserIds(
            @Param("userIds") java.util.Collection<UUID> userIds);

    @Query("""
            select u from User u
            where (:search is null or lower(u.username) like concat('%', :search, '%')
                   or lower(u.email) like concat('%', :search, '%')
                   or lower(u.fullName) like concat('%', :search, '%'))
              and (:role is null or u.systemRole = :role)
              and (:active is null or u.isActive = :active)
            """)
    Page<User> searchAdminUsers(
            @Param("search") String search,
            @Param("role") UserSystemRole role,
            @Param("active") Boolean active,
            Pageable pageable);

    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);

    boolean existsByEmail(String email);

    boolean existsBySystemRole(UserSystemRole systemRole);

    Optional<User> findFirstBySystemRole(UserSystemRole systemRole);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findFirstByUsernameStartingWithOrderByUsernameDesc(String prefix);

    Optional<User> findByUsernameOrEmail(String username, String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE u.email = :email")
    Optional<User> findByEmailWithProfile(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);
}
