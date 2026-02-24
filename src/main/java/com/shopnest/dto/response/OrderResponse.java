package com.shopnest.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal totalAmount;

    // Delivery address
    private String deliveryStreet;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryPincode;

    private List<OrderItemResponse> orderItems;
    private LocalDateTime placedAt;
    private LocalDateTime updatedAt;
}