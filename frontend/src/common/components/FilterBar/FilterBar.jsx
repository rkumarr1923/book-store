import React, { useState } from 'react';
import {
  Box,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Slider,
  Typography,
} from '@mui/material';

const LANGUAGES = ['All', 'English', 'Tamil', 'Hindi', 'Malayalam', 'Kannada', 'Telugu', 'Marathi'];
const FORMATS = [
  { value: 'all', label: 'All' },
  { value: 'PAPERBACK', label: 'Paperback' },
  { value: 'HARDCOVER', label: 'Hardcover' },
  { value: 'EBOOK',     label: 'eBook' },
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
  const rawFormat = filters.format || '';
  const displayFormat = rawFormat === '' ? 'all' : rawFormat;
  const { language = 'All', minPrice = 0, maxPrice = 5000, sortBy = 'relevance' } = filters;

  // Local slider state so the thumb moves smoothly while dragging
  // without triggering an API call on every pixel.
  const [localPrice, setLocalPrice] = useState([minPrice, maxPrice]);

  // Keep local state in sync if the filter is reset externally (e.g. URL change)
  React.useEffect(() => {
    setLocalPrice([minPrice, maxPrice]);
  }, [minPrice, maxPrice]);

  const set = (key) => (e) => {
    const val = e.target.value;
    onChange({ [key]: key === 'format' && val === 'all' ? '' : val });
  };
  // While dragging: update local display only (no API call)
  const handlePriceChange = (_, v) => setLocalPrice(v);
  // On release: commit to parent → triggers API call once
  const handlePriceCommit = (_, v) => onChange({ minPrice: v[0], maxPrice: v[1] });

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
      <FormControl size="small" sx={{ minWidth: 120 }}>
        <InputLabel>Format</InputLabel>
        <Select value={displayFormat} label="Format" onChange={set('format')}>
          {FORMATS.map((f) => (
            <MenuItem key={f.value} value={f.value}>{f.label}</MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* Price Range */}
      <Box sx={{ minWidth: 160, px: 1 }}>
        <Typography variant="caption" color="text.secondary" gutterBottom>
          Price: ₹{localPrice[0]} – ₹{localPrice[1]}
        </Typography>
        <Slider
          value={localPrice}
          onChange={handlePriceChange}
          onChangeCommitted={handlePriceCommit}
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
