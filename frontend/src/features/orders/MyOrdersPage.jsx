import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import {
  Box,
  Button,
  Chip,
  CircularProgress,
  Stack,
  Typography,
} from '@mui/material';
import ShoppingBagIcon from '@mui/icons-material/ShoppingBag';

import { orderApi } from '../../common/api/orderApi';
import { QUERY_KEYS } from '../../common/constants/queryKeys';
import { buildRoute, ROUTES } from '../../common/constants/routes';
import { formatCurrency } from '../../common/utils/formatCurrency';
import { formatDeliveryDate, formatLongDate } from '../../common/utils/formatDate';
import LoadingSpinner from '../../common/components/LoadingSpinner/LoadingSpinner';
import EmptyState from '../../common/components/EmptyState/EmptyState';
import ErrorMessage from '../../common/components/ErrorMessage/ErrorMessage';
import AppPagination from '../../common/components/Pagination/AppPagination';
import { useCart } from '../../common/hooks/useCart';
import { useToast } from '../../common/context/ToastContext';

const FORMAT_LABELS = { PAPERBACK: 'Paperback', HARDCOVER: 'Hard Cover', EBOOK: 'eBook' };

const STATUS_COLORS = {
  PLACED: 'warning',
  CONFIRMED: 'primary',
  SHIPPED: 'info',
  DELIVERED: 'success',
  CANCELLED: 'error',
};

// Orders with PLACED status haven't been paid yet — don't show them in history
const VISIBLE_STATUSES = new Set(['CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED']);

// ─── Order Card ───────────────────────────────────────────────────────────────

function OrderCard({ order, onBuyAgain, isBuyingAgain }) {
  const navigate = useNavigate();
  const items = order.items || [];

  return (
    <Box
      sx={{
        bgcolor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        p: 2.5,
      }}
    >
      {/* Header */}
      <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2} flexWrap="wrap" gap={1}>
        <Box>
          <Typography variant="body2" color="text.secondary" mb={0.25}>
            Order #{order.orderNumber}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Placed on {formatLongDate(order.placedAt)}
          </Typography>
        </Box>
        <Box display="flex" gap={1.5} alignItems="center">
          <Chip
            label={order.status}
            size="small"
            color={STATUS_COLORS[order.status] || 'default'}
            variant="outlined"
          />
          <Typography variant="body1" fontWeight={700}>
            {formatCurrency(order.totalAmount)}
          </Typography>
        </Box>
      </Box>

      {/* Items */}
      <Stack spacing={1.5} mb={2}>
        {items.slice(0, 3).map((item, i) => (
          <Box
            key={item.id || i}
            display="flex"
            gap={1.5}
            alignItems="flex-start"
            sx={{ cursor: 'pointer', '&:hover .item-title': { color: 'primary.main' } }}
            onClick={() => navigate(buildRoute.bookDetail(item.bookId))}
          >
            <Box
              component="img"
              src={item.coverImageUrl}
              alt={item.title}
              referrerPolicy="no-referrer"
              sx={{ width: 48, height: 64, objectFit: 'cover', borderRadius: 0.75, flexShrink: 0, bgcolor: 'rgba(255,255,255,0.04)' }}
              onError={(e) => { e.target.style.opacity = 0.3; }}
            />
            <Box>
              <Typography className="item-title" variant="body2" fontWeight={600} sx={{ transition: 'color 0.15s' }}>
                {item.title}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {FORMAT_LABELS[item.format] || item.format} · Qty {item.quantity} · {formatCurrency(item.unitPrice)}
              </Typography>
            </Box>
          </Box>
        ))}
        {items.length > 3 && (
          <Typography variant="caption" color="text.secondary">
            +{items.length - 3} more items
          </Typography>
        )}
      </Stack>

      {order.estimatedDeliveryDate && (
        <Typography variant="caption" color="text.secondary" display="block" mb={2}>
          {order.status === 'DELIVERED' ? 'Delivered on' : 'Est. delivery by'}{' '}
          {formatDeliveryDate(order.estimatedDeliveryDate)}
        </Typography>
      )}

      {/* Actions */}
      <Box display="flex" gap={1} justifyContent="flex-end">
        <Button
          variant="contained"
          size="small"
          startIcon={isBuyingAgain ? <CircularProgress size={14} color="inherit" /> : <ShoppingBagIcon fontSize="small" />}
          onClick={() => onBuyAgain(order.id)}
          disabled={isBuyingAgain}
        >
          Buy Again
        </Button>
      </Box>
    </Box>
  );
}

// ─── My Orders Page ───────────────────────────────────────────────────────────

function MyOrdersPage() {
  const navigate = useNavigate();
  const { invalidateCart, syncCartCount } = useCart();
  const toast = useToast();
  const [page, setPage] = useState(0);
  const [buyingAgainId, setBuyingAgainId] = useState(null);

  const { data, isLoading, isError } = useQuery({
    queryKey: [...QUERY_KEYS.ORDERS, page],
    queryFn: () => orderApi.getOrders({ page, size: 10 }),
  });

  const allOrders = data?.data?.content || [];
  const orders = allOrders.filter((o) => VISIBLE_STATUSES.has(o.status));
  const totalPages = data?.data?.totalPages || 0;

  const buyAgainMutation = useMutation({
    mutationFn: (orderId) => orderApi.buyAgain(orderId),
    onMutate: (id) => setBuyingAgainId(id),
    onSuccess: async (res) => {
      invalidateCart();
      await syncCartCount();
      const count = res.data?.itemsAdded;
      toast.success(`${count ? `${count} item(s)` : 'Items'} added to cart.`);
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to add items to cart.'),
    onSettled: () => setBuyingAgainId(null),
  });

  return (
    <Box maxWidth={900} mx="auto" px={{ xs: 2, md: 4 }} py={4}>
      <Typography variant="h4" fontWeight={700} mb={0.5}>
        My Orders
      </Typography>
      {orders.length > 0 && (
        <Typography variant="body2" color="text.secondary" mb={3}>
          {orders.length} {orders.length === 1 ? 'order' : 'orders'}
        </Typography>
      )}

      {isLoading ? (
        <LoadingSpinner />
      ) : isError ? (
        <ErrorMessage message="Failed to load orders. Please try again." variant="page" />
      ) : orders.length === 0 ? (
        <EmptyState
          title="No orders yet"
          subtitle="Your purchase history will appear here after your first order."
          action={<Button variant="contained" onClick={() => navigate(ROUTES.HOME)}>Start Shopping</Button>}
        />
      ) : (
        <Stack spacing={2}>
          {orders.map((order) => (
            <OrderCard
              key={order.id}
              order={order}
              onBuyAgain={(id) => buyAgainMutation.mutate(id)}
              isBuyingAgain={buyingAgainId === order.id}
            />
          ))}
        </Stack>
      )}

      <AppPagination page={page + 1} totalPages={totalPages} onChange={(_, p) => setPage(p - 1)} />
    </Box>
  );
}

export default MyOrdersPage;
