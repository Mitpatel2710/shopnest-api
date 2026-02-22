package com.shopnest.repository;

import com.shopnest.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Derived query — Spring generates SQL automatically
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    // Find all active users
    java.util.List<UserEntity> findByActiveTrue();
}