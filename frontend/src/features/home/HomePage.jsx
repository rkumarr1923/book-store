import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Box,
  Button,
  Divider,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';

import { homeApi } from '../../common/api/homeApi';
import { QUERY_KEYS } from '../../common/constants/queryKeys';
import { ROUTES } from '../../common/constants/routes';
import GenreSidebar from '../../common/components/GenreSidebar/GenreSidebar';
import SearchBar from '../../common/components/SearchBar/SearchBar';
import FilterBar from '../../common/components/FilterBar/FilterBar';
import ErrorMessage from '../../common/components/ErrorMessage/ErrorMessage';
import BookRow from './BookRow';

// ─── Hero Banner ──────────────────────────────────────────────────────────────

function HeroBanner({ onSearch }) {
  const navigate = useNavigate();
  const [q, setQ] = useState('');

  const handleSearch = (v) => {
    setQ(v);
    if (onSearch) onSearch(v);
  };

  const handleSubmit = (e) => {
    e && e.preventDefault();
    if (q.trim()) navigate(`${ROUTES.CATALOGUE}?search=${encodeURIComponent(q.trim())}`);
  };

  return (
    <Box
      component="form"
      onSubmit={handleSubmit}
      sx={{
        bgcolor: 'background.paper',
        borderBottom: '1px solid',
        borderColor: 'divider',
        py: { xs: 1.5, md: 2 },
        px: { xs: 2, md: 3 },
      }}
    >
      <SearchBar
        value={q}
        onChange={handleSearch}
        placeholder="Search you want to read here"
        sx={{ maxWidth: 480 }}
      />
    </Box>
  );
}

// ─── Section Header ───────────────────────────────────────────────────────────

function SectionHeader({ title, linkTo }) {
  const navigate = useNavigate();
  return (
    <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
      <Typography variant="h6" fontWeight={700}>
        {title}
      </Typography>
      {linkTo && (
        <Button
          size="small"
          endIcon={<ArrowForwardIcon fontSize="small" />}
          onClick={() => navigate(linkTo)}
          sx={{ color: 'text.secondary', fontWeight: 400 }}
        >
          View all
        </Button>
      )}
    </Box>
  );
}

// ─── Home Page ────────────────────────────────────────────────────────────────

function HomePage() {
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  const [filters, setFilters] = useState({
    language: 'All',
    format: '',
    minPrice: 0,
    maxPrice: 5000,
    sortBy: 'relevance',
  });

  const { data, isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.HOME,
    queryFn: homeApi.getHomeSections,
    staleTime: 1000 * 60 * 5,
  });

  const sections = data?.data || {};
  const recommended = sections.recommended || [];
  const bestsellers = sections.bestsellers || [];
  const newLaunches = sections.newLaunches || [];

  const handleGenreSelect = (_, slug) => {
    const params = slug ? `?genreSlug=${slug}` : '';
    navigate(`${ROUTES.CATALOGUE}${params}`);
  };

  const handleFilterChange = (partial) => {
    const next = { ...filters, ...partial };
    setFilters(next);
    const params = new URLSearchParams();
    if (next.language && next.language !== 'All') params.set('language', next.language);
    if (next.format) params.set('format', next.format);
    if (next.minPrice > 0) params.set('minPrice', String(next.minPrice));
    if (next.maxPrice < 5000) params.set('maxPrice', String(next.maxPrice));
    if (next.sortBy && next.sortBy !== 'relevance') params.set('sortBy', next.sortBy);
    navigate(`${ROUTES.CATALOGUE}?${params.toString()}`);
  };

  return (
    <Box display="flex" flexDirection="column" minHeight="100%">
      {/* Filter bar row */}
      <Box
        sx={{
          bgcolor: 'background.paper',
          borderBottom: '1px solid',
          borderColor: 'divider',
          px: { xs: 2, md: 3 },
          py: 1,
        }}
      >
        <Box
          display="flex"
          flexWrap="wrap"
          gap={1.5}
          alignItems="center"
          maxWidth={1400}
          mx="auto"
        >
          <SearchBar
            value=""
            onChange={(v) => {
              if (v.trim()) navigate(`${ROUTES.CATALOGUE}?search=${encodeURIComponent(v.trim())}`);
            }}
            placeholder="Search what you want to read"
            sx={{ maxWidth: 320, flex: '1 1 200px' }}
          />
          <FilterBar filters={filters} onChange={handleFilterChange} />
        </Box>
      </Box>

      {/* Main layout — sidebar + content */}
      <Box
        display="flex"
        flex={1}
        maxWidth={1400}
        mx="auto"
        width="100%"
        pt={2}
        pb={4}
        px={{ xs: 1.5, md: 3 }}
        gap={{ xs: 0, md: 3 }}
      >
        {/* Genre Sidebar */}
        {!isMobile && (
          <Box flexShrink={0}>
            <GenreSidebar
              onSelect={handleGenreSelect}
            />
          </Box>
        )}

        {/* Content */}
        <Box flex={1} minWidth={0}>
          {isError && (
            <ErrorMessage
              message="Failed to load home page content. Please refresh."
              variant="page"
            />
          )}

          {/* Recommended for You */}
          <Box mb={4}>
            <SectionHeader title="Recommended for You" linkTo={`${ROUTES.CATALOGUE}?sortBy=relevance`} />
            <BookRow books={recommended} loading={isLoading} skeletonCount={5} />
          </Box>

          <Divider sx={{ mb: 4 }} />

          {/* Bestsellers */}
          <Box mb={4}>
            <SectionHeader title="Bestsellers this Month" linkTo={`${ROUTES.CATALOGUE}?sortBy=bestseller`} />
            <BookRow books={bestsellers} loading={isLoading} skeletonCount={5} />
          </Box>

          <Divider sx={{ mb: 4 }} />

          {/* New Launches */}
          <Box mb={4}>
            <SectionHeader title="New Launches" linkTo={`${ROUTES.CATALOGUE}?sortBy=newest`} />
            <BookRow books={newLaunches} loading={isLoading} skeletonCount={5} />
          </Box>
        </Box>
      </Box>
    </Box>
  );
}

export default HomePage;
