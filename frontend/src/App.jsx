import React, { useEffect, useState } from 'react';
import { Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { ROUTES } from './common/constants/routes';
import MainLayout from './common/layout/MainLayout';
import ProtectedRoute from './common/components/ProtectedRoute/ProtectedRoute';
import ScrollToTop from './common/components/ScrollToTop/ScrollToTop';
import { useAuth } from './common/hooks/useAuth';

// Feature pages
import HomePage from './features/home/HomePage';
import CataloguePage from './features/catalogue/CataloguePage';
import BookDetailPage from './features/bookDetail/BookDetailPage';
import AuthorProfilePage from './features/author/AuthorProfilePage';
import CheckoutPage from './features/cart/CheckoutPage';
import MyOrdersPage from './features/orders/MyOrdersPage';
import WishlistPage from './features/wishlist/WishlistPage';
import MyWritersPage from './features/myWriters/MyWritersPage';
import ProfilePage from './features/profile/ProfilePage';
import OrderConfirmationPage from './features/payment/OrderConfirmationPage';
import NotFoundPage from './features/NotFoundPage';
import AuthModal from './features/auth/AuthModal';
import { SessionTimeoutModal } from './features/auth/SessionTimeoutModal';

// Static pages
import AboutPage from './features/static/AboutPage';
import PrivacyPage from './features/static/PrivacyPage';
import TermsPage from './features/static/TermsPage';
import ContactPage from './features/static/ContactPage';

function App() {
  const [loginOpen, setLoginOpen] = useState(false);
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  // Allow any component to open the login modal via a custom event
  useEffect(() => {
    const handler = () => setLoginOpen(true);
    window.addEventListener('open:login', handler);
    return () => window.removeEventListener('open:login', handler);
  }, []);

  // Open login modal when ProtectedRoute redirects here with requireLogin flag
  useEffect(() => {
    if (location.state?.requireLogin) {
      setLoginOpen(true);
    }
  }, [location.state]);

  // After successful login, redirect back to the intended page
  useEffect(() => {
    if (isAuthenticated && location.state?.from) {
      const from = location.state.from;
      navigate(from.pathname + (from.search || '') + (from.hash || ''), { replace: true });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated]);

  return (
    <>
      <ScrollToTop />
      <Routes>
        <Route element={<MainLayout onLoginOpen={() => setLoginOpen(true)} />}>
          {/* Public routes */}
          <Route index element={<HomePage />} />
          <Route path={ROUTES.CATALOGUE} element={<CataloguePage />} />
          <Route path={ROUTES.BOOK_DETAIL} element={<BookDetailPage />} />
          <Route path={ROUTES.AUTHOR_PROFILE} element={<AuthorProfilePage />} />

          {/* Protected routes */}
          <Route
            path={ROUTES.CHECKOUT}
            element={
              <ProtectedRoute>
                <CheckoutPage />
              </ProtectedRoute>
            }
          />
          <Route
            path={ROUTES.ORDERS}
            element={
              <ProtectedRoute>
                <MyOrdersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path={ROUTES.WISHLIST}
            element={
              <ProtectedRoute>
                <WishlistPage />
              </ProtectedRoute>
            }
          />
          <Route
            path={ROUTES.MY_WRITERS}
            element={
              <ProtectedRoute>
                <MyWritersPage />
              </ProtectedRoute>
            }
          />
          <Route
            path={ROUTES.PROFILE}
            element={
              <ProtectedRoute>
                <ProfilePage />
              </ProtectedRoute>
            }
          />
          <Route
            path={ROUTES.ORDER_CONFIRMATION}
            element={
              <ProtectedRoute>
                <OrderConfirmationPage />
              </ProtectedRoute>
            }
          />

          {/* Static / info pages */}
          <Route path={ROUTES.ABOUT}   element={<AboutPage />} />
          <Route path={ROUTES.PRIVACY} element={<PrivacyPage />} />
          <Route path={ROUTES.TERMS}   element={<TermsPage />} />
          <Route path={ROUTES.CONTACT} element={<ContactPage />} />

          {/* 404 */}
          <Route path={ROUTES.NOT_FOUND} element={<NotFoundPage />} />
        </Route>
      </Routes>

      {/* Global auth modal & inactivity session timeout monitor */}
      <AuthModal open={loginOpen} onClose={() => setLoginOpen(false)} />
      <SessionTimeoutModal />
    </>
  );
}

export default App;
