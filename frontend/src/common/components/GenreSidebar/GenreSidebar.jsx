import React from 'react';
import { Box, Skeleton, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { genreApi } from '../../api/genreApi';
import { QUERY_KEYS } from '../../constants/queryKeys';

/**
 * Genre sidebar for the left panel.
 * Used inside Home and Catalogue layouts.
 *
 * Props:
 *  selected: string — currently active genre name
 *  onSelect: (genreName, slug) => void
 */
function GenreSidebar({ selected = 'All', onSelect }) {
  const { data, isLoading } = useQuery({
    queryKey: QUERY_KEYS.GENRES,
    queryFn: genreApi.getAllGenres,
    staleTime: 1000 * 60 * 10,
  });

  const apiGenres = data?.data || [];
  const list = [{ name: 'All', slug: '' }, ...apiGenres];

  return (
    <Box
      component="nav"
      sx={{
        width: { xs: '100%', md: 180 },
        flexShrink: 0,
      }}
    >
      {isLoading
        ? Array.from({ length: 8 }).map((_, i) => (
            <Skeleton key={i} height={28} sx={{ mb: 0.25, borderRadius: 1 }} />
          ))
        : list.map((g) => {
        const active = g.name === selected;
        return (
          <Typography
            key={g.name}
            variant="body2"
            component="div"
            onClick={() => onSelect?.(g.name, g.slug)}
            sx={{
              px: 1.5,
              py: 0.6,
              mb: 0.25,
              borderRadius: 1,
              cursor: 'pointer',
              fontWeight: active ? 700 : 400,
              color: active ? 'text.primary' : 'text.secondary',
              bgcolor: active ? 'rgba(41,121,255,0.12)' : 'transparent',
              borderLeft: active ? '3px solid' : '3px solid transparent',
              borderLeftColor: active ? 'primary.main' : 'transparent',
              transition: 'all 0.15s',
              '&:hover': {
                bgcolor: 'rgba(255,255,255,0.06)',
                color: 'text.primary',
              },
            }}
          >
            {g.name}
          </Typography>
          );
        })}
    </Box>
  );
}

export default GenreSidebar;
