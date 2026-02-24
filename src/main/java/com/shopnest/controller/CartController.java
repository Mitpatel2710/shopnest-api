package com.shopnest.controller;

import com.shopnest.dto.request.AddToCartRequest;
import com.shopnest.dto.response.CartResponse;
import com.shopnest.service.CartService;
import com.shopnest.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // GET /api/cart/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success(cartService.getCart(userId)));
    }

    // POST /api/cart/add
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Item added to cart",
                        cartService.addToCart(request)));
    }

    // PUT /api/cart/{userId}/items/{productId}?quantity=2
    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(
                ApiResponse.success("Cart updated",
                        cartService.updateQuantity(userId, productId, quantity)));
    }

    // DELETE /api/cart/{userId}/items/{productId}
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.success("Item removed from cart",
                        cartService.removeFromCart(userId, productId)));
    }

    // DELETE /api/cart/{userId}/clear
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Cart cleared", null));
    }
}