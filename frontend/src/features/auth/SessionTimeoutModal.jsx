import React, { useEffect, useRef, useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  LinearProgress,
  Typography,
  Box,
} from '@mui/material';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import { useAuth } from '../../common/hooks/useAuth';
import { useToast } from '../../common/context/ToastContext';

const INACTIVITY_THRESHOLD_MS = 5 * 60 * 1000; // 5 minutes inactivity before warning
const COUNTDOWN_SECONDS = 120; // 2 minutes warning countdown before auto logout

export function SessionTimeoutModal() {
  const { isAuthenticated, logout } = useAuth();
  const toast = useToast();

  const [open, setOpen] = useState(false);
  const [remainingSeconds, setCountdownSeconds] = useState(COUNTDOWN_SECONDS);

  const lastActiveRef = useRef(Date.now());
  const countdownIntervalRef = useRef(null);
  const checkIntervalRef = useRef(null);

  // Activity handler to track last interaction time
  useEffect(() => {
    if (!isAuthenticated) {
      setOpen(false);
      return;
    }

    const handleActivity = () => {
      // Only update active time if the timeout warning modal is NOT open
      if (!open) {
        lastActiveRef.current = Date.now();
      }
    };

    const events = ['mousemove', 'mousedown', 'keydown', 'scroll', 'touchstart', 'click'];
    events.forEach((event) => window.addEventListener(event, handleActivity, { passive: true }));

    // Check inactivity every 10 seconds
    checkIntervalRef.current = setInterval(() => {
      if (!open && isAuthenticated) {
        const inactiveMs = Date.now() - lastActiveRef.current;
        if (inactiveMs >= INACTIVITY_THRESHOLD_MS) {
          setOpen(true);
          setCountdownSeconds(COUNTDOWN_SECONDS);
        }
      }
    }, 10000);

    return () => {
      events.forEach((event) => window.removeEventListener(event, handleActivity));
      if (checkIntervalRef.current) clearInterval(checkIntervalRef.current);
    };
  }, [isAuthenticated, open]);

  // Handle countdown when warning modal is open
  useEffect(() => {
    if (open) {
      countdownIntervalRef.current = setInterval(() => {
        setCountdownSeconds((prev) => {
          if (prev <= 1) {
            clearInterval(countdownIntervalRef.current);
            handleAutoLogout();
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } else {
      if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
    }

    return () => {
      if (countdownIntervalRef.current) clearInterval(countdownIntervalRef.current);
    };
  }, [open]);

  const handleAutoLogout = async () => {
    setOpen(false);
    await logout();
    toast.authNotify(
      'warning',
      'Session Expired',
      'Your session has expired due to inactivity. Please log in again.'
    );
  };

  const handleContinueSession = () => {
    lastActiveRef.current = Date.now();
    setOpen(false);
  };

  const handleManualLogout = async () => {
    setOpen(false);
    await logout();
    toast.authNotify('info', 'Signed Out', 'You have been signed out. See you soon!');
  };

  if (!isAuthenticated) return null;

  const progressPercent = (remainingSeconds / COUNTDOWN_SECONDS) * 100;
  const minutes = Math.floor(remainingSeconds / 60);
  const seconds = remainingSeconds % 60;
  const formattedTime = `${minutes}:${seconds < 10 ? '0' : ''}${seconds}`;

  return (
    <Dialog
      open={open}
      onClose={handleContinueSession}
      maxWidth="xs"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 3,
          p: 1,
          bgcolor: 'background.paper',
          border: '1px solid rgba(255, 255, 255, 0.12)',
          boxShadow: '0 16px 40px rgba(0,0,0,0.5)',
        },
      }}
    >
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1.5, pb: 1 }}>
        <AccessTimeIcon color="warning" sx={{ fontSize: 32 }} />
        <Typography variant="h6" fontWeight={700}>
          Session Expiring Soon
        </Typography>
      </DialogTitle>

      <DialogContent sx={{ py: 1.5 }}>
        <Typography variant="body1" color="text.secondary" paragraph>
          You have been inactive for a while. For your security, your session will expire in:
        </Typography>

        <Box textAlign="center" my={2}>
          <Typography variant="h4" fontWeight={800} color="warning.main">
            {formattedTime}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            minutes remaining
          </Typography>
        </Box>

        <LinearProgress
          variant="determinate"
          value={progressPercent}
          color="warning"
          sx={{ height: 8, borderRadius: 4, mb: 1 }}
        />
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2, pt: 1, gap: 1 }}>
        <Button variant="outlined" color="inherit" onClick={handleManualLogout} fullWidth>
          Logout
        </Button>
        <Button variant="contained" color="primary" onClick={handleContinueSession} fullWidth autoFocus>
          Continue Session
        </Button>
      </DialogActions>
    </Dialog>
  );
}
