package com.shopnest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Delivery street is required")
    @Size(max = 255, message = "Street cannot exceed 255 characters")
    private String deliveryStreet;

    @NotBlank(message = "Delivery city is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String deliveryCity;

    @NotBlank(message = "Delivery state is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String deliveryState;

    @NotBlank(message = "Delivery pincode is required")
    @Pattern(
            regexp = "^[1-9][0-9]{5}$",
            message = "Pincode must be a valid 6-digit Indian pincode"
    )
    private String deliveryPincode;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotEmpty(message = "Order must have at least one item")
    @Valid                          // validates each item in list
    private List<OrderItemRequest> items;

    // ── Nested DTO ────────────────────────────────────
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 100, message = "Quantity cannot exceed 100")
        private int quantity;
    }
}