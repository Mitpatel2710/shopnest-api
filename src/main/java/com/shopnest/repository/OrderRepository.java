package com.shopnest.repository;

import com.shopnest.entity.OrderEntity;
import com.shopnest.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // Derived queries
    List<OrderEntity> findByUserId(Long userId);

    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);

    List<OrderEntity> findByStatus(OrderStatus status);

    List<OrderEntity> findByUserIdAndStatus(Long userId, OrderStatus status);

    // Custom JPQL — fetch order with items in one query (avoids N+1)
    @Query("SELECT o FROM OrderEntity o " +
            "JOIN FETCH o.orderItems " +
            "WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") Long id);

    // Custom JPQL — high value orders
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.totalAmount > :amount " +
            "ORDER BY o.totalAmount DESC")
    List<OrderEntity> findHighValueOrders(@Param("amount") BigDecimal amount);

    // Custom JPQL — orders placed in date range
    @Query("SELECT o FROM OrderEntity o " +
            "WHERE o.placedAt BETWEEN :from AND :to " +
            "ORDER BY o.placedAt DESC")
    List<OrderEntity> findByDateRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Aggregate — total revenue
    @Query("SELECT SUM(o.totalAmount) FROM OrderEntity o " +
            "WHERE o.status = 'DELIVERED'")
    Optional<BigDecimal> getTotalRevenue();

    // Aggregate — order count by status
    @Query("SELECT o.status, COUNT(o) FROM OrderEntity o GROUP BY o.status")
    List<Object[]> countByStatus();
}