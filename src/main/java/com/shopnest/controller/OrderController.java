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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully",
                        orderService.placeOrder(request)));
    }

    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getOrderById(id)));
    }

    // GET /api/orders/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getOrdersByUser(userId, page, size)));
    }

    // GET /api/orders?page=0&size=10
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getAllOrders(page, size)));
    }

    // PATCH /api/orders/{id}/confirm
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order confirmed",
                        orderService.confirmOrder(id)));
    }

    // PATCH /api/orders/{id}/ship
    @PatchMapping("/{id}/ship")
    public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order shipped",
                        orderService.shipOrder(id)));
    }

    // PATCH /api/orders/{id}/deliver
    @PatchMapping("/{id}/deliver")
    public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order delivered",
                        orderService.deliverOrder(id)));
    }

    // PATCH /api/orders/{id}/cancel
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order cancelled",
                        orderService.cancelOrder(id)));
    }
}