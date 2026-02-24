package com.shopnest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal priceAtPurchase;
    private int quantity;
    private BigDecimal subtotal;
}