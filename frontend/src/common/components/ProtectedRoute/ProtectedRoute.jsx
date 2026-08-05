import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { ROUTES } from '../../constants/routes';
import PageLoader from '../PageLoader/PageLoader';

/**
 * Wraps a route and redirects unauthenticated users.
 * The original location is preserved so the user can be
 * redirected back after a successful login.
 */
function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return <PageLoader />;
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.HOME} state={{ from: location, requireLogin: true }} replace />;
  }

  return children;
}

export default ProtectedRoute;
