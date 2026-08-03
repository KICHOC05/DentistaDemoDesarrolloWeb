package com.dnt.auth.repository;

import com.dnt.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByPublicId(String publicId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
