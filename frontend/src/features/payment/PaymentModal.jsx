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

// ─── Credit / Debit Card Form ─────────────────────────────────────────────────

function CardForm({ form, setForm, errors }) {
  const set = (k) => (e) => { setForm((f) => ({ ...f, [k]: e.target.value })); };
  return (
    <Box display="grid" gridTemplateColumns="1fr 1fr" gap={1.5}>
      <TextField
        label="Card Number"
        placeholder="XXXX-XXXX-XXXX-XXXX"
        value={form.cardNumber}
        onChange={set('cardNumber')}
        error={!!errors.cardNumber}
        helperText={errors.cardNumber}
        inputProps={{ maxLength: 19 }}
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
        onChange={set('cvv')}
        error={!!errors.cvv}
        helperText={errors.cvv}
        inputProps={{ maxLength: 4 }}
        size="small"
      />
      <TextField
        label="Date of Expiry"
        placeholder="MM/YYYY"
        value={form.expiryDate}
        onChange={set('expiryDate')}
        error={!!errors.expiryDate}
        helperText={errors.expiryDate}
        inputProps={{ maxLength: 7 }}
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

  const mutation = useMutation({
    mutationFn: (payload) => paymentApi.processPayment(payload),
    onSuccess: (data) => onSuccess(data.data),
    onError: (err) => setApiError(err.response?.data?.message || 'Payment failed. Please try again.'),
  });

  const validateAndSubmit = () => {
    setApiError('');
    const payload = { orderId: order.orderId, paymentMethod: method };

    if (method === 'CREDIT_CARD' || method === 'DEBIT_CARD') {
      const e = {};
      if (!cardForm.cardNumber.replace(/\s/g, '').match(/^\d{13,19}$/)) e.cardNumber = 'Enter a valid card number';
      if (!cardForm.nameOnCard.trim()) e.nameOnCard = 'Required';
      if (!cardForm.cvv.match(/^\d{3,4}$/)) e.cvv = 'Enter 3 or 4 digit CVV';
      if (!cardForm.expiryDate.match(/^(0[1-9]|1[0-2])\/\d{4}$/)) e.expiryDate = 'Format: MM/YYYY';
      if (Object.keys(e).length) { setCardErrors(e); return; }
      setCardErrors({});
      payload.cardDetails = {
        cardNumber: cardForm.cardNumber.replace(/\s/g, ''),
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
