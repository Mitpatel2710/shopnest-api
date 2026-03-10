package com.shopnest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse implements Serializable {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQty;
    private String imageUrl;
    private boolean active;
    private String type;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerName;          // mapped from seller.firstName
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}