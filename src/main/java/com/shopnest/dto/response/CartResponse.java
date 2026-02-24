package com.shopnest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CartResponse {

    private Long id;
    private Long userId;
    private String userName;
    private List<CartItemResponse> items;
    private int totalItems;
    private BigDecimal totalPrice;
    private LocalDateTime updatedAt;
}