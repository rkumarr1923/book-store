import React from 'react';
import { Box, Skeleton } from '@mui/material';
import BookCard from '../../common/components/BookCard/BookCard';

/**
 * Responsive grid of BookCard components — used in Catalogue.
 */
function BookGrid({ books = [], loading = false, skeletonCount = 12 }) {
  if (loading) {
    return (
      <Box
        display="grid"
        gridTemplateColumns="repeat(auto-fill, minmax(172px, 1fr))"
        gap={2}
      >
        {Array.from({ length: skeletonCount }).map((_, i) => (
          <Box key={i}>
            <Skeleton variant="rectangular" height={220} sx={{ borderRadius: 1, mb: 1 }} />
            <Skeleton width="85%" height={16} sx={{ mb: 0.5 }} />
            <Skeleton width="55%" height={14} sx={{ mb: 0.5 }} />
            <Skeleton width="40%" height={14} />
          </Box>
        ))}
      </Box>
    );
  }

  return (
    <Box
      display="grid"
      gridTemplateColumns="repeat(auto-fill, minmax(172px, 1fr))"
      gap={2}
    >
      {books.map((book) => (
        <BookCard key={book.id} book={book} />
      ))}
    </Box>
  );
}

export default BookGrid;
