package org.example.repository;

import org.example.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginRepository extends JpaRepository<Login, Long> {
    Optional<Login> findByUsernameAndPassword(String username, String password);
    boolean existsByUsername(String username); // ✅ FIXED
}
