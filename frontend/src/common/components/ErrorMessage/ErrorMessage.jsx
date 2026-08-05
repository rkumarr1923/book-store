import React from 'react';
import { Box, Alert, Typography } from '@mui/material';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';

/**
 * Generic error state component.
 *
 * @param {string}  message   - Error message to display.
 * @param {string}  variant   - "page" for full-page, "inline" for inline alert (default).
 * @param {node}    action    - Optional action element (e.g., a retry button).
 */
function ErrorMessage({ message = 'Something went wrong.', variant = 'inline', action }) {
  if (variant === 'page') {
    return (
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="40vh"
        gap={2}
        py={6}
        textAlign="center"
      >
        <ErrorOutlineIcon sx={{ fontSize: 56, color: 'error.main', opacity: 0.7 }} />
        <Typography variant="h6" color="text.secondary">
          {message}
        </Typography>
        {action}
      </Box>
    );
  }

  return (
    <Alert severity="error" sx={{ my: 1 }} action={action}>
      {message}
    </Alert>
  );
}

export default ErrorMessage;
