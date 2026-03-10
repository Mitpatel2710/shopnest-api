package com.shopnest.controller;

import com.shopnest.dto.request.AddToCartRequest;
import com.shopnest.dto.response.CartResponse;
import com.shopnest.service.CartService;
import com.shopnest.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // ADMIN, CUSTOMER
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(cartService.getCart(userId)));
    }

    // CUSTOMER only
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Item added to cart",
                        cartService.addToCart(request)));
    }

    // CUSTOMER only
    @PutMapping("/{userId}/items/{productId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(
                ApiResponse.success("Cart updated",
                        cartService.updateQuantity(userId, productId, quantity)));
    }

    // CUSTOMER only
    @DeleteMapping("/{userId}/items/{productId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.success("Item removed from cart",
                        cartService.removeFromCart(userId, productId)));
    }

    // CUSTOMER only
    @DeleteMapping("/{userId}/clear")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Cart cleared", null));
    }
}