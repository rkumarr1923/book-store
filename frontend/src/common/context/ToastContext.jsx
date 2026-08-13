import React, { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { Alert, Slide, Snackbar, Stack } from '@mui/material';

// ─── Context ─────────────────────────────────────────────────────────────────

export const ToastContext = createContext(null);

// ─── Hook ─────────────────────────────────────────────────────────────────────

/**
 * useToast — fire app-wide toast notifications from any component.
 *
 * Usage:
 *   const toast = useToast();
 *   toast.success('Added to cart!');
 *   toast.error('Something went wrong.');
 *   toast.info('Please sign in to continue.');
 *   toast.warning('Low stock — only 2 left.');
 */
export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used inside <ToastProvider>');
  return ctx;
}

// ─── Provider ─────────────────────────────────────────────────────────────────

let idCounter = 0;
const MAX_VISIBLE = 3; // never stack more than 3 toasts at once

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const push = useCallback((severity, message, title = null, duration = 4000) => {
    const id = ++idCounter;
    setToasts((prev) => {
      const next = [...prev, { id, severity, message, title, open: true }];
      return next.slice(-MAX_VISIBLE);
    });
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, duration + 400);
    return id;
  }, []);

  const close = useCallback((id) => {
    setToasts((prev) =>
      prev.map((t) => (t.id === id ? { ...t, open: false } : t))
    );
  }, []);

  const clearAll = useCallback(() => {
    setToasts([]);
  }, []);

  const api = useMemo(
    () => ({
      success: (msg, titleOrDuration, maybeDuration) => {
        const title = typeof titleOrDuration === 'string' ? titleOrDuration : null;
        const duration = typeof titleOrDuration === 'number' ? titleOrDuration : (maybeDuration || 4000);
        return push('success', msg, title, duration);
      },
      error: (msg, titleOrDuration, maybeDuration) => {
        const title = typeof titleOrDuration === 'string' ? titleOrDuration : null;
        const duration = typeof titleOrDuration === 'number' ? titleOrDuration : (maybeDuration || 5000);
        return push('error', msg, title, duration);
      },
      info: (msg, titleOrDuration, maybeDuration) => {
        const title = typeof titleOrDuration === 'string' ? titleOrDuration : null;
        const duration = typeof titleOrDuration === 'number' ? titleOrDuration : (maybeDuration || 4000);
        return push('info', msg, title, duration);
      },
      warning: (msg, titleOrDuration, maybeDuration) => {
        const title = typeof titleOrDuration === 'string' ? titleOrDuration : null;
        const duration = typeof titleOrDuration === 'number' ? titleOrDuration : (maybeDuration || 5000);
        return push('warning', msg, title, duration);
      },
      authNotify: (severity, title, message, duration = 5000) =>
        push(severity, message, title, duration),
      clearAuthNotify: clearAll,
    }),
    [push, clearAll]
  );

  return (
    <ToastContext.Provider value={api}>
      {children}

      {/* Global Top-Center Prominent Toast Notification Banner Stack */}
      <Stack
        spacing={1.5}
        sx={{
          position: 'fixed',
          top: { xs: 16, sm: 24 },
          left: '50%',
          transform: 'translateX(-50%)',
          zIndex: 3000,
          alignItems: 'center',
          pointerEvents: 'none',
          maxWidth: { xs: '92vw', sm: 520 },
          width: '100%',
        }}
      >
        {toasts.map((t) => (
          <Snackbar
            key={t.id}
            open={t.open}
            autoHideDuration={4500}
            onClose={() => close(t.id)}
            anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
            TransitionComponent={Slide}
            TransitionProps={{ direction: 'down' }}
            sx={{
              position: 'static',
              transform: 'none !important',
              width: '100%',
            }}
          >
            <Alert
              severity={t.severity}
              onClose={() => close(t.id)}
              variant="filled"
              elevation={10}
              sx={{
                width: '100%',
                borderRadius: 2.5,
                fontSize: '0.95rem',
                fontWeight: 500,
                alignItems: 'center',
                boxShadow: '0 14px 32px rgba(0,0,0,0.45)',
                px: 2.5,
                py: 1.5,
                pointerEvents: 'auto',
                border: '1px solid rgba(255,255,255,0.18)',
                backdropFilter: 'blur(8px)',
                '& .MuiAlert-icon': {
                  fontSize: 26,
                  mr: 1.5,
                },
                '& .MuiAlert-message': {
                  overflow: 'hidden',
                  width: '100%',
                },
              }}
            >
              {t.title && (
                <span
                  style={{
                    display: 'block',
                    fontWeight: 700,
                    marginBottom: 2,
                    fontSize: '1rem',
                    letterSpacing: '0.2px',
                  }}
                >
                  {t.title}
                </span>
              )}
              {t.message}
            </Alert>
          </Snackbar>
        ))}
      </Stack>
    </ToastContext.Provider>
  );
}
