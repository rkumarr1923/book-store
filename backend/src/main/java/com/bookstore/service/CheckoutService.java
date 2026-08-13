package com.bookstore.service;

import com.bookstore.common.constants.AppConstants;
import com.bookstore.common.exception.BadRequestException;
import com.bookstore.common.exception.BusinessRuleException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.common.util.DeliveryDateCalculator;
import com.bookstore.common.util.PriceCalculator;
import com.bookstore.domain.Address;
import com.bookstore.domain.Cart;
import com.bookstore.domain.Coupon;
import com.bookstore.domain.Order;
import com.bookstore.domain.OrderItem;
import com.bookstore.domain.User;
import com.bookstore.domain.enums.DiscountType;
import com.bookstore.domain.enums.OrderStatus;
import com.bookstore.dto.request.DeliveryAddressRequest;
import com.bookstore.dto.request.PlaceOrderRequest;
import com.bookstore.dto.request.ValidateCouponRequest;
import com.bookstore.dto.response.CheckoutSummaryResponse;
import com.bookstore.dto.response.CouponSummaryResponse;
import com.bookstore.dto.response.OrderItemResponse;
import com.bookstore.dto.response.OrderPlacedResponse;
import com.bookstore.dto.response.ValidateCouponResponse;
import com.bookstore.mapper.CouponMapper;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.repository.AddressRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.CouponRepository;
import com.bookstore.repository.OrderRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for the checkout flow: summary, coupon validation, and order placement.
 *
 * <p>Cart is NOT cleared at order placement — it is cleared after successful payment
 * by {@link PaymentService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final List<OrderStatus> SUCCESSFUL_STATUSES =
            List.of(OrderStatus.CONFIRMED, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final CartService            cartService;
    private final CartRepository         cartRepository;
    private final CouponRepository       couponRepository;
    private final OrderRepository        orderRepository;
    private final AddressRepository      addressRepository;
    private final UserRepository         userRepository;
    private final PriceCalculator        priceCalculator;
    private final DeliveryDateCalculator deliveryDateCalculator;
    private final CouponMapper           couponMapper;
    private final OrderMapper            orderMapper;

    // ── Checkout Summary ───────────────────────────────────────────────────────

    /**
     * Compute and return the checkout summary, optionally previewing a coupon discount.
     *
     * @param userId     the authenticated user's ID
     * @param couponCode optional coupon code to preview
     */
    @Transactional(readOnly = true)
    public CheckoutSummaryResponse getSummary(Long userId, String couponCode) {
        var cart = cartService.getCart(userId);

        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Your cart is empty.");
        }

        BigDecimal subtotal       = cart.getSubtotal();
        BigDecimal taxAmount      = priceCalculator.computeTax(subtotal);
        BigDecimal deliveryCharge = computeDeliveryCharge(subtotal);
        BigDecimal discountAmount = BigDecimal.ZERO;
        CouponSummaryResponse couponSummary = null;

        if (StringUtils.hasText(couponCode)) {
            Coupon coupon = findValidCoupon(userId, couponCode.trim(), subtotal);
            discountAmount = computeCouponDiscount(coupon, subtotal);
            couponSummary  = couponMapper.toSummaryResponse(coupon);
        }

        BigDecimal totalAmount = priceCalculator.computeTotal(
                subtotal, taxAmount, deliveryCharge, discountAmount);

        return CheckoutSummaryResponse.builder()
                .items(cart.getItems())
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .deliveryCharge(deliveryCharge)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .coupon(couponSummary)
                .build();
    }

    // ── Validate Coupon ────────────────────────────────────────────────────────

    /**
     * Validate a coupon code against the current cart subtotal.
     *
     * @param userId  the authenticated user's ID
     * @param request contains the coupon code
     */
    @Transactional(readOnly = true)
    public ValidateCouponResponse validateCoupon(Long userId, ValidateCouponRequest request) {
        var cart       = cartService.getCart(userId);
        BigDecimal subtotal = cart.getSubtotal();

        Coupon coupon = findValidCoupon(userId, request.getCouponCode().trim(), subtotal);
        BigDecimal discountAmount = computeCouponDiscount(coupon, subtotal);

        ValidateCouponResponse base = couponMapper.toValidateResponse(coupon);
        return ValidateCouponResponse.builder()
                .couponCode(base.getCouponCode())
                .discountType(base.getDiscountType())
                .discountValue(base.getDiscountValue())
                .discountAmount(discountAmount)
                .valid(true)
                .build();
    }

    // ── Place Order ────────────────────────────────────────────────────────────

    /**
     * Create an order from the current cart contents.
     *
     * <p>Validates the cart, resolves the delivery address, computes prices,
     * applies any coupon, and persists the order + line items.
     * The cart is left intact — it is cleared by {@link PaymentService} after payment.
     *
     * @param userId  the authenticated user's ID
     * @param request delivery address and optional coupon code
     */
    @Transactional
    public OrderPlacedResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseThrow(() -> new BusinessRuleException("Your cart is empty."));

        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("Your cart is empty.");
        }

        // ── Compute prices ────────────────────────────────────────────────────
        BigDecimal subtotal = cart.getItems().stream()
                .map(item -> item.getBook().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxAmount      = priceCalculator.computeTax(subtotal);
        BigDecimal deliveryCharge = computeDeliveryCharge(subtotal);
        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon     coupon         = null;

        if (StringUtils.hasText(request.getCouponCode())) {
            coupon        = findValidCoupon(userId, request.getCouponCode().trim(), subtotal);
            discountAmount = computeCouponDiscount(coupon, subtotal);
        }

        BigDecimal totalAmount = priceCalculator.computeTotal(
                subtotal, taxAmount, deliveryCharge, discountAmount);

        // ── Resolve delivery address ──────────────────────────────────────────
        Map<String, Object> addressSnapshot =
                resolveAddressSnapshot(userId, request.getDeliveryAddress());

        // ── Build and persist the order ───────────────────────────────────────
        String    orderNumber        = generateOrderNumber();
        LocalDate estimatedDelivery  = deliveryDateCalculator.calculate();
        User      user               = userRepository.getReferenceById(userId);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .status(OrderStatus.PLACED)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .deliveryCharge(deliveryCharge)
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .coupon(coupon)
                .deliveryAddressSnapshot(addressSnapshot)
                .placedAt(Instant.now())
                .estimatedDeliveryDate(estimatedDelivery)
                .build();
        order = orderRepository.save(order);

        // ── Snapshot order items ──────────────────────────────────────────────
        List<OrderItem> orderItems = new ArrayList<>();
        for (var cartItem : cart.getItems()) {
            var book     = cartItem.getBook();
            var qty      = cartItem.getQuantity();
            var lineTotal = book.getPrice().multiply(BigDecimal.valueOf(qty));

            orderItems.add(OrderItem.builder()
                    .order(order)
                    .book(book)
                    .title(book.getTitle())
                    .coverImageUrl(book.getCoverImageUrl())
                    .format(book.getFormat())
                    .unitPrice(book.getPrice())
                    .quantity(qty)
                    .lineTotal(lineTotal)
                    .build());
        }
        order.getItems().addAll(orderItems);
        order = orderRepository.save(order);

        log.info("Order {} placed for userId={}", order.getOrderNumber(), userId);

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(orderMapper::toItemResponse)
                .collect(Collectors.toList());

        return OrderPlacedResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .deliveryCharge(order.getDeliveryCharge())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .items(itemResponses)
                .build();
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    /**
     * Find a valid coupon: active, unexpired, minimum-order-met, and within the
     * per-user usage limit.
     *
     * <p>Usage is counted against CONFIRMED/SHIPPED/DELIVERED orders only —
     * PLACED (payment pending) and CANCELLED orders do not consume the coupon.
     */
    private Coupon findValidCoupon(Long userId, String code, BigDecimal subtotal) {
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrueAndExpiresAtAfter(
                        code, Instant.now())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));

        if (subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new BusinessRuleException(
                    "Minimum order value of ₹" + coupon.getMinOrderValue()
                            + " required to apply this coupon.");
        }

        // Per-user usage limit (0 = unlimited)
        int limit = coupon.getMaxUsagePerUser();
        if (limit > 0) {
            long used = orderRepository.countSuccessfulCouponUsages(
                    userId, coupon.getId(), SUCCESSFUL_STATUSES);
            if (used >= limit) {
                String msg = limit == 1
                        ? coupon.getCode() + " can only be used once per customer."
                        : coupon.getCode() + " can only be used " + limit + " times per customer.";
                throw new BusinessRuleException(msg);
            }
        }

        return coupon;
    }

    private BigDecimal computeCouponDiscount(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getDiscountType() == DiscountType.FIXED) {
            return priceCalculator.computeFixedDiscount(subtotal, coupon.getDiscountValue());
        }
        return priceCalculator.computePercentageDiscount(subtotal, coupon.getDiscountValue());
    }

    /**
     * Free delivery above the threshold; otherwise flat ₹50.
     */
    private BigDecimal computeDeliveryCharge(BigDecimal subtotal) {
        return subtotal.compareTo(
                BigDecimal.valueOf(AppConstants.Business.FREE_DELIVERY_THRESHOLD)) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("50.00");
    }

    private Map<String, Object> resolveAddressSnapshot(Long userId, DeliveryAddressRequest req) {
        if (req.getSavedAddressId() != null) {
            Address addr = addressRepository.findByIdAndUserId(req.getSavedAddressId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Address", "id", req.getSavedAddressId()));
            return addressToSnapshot(addr);
        }
        validateInlineAddress(req);
        return inlineToSnapshot(req);
    }

    private Map<String, Object> addressToSnapshot(Address addr) {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("firstName",    addr.getFirstName());
        snap.put("lastName",     addr.getLastName());
        snap.put("addressLine1", addr.getAddressLine1());
        snap.put("addressLine2", addr.getAddressLine2());
        snap.put("city",         addr.getCity());
        snap.put("state",        addr.getState());
        snap.put("country",      addr.getCountry());
        snap.put("pinCode",      addr.getPinCode());
        snap.put("phoneNumber",  addr.getPhoneNumber());
        snap.put("email",        addr.getEmail());
        return snap;
    }

    private Map<String, Object> inlineToSnapshot(DeliveryAddressRequest req) {
        Map<String, Object> snap = new LinkedHashMap<>();
        snap.put("firstName",    req.getFirstName());
        snap.put("lastName",     req.getLastName());
        snap.put("addressLine1", req.getAddressLine1());
        snap.put("addressLine2", req.getAddressLine2());
        snap.put("city",         req.getCity());
        snap.put("state",        req.getState());
        snap.put("country",      req.getCountry() != null ? req.getCountry() : "India");
        snap.put("pinCode",      req.getPinCode());
        snap.put("phoneNumber",  req.getPhoneNumber());
        snap.put("email",        req.getEmail());
        return snap;
    }

    private void validateInlineAddress(DeliveryAddressRequest req) {
        if (!StringUtils.hasText(req.getFirstName())
                || !StringUtils.hasText(req.getLastName())
                || !StringUtils.hasText(req.getAddressLine1())
                || !StringUtils.hasText(req.getCity())
                || !StringUtils.hasText(req.getState())
                || !StringUtils.hasText(req.getPinCode())
                || !StringUtils.hasText(req.getPhoneNumber())
                || !StringUtils.hasText(req.getEmail())) {
            throw new BadRequestException(
                    "All required address fields must be provided when not using a saved address.");
        }
    }

    /**
     * Generate a unique order number in the format {@code BST-YYYYMMDD-NNNNN}.
     */
    private String generateOrderNumber() {
        String datePart = LocalDate.now().toString().replace("-", "");
        long   seq      = orderRepository.count() + 1;
        return String.format("%s-%s-%05d",
                AppConstants.Business.ORDER_NUMBER_PREFIX, datePart, seq);
    }
}
