import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Chip,
  Divider,
  Stack,
  Typography,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

import { formatCurrency } from '../../common/utils/formatCurrency';
import { formatDeliveryDate } from '../../common/utils/formatDate';
import { buildRoute, ROUTES } from '../../common/constants/routes';

const FORMAT_LABELS = { PAPERBACK: 'Paperback', HARDCOVER: 'Hard Cover', EBOOK: 'eBook' };

function OrderConfirmationPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const result = location.state?.result;

  // If the user lands here directly (e.g. via refresh), redirect home.
  useEffect(() => {
    if (!result) {
      navigate(ROUTES.HOME, { replace: true });
    }
  }, [result, navigate]);

  if (!result) return null;

  const order = result?.order;
  const items = order?.items || [];

  return (
    <Box
      display="flex"
      alignItems="center"
      justifyContent="center"
      minHeight="calc(100vh - 120px)"
      px={2}
      sx={{
        background: 'radial-gradient(ellipse at center top, rgba(13,71,161,0.35) 0%, transparent 60%)',
      }}
    >
      <Box
        sx={{
          bgcolor: 'background.paper',
          borderRadius: 3,
          p: { xs: 3, sm: 5 },
          maxWidth: 640,
          width: '100%',
          textAlign: 'center',
        }}
      >
        {/* Success icon */}
        <CheckCircleIcon sx={{ fontSize: 64, color: 'success.main', mb: 2 }} />

        <Typography variant="h5" fontWeight={700} mb={0.75}>
          Your purchase of the<br />following reads is successful
        </Typography>

        {result?.orderNumber && (
          <Typography variant="body2" color="text.secondary" mb={3}>
            Order #{result.orderNumber}
          </Typography>
        )}

        {/* Purchased books */}
        {items.length > 0 && (
          <Box
            display="grid"
            gridTemplateColumns="repeat(auto-fill, minmax(220px, 1fr))"
            gap={2}
            mb={4}
            textAlign="left"
          >
            {items.map((item, i) => (
              <Box
                key={item.id || i}
                display="flex"
                gap={1.5}
                sx={{
                  cursor: 'pointer',
                  '&:hover .item-title': { color: 'primary.main' },
                }}
                onClick={() => navigate(buildRoute.bookDetail(item.bookId))}
              >
                <Box
                  component="img"
                  src={item.coverImageUrl}
                  alt={item.title}
                  referrerPolicy="no-referrer"
                  sx={{
                    width: 72,
                    height: 100,
                    objectFit: 'cover',
                    borderRadius: 1,
                    flexShrink: 0,
                    bgcolor: 'rgba(255,255,255,0.04)',
                  }}
                  onError={(e) => { e.target.style.opacity = 0.3; }}
                />
                <Box flex={1} minWidth={0}>
                  <Typography
                    className="item-title"
                    variant="body2"
                    fontWeight={700}
                    sx={{ transition: 'color 0.15s', mb: 0.25 }}
                  >
                    {item.title}
                  </Typography>
                  <Typography variant="caption" color="primary.light" display="block" mb={0.5}>
                    by {item.author?.name}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" display="block">
                    {FORMAT_LABELS[item.format] || item.format}
                  </Typography>
                  <Stack direction="row" flexWrap="wrap" gap={0.5} my={0.5}>
                    {item.genres?.slice(0, 2).map((g) => (
                      <Chip key={g.id} label={g.name} size="small" variant="outlined" sx={{ borderColor: 'divider', fontSize: '0.65rem', height: 18 }} />
                    ))}
                  </Stack>
                  <Typography variant="body2" fontWeight={700}>{formatCurrency(item.unitPrice)}</Typography>
                  {item.estimatedDeliveryDate && (
                    <Typography variant="caption" color="text.secondary">
                      Delivery by {formatDeliveryDate(item.estimatedDeliveryDate)}
                    </Typography>
                  )}
                </Box>
              </Box>
            ))}
          </Box>
        )}

        <Divider sx={{ mb: 3 }} />

        <Button
          variant="contained"
          size="large"
          onClick={() => navigate(ROUTES.HOME)}
          endIcon={<span>🛒</span>}
        >
          Continue your Shopping
        </Button>
      </Box>
    </Box>
  );
}

export default OrderConfirmationPage;
