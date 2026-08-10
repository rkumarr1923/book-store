import React, { useState } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Divider,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import EmailIcon from '@mui/icons-material/Email';
import LocationOnIcon from '@mui/icons-material/LocationOn';
import AccessTimeIcon from '@mui/icons-material/AccessTime';
import LocalLibraryIcon from '@mui/icons-material/LocalLibrary';
import SendIcon from '@mui/icons-material/Send';

import { contactApi } from '../../common/api/contactApi';
import { isValidEmail } from '../../common/utils/validators';
import { useToast } from '../../common/context/ToastContext';

// ─── Info Card ────────────────────────────────────────────────────────────────

function InfoCard({ icon, label, value }) {
  return (
    <Box
      display="flex"
      alignItems="flex-start"
      gap={2}
      sx={{
        bgcolor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        p: 2,
      }}
    >
      <Box
        sx={{
          color: 'primary.main',
          flexShrink: 0,
          width: 38,
          height: 38,
          bgcolor: 'rgba(41,121,255,0.12)',
          borderRadius: 1.5,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {icon}
      </Box>
      <Box>
        <Typography variant="body2" fontWeight={600} color="text.primary" mb={0.25}>
          {label}
        </Typography>
        <Typography variant="body2" color="text.secondary" lineHeight={1.6}>
          {value}
        </Typography>
      </Box>
    </Box>
  );
}

// ─── Contact Page ─────────────────────────────────────────────────────────────

const EMPTY_FORM = { name: '', email: '', subject: '', message: '' };

function ContactPage() {
  const toast = useToast();
  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});
  const [sending, setSending] = useState(false);

  const set = (k) => (e) => {
    setForm((f) => ({ ...f, [k]: e.target.value }));
    setErrors((er) => ({ ...er, [k]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.name.trim())    e.name    = 'Name is required';
    if (!form.email.trim())   e.email   = 'Email is required';
    else if (!isValidEmail(form.email.trim())) e.email = 'Enter a valid email address';
    if (!form.message.trim()) e.message = 'Message is required';
    return e;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }

    setSending(true);
    try {
      await contactApi.sendMessage({
        name:    form.name.trim(),
        email:   form.email.trim(),
        subject: form.subject.trim() || undefined,
        message: form.message.trim(),
      });
      setForm(EMPTY_FORM);
      setErrors({});
      toast.success("Message sent successfully! We'll get back to you soon.", 'Message Sent');
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to send message. Please try again.';
      toast.error(msg, 'Send Failed');
    } finally {
      setSending(false);
    }
  };

  return (
    <Box maxWidth={940} mx="auto" px={{ xs: 2, md: 4 }} py={{ xs: 4, md: 7 }}>

      {/* ── Page header ── */}
      <Box mb={1}>
        <Typography variant="h4" fontWeight={700} mb={0.75}>
          Contact Us
        </Typography>
        <Typography variant="body1" color="text.secondary" maxWidth={560}>
          Have a question, feedback, or just want to say hello?
          We'd love to hear from you.
        </Typography>
      </Box>

      <Divider sx={{ my: 4 }} />

      <Box display="grid" gridTemplateColumns={{ xs: '1fr', md: '5fr 7fr' }} gap={{ xs: 4, md: 6 }}>

        {/* ── Left column: contact info ── */}
        <Stack spacing={2}>
          <Typography variant="h6" fontWeight={700} mb={0.5}>
            Get in Touch
          </Typography>

          <InfoCard
            icon={<EmailIcon fontSize="small" />}
            label="Email"
            value="teambookstore@gmail.com"
          />
          <InfoCard
            icon={<LocationOnIcon fontSize="small" />}
            label="Location"
            value="Chennai, Tamil Nadu, India"
          />
          <InfoCard
            icon={<AccessTimeIcon fontSize="small" />}
            label="Support Hours"
            value="Monday – Friday · 9 AM – 6 PM IST"
          />

          {/* Portfolio notice */}
          <Box
            sx={{
              bgcolor: 'rgba(41,121,255,0.06)',
              border: '1px solid',
              borderColor: 'rgba(41,121,255,0.2)',
              borderRadius: 2,
              p: 2,
              mt: 0.5,
            }}
          >
            <Box display="flex" alignItems="center" gap={1} mb={0.75}>
              <LocalLibraryIcon sx={{ color: 'primary.main', fontSize: 18 }} />
              <Typography variant="body2" fontWeight={700} color="primary.main">
                Portfolio Project
              </Typography>
            </Box>
            <Typography variant="body2" color="text.secondary" lineHeight={1.7}>
              Built by{' '}
              <Typography component="span" variant="body2" color="text.primary" fontWeight={600}>
                Ram
              </Typography>
              {' '}as a full-stack portfolio project showcasing a real-world e-commerce experience.
              Feel free to reach out with feedback, opportunities, or just to connect.
            </Typography>
          </Box>
        </Stack>

        {/* ── Right column: contact form ── */}
        <Box
          component="form"
          onSubmit={handleSubmit}
          noValidate
          sx={{
            bgcolor: 'background.paper',
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: 2,
            p: { xs: 2.5, sm: 3.5 },
          }}
        >
          <Typography variant="h6" fontWeight={700} mb={2.5}>
            Send a Message
          </Typography>

          <Stack spacing={2.5}>
            {/* Name + Email row */}
            <Box display="grid" gridTemplateColumns={{ xs: '1fr', sm: '1fr 1fr' }} gap={2}>
              <TextField
                label="Your Name"
                value={form.name}
                onChange={set('name')}
                error={!!errors.name}
                helperText={errors.name}
                size="small"
                fullWidth
                autoComplete="name"
                disabled={sending}
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
                autoComplete="email"
                disabled={sending}
              />
            </Box>

            {/* Subject */}
            <TextField
              label="Subject (optional)"
              value={form.subject}
              onChange={set('subject')}
              size="small"
              fullWidth
              disabled={sending}
            />

            {/* Message */}
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
              disabled={sending}
            />

            {/* Submit */}
            <Box>
              <Button
                type="submit"
                variant="contained"
                size="large"
                disabled={sending}
                startIcon={
                  sending
                    ? <CircularProgress size={16} color="inherit" />
                    : <SendIcon fontSize="small" />
                }
                sx={{ px: 4 }}
              >
                {sending ? 'Sending…' : 'Send Message'}
              </Button>
            </Box>
          </Stack>
        </Box>

      </Box>
    </Box>
  );
}

export default ContactPage;
