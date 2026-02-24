package com.shopnest.service;

import com.shopnest.dto.request.CreateOrderRequest;
import com.shopnest.dto.response.OrderItemResponse;
import com.shopnest.dto.response.OrderResponse;
import com.shopnest.entity.*;
import com.shopnest.exception.InsufficientStockException;
import com.shopnest.exception.ResourceNotFoundException;
import com.shopnest.repository.*;
import com.shopnest.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;

    // ── PLACE ORDER ───────────────────────────────────
    // @Transactional — if ANY step fails, entire order rolls back
    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {
        // 1. Validate user
        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", request.getUserId()));

        // 2. Validate all items and calculate total
        List<OrderItemEntity> orderItems = request.getItems().stream()
                .map(itemReq -> {
                    ProductEntity product = productRepository
                            .findById(itemReq.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Product", itemReq.getProductId()));

                    if (!product.isAvailable())
                        throw new IllegalArgumentException(
                                "Product '" + product.getName() + "' is not available");

                    if (product.getStockQty() < itemReq.getQuantity())
                        throw new InsufficientStockException(
                                product.getName(),
                                itemReq.getQuantity(),
                                product.getStockQty());

                    // Snapshot — copy name and price at purchase time
                    return OrderItemEntity.builder()
                            .product(product)
                            .productName(product.getName())         // snapshot
                            .priceAtPurchase(product.getPrice())    // snapshot
                            .quantity(itemReq.getQuantity())
                            .build();
                })
                .toList();

        // 3. Calculate total amount
        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItemEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. Create order
        OrderEntity order = OrderEntity.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .deliveryAddress(DeliveryAddress.builder()
                        .street(request.getDeliveryStreet())
                        .city(request.getDeliveryCity())
                        .state(request.getDeliveryState())
                        .pincode(request.getDeliveryPincode())
                        .build())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        OrderEntity savedOrder = orderRepository.save(order);

        // 5. Save order items and reduce stock
        orderItems.forEach(item -> {
            item.setOrder(savedOrder);
            // Reduce stock — if this fails, entire transaction rolls back
            item.getProduct().reduceStock(item.getQuantity());
        });

        savedOrder.setOrderItems(orderItems);
        log.debug("Order placed: {} for user: {}", savedOrder.getId(), user.getId());

        // 6. Clear cart after successful order
        cartRepository.findByUserId(user.getId())
                .ifPresent(cart -> cart.getItems().clear());

        return toOrderResponse(savedOrder);
    }

    // ── GET ORDER BY ID ───────────────────────────────
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        OrderEntity order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        return toOrderResponse(order);
    }

    // ── GET ORDERS BY USER ────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByUser(
            Long userId, int page, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Pageable pageable = PageRequest.of(
                page, size, Sort.by("placedAt").descending());
        Page<OrderEntity> orders = orderRepository.findByUserId(userId, pageable);
        return PageResponse.from(orders.map(this::toOrderResponse));
    }

    // ── GET ALL ORDERS (admin) ────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(
                page, size, Sort.by("placedAt").descending());
        Page<OrderEntity> orders = orderRepository.findAll(pageable);
        return PageResponse.from(orders.map(this::toOrderResponse));
    }

    // ── CONFIRM ORDER ─────────────────────────────────
    @Transactional
    public OrderResponse confirmOrder(Long id) {
        OrderEntity order = findOrderById(id);
        order.confirm();
        log.debug("Order confirmed: {}", id);
        return toOrderResponse(order);
    }

    // ── SHIP ORDER ────────────────────────────────────
    @Transactional
    public OrderResponse shipOrder(Long id) {
        OrderEntity order = findOrderById(id);
        order.ship();
        log.debug("Order shipped: {}", id);
        return toOrderResponse(order);
    }

    // ── DELIVER ORDER ─────────────────────────────────
    @Transactional
    public OrderResponse deliverOrder(Long id) {
        OrderEntity order = findOrderById(id);
        order.deliver();
        log.debug("Order delivered: {}", id);
        return toOrderResponse(order);
    }

    // ── CANCEL ORDER ──────────────────────────────────
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        OrderEntity order = findOrderById(id);
        order.cancel();

        // Restore stock on cancellation
        order.getOrderItems().forEach(item ->
                item.getProduct().addStock(item.getQuantity()));

        log.debug("Order cancelled and stock restored: {}", id);
        return toOrderResponse(order);
    }

    // ── PRIVATE HELPERS ───────────────────────────────
    private OrderEntity findOrderById(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private OrderResponse toOrderResponse(OrderEntity order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProductName())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .userName(order.getUser().getFullName())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .paymentMethod(order.getPaymentMethod() != null
                        ? order.getPaymentMethod().name() : null)
                .totalAmount(order.getTotalAmount())
                .deliveryStreet(order.getDeliveryAddress().getStreet())
                .deliveryCity(order.getDeliveryAddress().getCity())
                .deliveryState(order.getDeliveryAddress().getState())
                .deliveryPincode(order.getDeliveryAddress().getPincode())
                .orderItems(itemResponses)
                .placedAt(order.getPlacedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}