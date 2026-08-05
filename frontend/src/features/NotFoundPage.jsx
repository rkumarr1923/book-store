import React from 'react';
import { Box, Button, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { ROUTES } from '../common/constants/routes';

/**
 * 404 Not Found page.
 */
function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      minHeight="60vh"
      gap={3}
      textAlign="center"
      p={4}
    >
      <Typography variant="h1" color="primary" fontWeight={700} sx={{ fontSize: '6rem' }}>
        404
      </Typography>
      <Typography variant="h5" color="text.primary">
        Page Not Found
      </Typography>
      <Typography variant="body1" color="text.secondary" maxWidth={400}>
        The page you're looking for doesn't exist or has been moved.
      </Typography>
      <Button variant="contained" onClick={() => navigate(ROUTES.HOME)}>
        Back to Home
      </Button>
    </Box>
  );
}

export default NotFoundPage;
