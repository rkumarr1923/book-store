import React from 'react';
import { Box, Grid, Skeleton, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';

const GENRES = [
  'All', 'Romance', 'Mystery', 'Science Fiction', 'Fantasy', 'Historical',
  'Biography', 'Self-help', 'Memoir', 'Travel', 'Cooking', "Children's",
  'Young Adult', 'Comics & Graphic Novels', 'Poetry', 'Drama', 'Science',
  'Philosophy', 'Religion', 'Language Learning',
];

const GENRE_SLUG_MAP = {
  'All': '',
  'Romance': 'romance',
  'Mystery': 'mystery',
  'Science Fiction': 'science-fiction',
  'Fantasy': 'fantasy',
  'Historical': 'historical',
  'Biography': 'biography',
  'Self-help': 'self-help',
  'Memoir': 'memoir',
  'Travel': 'travel',
  'Cooking': 'cooking',
  "Children's": 'childrens',
  'Young Adult': 'young-adult',
  'Comics & Graphic Novels': 'comics-graphic-novels',
  'Poetry': 'poetry',
  'Drama': 'drama',
  'Science': 'science',
  'Philosophy': 'philosophy',
  'Religion': 'religion',
  'Language Learning': 'language-learning',
};

/**
 * Genre sidebar for the left panel.
 * Used inside Home and Catalogue layouts.
 *
 * Props:
 *  selected: string — currently active genre name
 *  genres: array — optional API genres (falls back to static list)
 *  onSelect: (genreName, slug) => void
 */
function GenreSidebar({ selected = 'All', genres, onSelect }) {
  const navigate = useNavigate();
  const list = genres?.length
    ? [{ name: 'All', slug: '' }, ...genres]
    : GENRES.map((n) => ({ name: n, slug: GENRE_SLUG_MAP[n] || n.toLowerCase().replace(/\s+/g, '-') }));

  return (
    <Box
      component="nav"
      sx={{
        width: { xs: '100%', md: 180 },
        flexShrink: 0,
      }}
    >
      {list.map((g) => {
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
