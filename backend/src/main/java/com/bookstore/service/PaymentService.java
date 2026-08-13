package com.bookstore.service;

import com.bookstore.common.exception.BadRequestException;
import com.bookstore.common.exception.BusinessRuleException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.domain.Order;
import com.bookstore.domain.Payment;
import com.bookstore.domain.enums.OrderStatus;
import com.bookstore.domain.enums.PaymentMethod;
import com.bookstore.domain.enums.PaymentStatus;
import com.bookstore.dto.request.PaymentRequest;
import com.bookstore.dto.response.DeliveryAddressSnapshotResponse;
import com.bookstore.dto.response.OrderDetailResponse;
import com.bookstore.dto.response.OrderItemResponse;
import com.bookstore.dto.response.PaymentRecordResponse;
import com.bookstore.dto.response.PaymentResultResponse;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.mapper.PaymentMapper;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Payment service — Phase 1 mock implementation.
 *
 * <p>Simulates a payment gateway: all well-formed requests return {@code SUCCESS}.
 * No real financial transaction is made; card data is never persisted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository    orderRepository;
    private final PaymentRepository  paymentRepository;
    private final CartRepository     cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaymentMapper      paymentMapper;
    private final OrderMapper        orderMapper;

    // ── Process Payment ────────────────────────────────────────────────────────

    /**
     * Mock-process a payment for a placed order.
     *
     * <ol>
     *   <li>Verify the order belongs to the user and is in {@code PLACED} status.</li>
     *   <li>Validate payment method specific fields.</li>
     *   <li>Persist a {@link Payment} record with {@code SUCCESS} status.</li>
     *   <li>Update the order to {@code CONFIRMED}.</li>
     *   <li>Clear the user's shopping cart.</li>
     * </ol>
     */
    @Transactional
    public PaymentResultResponse processPayment(Long userId, PaymentRequest request) {
        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new BusinessRuleException(
                    "Order is not in PLACED status. Current status: " + order.getStatus());
        }

        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BusinessRuleException("Payment has already been initiated for this order.");
        }

        validatePaymentRequest(request);

        // ── Simulate payment success ──────────────────────────────────────────
        String txRef = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .payableAmount(order.getTotalAmount())
                .status(PaymentStatus.SUCCESS)
                .transactionReference(txRef)
                .paidAt(Instant.now())
                .build();
        payment = paymentRepository.save(payment);

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Payment {} processed for order {} (user={})",
                payment.getId(), order.getOrderNumber(), userId);

        // Clear the user's cart
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartItemRepository.deleteByCartId(cart.getId());
            log.debug("Cart cleared after payment for userId={}", userId);
        });

        OrderDetailResponse orderDetail = buildOrderDetail(order, payment);

        PaymentResultResponse base = paymentMapper.toResultResponse(payment);
        return PaymentResultResponse.builder()
                .paymentId(base.getPaymentId())
                .orderId(base.getOrderId())
                .orderNumber(base.getOrderNumber())
                .paymentMethod(base.getPaymentMethod())
                .payableAmount(base.getPayableAmount())
                .status(base.getStatus())
                .paidAt(base.getPaidAt())
                .order(orderDetail)
                .build();
    }

    // ── Get Payment ────────────────────────────────────────────────────────────

    /**
     * Return the payment record for an order belonging to the authenticated user.
     */
    @Transactional(readOnly = true)
    public PaymentRecordResponse getPayment(Long userId, Long orderId) {
        // Verify ownership
        orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));

        return paymentMapper.toRecordResponse(payment);
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    private void validatePaymentRequest(PaymentRequest request) {
        PaymentMethod method = request.getPaymentMethod();
        if ((method == PaymentMethod.CREDIT_CARD || method == PaymentMethod.DEBIT_CARD)) {
            if (request.getCardDetails() == null) {
                throw new BadRequestException("Card details are required for " + method + " payment.");
            }
            // Demo failure simulation hooks (e.g. CVV '000' or Card ending in '0000')
            String cardNum = request.getCardDetails().getCardNumber();
            String cvv = request.getCardDetails().getCvv();
            if ("000".equals(cvv) || (cardNum != null && cardNum.endsWith("0000"))) {
                throw new BusinessRuleException("Payment Declined: Demo card authorization failed or insufficient funds.");
            }
        }
        if (method == PaymentMethod.UPI) {
            if (request.getUpiDetails() == null) {
                throw new BadRequestException("UPI details are required for UPI payment.");
            }
            // Demo failure simulation hook (e.g. fail@upi or decline@upi)
            String upiId = request.getUpiDetails().getUpiId();
            if (upiId != null && (upiId.toLowerCase().startsWith("fail") || upiId.toLowerCase().startsWith("decline"))) {
                throw new BusinessRuleException("UPI Payment Failed: Transaction rejected by bank/UPI gateway.");
            }
        }
    }

    private OrderDetailResponse buildOrderDetail(Order order, Payment payment) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(orderMapper::toItemResponse)
                .collect(Collectors.toList());

        DeliveryAddressSnapshotResponse deliveryAddr =
                buildDeliverySnapshot(order.getDeliveryAddressSnapshot());

        PaymentRecordResponse paymentRecord = paymentMapper.toRecordResponse(payment);

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
