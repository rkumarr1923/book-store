package com.bookstore.service;

import com.bookstore.common.exception.DuplicateResourceException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.domain.Book;
import com.bookstore.domain.Review;
import com.bookstore.domain.User;
import com.bookstore.dto.request.CreateReviewRequest;
import com.bookstore.dto.response.ReviewPageResponse;
import com.bookstore.dto.response.ReviewResponse;
import com.bookstore.mapper.ReviewMapper;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.ReviewRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for book reviews.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository   bookRepository;
    private final UserRepository   userRepository;
    private final ReviewMapper     reviewMapper;

    // ── List Reviews ───────────────────────────────────────────────────────────

    /**
     * Return paginated reviews for a book, including aggregate statistics.
     */
    @Transactional(readOnly = true)
    public ReviewPageResponse listReviews(Long bookId, int page, int size) {
        // Verify book exists
        bookRepository.findByIdAndIsActiveTrue(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        Page<Review> reviewPage =
                reviewRepository.findByBookIdOrderByCreatedAtDesc(bookId, PageRequest.of(page, size));

        Double avgRating  = reviewRepository.findAverageRatingByBookId(bookId);
        long   totalCount = reviewRepository.countByBookId(bookId);

        List<ReviewResponse> content = reviewPage.getContent()
                .stream()
                .map(this::toResponseWithUserName)
                .collect(Collectors.toList());

        return ReviewPageResponse.builder()
                .averageRating(avgRating)
                .reviewCount(totalCount)
                .content(content)
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .last(reviewPage.isLast())
                .build();
    }

    // ── Create Review ──────────────────────────────────────────────────────────

    /**
     * Submit a new review. One review per user per book is enforced.
     */
    @Transactional
    public ReviewResponse createReview(Long bookId, Long userId, CreateReviewRequest request) {
        Book book = bookRepository.findByIdAndIsActiveTrue(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new DuplicateResourceException(
                    "You have already submitted a review for this book.");
        }

        User user = userRepository.getReferenceById(userId);

        Review review = reviewMapper.toEntity(request);
        review.setBook(book);
        review.setUser(user);
        review = reviewRepository.save(review);

        return toResponseWithUserName(review);
    }

    // ── Private Helpers ────────────────────────────────────────────────────────

    private ReviewResponse toResponseWithUserName(Review review) {
        ReviewResponse base = reviewMapper.toResponse(review);
        String userName = review.getUser().getFirstName() + " " + review.getUser().getLastName();
        return ReviewResponse.builder()
                .id(base.getId())
                .userId(base.getUserId())
                .userName(userName)
                .rating(base.getRating())
                .reviewText(base.getReviewText())
                .createdAt(base.getCreatedAt())
                .build();
    }
}
