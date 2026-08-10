import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Box,
  Button,
  Checkbox,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControlLabel,
  IconButton,
  Stack,
  Tab,
  Tabs,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import HomeIcon from '@mui/icons-material/Home';
import AddIcon from '@mui/icons-material/Add';
import StarIcon from '@mui/icons-material/Star';

import { userApi } from '../../common/api/userApi';
import { useAuth } from '../../common/hooks/useAuth';
import { useToast } from '../../common/context/ToastContext';
import { validateAddressForm, validateProfileForm } from '../../common/utils/validators';
import LoadingSpinner from '../../common/components/LoadingSpinner/LoadingSpinner';
import ErrorMessage from '../../common/components/ErrorMessage/ErrorMessage';

// ─── Address Dialog ───────────────────────────────────────────────────────────

const EMPTY_ADDR = {
  firstName: '', lastName: '', addressLine1: '', addressLine2: '',
  city: '', state: '', country: 'India', pinCode: '', email: '', phoneNumber: '', isDefault: false,
};

function AddressDialog({ open, onClose, initial, onSave, saving }) {
  const [form, setForm] = useState(initial || EMPTY_ADDR);
  const [errors, setErrors] = useState({});

  React.useEffect(() => {
    const base = initial || EMPTY_ADDR;
    setForm({
      ...base,
      phoneNumber: (base.phoneNumber || '').replace(/^\+91/, ''),
    });
    setErrors({});
  }, [initial, open]);

  const set = (k) => (e) => {
    let val = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    if (k === 'phoneNumber') val = val.replace(/\D/g, '').slice(0, 10);
    if (k === 'pinCode')     val = val.replace(/\D/g, '').slice(0, 6);
    setForm((f) => ({ ...f, [k]: val }));
    setErrors((er) => ({ ...er, [k]: '' }));
  };

  const validate = () => validateAddressForm(form);

  const handleSave = () => {
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    // Backend expects phoneNumber with +91 prefix
    onSave({ ...form, phoneNumber: `+91${form.phoneNumber}` });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{initial?.id ? 'Edit Address' : 'Add New Address'}</DialogTitle>
      <DialogContent>
        <Box display="grid" gridTemplateColumns="1fr 1fr" gap={1.5} pt={1}>
          <TextField label="First Name" value={form.firstName} onChange={set('firstName')} error={!!errors.firstName} helperText={errors.firstName} size="small" />
          <TextField label="Last Name" value={form.lastName} onChange={set('lastName')} error={!!errors.lastName} helperText={errors.lastName} size="small" />
          <TextField label="Address Line 1" value={form.addressLine1} onChange={set('addressLine1')} error={!!errors.addressLine1} helperText={errors.addressLine1} size="small" sx={{ gridColumn: '1 / -1' }} />
          <TextField label="Address Line 2 (optional)" value={form.addressLine2} onChange={set('addressLine2')} size="small" sx={{ gridColumn: '1 / -1' }} />
          <TextField label="Email" type="email" value={form.email} onChange={set('email')} error={!!errors.email} helperText={errors.email} size="small" />
          <TextField label="City" value={form.city} onChange={set('city')} error={!!errors.city} helperText={errors.city} size="small" />
          <TextField label="PIN Code" value={form.pinCode} onChange={set('pinCode')} error={!!errors.pinCode} helperText={errors.pinCode} size="small" inputProps={{ maxLength: 6, inputMode: 'numeric' }} />
          <TextField
            label="Phone Number"
            value={form.phoneNumber}
            onChange={set('phoneNumber')}
            error={!!errors.phoneNumber}
            helperText={errors.phoneNumber}
            size="small"
            inputProps={{ maxLength: 10, inputMode: 'numeric' }}
            InputProps={{
              startAdornment: <Typography variant="body2" sx={{ mr: 0.5, color: 'text.secondary' }}>+91</Typography>,
            }}
          />
          <TextField label="State" value={form.state} onChange={set('state')} error={!!errors.state} helperText={errors.state} size="small" />
          <TextField label="Country" value={form.country} onChange={set('country')} size="small" />
          <FormControlLabel
            control={<Checkbox checked={!!form.isDefault} onChange={set('isDefault')} size="small" />}
            label={<Typography variant="body2">Set as default address</Typography>}
            sx={{ gridColumn: '1 / -1' }}
          />
        </Box>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} variant="outlined" color="inherit">Cancel</Button>
        <Button
          onClick={handleSave}
          variant="contained"
          disabled={saving}
          startIcon={saving ? <CircularProgress size={14} color="inherit" /> : null}
        >
          {saving ? 'Saving…' : 'Save Address'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

// ─── Addresses Tab ────────────────────────────────────────────────────────────

function AddressesTab() {
  const queryClient = useQueryClient();
  const toast = useToast();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editTarget, setEditTarget] = useState(null);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['addresses'],
    queryFn: userApi.getAddresses,
  });

  const addresses = data?.data || [];

  const addMutation = useMutation({
    mutationFn: (payload) => userApi.addAddress(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
      setDialogOpen(false);
      toast.success('Address added successfully.');
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to add address.'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }) => userApi.updateAddress(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
      setDialogOpen(false);
      toast.success('Address updated.');
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to update address.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => userApi.deleteAddress(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
      toast.info('Address deleted.');
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to delete address.'),
  });

  const defaultMutation = useMutation({
    mutationFn: (id) => userApi.setDefaultAddress(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['addresses'] });
      toast.success('Default address updated.');
    },
    onError: () => toast.error('Failed to update default address.'),
  });

  const handleSave = (form) => {
    if (editTarget?.id) {
      updateMutation.mutate({ id: editTarget.id, payload: form });
    } else {
      addMutation.mutate(form);
    }
  };

  const isSaving = addMutation.isPending || updateMutation.isPending;

  if (isLoading) return <LoadingSpinner />;
  if (isError) return <ErrorMessage message="Failed to load addresses." />;

  return (
    <Box>
      <Box display="flex" justifyContent="flex-end" mb={2}>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          size="small"
          onClick={() => { setEditTarget(null); setDialogOpen(true); }}
        >
          Add Address
        </Button>
      </Box>

      {addresses.length === 0 ? (
        <Typography variant="body2" color="text.secondary" textAlign="center" py={4}>
          No saved addresses yet.
        </Typography>
      ) : (
        <Stack spacing={2}>
          {addresses.map((addr) => (
            <Box
              key={addr.id}
              sx={{
                bgcolor: 'background.paper',
                border: '1px solid',
                borderColor: addr.isDefault ? 'primary.main' : 'divider',
                borderRadius: 2,
                p: 2,
              }}
            >
              <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                <Box>
                  {addr.isDefault && (
                    <Box display="flex" alignItems="center" gap={0.5} mb={0.5}>
                      <StarIcon fontSize="small" sx={{ color: 'warning.main', fontSize: 14 }} />
                      <Typography variant="caption" color="warning.main" fontWeight={700}>Default</Typography>
                    </Box>
                  )}
                  <Typography variant="body2" fontWeight={600}>{addr.firstName} {addr.lastName}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {addr.addressLine1}{addr.addressLine2 ? `, ${addr.addressLine2}` : ''}, {addr.city}, {addr.state} – {addr.pinCode}
                  </Typography>
                  <Typography variant="caption" color="text.secondary" display="block">
                    {addr.country} · {addr.phoneNumber} · {addr.email}
                  </Typography>
                </Box>
                <Stack direction="row" gap={0.5}>
                  {!addr.isDefault && (
                    <Tooltip title="Set as default">
                      <IconButton size="small" onClick={() => defaultMutation.mutate(addr.id)}>
                        <HomeIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  )}
                  <Tooltip title="Edit">
                    <IconButton size="small" onClick={() => { setEditTarget(addr); setDialogOpen(true); }}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete">
                    <IconButton size="small" color="error" onClick={() => deleteMutation.mutate(addr.id)}>
                      <DeleteOutlineIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </Stack>
              </Box>
            </Box>
          ))}
        </Stack>
      )}

      <AddressDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        initial={editTarget}
        onSave={handleSave}
        saving={isSaving}
      />
    </Box>
  );
}

// ─── Profile Tab ──────────────────────────────────────────────────────────────

function ProfileTab() {
  const { user, refreshUser } = useAuth();
  const toast = useToast();
  const [form, setForm] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    phoneNumber: (user?.phoneNumber || '').replace(/^\+91/, ''),
  });
  const [errors, setErrors] = useState({});

  const mutation = useMutation({
    mutationFn: (payload) => userApi.updateMe(payload),
    onSuccess: async () => {
      await refreshUser();
      toast.success('Profile updated successfully.');
    },
    onError: (err) => toast.error(err.response?.data?.message || 'Failed to update profile.'),
  });

  const set = (k) => (e) => {
    let val = e.target.value;
    if (k === 'phoneNumber') val = val.replace(/\D/g, '').slice(0, 10);
    setForm((f) => ({ ...f, [k]: val }));
    setErrors((er) => ({ ...er, [k]: '' }));
  };

  const handleSave = () => {
    const e = validateProfileForm(form);
    if (Object.keys(e).length) { setErrors(e); return; }
    mutation.mutate({
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      phoneNumber: form.phoneNumber.trim() ? `+91${form.phoneNumber.trim()}` : undefined,
    });
  };

  return (
    <Box maxWidth={480}>
      <Typography variant="body2" color="text.secondary" mb={3}>
        Update your personal information below.
      </Typography>
      <Stack spacing={2} mb={3}>
        <TextField
          label="First Name"
          value={form.firstName}
          onChange={set('firstName')}
          error={!!errors.firstName}
          helperText={errors.firstName}
          size="small"
        />
        <TextField
          label="Last Name"
          value={form.lastName}
          onChange={set('lastName')}
          error={!!errors.lastName}
          helperText={errors.lastName}
          size="small"
        />
        <TextField
          label="Email Address"
          value={user?.email || ''}
          disabled
          size="small"
          helperText="Email cannot be changed"
        />
        <TextField
          label="Phone Number (optional)"
          value={form.phoneNumber}
          onChange={set('phoneNumber')}
          size="small"
          error={!!errors.phoneNumber}
          helperText={errors.phoneNumber || 'e.g. 9876543210'}
          inputProps={{ maxLength: 10, inputMode: 'numeric' }}
          InputProps={{
            startAdornment: <Typography variant="body2" sx={{ mr: 0.5, color: 'text.secondary' }}>+91</Typography>,
          }}
        />
      </Stack>

      <Button
        variant="contained"
        onClick={handleSave}
        disabled={mutation.isPending}
        startIcon={mutation.isPending ? <CircularProgress size={14} color="inherit" /> : null}
      >
        {mutation.isPending ? 'Saving…' : 'Save Changes'}
      </Button>
    </Box>
  );
}

// ─── Profile Page (with tabs) ─────────────────────────────────────────────────

function ProfilePage() {
  const { user } = useAuth();
  const [tab, setTab] = useState(0);

  return (
    <Box maxWidth={900} mx="auto" px={{ xs: 2, md: 4 }} py={4}>
      <Typography variant="h4" fontWeight={700} mb={0.5}>
        My Account
      </Typography>
      <Typography variant="body2" color="text.secondary" mb={3}>
        {user?.email}
      </Typography>

      <Tabs
        value={tab}
        onChange={(_, v) => setTab(v)}
        sx={{ borderBottom: '1px solid', borderColor: 'divider', mb: 3 }}
      >
        <Tab label="Profile" />
        <Tab label="Saved Addresses" />
      </Tabs>

      {tab === 0 && <ProfileTab />}
      {tab === 1 && <AddressesTab />}
    </Box>
  );
}

export default ProfilePage;
