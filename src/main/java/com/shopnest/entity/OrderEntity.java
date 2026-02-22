package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user_id",   columnList = "user_id"),
                @Index(name = "idx_orders_status",    columnList = "status"),
                @Index(name = "idx_orders_placed_at", columnList = "placed_at")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ManyToOne — many orders belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Embedded delivery address snapshot
    @Embedded
    private DeliveryAddress deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "placed_at", nullable = false, updatable = false)
    private LocalDateTime placedAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // OneToMany — order has many items
    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    // OneToOne — order has one payment
    @OneToOne(mappedBy = "order",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private PaymentEntity payment;

    @PrePersist
    void prePersist() {
        this.placedAt  = LocalDateTime.now();
        this.status    = OrderStatus.PENDING;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    // ── State machine ─────────────────────────────────
    public void confirm() {
        validateTransition(OrderStatus.PENDING);
        this.status = OrderStatus.CONFIRMED;
    }

    public void ship() {
        validateTransition(OrderStatus.CONFIRMED);
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        validateTransition(OrderStatus.SHIPPED);
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED)
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        this.status = OrderStatus.CANCELLED;
    }

    private void validateTransition(OrderStatus required) {
        if (this.status != required)
            throw new IllegalStateException(
                    "Cannot transition from " + this.status + " to next state. Required: " + required);
    }

    public boolean isCancellable() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
}