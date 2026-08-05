import React from 'react';
import {
  Box,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Slider,
  Typography,
} from '@mui/material';

const LANGUAGES = ['All', 'English', 'Hindi', 'Tamil', 'Telugu', 'Malayalam', 'Kannada'];
const FORMATS = [
  { value: '', label: 'All' },
  { value: 'PAPERBACK', label: 'Paperback' },
  { value: 'HARDCOVER', label: 'Hard Cover' },
  { value: 'EBOOK', label: 'eBook' },
];
const SORT_OPTIONS = [
  { value: 'relevance', label: 'Relevance' },
  { value: 'price_asc', label: 'Price: Low to High' },
  { value: 'price_desc', label: 'Price: High to Low' },
  { value: 'newest', label: 'Newest' },
  { value: 'bestseller', label: 'Bestseller' },
];

/**
 * FilterBar — language, format, price range, sort controls for the catalogue.
 *
 * Props:
 *   filters: { language, format, minPrice, maxPrice, sortBy }
 *   onChange: (partial filters) => void
 */
function FilterBar({ filters = {}, onChange }) {
  const { language = 'All', format = '', minPrice = 0, maxPrice = 2000, sortBy = 'relevance' } = filters;

  const set = (key) => (e) => onChange({ [key]: e.target.value });
  const setPrice = (_, v) => onChange({ minPrice: v[0], maxPrice: v[1] });

  return (
    <Box
      display="flex"
      flexWrap="wrap"
      gap={1.5}
      alignItems="center"
    >
      {/* Language */}
      <FormControl size="small" sx={{ minWidth: 120 }}>
        <InputLabel>Language</InputLabel>
        <Select value={language} label="Language" onChange={set('language')}>
          {LANGUAGES.map((l) => (
            <MenuItem key={l} value={l}>{l}</MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* Format */}
      <FormControl size="small" sx={{ minWidth: 160 }}>
        <InputLabel>Format (Paperback, eBook etc)</InputLabel>
        <Select value={format} label="Format (Paperback, eBook etc)" onChange={set('format')}>
          {FORMATS.map((f) => (
            <MenuItem key={f.value} value={f.value}>{f.label}</MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* Price Range */}
      <Box sx={{ minWidth: 160, px: 1 }}>
        <Typography variant="caption" color="text.secondary" gutterBottom>
          Price Range: ₹{minPrice} – ₹{maxPrice}
        </Typography>
        <Slider
          value={[minPrice, maxPrice]}
          onChange={setPrice}
          min={0}
          max={5000}
          step={50}
          size="small"
          sx={{ mt: 0.5 }}
        />
      </Box>

      {/* Sort */}
      <FormControl size="small" sx={{ minWidth: 140 }}>
        <InputLabel>Sort by</InputLabel>
        <Select value={sortBy} label="Sort by" onChange={set('sortBy')}>
          {SORT_OPTIONS.map((s) => (
            <MenuItem key={s.value} value={s.value}>{s.label}</MenuItem>
          ))}
        </Select>
      </FormControl>
    </Box>
  );
}

export default FilterBar;
