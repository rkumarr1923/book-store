import React, { useState, useEffect } from 'react';
import { useMutation } from '@tanstack/react-query';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogContent,
  Divider,
  IconButton,
  InputAdornment,
  Tab,
  Tabs,
  TextField,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import LockIcon from '@mui/icons-material/Lock';

import { paymentApi } from '../../common/api/paymentApi';
import { formatCurrency } from '../../common/utils/formatCurrency';
import { useToast } from '../../common/context/ToastContext';

// ─── Credit / Debit Card Form ─────────────────────────────────────────────────

function CardForm({ form, setForm, errors }) {
  const set = (k) => (e) => { setForm((f) => ({ ...f, [k]: e.target.value })); };

  const handleCardNumber = (e) => {
    // Strip everything except digits, limit to 16 digits, then insert dashes every 4
    const digits = e.target.value.replace(/\D/g, '').slice(0, 16);
    const formatted = digits.replace(/(.{4})/g, '$1-').replace(/-$/, '');
    setForm((f) => ({ ...f, cardNumber: formatted }));
  };

  const handleCvv = (e) => {
    const digits = e.target.value.replace(/\D/g, '').slice(0, 3);
    setForm((f) => ({ ...f, cvv: digits }));
  };

  const handleExpiry = (e) => {
    // Strip non-digits, cap at 6 (MMYYYY)
    let digits = e.target.value.replace(/\D/g, '').slice(0, 6);

    // Clamp first digit: can only be 0 or 1
    if (digits.length >= 1 && digits[0] > '1') digits = '0' + digits.slice(0, 1) + digits.slice(1);

    // Clamp month to 01–12: once 2 digits typed, ensure value <= 12
    if (digits.length >= 2) {
      const mm = parseInt(digits.slice(0, 2), 10);
      if (mm < 1)  digits = '01' + digits.slice(2);
      if (mm > 12) digits = '12' + digits.slice(2);
    }

    // Clamp year: once 4 year digits present, ensure >= current year
    if (digits.length === 6) {
      const currentYear = new Date().getFullYear();
      const yyyy = parseInt(digits.slice(2), 10);
      if (yyyy < currentYear) digits = digits.slice(0, 2) + String(currentYear);
    }

    // Auto-insert slash after MM
    const formatted = digits.length > 2
      ? digits.slice(0, 2) + '/' + digits.slice(2)
      : digits;
    setForm((f) => ({ ...f, expiryDate: formatted }));
  };

  return (
    <Box display="grid" gridTemplateColumns="1fr 1fr" gap={1.5}>
      <TextField
        label="Card Number"
        placeholder="XXXX-XXXX-XXXX-XXXX"
        value={form.cardNumber}
        onChange={handleCardNumber}
        error={!!errors.cardNumber}
        helperText={errors.cardNumber}
        inputProps={{ maxLength: 19, inputMode: 'numeric' }}
        sx={{ gridColumn: '1 / -1' }}
        size="small"
      />
      <TextField
        label="Name on Card"
        placeholder="Name"
        value={form.nameOnCard}
        onChange={set('nameOnCard')}
        error={!!errors.nameOnCard}
        helperText={errors.nameOnCard}
        size="small"
      />
      <TextField
        label="CVV"
        placeholder="XXX"
        value={form.cvv}
        onChange={handleCvv}
        error={!!errors.cvv}
        helperText={errors.cvv}
        inputProps={{ maxLength: 3, inputMode: 'numeric' }}
        size="small"
      />
      <TextField
        label="Date of Expiry"
        placeholder="MM/YYYY"
        value={form.expiryDate}
        onChange={handleExpiry}
        error={!!errors.expiryDate}
        helperText={errors.expiryDate}
        inputProps={{ maxLength: 7, inputMode: 'numeric' }}
        size="small"
        sx={{ gridColumn: '1 / -1' }}
      />
    </Box>
  );
}

// ─── UPI Form ─────────────────────────────────────────────────────────────────

function UpiForm({ upiId, setUpiId, error }) {
  return (
    <TextField
      fullWidth
      label="UPI ID"
      placeholder="yourname@bankname"
      value={upiId}
      onChange={(e) => setUpiId(e.target.value)}
      error={!!error}
      helperText={error || 'e.g. arjun@okaxis'}
      size="small"
    />
  );
}

// ─── Wallet Form ──────────────────────────────────────────────────────────────

function WalletForm({ amount }) {
  return (
    <Box textAlign="center" py={2}>
      <Typography variant="body1" color="text.secondary" mb={1}>
        Wallet Balance Available
      </Typography>
      <Typography variant="h5" fontWeight={700} color="success.main">
        ₹1,000.00
      </Typography>
      <Typography variant="caption" color="text.secondary" mt={0.5} display="block">
        Amount to be deducted: {formatCurrency(amount)}
      </Typography>
    </Box>
  );
}

// ─── Payment Modal ────────────────────────────────────────────────────────────

const METHODS = [
  { value: 'CREDIT_CARD', label: 'Credit Card' },
  { value: 'DEBIT_CARD', label: 'Debit card' },
  { value: 'UPI', label: 'UPI' },
  { value: 'WALLET', label: 'Wallet' },
];

const CARD_EMPTY = { cardNumber: '', nameOnCard: '', cvv: '', expiryDate: '' };

function PaymentModal({ open, onClose, order, onSuccess }) {
  const [method, setMethod] = useState('CREDIT_CARD');
  const [cardForm, setCardForm] = useState(CARD_EMPTY);
  const [cardErrors, setCardErrors] = useState({});
  const [upiId, setUpiId] = useState('');
  const [upiError, setUpiError] = useState('');
  const [apiError, setApiError] = useState('');

  // Reset all form state every time the modal opens
  useEffect(() => {
    if (open) {
      setMethod('CREDIT_CARD');
      setCardForm(CARD_EMPTY);
      setCardErrors({});
      setUpiId('');
      setUpiError('');
      setApiError('');
    }
  }, [open]);

  const amount = order?.totalAmount || 0;

  const toast = useToast();

  const mutation = useMutation({
    mutationFn: (payload) => paymentApi.processPayment(payload),
    onSuccess: (data) => onSuccess(data.data),
    onError: (err) => {
      const msg = err.response?.data?.message || 'Payment failed. Please try again.';
      setApiError(msg);
      toast.error(msg, 'Payment Declined');
    },
  });

  const validateAndSubmit = () => {
    setApiError('');
    const payload = { orderId: order.orderId, paymentMethod: method };

    if (method === 'CREDIT_CARD' || method === 'DEBIT_CARD') {
      const e = {};
      if (!cardForm.cardNumber.replace(/-/g, '').match(/^\d{16}$/)) e.cardNumber = 'Enter a valid 16-digit card number';
      if (!cardForm.nameOnCard.trim()) e.nameOnCard = 'Required';
      if (!cardForm.cvv.match(/^\d{3}$/)) e.cvv = 'Enter a 3-digit CVV';
      if (!cardForm.expiryDate.match(/^(0[1-9]|1[0-2])\/\d{4}$/)) {
        e.expiryDate = 'Format: MM/YYYY';
      } else {
        const [mm, yyyy] = cardForm.expiryDate.split('/');
        const expiry = new Date(Number(yyyy), Number(mm) - 1, 1);
        const today = new Date();
        if (expiry < new Date(today.getFullYear(), today.getMonth(), 1)) {
          e.expiryDate = 'Card has expired';
        }
      }
      if (Object.keys(e).length) { setCardErrors(e); return; }
      setCardErrors({});
      payload.cardDetails = {
        cardNumber: cardForm.cardNumber.replace(/-/g, ''),
        nameOnCard: cardForm.nameOnCard,
        cvv: cardForm.cvv,
        expiryDate: cardForm.expiryDate,
      };
    }

    if (method === 'UPI') {
      if (!upiId.match(/^[a-zA-Z0-9._]+@[a-zA-Z]+$/)) {
        setUpiError('Enter a valid UPI ID (e.g. name@okaxis)');
        return;
      }
      setUpiError('');
      payload.upiDetails = { upiId };
    }

    mutation.mutate(payload);
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{ sx: { bgcolor: 'background.paper', backgroundImage: 'none' } }}
    >
      <IconButton
        onClick={onClose}
        size="small"
        sx={{ position: 'absolute', top: 8, right: 8, color: 'text.secondary' }}
      >
        <CloseIcon fontSize="small" />
      </IconButton>

      <DialogContent sx={{ pt: 3, pb: 3 }}>
        {/* Demo Notice Banner */}
        <Alert severity="info" sx={{ mb: 2, bgcolor: 'rgba(2, 136, 209, 0.12)', border: '1px solid rgba(2, 136, 209, 0.3)' }}>
          <strong>DEMO / MOCK PAYMENT:</strong> No real money will be charged. Enter any test credentials (e.g. CVV 000 to test failure).
        </Alert>

        {/* Header */}
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2.5}>
          <Typography variant="h6" fontWeight={700}>
            Complete Payment
          </Typography>
          <Typography variant="body1" fontWeight={700}>
            Payable Amount: {formatCurrency(amount)}
          </Typography>
        </Box>

        {apiError && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setApiError('')}>
            {apiError}
          </Alert>
        )}

        {/* Method tabs (vertical left) + form right */}
        <Box display="flex" gap={0} border="1px solid" borderColor="divider" borderRadius={2} overflow="hidden" minHeight={260}>
          {/* Method list */}
          <Box
            sx={{
              borderRight: '1px solid',
              borderColor: 'divider',
              width: 130,
              flexShrink: 0,
            }}
          >
            {METHODS.map((m) => (
              <Box
                key={m.value}
                onClick={() => setMethod(m.value)}
                sx={{
                  px: 2,
                  py: 1.5,
                  cursor: 'pointer',
                  borderLeft: method === m.value ? '3px solid' : '3px solid transparent',
                  borderLeftColor: method === m.value ? 'primary.main' : 'transparent',
                  bgcolor: method === m.value ? 'rgba(41,121,255,0.08)' : 'transparent',
                  '&:hover': { bgcolor: 'rgba(255,255,255,0.04)' },
                }}
              >
                <Typography variant="body2" fontWeight={method === m.value ? 700 : 400}>
                  {m.label}
                </Typography>
              </Box>
            ))}
          </Box>

          {/* Form area */}
          <Box flex={1} p={2.5}>
            {(method === 'CREDIT_CARD' || method === 'DEBIT_CARD') && (
              <CardForm form={cardForm} setForm={setCardForm} errors={cardErrors} />
            )}
            {method === 'UPI' && (
              <UpiForm upiId={upiId} setUpiId={setUpiId} error={upiError} />
            )}
            {method === 'WALLET' && <WalletForm amount={amount} />}
          </Box>
        </Box>

        {/* Pay Now */}
        <Box display="flex" justifyContent="flex-end" mt={2.5}>
          <Button
            variant="contained"
            size="large"
            onClick={validateAndSubmit}
            disabled={mutation.isPending}
            startIcon={mutation.isPending ? <CircularProgress size={16} color="inherit" /> : <LockIcon />}
            sx={{ minWidth: 140 }}
          >
            {mutation.isPending ? 'Processing…' : 'Pay Now'}
          </Button>
        </Box>
      </DialogContent>
    </Dialog>
  );
}

export default PaymentModal;
