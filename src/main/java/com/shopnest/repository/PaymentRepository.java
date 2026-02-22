package com.shopnest.repository;

import com.shopnest.entity.PaymentEntity;
import com.shopnest.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByOrderId(Long orderId);

    Optional<PaymentEntity> findByTransactionId(String transactionId);

    List<PaymentEntity> findByStatus(PaymentStatus status);

    boolean existsByOrderId(Long orderId);
}