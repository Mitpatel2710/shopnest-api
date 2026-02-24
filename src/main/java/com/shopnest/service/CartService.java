package com.shopnest.service;

import com.shopnest.dto.request.AddToCartRequest;
import com.shopnest.dto.response.CartItemResponse;
import com.shopnest.dto.response.CartResponse;
import com.shopnest.entity.*;
import com.shopnest.exception.InsufficientStockException;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ── GET CART ──────────────────────────────────────
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        CartEntity cart = getOrCreateCart(userId);
        return toCartResponse(cart);
    }

    // ── ADD TO CART ───────────────────────────────────
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", request.getUserId()));

        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", request.getProductId()));

        if (!product.isAvailable())
            throw new IllegalArgumentException(
                    "Product '" + product.getName() + "' is not available");

        if (product.getStockQty() < request.getQuantity())
            throw new InsufficientStockException(
                    product.getName(), request.getQuantity(), product.getStockQty());

        CartEntity cart = getOrCreateCart(request.getUserId());

        // Check if product already in cart — merge quantities
        Optional<CartItemEntity> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            int newQty = item.getQuantity() + request.getQuantity();
            if (newQty > product.getStockQty())
                throw new InsufficientStockException(
                        product.getName(), newQty, product.getStockQty());
            item.increaseQuantity(request.getQuantity());
            log.debug("Updated cart item quantity for product: {}", product.getName());
        } else {
            CartItemEntity newItem = CartItemEntity.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
            log.debug("Added new item to cart: {}", product.getName());
        }

        return toCartResponse(cart);
    }

    // ── UPDATE QUANTITY ───────────────────────────────
    @Transactional
    public CartResponse updateQuantity(Long userId, Long productId, int quantity) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart", "userId", String.valueOf(userId)));

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CartItem", "productId", String.valueOf(productId)));

        ProductEntity product = item.getProduct();
        if (quantity > product.getStockQty())
            throw new InsufficientStockException(
                    product.getName(), quantity, product.getStockQty());

        if (quantity <= 0) {
            cart.removeItem(item);
            log.debug("Removed item from cart: {}", productId);
        } else {
            item.setQuantity(quantity);
            log.debug("Updated quantity for product: {}", productId);
        }

        return toCartResponse(cart);
    }

    // ── REMOVE ITEM ───────────────────────────────────
    @Transactional
    public CartResponse removeFromCart(Long userId, Long productId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart", "userId", String.valueOf(userId)));

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CartItem", "productId", String.valueOf(productId)));

        cart.removeItem(item);
        log.debug("Removed product {} from cart for user {}", productId, userId);
        return toCartResponse(cart);
    }

    // ── CLEAR CART ────────────────────────────────────
    @Transactional
    public void clearCart(Long userId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart", "userId", String.valueOf(userId)));
        cart.getItems().clear();
        log.debug("Cleared cart for user: {}", userId);
    }

    // ── PRIVATE HELPERS ───────────────────────────────
    private CartEntity getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserEntity user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "User", userId));
                    CartEntity newCart = CartEntity.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse toCartResponse(CartEntity cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .toList();

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .userName(cart.getUser().getFullName())
                .items(itemResponses)
                .totalItems(cart.getTotalItems())
                .totalPrice(cart.getTotalPrice())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItemEntity item) {
        return CartItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productBrand(item.getProduct().getBrand())
                .productPrice(item.getProduct().getPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .build();
    }
}