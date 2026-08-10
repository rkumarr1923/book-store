import React, { useState } from 'react';
import { useParams, useNavigate, Link as RouterLink } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Alert,
  Avatar,
  Box,
  Breadcrumbs,
  Button,
  Chip,
  CircularProgress,
  Divider,
  Link,
  Skeleton,
  Stack,
  TextField,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import FavoriteIcon from '@mui/icons-material/FavoriteBorder';
import NavigateNextIcon from '@mui/icons-material/NavigateNext';
import HomeIcon from '@mui/icons-material/Home';

import { bookApi } from '../../common/api/bookApi';
import { reviewApi } from '../../common/api/reviewApi';
import { wishlistApi } from '../../common/api/wishlistApi';
import { QUERY_KEYS } from '../../common/constants/queryKeys';
import { buildRoute, ROUTES } from '../../common/constants/routes';
import { formatCurrency } from '../../common/utils/formatCurrency';
import { formatDeliveryDate, formatLongDate } from '../../common/utils/formatDate';
import StarRating from '../../common/components/StarRating/StarRating';
import BookCard from '../../common/components/BookCard/BookCard';
import LoadingSpinner from '../../common/components/LoadingSpinner/LoadingSpinner';
import ErrorMessage from '../../common/components/ErrorMessage/ErrorMessage';
import EmptyState from '../../common/components/EmptyState/EmptyState';
import AppPagination from '../../common/components/Pagination/AppPagination';
import { useAuth } from '../../common/hooks/useAuth';
import { useCart } from '../../common/hooks/useCart';
import { useToast } from '../../common/context/ToastContext';

const FORMAT_LABELS = { PAPERBACK: 'Paperback', HARDCOVER: 'Hard Cover', EBOOK: 'eBook' };

// ─── Breadcrumb ───────────────────────────────────────────────────────────────

function BookBreadcrumb({ book }) {
  const genre = book?.genres?.[0];
  return (
    <Breadcrumbs separator={<NavigateNextIcon fontSize="inherit" />} sx={{ mb: 2 }}>
      <Link component={RouterLink} to={ROUTES.HOME} underline="hover" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
        <HomeIcon fontSize="inherit" />
        Home
      </Link>
      {genre && (
        <Link
          component={RouterLink}
          to={`${ROUTES.CATALOGUE}?genreSlug=${genre.slug}`}
          underline="hover"
          color="text.secondary"
        >
          {genre.name}
        </Link>
      )}
      <Typography color="text.primary" variant="body2" noWrap sx={{ maxWidth: 200 }}>
        {book?.title}
      </Typography>
    </Breadcrumbs>
  );
}

// ─── Related Reads Sidebar ────────────────────────────────────────────────────

function RelatedReadsSidebar({ bookId }) {
  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.BOOK_RELATED(bookId),
    queryFn: () => bookApi.getRelatedBooks(bookId),
    enabled: !!bookId,
  });

  const relatedBooks = data?.data || [];

  return (
    <Box sx={{ width: { xs: '100%', md: 260 }, flexShrink: 0 }}>
      <Typography variant="h6" fontWeight={700} mb={2}>
        Related Reads
      </Typography>
      {isLoading ? (
        <Stack spacing={2}>
          {[1, 2, 3].map((i) => (
            <Box key={i} display="flex" gap={1.5}>
              <Skeleton variant="rectangular" width={64} height={88} sx={{ borderRadius: 1 }} />
              <Box flex={1}>
                <Skeleton height={16} sx={{ mb: 0.5 }} />
                <Skeleton width="60%" height={14} sx={{ mb: 0.5 }} />
                <Skeleton width="40%" height={14} />
              </Box>
            </Box>
          ))}
        </Stack>
      ) : relatedBooks.length === 0 ? (
        <Typography variant="body2" color="text.secondary">No related books found.</Typography>
      ) : (
        <Stack spacing={2}>
          {relatedBooks.map((b) => (
            <BookCard key={b.id} book={b} compact />
          ))}
        </Stack>
      )}
    </Box>
  );
}

// ─── Reviews Section ──────────────────────────────────────────────────────────

function ReviewsSection({ bookId }) {
  const { isAuthenticated } = useAuth();
  const queryClient = useQueryClient();
  const toast = useToast();
  const [page, setPage] = useState(0);
  const [form, setForm] = useState({ rating: 0, reviewText: '' });
  const [submitMsg, setSubmitMsg] = useState(null);

  const { data, isLoading } = useQuery({
    queryKey: [...QUERY_KEYS.REVIEWS(bookId), page],
    queryFn: () => reviewApi.getReviews(bookId, { page, size: 5 }),
    enabled: !!bookId,
  });

  const reviews = data?.data?.content || [];
  const totalPages = data?.data?.totalPages || 0;

  const mutation = useMutation({
    mutationFn: (payload) => reviewApi.submitReview(bookId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.REVIEWS(bookId) });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.BOOK_DETAIL(bookId) });
      setForm({ rating: 0, reviewText: '' });
      setSubmitMsg(null);
      toast.success('Review submitted! Thank you for your feedback.');
    },
    onError: (err) => {
      setSubmitMsg({ type: 'error', text: err.response?.data?.message || 'Failed to submit review.' });
    },
  });

  const handleSubmit = () => {
    if (!form.rating) { setSubmitMsg({ type: 'error', text: 'Please select a star rating.' }); return; }
    mutation.mutate({ rating: form.rating, reviewText: form.reviewText });
  };

  return (
    <Box>
      <Typography variant="h6" fontWeight={700} mb={2}>
        Reviews
      </Typography>

      {/* Submit review */}
      {isAuthenticated ? (
        <Box
          sx={{
            bgcolor: 'background.paper',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 2,
            p: 2,
            mb: 3,
          }}
        >
          <Typography variant="body2" fontWeight={600} mb={1.5}>
            Leave Your Review
          </Typography>
          {submitMsg && (
            <Alert severity={submitMsg.type} sx={{ mb: 1.5 }} onClose={() => setSubmitMsg(null)}>
              {submitMsg.text}
            </Alert>
          )}
          <TextField
            fullWidth
            multiline
            rows={3}
            placeholder="Share your thoughts about this book…"
            value={form.reviewText}
            onChange={(e) => setForm((f) => ({ ...f, reviewText: e.target.value.slice(0, 100) }))}
            inputProps={{ maxLength: 100 }}
            helperText={`${form.reviewText.length}/100`}
            sx={{ mb: 1.5 }}
          />
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <StarRating
              value={form.rating}
              readOnly={false}
              onChange={(v) => setForm((f) => ({ ...f, rating: v }))}
              size="medium"
            />
            <Button
              variant="contained"
              onClick={handleSubmit}
              disabled={mutation.isPending}
              startIcon={mutation.isPending ? <CircularProgress size={14} color="inherit" /> : null}
            >
              Submit
            </Button>
          </Box>
        </Box>
      ) : (
        <Alert severity="info" sx={{ mb: 2 }}>
          Please sign in to leave a review.
        </Alert>
      )}

      {/* Reviews list */}
      {isLoading ? (
        <LoadingSpinner />
      ) : reviews.length === 0 ? (
        <EmptyState title="No reviews yet" subtitle="Be the first to review this book." />
      ) : (
        <>
          <Stack spacing={2} divider={<Divider />}>
            {reviews.map((r) => (
              <Box key={r.id}>
                <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={0.5}>
                  <Typography variant="body2" fontWeight={600}>{r.userName}</Typography>
                  <Typography variant="caption" color="text.secondary">{formatLongDate(r.createdAt)}</Typography>
                </Box>
                <StarRating value={r.rating} readOnly size="small" />
                {r.reviewText && (
                  <Typography variant="body2" color="text.secondary" mt={0.75}>
                    {r.reviewText}
                  </Typography>
                )}
              </Box>
            ))}
          </Stack>
          <AppPagination
            page={page + 1}
            totalPages={totalPages}
            onChange={(_, p) => setPage(p - 1)}
          />
        </>
      )}
    </Box>
  );
}

// ─── Book Detail Page ─────────────────────────────────────────────────────────

function BookDetailPage() {
  const { bookId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { addToCart } = useCart();
  const toast = useToast();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const queryClient = useQueryClient();

  const { data, isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.BOOK_DETAIL(bookId),
    queryFn: () => bookApi.getBookById(bookId),
    enabled: !!bookId,
  });

  const wishlistMutation = useMutation({
    mutationFn: () => wishlistApi.addToWishlist({ bookId: Number(bookId) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.WISHLIST });
      toast.success('Added to wishlist!');
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to add to wishlist.'),
  });

  const book = data?.data;

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      window.dispatchEvent(new Event('open:login'));
      return;
    }
    const res = await addToCart({ bookId: Number(bookId), quantity: 1 });
    if (res.success) {
      toast.success('Added to cart!');
    } else {
      toast.error(res.message);
    }
  };

  const handleAddToWishlist = () => {
    if (!isAuthenticated) {
      window.dispatchEvent(new Event('open:login'));
      return;
    }
    wishlistMutation.mutate();
  };

  if (isLoading) return <LoadingSpinner sx={{ mt: 8 }} />;
  if (isError || !book)
    return (
      <ErrorMessage
        message="Book not found or failed to load."
        variant="page"
        action={<Button onClick={() => navigate(-1)}>Go Back</Button>}
      />
    );

  return (
    <Box maxWidth={1400} mx="auto" px={{ xs: 2, md: 3 }} py={3}>
      <BookBreadcrumb book={book} />

      {/* Main layout */}
      <Box display="flex" gap={{ xs: 2, md: 4 }} flexDirection={{ xs: 'column', md: 'row' }}>
        {/* Left: main content */}
        <Box flex={1} minWidth={0}>
          <Box display="flex" gap={3} flexDirection={{ xs: 'column', sm: 'row' }}>
            {/* Cover */}
            <Box
              sx={{
                width: { xs: '100%', sm: 220 },
                flexShrink: 0,
              }}
            >
              <Box
                sx={{
                  position: 'relative',
                  width: '100%',
                  maxWidth: 220,
                  aspectRatio: '2/3',
                  borderRadius: 2,
                  overflow: 'hidden',
                  bgcolor: 'background.paper',
                }}
              >
                <Box
                  component="img"
                  src={book.coverImageUrl}
                  alt={book.title}
                  referrerPolicy="no-referrer"
                  sx={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
                  onError={(e) => {
                    e.target.style.display = 'none';
                    e.target.nextSibling.style.display = 'flex';
                  }}
                />
                <Box
                  sx={{
                    display: 'none',
                    position: 'absolute',
                    inset: 0,
                    alignItems: 'center',
                    justifyContent: 'center',
                    p: 2,
                  }}
                >
                  <Typography variant="body2" color="text.secondary" textAlign="center">
                    {book.title}
                  </Typography>
                </Box>
              </Box>
            </Box>

            {/* Details */}
            <Box flex={1}>
              <Typography variant="h4" fontWeight={700} mb={0.75} sx={{ lineHeight: 1.2 }}>
                {book.title}
              </Typography>

              <Typography variant="body1" mb={0.5}>
                by{' '}
                <Link
                  component={RouterLink}
                  to={buildRoute.authorProfile(book.author?.id)}
                  color="primary.light"
                  underline="hover"
                  fontWeight={600}
                >
                  {book.author?.name}
                </Link>
              </Typography>

              {book.publisher && (
                <Typography variant="body2" color="text.secondary" mb={1.5}>
                  Published by:{' '}
                  {book.publisher.website ? (
                    <Link href={book.publisher.website} target="_blank" rel="noreferrer" underline="hover" color="text.secondary">
                      {book.publisher.name}
                    </Link>
                  ) : (
                    book.publisher.name
                  )}
                </Typography>
              )}

              <Typography variant="body2" color="text.secondary" mb={1.5}>
                {book.description}
              </Typography>

              <Typography variant="body2" color="text.secondary" mb={1}>
                {FORMAT_LABELS[book.format] || book.format}
              </Typography>

              {/* Genre chips */}
              <Stack direction="row" flexWrap="wrap" gap={0.75} mb={2}>
                {book.genres?.map((g) => (
                  <Chip
                    key={g.id}
                    label={g.name}
                    size="small"
                    variant="outlined"
                    clickable
                    onClick={() => navigate(`${ROUTES.CATALOGUE}?genreSlug=${g.slug}`)}
                    sx={{ borderColor: 'divider' }}
                  />
                ))}
              </Stack>

              {/* Price + Delivery */}
              <Typography variant="h5" fontWeight={700} mb={0.5}>
                {formatCurrency(book.price)}
              </Typography>
              {book.estimatedDeliveryDate && (
                <Typography variant="body2" color="text.secondary" mb={2}>
                  Delivery by {formatDeliveryDate(book.estimatedDeliveryDate)}
                </Typography>
              )}

              {/* Cart / Wishlist actions */}
              <Stack direction="row" gap={1.5} mb={2} flexWrap="wrap">
                <Button
                  variant="contained"
                  startIcon={<ShoppingCartIcon />}
                  onClick={handleAddToCart}
                  sx={{ minWidth: 160 }}
                >
                  Add to Cart
                </Button>
                <Button
                  variant="outlined"
                  startIcon={<FavoriteIcon />}
                  onClick={handleAddToWishlist}
                  disabled={wishlistMutation.isPending}
                  sx={{ minWidth: 160 }}
                >
                  Add to Wishlist
                </Button>
              </Stack>

              {/* Meta row */}
              <Stack direction="row" gap={3} flexWrap="wrap">
                <Box>
                  <Typography variant="caption" color="text.secondary">Language</Typography>
                  <Typography variant="body2">{book.language}</Typography>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Rating</Typography>
                  <Box display="flex" alignItems="center" gap={0.5}>
                    <StarRating value={book.averageRating} readOnly showLabel />
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" color="text.secondary">Sells</Typography>
                  <Typography variant="body2">{book.copiesSold?.toLocaleString('en-IN')} copies sold</Typography>
                </Box>
              </Stack>
            </Box>
          </Box>

          <Divider sx={{ my: 3 }} />

          {/* About the Writer */}
          {book.author && (
            <Box mb={3}>
              <Typography variant="h6" fontWeight={700} mb={2}>
                About the writer
              </Typography>
              <Box display="flex" gap={2} flexDirection={{ xs: 'column', sm: 'row' }}>
                <Avatar
                  src={book.author.profileImageUrl}
                  alt={book.author.name}
                  sx={{ width: 72, height: 72, flexShrink: 0, cursor: 'pointer' }}
                  onClick={() => navigate(buildRoute.authorProfile(book.author.id))}
                />
                <Box>
                  <Typography
                    variant="body1"
                    fontWeight={700}
                    mb={0.5}
                    sx={{ cursor: 'pointer', '&:hover': { color: 'primary.main' } }}
                    onClick={() => navigate(buildRoute.authorProfile(book.author.id))}
                  >
                    {book.author.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {book.author.biography}
                  </Typography>
                </Box>
              </Box>
            </Box>
          )}

          <Divider sx={{ my: 3 }} />

          {/* Reviews */}
          <ReviewsSection bookId={bookId} />
        </Box>

        {/* Right: Related Reads */}
        <RelatedReadsSidebar bookId={bookId} />
      </Box>
    </Box>
  );
}

export default BookDetailPage;
