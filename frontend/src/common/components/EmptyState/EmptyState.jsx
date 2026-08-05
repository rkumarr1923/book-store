import React from 'react';
import { Box, Typography } from '@mui/material';
import InboxIcon from '@mui/icons-material/Inbox';

/**
 * Empty state placeholder component.
 *
 * @param {string}  title     - Main empty state heading.
 * @param {string}  subtitle  - Optional supporting text.
 * @param {node}    action    - Optional CTA element.
 * @param {node}    icon      - Optional override icon.
 */
function EmptyState({
  title = 'Nothing here yet',
  subtitle,
  action,
  icon,
}) {
  return (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      minHeight="30vh"
      gap={2}
      py={6}
      textAlign="center"
    >
      {icon || (
        <InboxIcon sx={{ fontSize: 56, color: 'text.secondary', opacity: 0.4 }} />
      )}
      <Typography variant="h6" color="text.secondary">
        {title}
      </Typography>
      {subtitle && (
        <Typography variant="body2" color="text.secondary" maxWidth={400}>
          {subtitle}
        </Typography>
      )}
      {action}
    </Box>
  );
}

export default EmptyState;
