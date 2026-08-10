package com.bookstore.service;

import com.bookstore.common.exception.DuplicateResourceException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.common.response.PagedResponse;
import com.bookstore.domain.Book;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.User;
import com.bookstore.domain.Wishlist;
import com.bookstore.domain.WishlistItem;
import com.bookstore.dto.request.AddToWishlistRequest;
import com.bookstore.dto.response.MoveToCartResponse;
import com.bookstore.dto.response.WishlistItemCreatedResponse;
import com.bookstore.dto.response.WishlistItemResponse;
import com.bookstore.mapper.WishlistMapper;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.repository.WishlistItemRepository;
import com.bookstore.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for wishlist operations.
 *
 * <p>Wishlists are created lazily on first access.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository     wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final BookRepository         bookRepository;
    private final CartItemRepository     cartItemRepository;
    private final UserRepository         userRepository;
    private final WishlistMapper         wishlistMapper;
    private final CartService            cartService;

    // ── List Wishlist ──────────────────────────────────────────────────────────

    /**
     * Return a paginated list of the user's wishlist items.
     */
    @Transactional
    public PagedResponse<WishlistItemResponse> listWishlist(Long userId, int page, int size) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        Page<WishlistItem> itemPage = wishlistItemRepository
                .findByWishlistIdOrderByCreatedAtDesc(wishlist.getId(), PageRequest.of(page, size));

        List<WishlistItemResponse> content = itemPage.getContent()
                .stream()
                .map(wishlistMapper::toItemResponse)
                .collect(Collectors.toList());

        return PagedResponse.<WishlistItemResponse>builder()
                .content(content)
                .page(itemPage.getNumber())
                .size(itemPage.getSize())
                .totalElements(itemPage.getTotalElements())
                .totalPages(itemPage.getTotalPages())
                .last(itemPage.isLast())
                .build();
    }

    // ── Add to Wishlist ────────────────────────────────────────────────────────

    /**
     * Add a book to the user's wishlist.
     */
    @Transactional
    public WishlistItemCreatedResponse addToWishlist(Long userId, AddToWishlistRequest request) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        Book book = bookRepository.findByIdAndIsActiveTrue(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        if (wishlistItemRepository.existsByWishlistIdAndBookId(wishlist.getId(), book.getId())) {
            throw new DuplicateResourceException("Book is already in your wishlist.");
        }

        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .book(book)
                .build();
        item = wishlistItemRepository.save(item);

        log.debug("Book {} added to wishlist for userId={}", book.getId(), userId);
        return wishlistMapper.toCreatedResponse(item);
    }

    // ── Remove from Wishlist ───────────────────────────────────────────────────

    /**
     * Remove an item from the user's wishlist.
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long wishlistItemId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        WishlistItem item = wishlistItemRepository
                .findByIdAndWishlistId(wishlistItemId, wishlist.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WishlistItem", "id", wishlistItemId));
        wishlistItemRepository.delete(item);
        log.debug("WishlistItem {} removed for userId={}", wishlistItemId, userId);
    }

    // ── Move to Cart ───────────────────────────────────────────────────────────

    /**
     * Move a wishlist item into the shopping cart and remove it from the wishlist.
     * If the book is already in the cart its quantity is incremented by 1.
     */
    @Transactional
    public MoveToCartResponse moveToCart(Long userId, Long wishlistItemId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        WishlistItem item = wishlistItemRepository
                .findByIdAndWishlistId(wishlistItemId, wishlist.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WishlistItem", "id", wishlistItemId));

        Book book = item.getBook();
        var cart = cartService.getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository
                .findByCartIdAndBookId(cart.getId(), book.getId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + 1);
                    return cartItemRepository.save(existing);
                })
                .orElseGet(() -> cartItemRepository.save(
                        CartItem.builder().cart(cart).book(book).quantity(1).build()));

        wishlistItemRepository.delete(item);
        log.debug("WishlistItem {} moved to cart for userId={}", wishlistItemId, userId);

        return MoveToCartResponse.builder()
                .cartItemId(cartItem.getId())
                .bookId(book.getId())
                .build();
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    /**
     * Get the wishlist for a user, or create one if it does not yet exist.
     */
    @Transactional
    public Wishlist getOrCreateWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    Wishlist w = Wishlist.builder().user(user).build();
                    Wishlist saved = wishlistRepository.save(w);
                    log.debug("New wishlist created for userId={}", userId);
                    return saved;
                });
    }
}
