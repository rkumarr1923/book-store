import React, { useState } from 'react';
import { Box } from '@mui/material';
import { Outlet } from 'react-router-dom';
import Navbar from '../components/Navbar/Navbar';
import Footer from '../components/Footer/Footer';

/**
 * Main application layout wrapping all routes.
 * Accepts an optional `onLoginOpen` callback forwarded from the router
 * layer via context or props.
 */
function MainLayout({ onLoginOpen }) {
  return (
    <Box
      display="flex"
      flexDirection="column"
      minHeight="100vh"
      bgcolor="background.default"
    >
      <Navbar onLoginOpen={onLoginOpen} />

      {/* Page content */}
      <Box component="main" flex={1}>
        <Outlet context={{ onLoginOpen }} />
      </Box>

      <Footer />
    </Box>
  );
}

export default MainLayout;
