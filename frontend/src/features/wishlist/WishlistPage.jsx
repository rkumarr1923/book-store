import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Divider,
  IconButton,
  Snackbar,
  Stack,
  Tooltip,
  Typography,
} from '@mui/material';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';

import { wishlistApi } from '../../common/api/wishlistApi';
import { QUERY_KEYS } from '../../common/constants/queryKeys';
import { buildRoute, ROUTES } from '../../common/constants/routes';
import { formatCurrency } from '../../common/utils/formatCurrency';
import { formatDeliveryDate } from '../../common/utils/formatDate';
import LoadingSpinner from '../../common/components/LoadingSpinner/LoadingSpinner';
import EmptyState from '../../common/components/EmptyState/EmptyState';
import ErrorMessage from '../../common/components/ErrorMessage/ErrorMessage';
import AppPagination from '../../common/components/Pagination/AppPagination';
import { useCart } from '../../common/hooks/useCart';

const FORMAT_LABELS = { PAPERBACK: 'Paperback', HARDCOVER: 'Hard Cover', EBOOK: 'eBook' };

// ─── Single wishlist item row ─────────────────────────────────────────────────

function WishlistItemCard({ item, onRemove, onMoveToCart, mutatingId }) {
  const navigate = useNavigate();
  const { book, wishlistItemId } = item;
  const isBusy = mutatingId === wishlistItemId;

  return (
    <Card
      sx={{
        display: 'flex',
        gap: 2,
        p: 2,
        alignItems: { xs: 'flex-start', sm: 'center' },
        flexDirection: { xs: 'column', sm: 'row' },
      }}
    >
      {/* Cover */}
      <Box
        component="img"
        src={book?.coverImageUrl}
        alt={book?.title}
        sx={{
          width: 80,
          height: 110,
          objectFit: 'cover',
          borderRadius: 1,
          flexShrink: 0,
          cursor: 'pointer',
          bgcolor: 'rgba(255,255,255,0.04)',
        }}
        onClick={() => navigate(buildRoute.bookDetail(book?.id))}
        onError={(e) => { e.target.style.opacity = 0.3; }}
      />

      {/* Details */}
      <Box flex={1} minWidth={0}>
        <Typography
          variant="body1"
          fontWeight={700}
          sx={{ cursor: 'pointer', '&:hover': { color: 'primary.main' }, mb: 0.25 }}
          onClick={() => navigate(buildRoute.bookDetail(book?.id))}
        >
          {book?.title}
        </Typography>
        <Typography
          variant="body2"
          color="primary.light"
          sx={{ cursor: 'pointer', '&:hover': { textDecoration: 'underline' }, mb: 0.5 }}
          onClick={() => navigate(buildRoute.authorProfile(book?.author?.id))}
        >
          by {book?.author?.name}
        </Typography>
        <Typography variant="caption" color="text.secondary" display="block" mb={0.5}>
          {FORMAT_LABELS[book?.format] || book?.format}
        </Typography>
        <Stack direction="row" flexWrap="wrap" gap={0.5} mb={1}>
          {book?.genres?.slice(0, 2).map((g) => (
            <Chip key={g.id} label={g.name} size="small" variant="outlined" sx={{ borderColor: 'divider', fontSize: '0.68rem', height: 20 }} />
          ))}
        </Stack>
        <Typography variant="body1" fontWeight={700}>{formatCurrency(book?.price)}</Typography>
        {book?.estimatedDeliveryDate && (
          <Typography variant="caption" color="text.secondary">
            Delivery by {formatDeliveryDate(book.estimatedDeliveryDate)}
          </Typography>
        )}
      </Box>

      {/* Actions */}
      <Stack direction={{ xs: 'row', sm: 'column' }} gap={1} flexShrink={0}>
        <Button
          variant="contained"
          size="small"
          startIcon={isBusy ? <CircularProgress size={14} color="inherit" /> : <ShoppingCartIcon fontSize="small" />}
          onClick={() => onMoveToCart(wishlistItemId)}
          disabled={isBusy}
          sx={{ whiteSpace: 'nowrap' }}
        >
          Move to Cart
        </Button>
        <Button
          variant="outlined"
          size="small"
          color="error"
          startIcon={isBusy ? <CircularProgress size={14} color="inherit" /> : <DeleteOutlineIcon fontSize="small" />}
          onClick={() => onRemove(wishlistItemId)}
          disabled={isBusy}
        >
          Remove
        </Button>
      </Stack>
    </Card>
  );
}

// ─── Wishlist Page ────────────────────────────────────────────────────────────

function WishlistPage() {
  const queryClient = useQueryClient();
  const { invalidateCart } = useCart();
  const [page, setPage] = useState(0);
  const [mutatingId, setMutatingId] = useState(null);
  const [snack, setSnack] = useState(null);

  const { data, isLoading, isError } = useQuery({
    queryKey: [...QUERY_KEYS.WISHLIST, page],
    queryFn: () => wishlistApi.getWishlist({ page, size: 10 }),
  });

  const items = data?.data?.content || [];
  const totalPages = data?.data?.totalPages || 0;
  const totalElements = data?.data?.totalElements || 0;

  const removeMutation = useMutation({
    mutationFn: (id) => wishlistApi.removeFromWishlist(id),
    onMutate: (id) => setMutatingId(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.WISHLIST });
      setSnack({ type: 'success', msg: 'Removed from wishlist.' });
    },
    onError: (err) => setSnack({ type: 'error', msg: err.response?.data?.message || 'Failed to remove.' }),
    onSettled: () => setMutatingId(null),
  });

  const moveToCartMutation = useMutation({
    mutationFn: (id) => wishlistApi.moveToCart(id),
    onMutate: (id) => setMutatingId(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.WISHLIST });
      invalidateCart();
      setSnack({ type: 'success', msg: 'Moved to cart!' });
    },
    onError: (err) => setSnack({ type: 'error', msg: err.response?.data?.message || 'Failed to move to cart.' }),
    onSettled: () => setMutatingId(null),
  });

  return (
    <Box maxWidth={900} mx="auto" px={{ xs: 2, md: 4 }} py={4}>
      <Typography variant="h4" fontWeight={700} mb={0.5}>
        My Wishlist
      </Typography>
      {totalElements > 0 && (
        <Typography variant="body2" color="text.secondary" mb={3}>
          {totalElements} {totalElements === 1 ? 'item' : 'items'} saved
        </Typography>
      )}

      {isLoading ? (
        <LoadingSpinner />
      ) : isError ? (
        <ErrorMessage message="Failed to load wishlist. Please try again." variant="page" />
      ) : items.length === 0 ? (
        <EmptyState
          title="Your wishlist is empty"
          subtitle="Save books you love and come back to them anytime."
          action={
            <Button variant="contained" href={ROUTES.HOME}>
              Browse Books
            </Button>
          }
        />
      ) : (
        <Stack spacing={2}>
          {items.map((item) => (
            <WishlistItemCard
              key={item.wishlistItemId}
              item={item}
              onRemove={(id) => removeMutation.mutate(id)}
              onMoveToCart={(id) => moveToCartMutation.mutate(id)}
              mutatingId={mutatingId}
            />
          ))}
        </Stack>
      )}

      <AppPagination
        page={page + 1}
        totalPages={totalPages}
        onChange={(_, p) => setPage(p - 1)}
      />

      <Snackbar
        open={!!snack}
        autoHideDuration={3000}
        onClose={() => setSnack(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        {snack ? (
          <Alert severity={snack.type} onClose={() => setSnack(null)} sx={{ width: '100%' }}>
            {snack.msg}
          </Alert>
        ) : <span />}
      </Snackbar>
    </Box>
  );
}

export default WishlistPage;
