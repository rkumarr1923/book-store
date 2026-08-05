import React from 'react';
import { InputAdornment, TextField } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

/**
 * Reusable search bar component.
 *
 * @param {string}    value       - Controlled input value.
 * @param {function}  onChange    - Change handler fn(value).
 * @param {string}    placeholder - Placeholder text.
 * @param {object}    sx          - Additional MUI sx overrides.
 */
function SearchBar({
  value,
  onChange,
  placeholder = 'Search you want to read here',
  sx = {},
}) {
  return (
    <TextField
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder={placeholder}
      size="small"
      fullWidth
      InputProps={{
        startAdornment: (
          <InputAdornment position="start">
            <SearchIcon fontSize="small" sx={{ color: 'text.secondary' }} />
          </InputAdornment>
        ),
      }}
      sx={{
        '& .MuiOutlinedInput-root': {
          backgroundColor: 'rgba(255,255,255,0.06)',
        },
        ...sx,
      }}
    />
  );
}

export default SearchBar;
