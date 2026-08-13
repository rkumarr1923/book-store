import React from 'react';
import { Box, CircularProgress } from '@mui/material';

/**
 * Full-viewport loading overlay for initial page transitions.
 */
function PageLoader() {
  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight="100vh"
      bgcolor="background.default"
    >
      <CircularProgress size={56} color="primary" />
    </Box>
  );
}

export default PageLoader;
