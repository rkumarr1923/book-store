/* eslint-disable react/prop-types */
import { useEffect, useState } from 'react';
import {
  AppBar,
  Badge,
  Box,
  Divider,
  IconButton,
  ListItemIcon,
  Menu,
  MenuItem,
  Toolbar,
  Tooltip,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import FavoriteIcon from '@mui/icons-material/Favorite';
import EditNoteIcon from '@mui/icons-material/EditNote';
import LogoutIcon from '@mui/icons-material/Logout';
import LocalLibraryIcon from '@mui/icons-material/LocalLibrary';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useCart } from '../../hooks/useCart';
import { ROUTES } from '../../constants/routes';

function Navbar({ onLoginOpen }) {
  const { isAuthenticated, user, logout } = useAuth();
  const { cartCount, syncCartCount } = useCart();
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  const [anchorEl, setAnchorEl] = useState(null);
  const menuOpen = Boolean(anchorEl);

  const handleMenuOpen = (e) => setAnchorEl(e.currentTarget);
  const handleMenuClose = () => setAnchorEl(null);

  // Sync cart count when user logs in
  useEffect(() => {
    if (isAuthenticated) {
      syncCartCount();
    }
  }, [isAuthenticated, syncCartCount]);

  const handleLogout = async () => {
    handleMenuClose();
    await logout();
    navigate(ROUTES.HOME);
  };

  const navLinks = [
    { label: 'My Orders', path: ROUTES.ORDERS },
    { label: 'My Wishlist', path: ROUTES.WISHLIST },
    { label: 'My Writers', path: ROUTES.MY_WRITERS },
  ];

  return (
    <AppBar position="sticky" elevation={0}>
      <Toolbar sx={{ minHeight: 56, gap: 1 }}>
        {/* Brand */}
        <Box
          component={RouterLink}
          to={ROUTES.HOME}
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 1,
            textDecoration: 'none',
            color: 'text.primary',
            mr: 2,
          }}
        >
          <LocalLibraryIcon sx={{ color: 'primary.main' }} />
          <Typography variant="h6" fontWeight={700} noWrap>
            {import.meta.env.VITE_APP_NAME || 'BookStore'}
          </Typography>
        </Box>

        {/* Separator */}
        <Divider orientation="vertical" flexItem sx={{ mx: 1, display: { xs: 'none', md: 'block' } }} />

        {/* Nav links — hidden on mobile */}
        {!isMobile && (
          <Box display="flex" gap={1}>
            {navLinks.map((link) =>
              isAuthenticated ? (
                <Typography
                  key={link.path}
                  component={RouterLink}
                  to={link.path}
                  variant="body2"
                  sx={{
                    color: 'text.secondary',
                    textDecoration: 'none',
                    px: 1,
                    py: 0.5,
                    borderRadius: 1,
                    '&:hover': { color: 'text.primary', bgcolor: 'rgba(255,255,255,0.05)' },
                  }}
                >
                  {link.label}
                </Typography>
              ) : null
            )}
          </Box>
        )}

        <Box flex={1} />

        {/* Cart icon */}
        <Tooltip title="Shopping Cart">
          <IconButton
            onClick={() => (isAuthenticated ? navigate(ROUTES.CHECKOUT) : onLoginOpen?.())}
            color="inherit"
            size="small"
          >
            <Badge badgeContent={cartCount || null} color="error">
              <ShoppingCartIcon />
            </Badge>
          </IconButton>
        </Tooltip>

        {/* Account / Auth icon */}
        {isAuthenticated ? (
          <>
            <Tooltip title={user?.firstName || 'Account'}>
              <IconButton onClick={handleMenuOpen} color="inherit" size="small">
                <AccountCircleIcon />
              </IconButton>
            </Tooltip>
            <Menu
              anchorEl={anchorEl}
              open={menuOpen}
              onClose={handleMenuClose}
              transformOrigin={{ horizontal: 'right', vertical: 'top' }}
              anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
              PaperProps={{ sx: { minWidth: 180 } }}
            >
              <MenuItem disabled>
                <Typography variant="caption" color="text.secondary">
                  {user?.email || user?.firstName}
                </Typography>
              </MenuItem>
              <Divider />
              <MenuItem onClick={() => { handleMenuClose(); navigate(ROUTES.ORDERS); }}>
                <ListItemIcon><EditNoteIcon fontSize="small" /></ListItemIcon>
                My Orders
              </MenuItem>
              <MenuItem onClick={() => { handleMenuClose(); navigate(ROUTES.WISHLIST); }}>
                <ListItemIcon><FavoriteIcon fontSize="small" /></ListItemIcon>
                My Wishlist
              </MenuItem>
              <MenuItem onClick={() => { handleMenuClose(); navigate(ROUTES.MY_WRITERS); }}>
                <ListItemIcon><LocalLibraryIcon fontSize="small" /></ListItemIcon>
                My Writers
              </MenuItem>
              <MenuItem onClick={() => { handleMenuClose(); navigate(ROUTES.PROFILE); }}>
                <ListItemIcon><ManageAccountsIcon fontSize="small" /></ListItemIcon>
                My Account
              </MenuItem>
              <Divider />
              <MenuItem onClick={handleLogout}>
                <ListItemIcon><LogoutIcon fontSize="small" /></ListItemIcon>
                Sign Out
              </MenuItem>
            </Menu>
          </>
        ) : (
          <Tooltip title="Sign In">
            <IconButton onClick={() => onLoginOpen?.()} color="inherit" size="small">
              <AccountCircleIcon />
            </IconButton>
          </Tooltip>
        )}
      </Toolbar>
    </AppBar>
  );
}

export default Navbar;
