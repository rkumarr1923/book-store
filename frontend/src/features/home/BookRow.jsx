import React from 'react';
import { Box, Skeleton } from '@mui/material';
import BookCard from '../../common/components/BookCard/BookCard';

/**
 * Horizontal scroll book grid for home sections.
 * Used for Recommended, Bestsellers, New Launches sections.
 */
function BookRow({ books = [], loading = false, skeletonCount = 5 }) {
  if (loading) {
    return (
      <Box
        display="grid"
        gridTemplateColumns="repeat(auto-fill, minmax(160px, 1fr))"
        gap={2}
      >
        {Array.from({ length: skeletonCount }).map((_, i) => (
          <Box key={i}>
            <Skeleton variant="rectangular" height={200} sx={{ borderRadius: 1, mb: 1 }} />
            <Skeleton width="80%" height={16} sx={{ mb: 0.5 }} />
            <Skeleton width="50%" height={14} />
          </Box>
        ))}
      </Box>
    );
  }

  return (
    <Box
      display="grid"
      gridTemplateColumns="repeat(auto-fill, minmax(160px, 1fr))"
      gap={2}
    >
      {books.map((book) => (
        <BookCard key={book.id} book={book} />
      ))}
    </Box>
  );
}

export default BookRow;
