import React from 'react';
import { Box, Pagination as MuiPagination } from '@mui/material';

/**
 * Centred pagination component wrapping MUI Pagination.
 *
 * @param {number}    page        - Current page (1-based).
 * @param {number}    totalPages  - Total number of pages.
 * @param {function}  onChange    - Callback fn(event, page).
 */
function AppPagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null;

  return (
    <Box display="flex" justifyContent="center" pt={3} pb={1}>
      <MuiPagination
        page={page}
        count={totalPages}
        onChange={onChange}
        color="primary"
        shape="rounded"
        showFirstButton
        showLastButton
      />
    </Box>
  );
}

export default AppPagination;
