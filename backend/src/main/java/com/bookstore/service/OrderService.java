package com.bookstore.service;

import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.common.response.PagedResponse;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.Order;
import com.bookstore.domain.OrderItem;
import com.bookstore.domain.enums.OrderStatus;
import com.bookstore.dto.response.BuyAgainResponse;
import com.bookstore.dto.response.DeliveryAddressSnapshotResponse;
import com.bookstore.dto.response.OrderDetailResponse;
import com.bookstore.dto.response.OrderItemResponse;
import com.bookstore.dto.response.OrderSummaryResponse;
import com.bookstore.dto.response.PaymentRecordResponse;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.mapper.PaymentMapper;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.OrderItemRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for order history, order detail, and Buy Again.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository     orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository  cartItemRepository;
    private final PaymentRepository   paymentRepository;
    private final OrderMapper         orderMapper;
    private final PaymentMapper       paymentMapper;
    private final CartService         cartService;

    // ── Order History ──────────────────────────────────────────────────────────

    /**
     * Return paginated order history for the authenticated user, optionally filtered by status.
     */
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryResponse> listOrders(Long userId, int page, int size,
                                                          String status) {
        Page<Order> orderPage;
        if (status != null && !status.isBlank()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orderPage = orderRepository.findByUserIdAndStatusOrderByPlacedAtDesc(
                    userId, orderStatus, PageRequest.of(page, size));
        } else {
            // Default My Orders view: exclude unconfirmed draft orders (status = PLACED)
            orderPage = orderRepository.findByUserIdAndStatusNotOrderByPlacedAtDesc(
                    userId, OrderStatus.PLACED, PageRequest.of(page, size));
        }

        List<OrderSummaryResponse> content = orderPage.getContent().stream()
                .map(order -> {
                    List<OrderItemResponse> items = order.getItems().stream()
                            .map(orderMapper::toItemResponse)
                            .collect(Collectors.toList());
                    return OrderSummaryResponse.builder()
                            .id(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .status(order.getStatus())
                            .totalAmount(order.getTotalAmount())
                            .itemCount(items.size())
                            .placedAt(order.getPlacedAt())
                            .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                            .items(items)
                            .build();
                })
                .collect(Collectors.toList());

        return PagedResponse.<OrderSummaryResponse>builder()
                .content(content)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .last(orderPage.isLast())
                .build();
    }

    // ── Order Detail ───────────────────────────────────────────────────────────

    /**
     * Return full details for a single order belonging to the authenticated user.
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserIdWithDetails(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        List<OrderItemResponse> items = order.getItems().stream()
                .map(orderMapper::toItemResponse)
                .collect(Collectors.toList());

        DeliveryAddressSnapshotResponse deliveryAddr =
                buildDeliverySnapshot(order.getDeliveryAddressSnapshot());

        // Payment may not exist yet (order PLACED but not yet paid)
        PaymentRecordResponse paymentRecord = null;
        if (order.getPayment() != null) {
            paymentRecord = paymentMapper.toRecordResponse(order.getPayment());
        } else {
            paymentRecord = paymentRepository.findByOrderId(orderId)
                    .map(paymentMapper::toRecordResponse)
                    .orElse(null);
        }

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .deliveryCharge(order.getDeliveryCharge())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .couponCode(order.getCoupon() != null ? order.getCoupon().getCode() : null)
                .placedAt(order.getPlacedAt())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .deliveryAddress(deliveryAddr)
                .payment(paymentRecord)
                .items(items)
                .build();
    }

    // ── Buy Again ──────────────────────────────────────────────────────────────

    /**
     * Re-add all items from a past order to the user's current cart.
     * Items already in the cart have their quantity incremented.
     * Books that are no longer active are silently skipped.
     */
    @Transactional
    public BuyAgainResponse buyAgain(Long userId, Long orderId) {
        // Verify ownership
        orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        var cart = cartService.getOrCreateCart(userId);
        int itemsAdded = 0;

        for (OrderItem orderItem : orderItems) {
            try {
                CartItem cartItem = cartItemRepository
                        .findByCartIdAndBookId(cart.getId(), orderItem.getBook().getId())
                        .map(existing -> {
                            existing.setQuantity(existing.getQuantity() + orderItem.getQuantity());
                            return cartItemRepository.save(existing);
                        })
                        .orElseGet(() -> cartItemRepository.save(
                                CartItem.builder()
                                        .cart(cart)
                                        .book(orderItem.getBook())
                                        .quantity(orderItem.getQuantity())
                                        .build()));
                itemsAdded++;
            } catch (Exception e) {
                log.warn("Skipped buy-again item bookId={} (no longer available): {}",
                        orderItem.getBook().getId(), e.getMessage());
            }
        }

        log.info("Buy Again: {} item(s) re-added to cart for userId={}, orderId={}",
                itemsAdded, userId, orderId);

        var cartResponse = cartService.buildCartResponse(cart);
        final int added = itemsAdded;

        return BuyAgainResponse.builder()
                .cartId(cartResponse.getCartId())
                .itemCount(cartResponse.getItemCount())
                .itemsAdded(added)
                .items(cartResponse.getItems())
                .build();
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    private DeliveryAddressSnapshotResponse buildDeliverySnapshot(Map<String, Object> snap) {
        if (snap == null) return null;
        return DeliveryAddressSnapshotResponse.builder()
                .firstName(   (String) snap.getOrDefault("firstName",    null))
                .lastName(    (String) snap.getOrDefault("lastName",     null))
                .addressLine1((String) snap.getOrDefault("addressLine1", null))
                .addressLine2((String) snap.getOrDefault("addressLine2", null))
                .city(        (String) snap.getOrDefault("city",         null))
                .state(       (String) snap.getOrDefault("state",        null))
                .country(     (String) snap.getOrDefault("country",      null))
                .pinCode(     (String) snap.getOrDefault("pinCode",      null))
                .build();
    }
}
