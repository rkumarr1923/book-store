import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Box,
  Drawer,
  Fab,
  IconButton,
  Typography,
  useMediaQuery,
  useTheme,
} from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import CloseIcon from '@mui/icons-material/Close';

import { bookApi } from '../../common/api/bookApi';
import { genreApi } from '../../common/api/genreApi';
import { QUERY_KEYS } from '../../common/constants/queryKeys';
import GenreSidebar from '../../common/components/GenreSidebar/GenreSidebar';
import SearchBar from '../../common/components/SearchBar/SearchBar';
import FilterBar from '../../common/components/FilterBar/FilterBar';
import AppPagination from '../../common/components/Pagination/AppPagination';
import EmptyState from '../../common/components/EmptyState/EmptyState';
import ErrorMessage from '../../common/components/ErrorMessage/ErrorMessage';
import { useDebounce } from '../../common/hooks/useDebounce';
import BookGrid from './BookGrid';

const PAGE_SIZE = 20;

function CataloguePage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [drawerOpen, setDrawerOpen] = useState(false);

  // ─── Fetch genres (needed to resolve name from slug) ─────────────
  const { data: genreData } = useQuery({
    queryKey: QUERY_KEYS.GENRES,
    queryFn: genreApi.getAllGenres,
    staleTime: 1000 * 60 * 10,
  });
  const allGenres = useMemo(() => genreData?.data || [], [genreData]);

  // ─── State derived from URL params ──────────────────────────────
  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [filters, setFilters] = useState({
    genreSlug: (searchParams.get('genreSlug') || '').toLowerCase() === 'all' ? '' : (searchParams.get('genreSlug') || ''),
    language: searchParams.get('language') || 'All',
    format: searchParams.get('format') || '',
    minPrice: Number(searchParams.get('minPrice')) || 0,
    maxPrice: Number(searchParams.get('maxPrice')) || 5000,
    sortBy: searchParams.get('sortBy') || 'relevance',
  });
  const [page, setPage] = useState(Number(searchParams.get('page')) || 0);

  // Derive the active genre name from the slug for sidebar highlighting
  const selectedGenre = useMemo(() => {
    const slug = filters.genreSlug;
    if (!slug) return 'All';
    const found = allGenres.find((g) => g.slug === slug);
    return found ? found.name : 'All';
  }, [filters.genreSlug, allGenres]);

  const debouncedSearch = useDebounce(search, 500);

  // Track whether the URL was changed by us (state→URL) or by an external navigation
  const selfUpdatingRef = useRef(false);

  // Sync URL → state only when navigating from another page (not from our own setSearchParams)
  useEffect(() => {
    if (selfUpdatingRef.current) {
      selfUpdatingRef.current = false;
      return;
    }
    const sp = searchParams;
    setSearch(sp.get('search') || '');
    const rawSlug = sp.get('genreSlug') || '';
    setFilters({
      genreSlug: rawSlug.toLowerCase() === 'all' ? '' : rawSlug,
      language: sp.get('language') || 'All',
      format: sp.get('format') || '',
      minPrice: Number(sp.get('minPrice')) || 0,
      maxPrice: Number(sp.get('maxPrice')) || 5000,
      sortBy: sp.get('sortBy') || 'relevance',
    });
    setPage(Number(sp.get('page')) || 0);
  }, [searchParams]);

  // Sync state → URL
  useEffect(() => {
    const params = {};
    if (debouncedSearch) params.search = debouncedSearch;
    if (filters.genreSlug) params.genreSlug = filters.genreSlug;
    if (filters.language && filters.language !== 'All') params.language = filters.language;
    if (filters.format) params.format = filters.format;
    if (filters.minPrice > 0) params.minPrice = String(filters.minPrice);
    if (filters.maxPrice < 5000) params.maxPrice = String(filters.maxPrice);
    if (filters.sortBy && filters.sortBy !== 'relevance') params.sortBy = filters.sortBy;
    if (page > 0) params.page = String(page);
    selfUpdatingRef.current = true;
    setSearchParams(params, { replace: true });
  }, [debouncedSearch, filters, page]);

  // Build API query params
  const apiParams = {
    page,
    size: PAGE_SIZE,
    ...(debouncedSearch ? { search: debouncedSearch } : {}),
    ...(filters.genreSlug ? { genreSlug: filters.genreSlug } : {}),
    ...(filters.language && filters.language !== 'All' ? { language: filters.language } : {}),
    ...(filters.format ? { format: filters.format } : {}),
    ...(filters.minPrice > 0 ? { minPrice: filters.minPrice } : {}),
    ...(filters.maxPrice < 5000 ? { maxPrice: filters.maxPrice } : {}),
    ...(filters.sortBy ? { sortBy: filters.sortBy } : {}),
  };

  const { data, isLoading, isError } = useQuery({
    queryKey: [...QUERY_KEYS.BOOKS, apiParams],
    queryFn: () => bookApi.getBooks(apiParams),
    keepPreviousData: true,
  });

  const books = data?.data?.content || [];
  const totalPages = data?.data?.totalPages || 0;
  const totalElements = data?.data?.totalElements || 0;

  const handleGenreSelect = (_, slug) => {
    setFilters((f) => ({ ...f, genreSlug: slug }));
    setPage(0);
  };

  const handleFilterChange = (partial) => {
    setFilters((f) => ({ ...f, ...partial }));
    setPage(0);
  };

  const handleSearchChange = (v) => {
    setSearch(v);
    setPage(0);
  };

  const handlePageChange = (_, p) => setPage(p - 1);

  const sidebar = (
    <GenreSidebar
      selected={selectedGenre}
      onSelect={(name, slug) => {
        handleGenreSelect(name, slug);
        setDrawerOpen(false);
      }}
    />
  );

  return (
    <Box display="flex" flexDirection="column" minHeight="100%">
      {/* Top filter bar */}
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
            value={search}
            onChange={handleSearchChange}
            placeholder="Search what you want to read"
            sx={{ maxWidth: 320, flex: '1 1 200px' }}
          />
          <FilterBar filters={filters} onChange={handleFilterChange} />
          {isMobile && (
            <IconButton onClick={() => setDrawerOpen(true)} size="small">
              <FilterListIcon />
            </IconButton>
          )}
        </Box>
      </Box>

      {/* Body: sidebar + grid */}
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
        {/* Desktop sidebar */}
        {!isMobile && (
          <Box flexShrink={0}>
            {sidebar}
          </Box>
        )}

        {/* Mobile sidebar drawer */}
        <Drawer
          anchor="left"
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          PaperProps={{ sx: { width: 220, pt: 2, px: 1 } }}
        >
          <Box display="flex" justifyContent="flex-end" mb={1} pr={1}>
            <IconButton size="small" onClick={() => setDrawerOpen(false)}>
              <CloseIcon />
            </IconButton>
          </Box>
          {sidebar}
        </Drawer>

        {/* Book Grid */}
        <Box flex={1} minWidth={0}>
          {/* Result count */}
          {!isLoading && !isError && (
            <Typography variant="body2" color="text.secondary" mb={2}>
              {totalElements > 0
                ? `Showing ${books.length} of ${totalElements} books`
                : ''}
            </Typography>
          )}

          {isError ? (
            <ErrorMessage
              message="Failed to load books. Please try again."
              variant="page"
            />
          ) : books.length === 0 && !isLoading ? (
            <EmptyState
              title="No books found"
              subtitle="Try adjusting your search or filters."
            />
          ) : (
            <BookGrid books={books} loading={isLoading} />
          )}

          <AppPagination
            page={page + 1}
            totalPages={totalPages}
            onChange={handlePageChange}
          />
        </Box>
      </Box>
    </Box>
  );
}

export default CataloguePage;
