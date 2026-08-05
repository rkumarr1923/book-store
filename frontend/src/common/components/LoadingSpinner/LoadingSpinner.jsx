import React from 'react';
import { Box, CircularProgress } from '@mui/material';

/**
 * Centred loading spinner for use inside content areas.
 */
function LoadingSpinner({ size = 40, sx = {} }) {
  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      py={4}
      sx={sx}
    >
      <CircularProgress size={size} color="primary" />
    </Box>
  );
}

export default LoadingSpinner;
