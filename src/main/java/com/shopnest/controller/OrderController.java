package com.shopnest.controller;

import com.shopnest.dto.request.CreateOrderRequest;
import com.shopnest.dto.response.OrderResponse;
import com.shopnest.service.OrderService;
import com.shopnest.util.ApiResponse;
import com.shopnest.util.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // CUSTOMER, ADMIN
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully",
                        orderService.placeOrder(request)));
    }

    // ADMIN, CUSTOMER (own order)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getOrderById(id)));
    }

    // ADMIN, CUSTOMER (own orders)
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getOrdersByUser(userId, page, size)));
    }

    // ADMIN only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getAllOrders(page, size)));
    }

    // ADMIN only
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order confirmed",
                        orderService.confirmOrder(id)));
    }

    // ADMIN only
    @PatchMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order shipped",
                        orderService.shipOrder(id)));
    }

    // ADMIN only
    @PatchMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order delivered",
                        orderService.deliverOrder(id)));
    }

    // ADMIN, CUSTOMER
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order cancelled",
                        orderService.cancelOrder(id)));
    }
}