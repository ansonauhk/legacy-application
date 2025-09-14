package com.example.legacyapp.repository;

import com.example.legacyapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByUsernameContainingIgnoreCase(String username);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.username = :username")
    Optional<User> findByEmailAndUsername(@Param("email") String email, 
                                         @Param("username") String username);

    @Query(value = "SELECT * FROM users WHERE created_at > DATEADD('DAY', -7, CURRENT_DATE())", 
           nativeQuery = true)
    List<User> findRecentUsers();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}