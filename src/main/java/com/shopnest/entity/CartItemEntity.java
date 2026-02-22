package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_cart_items_cart_product",
                        columnNames = {"cart_id", "product_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ManyToOne — many items belong to one cart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;

    // ManyToOne — item references a product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    @PrePersist
    void prePersist() {
        this.addedAt = LocalDateTime.now();
    }

    // ── Business helpers ──────────────────────────────
    public java.math.BigDecimal getSubtotal() {
        return product.getPrice()
                .multiply(java.math.BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.quantity += qty;
    }

    public void decreaseQuantity(int qty) {
        if (qty <= 0 || qty > this.quantity)
            throw new IllegalArgumentException("Invalid quantity");
        this.quantity -= qty;
    }
}