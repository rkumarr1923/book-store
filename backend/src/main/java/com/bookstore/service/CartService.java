package com.bookstore.service;

import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.common.util.DeliveryDateCalculator;
import com.bookstore.domain.Book;
import com.bookstore.domain.Cart;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.User;
import com.bookstore.dto.request.AddToCartRequest;
import com.bookstore.dto.request.UpdateCartItemRequest;
import com.bookstore.dto.response.CartItemCreatedResponse;
import com.bookstore.dto.response.CartItemResponse;
import com.bookstore.dto.response.CartResponse;
import com.bookstore.mapper.CartMapper;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.CartRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for shopping cart operations.
 *
 * <p>Carts are created lazily: the first time a user interacts with the cart
 * (add item, view, etc.) a Cart record is created if none exists yet.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository         cartRepository;
    private final CartItemRepository     cartItemRepository;
    private final BookRepository         bookRepository;
    private final UserRepository         userRepository;
    private final CartMapper             cartMapper;
    private final DeliveryDateCalculator deliveryDateCalculator;

    // ── Get Cart ───────────────────────────────────────────────────────────────

    /**
     * Return the current cart for a user, creating it if it does not yet exist.
     */
    @Transactional
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    // ── Add Item ───────────────────────────────────────────────────────────────

    /**
     * Add a book to the cart. If the book is already present its quantity is incremented.
     */
    @Transactional
    public CartItemCreatedResponse addItem(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Book book = bookRepository.findByIdAndIsActiveTrue(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        CartItem cartItem = cartItemRepository
                .findByCartIdAndBookId(cart.getId(), book.getId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.getQuantity());
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .book(book)
                            .quantity(request.getQuantity())
                            .build();
                    return cartItemRepository.save(newItem);
                });

        BigDecimal lineTotal = book.getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        log.debug("Cart item upserted: userId={}, bookId={}, qty={}", userId, book.getId(), cartItem.getQuantity());

        return CartItemCreatedResponse.builder()
                .cartItemId(cartItem.getId())
                .bookId(book.getId())
                .quantity(cartItem.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }

    // ── Update Item ────────────────────────────────────────────────────────────

    /**
     * Update the quantity of a specific cart item.
     * A quantity of 0 removes the item from the cart.
     */
    @Transactional
    public CartItemCreatedResponse updateItem(Long userId, Long cartItemId,
                                              UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));

        if (request.getQuantity() == 0) {
            cartItemRepository.delete(cartItem);
            log.debug("Cart item removed: cartItemId={}, userId={}", cartItemId, userId);
            return CartItemCreatedResponse.builder()
                    .cartItemId(cartItemId)
                    .bookId(cartItem.getBook().getId())
                    .quantity(0)
                    .lineTotal(BigDecimal.ZERO)
                    .build();
        }

        cartItem.setQuantity(request.getQuantity());
        cartItem = cartItemRepository.save(cartItem);

        BigDecimal lineTotal = cartItem.getBook().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemCreatedResponse.builder()
                .cartItemId(cartItem.getId())
                .bookId(cartItem.getBook().getId())
                .quantity(cartItem.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }

    // ── Remove Item ────────────────────────────────────────────────────────────

    /**
     * Remove a specific item from the cart.
     */
    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findByIdAndCartId(cartItemId, cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));
        cartItemRepository.delete(cartItem);
        log.debug("Cart item deleted: cartItemId={}, userId={}", cartItemId, userId);
    }

    // ── Clear Cart ─────────────────────────────────────────────────────────────

    /**
     * Remove all items from the user's cart.
     */
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCartId(cart.getId());
        log.debug("Cart cleared: userId={}", userId);
    }

    // ── Package-level helpers ──────────────────────────────────────────────────

    /**
     * Get the cart for a user, or create one if it does not yet exist.
     * Always runs in a write transaction so cart creation is safe.
     */
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    Cart newCart = Cart.builder().user(user).build();
                    Cart saved = cartRepository.save(newCart);
                    log.debug("New cart created for userId={}", userId);
                    return saved;
                });
    }

    /**
     * Build a full {@link CartResponse} from a cart entity.
     * Re-fetches items with JOIN FETCH to avoid N+1.
     */
    @Transactional(readOnly = true)
    public CartResponse buildCartResponse(Cart cart) {
        Cart loaded = cartRepository.findByUserIdWithItems(cart.getUser().getId())
                .orElse(cart);

        LocalDate delivery = deliveryDateCalculator.calculate();

        List<CartItemResponse> items = loaded.getItems().stream()
                .map(item -> {
                    CartItemResponse base = cartMapper.toItemResponse(item);
                    BigDecimal lineTotal = item.getBook().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));
                    return CartItemResponse.builder()
                            .cartItemId(base.getCartItemId())
                            .bookId(base.getBookId())
                            .title(base.getTitle())
                            .coverImageUrl(base.getCoverImageUrl())
                            .author(base.getAuthor())
                            .format(base.getFormat())
                            .genres(base.getGenres())
                            .unitPrice(base.getUnitPrice())
                            .quantity(base.getQuantity())
                            .lineTotal(lineTotal)
                            .estimatedDeliveryDate(delivery)
                            .build();
                })
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse base = cartMapper.toCartResponse(loaded);
        return CartResponse.builder()
                .cartId(base.getCartId())
                .itemCount(items.size())
                .items(items)
                .subtotal(subtotal)
                .updatedAt(base.getUpdatedAt())
                .build();
    }
}
