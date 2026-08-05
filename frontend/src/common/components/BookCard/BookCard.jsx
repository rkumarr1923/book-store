import React from 'react';
import { Box, Card, CardContent, CardMedia, Chip, Stack, Typography } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { buildRoute } from '../../constants/routes';
import { formatCurrency } from '../../utils/formatCurrency';
import { formatDeliveryDate } from '../../utils/formatDate';

const FORMAT_LABELS = {
  PAPERBACK: 'Paperback',
  HARDCOVER: 'Hard Cover',
  EBOOK: 'eBook',
};

/**
 * BookCard – used across Home, Catalogue, Related Reads, etc.
 *
 * Props:
 *   book: BookSummary object from API
 *   compact: boolean – smaller variant for sidebars
 */
function BookCard({ book, compact = false }) {
  const navigate = useNavigate();

  if (!book) return null;

  const {
    id,
    title,
    author,
    coverImageUrl,
    shortDescription,
    format,
    genres = [],
    price,
    estimatedDeliveryDate,
  } = book;

  const handleBookClick = () => navigate(buildRoute.bookDetail(id));
  const handleAuthorClick = (e) => {
    e.stopPropagation();
    if (author?.id) navigate(buildRoute.authorProfile(author.id));
  };
  const handleGenreClick = (e, slug) => {
    e.stopPropagation();
    navigate(`/catalogue?genreSlug=${slug}`);
  };

  if (compact) {
    return (
      <Box
        display="flex"
        gap={1.5}
        sx={{ cursor: 'pointer', '&:hover .book-title': { color: 'primary.main' } }}
        onClick={handleBookClick}
      >
        <Box
          component="img"
          src={coverImageUrl}
          alt={title}
          sx={{
            width: 64,
            height: 88,
            objectFit: 'cover',
            borderRadius: 1,
            flexShrink: 0,
            bgcolor: 'background.paper',
          }}
          onError={(e) => { e.target.style.display = 'none'; }}
        />
        <Box flex={1} minWidth={0}>
          <Typography
            className="book-title"
            variant="body2"
            fontWeight={600}
            noWrap
            sx={{ transition: 'color 0.15s' }}
          >
            {title}
          </Typography>
          <Typography
            variant="caption"
            color="primary.light"
            sx={{ cursor: 'pointer', '&:hover': { textDecoration: 'underline' } }}
            onClick={handleAuthorClick}
            display="block"
            noWrap
          >
            by {author?.name}
          </Typography>
          <Typography variant="caption" color="text.secondary" display="block">
            {FORMAT_LABELS[format] || format}
          </Typography>
          <Stack direction="row" flexWrap="wrap" gap={0.5} mt={0.5}>
            {genres.slice(0, 2).map((g) => (
              <Chip
                key={g.id}
                label={g.name}
                size="small"
                variant="outlined"
                sx={{ fontSize: '0.65rem', height: 18, cursor: 'pointer', borderColor: 'divider' }}
                onClick={(e) => handleGenreClick(e, g.slug)}
              />
            ))}
          </Stack>
          <Typography variant="body2" fontWeight={700} color="text.primary" mt={0.5}>
            {formatCurrency(price)}
          </Typography>
          {estimatedDeliveryDate && (
            <Typography variant="caption" color="text.secondary">
              Delivery by {formatDeliveryDate(estimatedDeliveryDate)}
            </Typography>
          )}
        </Box>
      </Box>
    );
  }

  return (
    <Card
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        cursor: 'pointer',
        transition: 'border-color 0.2s',
        '&:hover': { borderColor: 'primary.dark' },
        '&:hover .book-title': { color: 'primary.main' },
      }}
      onClick={handleBookClick}
    >
      <Box
        sx={{
          position: 'relative',
          paddingTop: '140%',
          bgcolor: 'rgba(255,255,255,0.04)',
          overflow: 'hidden',
        }}
      >
        {coverImageUrl ? (
          <CardMedia
            component="img"
            image={coverImageUrl}
            alt={title}
            sx={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '100%',
              height: '100%',
              objectFit: 'cover',
            }}
          />
        ) : (
          <Box
            sx={{
              position: 'absolute',
              inset: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Typography variant="caption" color="text.secondary" textAlign="center" p={1}>
              {title}
            </Typography>
          </Box>
        )}
      </Box>

      <CardContent sx={{ p: 1.5, flex: 1, display: 'flex', flexDirection: 'column', gap: 0.5 }}>
        <Typography
          className="book-title"
          variant="body2"
          fontWeight={700}
          sx={{
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            overflow: 'hidden',
            transition: 'color 0.15s',
          }}
        >
          {title}
        </Typography>

        <Typography
          variant="caption"
          color="primary.light"
          sx={{
            cursor: 'pointer',
            '&:hover': { textDecoration: 'underline' },
            display: 'block',
            mb: 0.25,
          }}
          onClick={handleAuthorClick}
          noWrap
        >
          by {author?.name}
        </Typography>

        {shortDescription && (
          <Typography
            variant="caption"
            color="text.secondary"
            sx={{
              display: '-webkit-box',
              WebkitLineClamp: 2,
              WebkitBoxOrient: 'vertical',
              overflow: 'hidden',
            }}
          >
            {shortDescription}
          </Typography>
        )}

        <Typography variant="caption" color="text.secondary">
          {FORMAT_LABELS[format] || format}
        </Typography>

        <Stack direction="row" flexWrap="wrap" gap={0.5}>
          {genres.slice(0, 2).map((g) => (
            <Chip
              key={g.id}
              label={g.name}
              size="small"
              variant="outlined"
              sx={{ fontSize: '0.68rem', height: 20, cursor: 'pointer', borderColor: 'divider' }}
              onClick={(e) => handleGenreClick(e, g.slug)}
            />
          ))}
        </Stack>

        <Box mt="auto" pt={0.5}>
          <Typography variant="body2" fontWeight={700} color="text.primary">
            {formatCurrency(price)}
          </Typography>
          {estimatedDeliveryDate && (
            <Typography variant="caption" color="text.secondary">
              Delivery by {formatDeliveryDate(estimatedDeliveryDate)}
            </Typography>
          )}
        </Box>
      </CardContent>
    </Card>
  );
}

export default BookCard;
