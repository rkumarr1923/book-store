import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  CircularProgress,
  Divider,
  FormControlLabel,
  IconButton,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';

import { cartApi } from '../../common/api/cartApi';
import { checkoutApi } from '../../common/api/checkoutApi';
import { userApi } from '../../common/api/userApi';
import { QUERY_KEYS } from '../../common/constants/queryKeys';
import { formatCurrency } from '../../common/utils/formatCurrency';
import { formatDeliveryDate } from '../../common/utils/formatDate';
import { buildRoute, ROUTES } from '../../common/constants/routes';
import LoadingSpinner from '../../common/components/LoadingSpinner/LoadingSpinner';
import EmptyState from '../../common/components/EmptyState/EmptyState';
import ErrorMessage from '../../common/components/ErrorMessage/ErrorMessage';
import { useCart } from '../../common/hooks/useCart';
import { useAuth } from '../../common/hooks/useAuth';
import { useToast } from '../../common/context/ToastContext';
import PaymentModal from '../payment/PaymentModal';

const FORMAT_LABELS = { PAPERBACK: 'Paperback', HARDCOVER: 'Hard Cover', EBOOK: 'eBook' };

// ─── Cart Item Row ────────────────────────────────────────────────────────────

function CartItemRow({ item, onQuantityChange, onRemove, isPending }) {
  const navigate = useNavigate();
  return (
    <Box
      sx={{
        display: 'flex',
        gap: 2,
        p: 2,
        bgcolor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        flexDirection: { xs: 'column', sm: 'row' },
        alignItems: { xs: 'flex-start', sm: 'center' },
      }}
    >
      {/* Cover */}
      <Box
        component="img"
        src={item.coverImageUrl}
        alt={item.title}
        referrerPolicy="no-referrer"
        sx={{
          width: 100,
          height: 140,
          objectFit: 'cover',
          borderRadius: 1,
          flexShrink: 0,
          cursor: 'pointer',
          bgcolor: 'rgba(255,255,255,0.04)',
        }}
        onClick={() => navigate(buildRoute.bookDetail(item.bookId))}
        onError={(e) => { e.target.style.opacity = 0.3; }}
      />

      {/* Details */}
      <Box flex={1} minWidth={0}>
        <Typography
          variant="body1"
          fontWeight={700}
          sx={{ cursor: 'pointer', '&:hover': { color: 'primary.main' }, mb: 0.25 }}
          onClick={() => navigate(buildRoute.bookDetail(item.bookId))}
        >
          {item.title}
        </Typography>
        <Typography
          variant="body2"
          color="primary.light"
          sx={{ cursor: 'pointer', '&:hover': { textDecoration: 'underline' }, mb: 0.5 }}
          onClick={() => navigate(buildRoute.authorProfile(item.author?.id))}
        >
          by {item.author?.name}
        </Typography>
        <Typography variant="caption" color="text.secondary" display="block" mb={0.5}>
          {FORMAT_LABELS[item.format] || item.format}
        </Typography>
        <Stack direction="row" flexWrap="wrap" gap={0.5} mb={1}>
          {item.genres?.slice(0, 2).map((g) => (
            <Chip key={g.id} label={g.name} size="small" variant="outlined" sx={{ borderColor: 'divider', fontSize: '0.68rem', height: 20 }} />
          ))}
        </Stack>
        <Typography variant="body1" fontWeight={700}>{formatCurrency(item.unitPrice)}</Typography>
        {item.estimatedDeliveryDate && (
          <Typography variant="caption" color="text.secondary">
            Delivery by {formatDeliveryDate(item.estimatedDeliveryDate)}
          </Typography>
        )}
      </Box>

      {/* Qty + Remove */}
      <Box display="flex" alignItems="center" gap={1} flexShrink={0}>
        <IconButton
          size="small"
          onClick={() => onQuantityChange(item.cartItemId, item.quantity - 1)}
          disabled={isPending || item.quantity <= 1}
        >
          <RemoveIcon fontSize="small" />
        </IconButton>
        <Typography variant="body1" fontWeight={700} minWidth={24} textAlign="center">
          {item.quantity}
        </Typography>
        <IconButton
          size="small"
          onClick={() => onQuantityChange(item.cartItemId, item.quantity + 1)}
          disabled={isPending}
        >
          <AddIcon fontSize="small" />
        </IconButton>
        <Tooltip title="Remove">
          <IconButton
            size="small"
            color="error"
            onClick={() => onRemove(item.cartItemId)}
            disabled={isPending}
          >
            <DeleteOutlineIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </Box>
    </Box>
  );
}

// ─── Address Form ─────────────────────────────────────────────────────────────

const EMPTY_ADDRESS = {
  firstName: '', lastName: '', addressLine1: '', addressLine2: '',
  city: '', state: '', country: 'India', pinCode: '', email: '', phoneNumber: '',
};

function AddressForm({ value, onChange, errors = {}, onErrorClear }) {
  const set = (k) => (e) => {
    let val = e.target.value;
    if (k === 'phoneNumber') {
      val = val.replace(/\D/g, '').slice(0, 10);
    } else if (k === 'pinCode') {
      val = val.replace(/\D/g, '').slice(0, 6);
    }
    onChange({ ...value, [k]: val });
    if (onErrorClear) onErrorClear(k);
  };

  return (
    <Box display="grid" gridTemplateColumns={{ xs: '1fr', sm: '1fr 1fr' }} gap={1.5}>
      <TextField label="First Name" value={value.firstName} onChange={set('firstName')} error={!!errors.firstName} helperText={errors.firstName} size="small" />
      <TextField label="Last Name" value={value.lastName} onChange={set('lastName')} error={!!errors.lastName} helperText={errors.lastName} size="small" />
      <TextField label="Address Line 1" value={value.addressLine1} onChange={set('addressLine1')} error={!!errors.addressLine1} helperText={errors.addressLine1} size="small" sx={{ gridColumn: { sm: '1 / -1' } }} />
      <TextField label="Address Line 2 (optional)" value={value.addressLine2} onChange={set('addressLine2')} size="small" sx={{ gridColumn: { sm: '1 / -1' } }} />
      <TextField label="e-mail" type="email" value={value.email} onChange={set('email')} error={!!errors.email} helperText={errors.email} size="small" />
      <TextField label="City" value={value.city} onChange={set('city')} error={!!errors.city} helperText={errors.city} size="small" />
      <TextField label="Pin" value={value.pinCode} onChange={set('pinCode')} error={!!errors.pinCode} helperText={errors.pinCode} size="small" inputProps={{ maxLength: 6 }} />
      <TextField
        label="Phone Number"
        value={value.phoneNumber}
        onChange={set('phoneNumber')}
        error={!!errors.phoneNumber}
        helperText={errors.phoneNumber}
        size="small"
        inputProps={{ maxLength: 10 }}
        InputProps={{
          startAdornment: <Typography variant="body2" sx={{ mr: 0.5, color: 'text.secondary' }}>+91</Typography>,
        }}
      />
      <TextField label="State" value={value.state} onChange={set('state')} error={!!errors.state} helperText={errors.state} size="small" />
      <TextField label="Country" value={value.country} onChange={set('country')} size="small" />
    </Box>
  );
}

function validateAddress(addr) {
  const e = {};
  if (!addr.firstName?.trim()) e.firstName = 'Required';
  if (!addr.lastName?.trim()) e.lastName = 'Required';
  if (!addr.addressLine1?.trim()) e.addressLine1 = 'Required';
  if (!addr.city?.trim()) e.city = 'Required';
  if (!addr.state?.trim()) e.state = 'Required';
  if (!addr.pinCode?.trim()) e.pinCode = 'Required';
  else if (!/^\d{6}$/.test(addr.pinCode)) e.pinCode = 'Must be 6 digits';
  if (!addr.email?.trim()) e.email = 'Required';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(addr.email)) e.email = 'Invalid email';
  if (!addr.phoneNumber?.trim()) e.phoneNumber = 'Required';
  else if (!/^\d{10}$/.test(addr.phoneNumber)) e.phoneNumber = 'Must be exactly 10 digits';
  return e;
}

// ─── Grand Total Panel ────────────────────────────────────────────────────────

function GrandTotalPanel({ summary, coupon, setCoupon, onApplyCoupon, couponLoading, couponError, couponApplied, onPayNow, payLoading }) {
  const [couponInput, setCouponInput] = useState(coupon);

  return (
    <Box
      sx={{
        bgcolor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        overflow: 'hidden',
      }}
    >
      {/* Decorative image strip matching screenshot */}
      <Box
        sx={{
          height: 120,
          background: 'linear-gradient(135deg, #0d47a1 0%, #1565c0 50%, #0d47a1 100%)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          overflow: 'hidden',
        }}
      >
        <LocalOfferIcon sx={{ fontSize: 48, color: 'rgba(255,255,255,0.3)' }} />
      </Box>

      <Box p={2.5}>
        <Typography variant="h6" fontWeight={700} mb={2}>
          Grand Total
        </Typography>

        <Stack spacing={1} mb={2}>
          <Box display="flex" justifyContent="space-between">
            <Typography variant="body2" color="text.secondary">
              Price ({summary?.items?.length || 0} items)
            </Typography>
            <Typography variant="body2">{formatCurrency(summary?.subtotal)}</Typography>
          </Box>
          <Box display="flex" justifyContent="space-between">
            <Typography variant="body2" color="text.secondary">Tax</Typography>
            <Typography variant="body2">{formatCurrency(summary?.taxAmount)}</Typography>
          </Box>
          <Box display="flex" justifyContent="space-between">
            <Typography variant="body2" color="text.secondary">Delivery Charges</Typography>
            <Typography variant="body2" color="success.main">
              {summary?.deliveryCharge === 0 ? 'Free' : formatCurrency(summary?.deliveryCharge)}
            </Typography>
          </Box>
        </Stack>

        {/* Coupon */}
        <Box display="flex" gap={1} mb={1}>
          <TextField
            size="small"
            placeholder="Apply Coupon"
            value={couponInput}
            onChange={(e) => setCouponInput(e.target.value.toUpperCase())}
            sx={{ flex: 1 }}
            inputProps={{ style: { textTransform: 'uppercase' } }}
            disabled={couponApplied}
          />
          <Button
            variant="contained"
            size="small"
            onClick={() => { setCoupon(couponInput); onApplyCoupon(couponInput); }}
            disabled={couponLoading || !couponInput.trim() || couponApplied}
            sx={{ whiteSpace: 'nowrap' }}
          >
            {couponLoading ? <CircularProgress size={14} color="inherit" /> : couponApplied ? 'Applied' : 'Apply'}
          </Button>
        </Box>
        {couponError && <Typography variant="caption" color="error">{couponError}</Typography>}

        {summary?.discountAmount > 0 && (
          <Box display="flex" justifyContent="space-between" mb={1}>
            <Typography variant="body2" color="success.main">Discount</Typography>
            <Typography variant="body2" color="success.main">-{formatCurrency(summary.discountAmount)}</Typography>
          </Box>
        )}

        <Divider sx={{ my: 1.5 }} />
        <Box display="flex" justifyContent="space-between" mb={2.5}>
          <Typography variant="body1" fontWeight={700}>Total Amount</Typography>
          <Typography variant="body1" fontWeight={700}>{formatCurrency(summary?.totalAmount)}</Typography>
        </Box>

        <Button
          variant="contained"
          fullWidth
          size="large"
          onClick={onPayNow}
          disabled={payLoading}
          startIcon={payLoading ? <CircularProgress size={18} color="inherit" /> : null}
        >
          Pay Now
        </Button>
      </Box>
    </Box>
  );
}

// ─── Checkout Page ────────────────────────────────────────────────────────────

function CheckoutPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { invalidateCart, updateCartCount } = useCart();
  const { isAuthenticated, user } = useAuth();
  const toast = useToast();

  const [address, setAddress] = useState(EMPTY_ADDRESS);
  const [addressErrors, setAddressErrors] = useState({});
  const [useSavedAddress, setUseSavedAddress] = useState(false);
  const [coupon, setCoupon] = useState('');
  const [couponApplied, setCouponApplied] = useState(false);
  const [couponError, setCouponError] = useState('');
  const [couponLoading, setCouponLoading] = useState(false);
  const [discount, setDiscount] = useState(0);
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [placedOrder, setPlacedOrder] = useState(null);
  const [placeError, setPlaceError] = useState('');

  // Load cart
  const { data: cartData, isLoading: cartLoading, isError: cartError } = useQuery({
    queryKey: QUERY_KEYS.CART,
    queryFn: cartApi.getCart,
  });

  // Load addresses
  const { data: addrData } = useQuery({
    queryKey: ['addresses'],
    queryFn: userApi.getAddresses,
    enabled: isAuthenticated,
  });

  const savedAddresses = addrData?.data || [];
  const defaultAddress = savedAddresses.find((a) => a.isDefault) || savedAddresses[0];

  const cart = cartData?.data;
  const items = cart?.items || [];

  // Auto-prefill address from saved default when addresses load
  useEffect(() => {
    if (defaultAddress && !useSavedAddress) {
      setUseSavedAddress(true);
      setAddress({
        firstName: defaultAddress.firstName || '',
        lastName: defaultAddress.lastName || '',
        addressLine1: defaultAddress.addressLine1 || '',
        addressLine2: defaultAddress.addressLine2 || '',
        city: defaultAddress.city || '',
        state: defaultAddress.state || '',
        country: defaultAddress.country || 'India',
        pinCode: defaultAddress.pinCode || '',
        email: defaultAddress.email || user?.email || '',
        phoneNumber: (defaultAddress.phoneNumber || '').replace(/^\+91/, ''),
      });
    } else if (!defaultAddress && user?.email && address.email === '') {
      // Prefill email from profile even when no address exists
      setAddress((prev) => ({ ...prev, email: user.email }));
    }
    // Only run when addresses first load
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [defaultAddress]);

  // Compute summary locally (server summary loaded when coupon applied)
  const subtotal = cart?.subtotal || 0;
  const taxRate = 0.12;
  const taxAmount = Math.round(subtotal * taxRate);
  const deliveryCharge = 0;
  const totalAmount = subtotal + taxAmount + deliveryCharge - discount;

  const summary = {
    items,
    subtotal,
    taxAmount,
    deliveryCharge,
    discountAmount: discount,
    totalAmount,
  };

  // Cart mutations
  const updateQtyMutation = useMutation({
    mutationFn: ({ cartItemId, quantity }) =>
      cartApi.updateItem(cartItemId, { quantity }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.CART });
    },
    onError: () => toast.error('Failed to update quantity. Please try again.'),
  });

  const removeItemMutation = useMutation({
    mutationFn: (cartItemId) => cartApi.removeItem(cartItemId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.CART });
      toast.info('Item removed from cart.');
    },
    onError: () => toast.error('Failed to remove item. Please try again.'),
  });

  // Apply coupon
  const handleApplyCoupon = async (code) => {
    if (!code?.trim()) return;
    setCouponLoading(true);
    setCouponError('');
    try {
      const res = await checkoutApi.validateCoupon({ couponCode: code });
      setDiscount(res.data.discountAmount || 0);
      setCouponApplied(true);
      toast.success(`Coupon applied! You saved ${res.data.discountAmount > 0 ? `₹${res.data.discountAmount}` : 'on this order'}.`);
    } catch (err) {
      const msg = err.response?.data?.message || 'Invalid coupon code.';
      setCouponError(msg);
      toast.error(msg);
      setCouponApplied(false);
      setDiscount(0);
    } finally {
      setCouponLoading(false);
    }
  };

  // Toggle saved address
  const handleUseSavedAddress = (checked) => {
    setUseSavedAddress(checked);
    if (checked && defaultAddress) {
      setAddress({
        firstName: defaultAddress.firstName || '',
        lastName: defaultAddress.lastName || '',
        addressLine1: defaultAddress.addressLine1 || '',
        addressLine2: defaultAddress.addressLine2 || '',
        city: defaultAddress.city || '',
        state: defaultAddress.state || '',
        country: defaultAddress.country || 'India',
        pinCode: defaultAddress.pinCode || '',
        email: defaultAddress.email || user?.email || '',
        phoneNumber: defaultAddress.phoneNumber?.replace('+91', '') || '',
      });
    }
  };

  // Place order → open payment modal
  const handlePayNow = async () => {
    // If an order was already placed (e.g. user closed modal), re-open payment for it
    if (placedOrder) {
      setPaymentOpen(true);
      return;
    }

    const errs = validateAddress(address);
    if (Object.keys(errs).length) { setAddressErrors(errs); return; }
    setAddressErrors({});
    setPlaceError('');

    try {
      const deliveryAddress = useSavedAddress && defaultAddress
        ? { savedAddressId: defaultAddress.id }
        : {
            firstName: address.firstName,
            lastName: address.lastName,
            addressLine1: address.addressLine1,
            addressLine2: address.addressLine2 || undefined,
            city: address.city,
            state: address.state,
            country: address.country,
            pinCode: address.pinCode,
            email: address.email,
            phoneNumber: `+91${address.phoneNumber}`,
          };

      const res = await checkoutApi.placeOrder({
        couponCode: couponApplied ? coupon : undefined,
        deliveryAddress,
      });
      setPlacedOrder(res.data);
      setPaymentOpen(true);
    } catch (err) {
      setPlaceError(err.response?.data?.message || 'Failed to place order. Please try again.');
    }
  };

  const handlePaymentSuccess = (paymentResult) => {
    setPaymentOpen(false);
    invalidateCart();
    updateCartCount(0);
    queryClient.invalidateQueries({ queryKey: QUERY_KEYS.ORDERS });
    toast.success('Payment successful! Your order has been placed.');
    navigate('/order-confirmation', { state: { result: paymentResult } });
  };

  if (cartLoading) return <LoadingSpinner sx={{ mt: 8 }} />;
  if (cartError) return <ErrorMessage message="Failed to load cart." variant="page" />;

  if (items.length === 0)
    return (
      <EmptyState
        title="Your cart is empty"
        subtitle="Add some books to your cart to proceed to checkout."
        action={<Button variant="contained" onClick={() => navigate(ROUTES.HOME)}>Browse Books</Button>}
      />
    );

  return (
    <Box maxWidth={1200} mx="auto" px={{ xs: 2, md: 4 }} py={3}>
      <Typography variant="h5" fontWeight={700} mb={3}>
        Shopping Cart
      </Typography>

      <Box display="flex" gap={3} flexDirection={{ xs: 'column', md: 'row' }}>
        {/* Left: items + address */}
        <Box flex={1} minWidth={0}>
          {/* Cart Items */}
          <Stack spacing={2} mb={4}>
            {items.map((item) => (
              <CartItemRow
                key={item.cartItemId}
                item={item}
                onQuantityChange={(id, qty) => updateQtyMutation.mutate({ cartItemId: id, quantity: qty })}
                onRemove={(id) => removeItemMutation.mutate(id)}
                isPending={updateQtyMutation.isPending || removeItemMutation.isPending}
              />
            ))}
          </Stack>

          {/* Address Section */}
          <Box
            sx={{
              bgcolor: 'background.paper',
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: 2,
              p: 2.5,
            }}
          >
            <Typography variant="h6" fontWeight={700} mb={2}>
              Address
            </Typography>

            {savedAddresses.length > 0 && (
              <Box mb={2}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={useSavedAddress}
                      onChange={(e) => handleUseSavedAddress(e.target.checked)}
                      size="small"
                    />
                  }
                  label={
                    <Typography variant="body2">
                      Use saved address
                      {useSavedAddress && defaultAddress && (
                        <Typography component="span" variant="caption" color="text.secondary" sx={{ ml: 0.5 }}>
                          ({defaultAddress.firstName} {defaultAddress.lastName}, {defaultAddress.city})
                        </Typography>
                      )}
                    </Typography>
                  }
                />
              </Box>
            )}

            {placeError && (
              <Alert severity="error" sx={{ mb: 2 }} onClose={() => setPlaceError('')}>
                {placeError}
              </Alert>
            )}

            <AddressForm
              value={address}
              onChange={setAddress}
              errors={addressErrors}
              onErrorClear={(k) => setAddressErrors((er) => ({ ...er, [k]: '' }))}
            />
          </Box>
        </Box>

        {/* Right: Grand Total */}
        <Box sx={{ width: { xs: '100%', md: 300 }, flexShrink: 0 }}>
          <GrandTotalPanel
            summary={summary}
            coupon={coupon}
            setCoupon={setCoupon}
            onApplyCoupon={handleApplyCoupon}
            couponLoading={couponLoading}
            couponError={couponError}
            couponApplied={couponApplied}
            onPayNow={handlePayNow}
            payLoading={false}
          />
        </Box>
      </Box>

      {/* Payment modal */}
      {placedOrder && (
        <PaymentModal
          open={paymentOpen}
          onClose={() => setPaymentOpen(false)}
          order={placedOrder}
          onSuccess={handlePaymentSuccess}
        />
      )}
    </Box>
  );
}

export default CheckoutPage;
