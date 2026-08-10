import React, { useState } from 'react';
import {
  Alert,
  Box,
  Button,
  Divider,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import EmailIcon from '@mui/icons-material/Email';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import LocalLibraryIcon from '@mui/icons-material/LocalLibrary';

function InfoRow({ icon, label, value }) {
  return (
    <Box display="flex" alignItems="flex-start" gap={1.5}>
      <Box
        sx={{
          color: 'primary.main',
          mt: 0.2,
          flexShrink: 0,
          width: 36,
          height: 36,
          bgcolor: 'rgba(41,121,255,0.10)',
          borderRadius: 1.5,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {icon}
      </Box>
      <Box>
        <Typography variant="body2" fontWeight={600} color="text.primary" mb={0.2}>
          {label}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {value}
        </Typography>
      </Box>
    </Box>
  );
}

function ContactPage() {
  const [form, setForm] = useState({ name: '', email: '', subject: '', message: '' });
  const [submitted, setSubmitted] = useState(false);
  const [errors, setErrors] = useState({});

  const set = (k) => (e) => {
    setForm((f) => ({ ...f, [k]: e.target.value }));
    setErrors((er) => ({ ...er, [k]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'Required';
    if (!form.email.trim()) e.email = 'Required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Enter a valid email';
    if (!form.message.trim()) e.message = 'Required';
    return e;
  };

  const handleSubmit = (ev) => {
    ev.preventDefault();
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setSubmitted(true);
  };

  return (
    <Box maxWidth={900} mx="auto" px={{ xs: 2, md: 4 }} py={6}>

      {/* ── Header ── */}
      <Box mb={1}>
        <Typography variant="h4" fontWeight={700} mb={0.5}>
          Contact Us
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Have a question, feedback, or just want to say hello? We'd love to hear from you.
        </Typography>
      </Box>

      <Divider sx={{ my: 4 }} />

      <Box display="grid" gridTemplateColumns={{ xs: '1fr', md: '5fr 7fr' }} gap={5}>

        {/* ── Left: contact info ── */}
        <Stack spacing={1}>
          <Typography variant="h6" fontWeight={700} mb={1}>
            Get in Touch
          </Typography>

          <Stack spacing={2.5}>
            <InfoRow
              icon={<EmailIcon fontSize="small" />}
              label="Email"
              value="support@bookstore.example.com"
            />
            <InfoRow
              icon={<LocationOnIcon fontSize="small" />}
              label="Location"
              value="Chennai, Tamil Nadu, India"
            />
            <InfoRow
              icon={<AccessTimeIcon fontSize="small" />}
              label="Support Hours"
              value="Monday – Friday, 9 AM – 6 PM IST"
            />
          </Stack>

          <Box
            sx={{
              bgcolor: 'background.paper',
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: 2,
              p: 2.5,
              mt: 2,
            }}
          >
            <Box display="flex" alignItems="center" gap={1} mb={1}>
              <LocalLibraryIcon sx={{ color: 'primary.main', fontSize: 20 }} />
              <Typography variant="body2" fontWeight={600} color="text.primary">
                Portfolio Project
              </Typography>
            </Box>
            <Typography variant="body2" color="text.secondary" lineHeight={1.7}>
              BookStore is a personal portfolio project by Ramkumar K. All contact information
              and responses are for demonstration purposes.
            </Typography>
          </Box>
        </Stack>

        {/* ── Right: contact form ── */}
        <Box>
          <Typography variant="h6" fontWeight={700} mb={2.5}>
            Send a Message
          </Typography>

          {submitted ? (
            <Alert
              severity="success"
              sx={{ borderRadius: 2, alignItems: 'flex-start' }}
            >
              <Typography variant="body2" fontWeight={600} mb={0.5}>
                Message received!
              </Typography>
              <Typography variant="body2">
                Thank you for reaching out. We'll get back to you within 1–2 business days.
              </Typography>
            </Alert>
          ) : (
            <Box component="form" onSubmit={handleSubmit} noValidate>
              <Stack spacing={2.5}>
                <Box display="grid" gridTemplateColumns={{ xs: '1fr', sm: '1fr 1fr' }} gap={2}>
                  <TextField
                    label="Your Name"
                    value={form.name}
                    onChange={set('name')}
                    error={!!errors.name}
                    helperText={errors.name}
                    size="small"
                    fullWidth
                  />
                  <TextField
                    label="Email Address"
                    type="email"
                    value={form.email}
                    onChange={set('email')}
                    error={!!errors.email}
                    helperText={errors.email}
                    size="small"
                    fullWidth
                  />
                </Box>
                <TextField
                  label="Subject (optional)"
                  value={form.subject}
                  onChange={set('subject')}
                  size="small"
                  fullWidth
                />
                <TextField
                  label="Message"
                  multiline
                  rows={5}
                  value={form.message}
                  onChange={set('message')}
                  error={!!errors.message}
                  helperText={errors.message}
                  size="small"
                  fullWidth
                  placeholder="Tell us how we can help…"
                />
                <Box>
                  <Button
                    type="submit"
                    variant="contained"
                    size="medium"
                    sx={{ px: 4, py: 1 }}
                  >
                    Send Message
                  </Button>
                </Box>
              </Stack>
            </Box>
          )}
        </Box>
      </Box>
    </Box>
  );
}

export default ContactPage;
