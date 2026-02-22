package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order",   columnList = "order_id"),
                @Index(name = "idx_order_items_product", columnList = "product_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ManyToOne — many items belong to one order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    // ManyToOne — references product (for lookup only)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    // Snapshot fields — copied at purchase time
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;         // copied from product

    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase; // locked at time of order

    @Column(name = "quantity", nullable = false)
    private int quantity;

    // ── Business helpers ──────────────────────────────
    public BigDecimal getSubtotal() {
        return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
    }
}