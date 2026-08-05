import React from 'react';
import { Box, Rating, Typography } from '@mui/material';
import StarIcon from '@mui/icons-material/Star';

/**
 * StarRating — read-only or interactive star rating display.
 *
 * Props:
 *   value: number (0–5)
 *   readOnly: boolean (default true)
 *   onChange: fn(newValue) — used when readOnly=false
 *   showLabel: boolean — show numeric value next to stars
 *   size: 'small' | 'medium' | 'large'
 */
function StarRating({
  value = 0,
  readOnly = true,
  onChange,
  showLabel = false,
  size = 'small',
}) {
  return (
    <Box display="flex" alignItems="center" gap={0.5}>
      <Rating
        value={value}
        readOnly={readOnly}
        precision={0.5}
        size={size}
        onChange={readOnly ? undefined : (_, v) => onChange?.(v)}
        emptyIcon={<StarIcon style={{ opacity: 0.3 }} fontSize="inherit" />}
      />
      {showLabel && (
        <Typography variant="caption" color="text.secondary">
          {Number(value).toFixed(1)}
        </Typography>
      )}
    </Box>
  );
}

export default StarRating;
