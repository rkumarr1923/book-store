/* eslint-disable react/prop-types */
import  { useState } from 'react';
import { authApi } from '../../common/api/authApi';
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
  Link,
  TextField,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import '@mui/icons-material/PersonOutline';
import { useAuth } from '../../common/hooks/useAuth';
import { isValidEmail, isValidPassword } from '../../common/utils/validators';

// ─── Sub-views ────────────────────────────────────────────────────────────────

function LoginView({ onSwitch, onGuest, onClose }) {
  const { login, loading } = useAuth();
  const [form, setForm] = useState({ identifier: '', password: '' });
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState('');

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!form.identifier.trim() || !form.password) {
      setError('Please enter your email/phone and password.');
      return;
    }
    const res = await login({ identifier: form.identifier.trim(), password: form.password });
    if (res.success) {
      onClose();
    } else {
      setError(res.message);
    }
  };

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate>
      <Typography variant="h6" fontWeight={700} mb={0.5}>
        Welcome to{' '}
        <Box component="span" color="primary.main">
          BookStore
        </Box>
      </Typography>
      <Typography variant="body2" color="text.secondary" mb={2.5}>
        Sign in to access your account
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>
          {error}
        </Alert>
      )}

      <TextField
        fullWidth
        label="Phone Number / e-mail"
        placeholder="Enter"
        value={form.identifier}
        onChange={set('identifier')}
        sx={{ mb: 2 }}
        autoComplete="username"
        autoFocus
      />
      <TextField
        fullWidth
        label="Password"
        type={showPass ? 'text' : 'password'}
        value={form.password}
        onChange={set('password')}
        autoComplete="current-password"
        InputProps={{
          endAdornment: (
            <InputAdornment position="end">
              <IconButton size="small" onClick={() => setShowPass((v) => !v)} edge="end">
                {showPass ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
              </IconButton>
            </InputAdornment>
          ),
        }}
        sx={{ mb: 1 }}
      />

      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Link
          component="button"
          type="button"
          variant="caption"
          underline="hover"
          color="primary"
          onClick={onSwitch.bind(null, 'forgot')}
        >
          Forgot Password?
        </Link>
      </Box>

      <Button
        type="submit"
        variant="contained"
        fullWidth
        disabled={loading}
        startIcon={loading ? <CircularProgress size={16} color="inherit" /> : null}
        sx={{ mb: 2 }}
      >
        {loading ? 'Signing in…' : 'Sign In'}
      </Button>

      <Divider sx={{ mb: 2 }}>
        <Typography variant="caption" color="text.secondary">
          OR
        </Typography>
      </Divider>

      <Box textAlign="center" mb={1.5}>
        <Typography variant="body2" color="text.secondary" component="span">
          New to BookStore?{' '}
        </Typography>
        <Link
          component="button"
          type="button"
          variant="body2"
          fontWeight={600}
          underline="hover"
          color="primary"
          onClick={onSwitch.bind(null, 'register')}
        >
          Sign Up Here
        </Link>
      </Box>

      <Button variant="outlined" fullWidth color="inherit" onClick={onGuest} sx={{ color: 'text.secondary' }}>
        Continue as Guest
      </Button>
    </Box>
  );
}

// ─── Register ─────────────────────────────────────────────────────────────────

function RegisterView({ onSwitch, onClose }) {
  const { register, loading } = useAuth();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    password: '',
    confirmPassword: '',
  });
  const [showPass, setShowPass] = useState(false);
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');

  const set = (k) => (e) => {
    setForm((f) => ({ ...f, [k]: e.target.value }));
    setErrors((er) => ({ ...er, [k]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.firstName.trim()) e.firstName = 'First name is required';
    if (!form.lastName.trim()) e.lastName = 'Last name is required';
    if (!form.email.trim()) e.email = 'Email is required';
    else if (!isValidEmail(form.email)) e.email = 'Enter a valid email address';
    if (!form.password) e.password = 'Password is required';
    else if (!isValidPassword(form.password))
      e.password = 'Min 8 chars, one uppercase, one number, one special character';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match';
    return e;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError('');
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    const payload = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      email: form.email.trim(),
      phoneNumber: form.phoneNumber.trim() || undefined,
      password: form.password,
    };
    const res = await register(payload);
    if (res.success) {
      onClose();
    } else {
      setApiError(res.message);
    }
  };

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate>
      <Typography variant="h6" fontWeight={700} mb={0.5}>
        Create your account
      </Typography>
      <Typography variant="body2" color="text.secondary" mb={2.5}>
        Join BookStore and start your reading journey
      </Typography>

      {apiError && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setApiError('')}>
          {apiError}
        </Alert>
      )}

      <Box display="grid" gridTemplateColumns="1fr 1fr" gap={1.5} mb={1.5}>
        <TextField
          label="First Name"
          value={form.firstName}
          onChange={set('firstName')}
          error={!!errors.firstName}
          helperText={errors.firstName}
          autoFocus
        />
        <TextField
          label="Last Name"
          value={form.lastName}
          onChange={set('lastName')}
          error={!!errors.lastName}
          helperText={errors.lastName}
        />
      </Box>

      <TextField
        fullWidth
        label="Email Address"
        type="email"
        value={form.email}
        onChange={set('email')}
        error={!!errors.email}
        helperText={errors.email}
        sx={{ mb: 1.5 }}
        autoComplete="email"
      />
      <TextField
        fullWidth
        label="Phone Number (optional)"
        placeholder="+91XXXXXXXXXX"
        value={form.phoneNumber}
        onChange={set('phoneNumber')}
        sx={{ mb: 1.5 }}
      />
      <TextField
        fullWidth
        label="Password"
        type={showPass ? 'text' : 'password'}
        value={form.password}
        onChange={set('password')}
        error={!!errors.password}
        helperText={errors.password}
        autoComplete="new-password"
        InputProps={{
          endAdornment: (
            <InputAdornment position="end">
              <IconButton size="small" onClick={() => setShowPass((v) => !v)} edge="end">
                {showPass ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
              </IconButton>
            </InputAdornment>
          ),
        }}
        sx={{ mb: 1.5 }}
      />
      <TextField
        fullWidth
        label="Confirm Password"
        type={showPass ? 'text' : 'password'}
        value={form.confirmPassword}
        onChange={set('confirmPassword')}
        error={!!errors.confirmPassword}
        helperText={errors.confirmPassword}
        autoComplete="new-password"
        sx={{ mb: 2 }}
      />

      <Button
        type="submit"
        variant="contained"
        fullWidth
        disabled={loading}
        startIcon={loading ? <CircularProgress size={16} color="inherit" /> : null}
        sx={{ mb: 2 }}
      >
        {loading ? 'Creating account…' : 'Create Account'}
      </Button>

      <Box textAlign="center">
        <Typography variant="body2" color="text.secondary" component="span">
          Already have an account?{' '}
        </Typography>
        <Link
          component="button"
          type="button"
          variant="body2"
          fontWeight={600}
          underline="hover"
          color="primary"
          onClick={onSwitch.bind(null, 'login')}
        >
          Sign In
        </Link>
      </Box>
    </Box>
  );
}

// ─── Forgot Password ──────────────────────────────────────────────────────────

function ForgotPasswordView({ onSwitch, onClose }) {
  const [email, setEmail] = useState('');
  const [status, setStatus] = useState('idle'); // idle | loading | done | error
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isValidEmail(email)) {
      setMessage('Enter a valid email address.');
      setStatus('error');
      return;
    }
    setStatus('loading');
    try {
      await authApi.forgotPassword({ email });
      setStatus('done');
      setMessage('If this email is registered, a reset link has been sent.');
    } catch {
      setStatus('error');
      setMessage('Something went wrong. Please try again.');
    }
  };

  return (
    <Box component="form" onSubmit={handleSubmit} noValidate>
      <Typography variant="h6" fontWeight={700} mb={0.5}>
        Reset your password
      </Typography>
      <Typography variant="body2" color="text.secondary" mb={2.5}>
        Enter your registered email address
      </Typography>

      {status === 'done' ? (
        <>
          <Alert severity="success" sx={{ mb: 2 }}>{message}</Alert>
          <Box textAlign="center">
            <Link
              component="button"
              type="button"
              variant="body2"
              underline="hover"
              color="primary"
              onClick={onClose}
            >
              Close
            </Link>
          </Box>
        </>
      ) : (
        <>
          {status === 'error' && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {message}
            </Alert>
          )}
          <TextField
            fullWidth
            label="Email Address"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            sx={{ mb: 2 }}
            autoFocus
          />
          <Button
            type="submit"
            variant="contained"
            fullWidth
            disabled={status === 'loading'}
            startIcon={status === 'loading' ? <CircularProgress size={16} color="inherit" /> : null}
            sx={{ mb: 2 }}
          >
            {status === 'loading' ? 'Sending…' : 'Send Reset Link'}
          </Button>
        </>
      )}

      <Box textAlign="center" mt={1}>
        <Link
          component="button"
          type="button"
          variant="body2"
          underline="hover"
          color="primary"
          onClick={onSwitch.bind(null, 'login')}
        >
          Back to Sign In
        </Link>
      </Box>
    </Box>
  );
}

// ─── Main Modal ───────────────────────────────────────────────────────────────

/**
 * AuthModal — composite login / register / forgot-password modal.
 *
 * Props:
 *  open: boolean
 *  onClose: () => void
 */
function AuthModal({ open, onClose }) {
  const [view, setView] = useState('login');

  const handleClose = () => {
    onClose();
    // defer reset so animation isn't jarring
    setTimeout(() => setView('login'), 300);
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="xs"
      fullWidth
      PaperProps={{ sx: { bgcolor: 'background.paper', backgroundImage: 'none' } }}
    >
      <IconButton
        onClick={handleClose}
        size="small"
        sx={{ position: 'absolute', top: 8, right: 8, color: 'text.secondary' }}
      >
        <CloseIcon fontSize="small" />
      </IconButton>

      <DialogContent sx={{ pt: 3, pb: 3 }}>
        {view === 'login' && (
          <LoginView onSwitch={setView} onGuest={handleClose} onClose={handleClose} />
        )}
        {view === 'register' && (
          <RegisterView onSwitch={setView} onClose={handleClose} />
        )}
        {view === 'forgot' && <ForgotPasswordView onSwitch={setView} onClose={handleClose} />}
      </DialogContent>
    </Dialog>
  );
}

export default AuthModal;
