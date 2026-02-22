package com.shopnest.repository;

import com.shopnest.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {

    // Derived — find cart by user
    Optional<CartEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    // JOIN FETCH — load cart with all items in one query
    @Query("SELECT c FROM CartEntity c " +
            "JOIN FETCH c.items i " +
            "JOIN FETCH i.product " +
            "WHERE c.user.id = :userId")
    Optional<CartEntity> findByUserIdWithItems(@Param("userId") Long userId);
}