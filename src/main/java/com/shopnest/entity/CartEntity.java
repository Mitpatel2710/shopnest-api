package com.shopnest.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity extends BaseEntity {

    // OneToOne — one cart per user
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    // OneToMany — cart has many items
    @OneToMany(mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,        // delete item when removed from list
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItemEntity> items = new ArrayList<>();

    // ── Business helpers ──────────────────────────────
    public void addItem(CartItemEntity item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItemEntity item) {
        items.remove(item);
        item.setCart(null);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItemEntity::getQuantity)
                .sum();
    }

    public java.math.BigDecimal getTotalPrice() {
        return items.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}