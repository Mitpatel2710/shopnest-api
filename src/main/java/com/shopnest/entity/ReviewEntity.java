package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "reviews",
        indexes = {
                @Index(name = "idx_reviews_product", columnList = "product_id")
        },
        uniqueConstraints = {
                // one review per user per product
                @UniqueConstraint(
                        name = "uq_reviews_user_product",
                        columnNames = {"user_id", "product_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEntity extends BaseEntity {

    // ── ManyToOne relationships ───────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    // ── Fields ────────────────────────────────────────
    @Column(name = "rating", nullable = false)
    private int rating;                 // 1-5

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "verified", nullable = false)
    private boolean verified;

    // ── Validation ────────────────────────────────────
    @PrePersist
    @PreUpdate
    private void validate() {
        if (rating < 1 || rating > 5)
            throw new IllegalArgumentException("Rating must be between 1 and 5");
    }
}