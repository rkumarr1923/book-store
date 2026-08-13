import React from 'react';
import { Box, Divider, Typography } from '@mui/material';

function TermsSection({ title, children }) {
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

function TermsPage() {
  return (
    <Box maxWidth={820} mx="auto" px={{ xs: 2, md: 4 }} py={6}>
      <Typography variant="h4" fontWeight={700} mb={0.5}>
        Terms of Service
      </Typography>
      <Typography variant="body2" color="text.secondary" mb={4}>
        Last updated: {new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}
      </Typography>

      <Divider sx={{ mb: 4 }} />

      <TermsSection title="1. Acceptance of Terms">
        By accessing or using BookStore you agree to be bound by these Terms of Service. If you do
        not agree to these terms, please do not use the application.
      </TermsSection>

      <TermsSection title="2. Use of the Service">
        BookStore is provided for personal, non-commercial use. You agree not to use the service for
        any unlawful purpose, to attempt to gain unauthorised access to any part of the system, or
        to interfere with the normal operation of the platform.
      </TermsSection>

      <TermsSection title="3. Account Registration">
        You must provide accurate information when creating an account. You are responsible for
        maintaining the confidentiality of your password and for all activity that occurs under your
        account. You agree to notify us immediately of any unauthorised use of your account.
      </TermsSection>

      <TermsSection title="4. Orders and Payments">
        All orders are subject to product availability. Prices displayed are inclusive of applicable
        taxes. We reserve the right to cancel or refuse any order at our discretion. Payment is
        required in full at the time of placing an order.
      </TermsSection>

      <TermsSection title="5. Coupons and Discounts">
        Coupons and discount codes are subject to individual terms and expiry dates. Only one coupon
        may be applied per order. BookStore reserves the right to withdraw or modify promotional
        offers at any time.
      </TermsSection>

      <TermsSection title="6. Intellectual Property">
        All content on BookStore — including text, graphics, logos, and software — is the property
        of BookStore or its content suppliers and is protected by applicable intellectual property
        laws. You may not reproduce or distribute any content without prior written permission.
      </TermsSection>

      <TermsSection title="7. User-Generated Content">
        By submitting a review or any other content, you grant BookStore a non-exclusive,
        royalty-free licence to use, display, and distribute that content on the platform. You
        represent that your content does not infringe the rights of any third party.
      </TermsSection>

      <TermsSection title="8. Limitation of Liability">
        BookStore is provided "as is" without warranties of any kind. To the maximum extent permitted
        by law, we are not liable for any indirect, incidental, or consequential damages arising from
        your use of the service.
      </TermsSection>

      <TermsSection title="9. Changes to Terms">
        We reserve the right to update these Terms of Service at any time. Continued use of
        BookStore after changes are posted constitutes acceptance of the revised terms.
      </TermsSection>

      <TermsSection title="10. Governing Law">
        These Terms are governed by the laws of India. Any disputes arising under these Terms shall
        be subject to the exclusive jurisdiction of the courts of Bengaluru, Karnataka.
      </TermsSection>

      <Divider sx={{ mb: 3 }} />

      <Typography variant="caption" color="text.secondary">
        BookStore is a personal portfolio project. These Terms of Service are provided for
        demonstration purposes only.
      </Typography>
    </Box>
  );
}

export default TermsPage;
