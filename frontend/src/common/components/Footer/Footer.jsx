import { Box, Divider, Stack, Typography } from '@mui/material';
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom';
import LocalLibraryIcon from '@mui/icons-material/LocalLibrary';
import { ROUTES } from '../../constants/routes';

const CREATOR_ANCHOR = 'about-creator';

/**
 * "Ram" link in the footer copyright line.
 * - If already on the About page: smooth-scrolls to #about-creator in place.
 * - From any other page: navigates to /about#about-creator; the AboutPage
 *   useEffect then scrolls to the anchor after paint.
 */
function RamLink() {
  const location = useLocation();
  const navigate = useNavigate();

  const handleClick = (e) => {
    e.preventDefault();
    if (location.pathname === ROUTES.ABOUT) {
      // Already on the page — just scroll
      const el = document.getElementById(CREATOR_ANCHOR);
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } else {
      navigate(`${ROUTES.ABOUT}#${CREATOR_ANCHOR}`);
    }
  };

  return (
    <Typography
      component="a"
      href={`${ROUTES.ABOUT}#${CREATOR_ANCHOR}`}
      onClick={handleClick}
      variant="caption"
      sx={{ color: 'primary.main', textDecoration: 'none', '&:hover': { textDecoration: 'underline' } }}
    >
      Ram
    </Typography>
  );
}

const FOOTER_LINKS = [
  { label: 'About Us',         to: ROUTES.ABOUT   },
  { label: 'Privacy Policy',   to: ROUTES.PRIVACY },
  { label: 'Terms of Service', to: ROUTES.TERMS   },
  { label: 'Contact Us',       to: ROUTES.CONTACT },
];

function Footer() {
  return (
    <Box
      component="footer"
      sx={{
        bgcolor: 'background.paper',
        borderTop: '1px solid',
        borderColor: 'divider',
        mt: 'auto',
        py: 4,
        px: { xs: 2, md: 4 },
      }}
    >
      <Box
        maxWidth={1200}
        mx="auto"
        display="flex"
        flexDirection={{ xs: 'column', md: 'row' }}
        gap={3}
        justifyContent="space-between"
        alignItems={{ xs: 'flex-start', md: 'center' }}
      >
        {/* Brand */}
        <Box
          component={RouterLink}
          to={ROUTES.HOME}
          sx={{ display: 'flex', alignItems: 'center', gap: 1, textDecoration: 'none', color: 'inherit' }}
        >
          <LocalLibraryIcon sx={{ color: 'primary.main' }} />
          <Typography variant="h6" fontWeight={700}>
            BookStore
          </Typography>
        </Box>

        {/* Links */}
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={{ xs: 1, sm: 3 }}
          divider={<Divider orientation="vertical" flexItem sx={{ display: { xs: 'none', sm: 'block' } }} />}
        >
          {FOOTER_LINKS.map(({ label, to }) => (
            <Typography
              key={label}
              component={RouterLink}
              to={to}
              variant="body2"
              sx={{
                color: 'text.secondary',
                textDecoration: 'none',
                '&:hover': { color: 'text.primary' },
              }}
            >
              {label}
            </Typography>
          ))}
        </Stack>
      </Box>

      <Divider sx={{ my: 2 }} />

      <Box textAlign="center">
        <Typography variant="caption" color="text.secondary">
          © {new Date().getFullYear()} BookStore — a portfolio project by{' '}
          <RamLink />
          . All rights reserved.
        </Typography>
      </Box>
    </Box>
  );
}

export default Footer;
