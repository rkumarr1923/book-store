import React, { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Box,
  Drawer,
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

/**
 * Read all catalogue filter state out of the URL search params.
 * Called once on component mount so state is immediately consistent
 * with the URL (supports deep-linking and browser back/forward).
 * React Router re-mounts this component on navigation so we never
 * need to re-sync URL → state after the initial read.
 */
function readStateFromParams(searchParams) {
  const rawSlug = searchParams.get('genreSlug') || '';
  return {
    search: searchParams.get('search') || '',
    filters: {
      genreSlug: rawSlug.toLowerCase() === 'all' ? '' : rawSlug,
      language: searchParams.get('language') || 'All',
      format: searchParams.get('format') || '',
      minPrice: Number(searchParams.get('minPrice')) || 0,
      maxPrice: Number(searchParams.get('maxPrice')) || 5000,
      sortBy: searchParams.get('sortBy') || 'relevance',
    },
    page: Number(searchParams.get('page')) || 0,
  };
}

function CataloguePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [drawerOpen, setDrawerOpen] = useState(false);

  // ─── Initialise state from URL exactly once on mount ─────────────────────
  // We read the URL params synchronously via useMemo (runs before effects) so
  // useState initialisers receive the correct values on the very first render.
  // After mount we only write URL ← state, never read URL → state again.
  // Back/forward navigation causes React Router to re-mount this component,
  // which re-runs this initialiser with the correct URL — deep-linking works.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  const initial = useMemo(() => readStateFromParams(searchParams), []);

  const [search, setSearch]   = useState(initial.search);
  const [filters, setFilters] = useState(initial.filters);
  const [page, setPage]       = useState(initial.page);

  // ─── Genres (needed to resolve slug → display name for sidebar) ───────────
  const { data: genreData } = useQuery({
    queryKey: QUERY_KEYS.GENRES,
    queryFn: genreApi.getAllGenres,
    staleTime: 1000 * 60 * 10,
  });
  const allGenres = useMemo(() => genreData?.data || [], [genreData]);

  // Derive the active genre display name from the slug for sidebar highlighting
  const selectedGenre = useMemo(() => {
    const slug = filters.genreSlug;
    if (!slug) return 'All';
    const found = allGenres.find((g) => g.slug === slug);
    return found ? found.name : 'All';
  }, [filters.genreSlug, allGenres]);

  // Search is debounced so a network request fires only after the user pauses
  const debouncedSearch = useDebounce(search, 500);

  // ─── Sync state → URL (one direction only) ───────────────────────────────
  // The URL is kept in sync so the address bar reflects the active filters
  // and users can share/bookmark URLs.  replace:true keeps a single history
  // entry for the catalogue rather than pushing on every keystroke.
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
    setSearchParams(params, { replace: true });
  }, [debouncedSearch, filters, page, setSearchParams]);

  // ─── Build React Query params ─────────────────────────────────────────────
  // Wrapped in useMemo so the object reference is stable between renders that
  // don't change filter values. React Query uses JSON-stable key comparison,
  // but a stable reference also avoids recreating the queryFn closure on every
  // render and prevents any future consumers that do reference checks.
  const apiParams = useMemo(() => ({
    page,
    size: PAGE_SIZE,
    ...(debouncedSearch ? { search: debouncedSearch } : {}),
    ...(filters.genreSlug ? { genreSlug: filters.genreSlug } : {}),
    ...(filters.language && filters.language !== 'All' ? { language: filters.language } : {}),
    ...(filters.format ? { format: filters.format } : {}),
    ...(filters.minPrice > 0 ? { minPrice: filters.minPrice } : {}),
    ...(filters.maxPrice < 5000 ? { maxPrice: filters.maxPrice } : {}),
    ...(filters.sortBy ? { sortBy: filters.sortBy } : {}),
  }), [debouncedSearch, filters, page]);

  const { data, isLoading, isError } = useQuery({
    queryKey: [...QUERY_KEYS.BOOKS, apiParams],
    queryFn: () => bookApi.getBooks(apiParams),
    keepPreviousData: true,
  });

  const books         = data?.data?.content   || [];
  const totalPages    = data?.data?.totalPages || 0;
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
