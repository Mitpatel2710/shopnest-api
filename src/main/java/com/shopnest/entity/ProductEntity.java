package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_category", columnList = "category_id"),
                @Index(name = "idx_products_price",    columnList = "price"),
                @Index(name = "idx_products_active",   columnList = "active")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal price;     // BigDecimal for money — never Double

    @Column(name = "stock_qty", nullable = false)
    private int stockQty;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "type", length = 50)
    private String type;                    // Electronics, Clothing etc

    @Column(name = "brand", length = 100)
    private String brand;

    // ── ManyToOne — product belongs to one category ──
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    // ── ManyToOne — product listed by one seller ─────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private UserEntity seller;

    // ── OneToMany — product has many reviews ─────────
    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReviewEntity> reviews = new ArrayList<>();

    // ── Business helpers ──────────────────────────────
    public boolean isAvailable() {
        return active && stockQty > 0;
    }

    public void reduceStock(int quantity) {
        if (quantity > this.stockQty)
            throw new IllegalArgumentException("Insufficient stock");
        this.stockQty -= quantity;
    }

    public void addStock(int quantity) {
        this.stockQty += quantity;
    }
}