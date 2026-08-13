import React from 'react';
import { Box, Divider, Typography } from '@mui/material';

function PolicySection({ title, children }) {
  return (
    <Box mb={3.5}>
      <Typography variant="h6" fontWeight={700} mb={1} color="text.primary">
        {title}
      </Typography>
      <Typography variant="body1" color="text.secondary">
        {children}
      </Typography>
    </Box>
  );
}

function PrivacyPage() {
  return (
    <Box maxWidth={820} mx="auto" px={{ xs: 2, md: 4 }} py={6}>
      <Typography variant="h4" fontWeight={700} mb={0.5}>
        Privacy Policy
      </Typography>
      <Typography variant="body2" color="text.secondary" mb={4}>
        Last updated: {new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}
      </Typography>

      <Divider sx={{ mb: 4 }} />

      <PolicySection title="1. Overview">
        BookStore ("we", "us", "our") is committed to protecting your personal information. This
        Privacy Policy explains what data we collect, how we use it, and the choices you have. By
        using BookStore you agree to the practices described in this policy.
      </PolicySection>

      <PolicySection title="2. Information We Collect">
        We collect information you provide directly — such as your name, email address, phone number,
        and delivery address when you register or place an order. We also collect transactional data
        (order history, items purchased) and technical data (browser type, pages visited) to improve
        the application experience.
      </PolicySection>

      <PolicySection title="3. How We Use Your Information">
        We use your information to process and fulfil orders, send order confirmations and delivery
        updates, personalise your book recommendations, maintain and improve the platform, and
        communicate promotional offers (you may opt out at any time).
      </PolicySection>

      <PolicySection title="4. Data Sharing">
        We do not sell, rent, or trade your personal information to third parties. We may share data
        with trusted service providers (payment processors, delivery partners) strictly to fulfil
        your orders, and only to the extent necessary.
      </PolicySection>

      <PolicySection title="5. Cookies">
        BookStore uses session cookies and local storage to keep you logged in and to remember your
        preferences. You can disable cookies in your browser settings, though some features may not
        work correctly as a result.
      </PolicySection>

      <PolicySection title="6. Data Security">
        We use industry-standard security measures including HTTPS encryption and hashed password
        storage (BCrypt). No method of transmission over the internet is 100% secure; we cannot
        guarantee absolute security but we take reasonable precautions to protect your data.
      </PolicySection>

      <PolicySection title="7. Data Retention">
        We retain your account and order data for as long as your account is active or as required
        for legal and business purposes. You may request deletion of your account by contacting us.
      </PolicySection>

      <PolicySection title="8. Your Rights">
        You have the right to access, correct, or delete your personal data. To exercise these rights,
        contact us at the email address on the Contact page. We will respond within 30 days.
      </PolicySection>

      <PolicySection title="9. Changes to This Policy">
        We may update this policy periodically. We will notify you of significant changes by posting
        a notice on the site. Continued use of BookStore after changes are posted constitutes your
        acceptance of the updated policy.
      </PolicySection>

      <Divider sx={{ mb: 3 }} />

      <Typography variant="caption" color="text.secondary">
        BookStore is a personal portfolio project. This Privacy Policy is provided for demonstration
        purposes only.
      </Typography>
    </Box>
  );
}

export default PrivacyPage;
