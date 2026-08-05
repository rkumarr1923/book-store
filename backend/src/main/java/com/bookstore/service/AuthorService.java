package com.bookstore.service;

import com.bookstore.common.exception.BusinessRuleException;
import com.bookstore.common.exception.DuplicateResourceException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.common.response.PagedResponse;
import com.bookstore.domain.Author;
import com.bookstore.domain.Book;
import com.bookstore.domain.User;
import com.bookstore.domain.UserFollowedAuthor;
import com.bookstore.domain.UserFollowedAuthorId;
import com.bookstore.dto.response.AuthorDetailResponse;
import com.bookstore.dto.response.AuthorFollowResponse;
import com.bookstore.dto.response.BookSummaryResponse;
import com.bookstore.dto.response.FollowedAuthorResponse;
import com.bookstore.mapper.AuthorMapper;
import com.bookstore.mapper.UserFollowedAuthorMapper;
import com.bookstore.repository.AuthorRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.UserFollowedAuthorRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for author profiles, follow/unfollow, and My Writers list.
 */
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository             authorRepository;
    private final UserFollowedAuthorRepository followRepository;
    private final UserRepository               userRepository;
    private final BookRepository               bookRepository;
    private final AuthorMapper                 authorMapper;
    private final UserFollowedAuthorMapper     followedAuthorMapper;
    private final BookService                  bookService;

    // ── Author Profile ─────────────────────────────────────────────────────────

    /**
     * Return the author profile including their paginated book list.
     *
     * @param authorId  the author's ID
     * @param userId    the requesting user's ID ({@code null} for guests)
     * @param page      page index
     * @param size      page size
     */
    @Transactional(readOnly = true)
    public AuthorDetailResponse findById(Long authorId, Long userId, int page, int size) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", authorId));

        boolean followed = (userId != null)
                && followRepository.existsByUserIdAndAuthorId(userId, authorId);

        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findByIsActiveTrueAndAuthorId(authorId, pageable);

        List<BookSummaryResponse> books = bookPage.getContent()
                .stream()
                .map(bookService::toSummaryWithComputedFields)
                .collect(Collectors.toList());

        PagedResponse<BookSummaryResponse> pagedBooks = PagedResponse.<BookSummaryResponse>builder()
                .content(books)
                .page(bookPage.getNumber())
                .size(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .last(bookPage.isLast())
                .build();

        AuthorDetailResponse base = authorMapper.toDetailResponse(author);
        return AuthorDetailResponse.builder()
                .id(base.getId())
                .name(base.getName())
                .biography(base.getBiography())
                .profileImageUrl(base.getProfileImageUrl())
                .isFollowed(followed)
                .books(pagedBooks)
                .build();
    }

    // ── Follow / Unfollow ──────────────────────────────────────────────────────

    /**
     * Follow an author.
     */
    @Transactional
    public AuthorFollowResponse follow(Long authorId, Long userId) {
        authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", authorId));

        if (followRepository.existsByUserIdAndAuthorId(userId, authorId)) {
            throw new DuplicateResourceException("You are already following this author.");
        }

        Author author = authorRepository.getReferenceById(authorId);
        User user = userRepository.getReferenceById(userId);

        UserFollowedAuthor relation = UserFollowedAuthor.builder()
                .id(new UserFollowedAuthorId(userId, authorId))
                .user(user)
                .author(author)
                .build();
        followRepository.save(relation);

        return AuthorFollowResponse.builder().authorId(authorId).followed(true).build();
    }

    /**
     * Unfollow an author.
     */
    @Transactional
    public AuthorFollowResponse unfollow(Long authorId, Long userId) {
        authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", authorId));

        if (!followRepository.existsByUserIdAndAuthorId(userId, authorId)) {
            throw new BusinessRuleException("You are not currently following this author.");
        }

        followRepository.deleteByUserIdAndAuthorId(userId, authorId);
        return AuthorFollowResponse.builder().authorId(authorId).followed(false).build();
    }

    // ── Followed Authors (My Writers) ──────────────────────────────────────────

    /**
     * Return the paginated list of authors the user follows.
     */
    @Transactional(readOnly = true)
    public PagedResponse<FollowedAuthorResponse> getFollowedAuthors(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserFollowedAuthor> followPage =
                followRepository.findByUserIdOrderByFollowedAtDesc(userId, pageable);

        List<FollowedAuthorResponse> content = followPage.getContent()
                .stream()
                .map(follow -> {
                    FollowedAuthorResponse base = followedAuthorMapper.toResponse(follow);

                    // Fetch up to 3 most recent books by this author
                    List<Book> recent =
                            bookRepository.findByIsActiveTrueAndAuthorIdOrderByPublishedDateDesc(
                                    follow.getAuthor().getId());
                    List<BookSummaryResponse> recentBooks = recent.stream()
                            .limit(3)
                            .map(bookService::toSummaryWithComputedFields)
                            .collect(Collectors.toList());

                    return FollowedAuthorResponse.builder()
                            .id(base.getId())
                            .name(base.getName())
                            .biography(base.getBiography())
                            .profileImageUrl(base.getProfileImageUrl())
                            .followedAt(base.getFollowedAt())
                            .recentBooks(recentBooks)
                            .build();
                })
                .collect(Collectors.toList());

        return PagedResponse.<FollowedAuthorResponse>builder()
                .content(content)
                .page(followPage.getNumber())
                .size(followPage.getSize())
                .totalElements(followPage.getTotalElements())
                .totalPages(followPage.getTotalPages())
                .last(followPage.isLast())
                .build();
    }
}
