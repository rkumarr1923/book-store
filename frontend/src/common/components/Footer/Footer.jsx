import { Box, Divider, Link, Stack, Typography } from '@mui/material';
import LocalLibraryIcon from '@mui/icons-material/LocalLibrary';

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
        <Box display="flex" alignItems="center" gap={1}>
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
          {['About Us', 'Privacy Policy', 'Terms of Service', 'Contact'].map((label) => (
            <Link
              key={label}
              href="#"
              underline="hover"
              color="text.secondary"
              variant="body2"
            >
              {label}
            </Link>
          ))}
        </Stack>
      </Box>

      <Divider sx={{ my: 2 }} />

      <Box textAlign="center">
        <Typography variant="caption" color="text.secondary">
          © {new Date().getFullYear()} BookStore. All rights reserved.
        </Typography>
      </Box>
    </Box>
  );
}

export default Footer;
