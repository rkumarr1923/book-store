import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Alert,
  Avatar,
  Box,
  Button,
  CircularProgress,
  Divider,
  Snackbar,
  Stack,
  Typography,
} from '@mui/material';
import PersonRemoveIcon from '@mui/icons-material/PersonRemove';

import { authorApi } from '../../common/api/authorApi';
import { QUERY_KEYS } from '../../common/constants/queryKeys';
import { buildRoute } from '../../common/constants/routes';
import LoadingSpinner from '../../common/components/LoadingSpinner/LoadingSpinner';
import EmptyState from '../../common/components/EmptyState/EmptyState';
import ErrorMessage from '../../common/components/ErrorMessage/ErrorMessage';
import AppPagination from '../../common/components/Pagination/AppPagination';
import BookCard from '../../common/components/BookCard/BookCard';

// ─── Single followed-author card ──────────────────────────────────────────────

function FollowedAuthorCard({ author, onUnfollow, isUnfollowing }) {
  const navigate = useNavigate();
  const recentBooks = author.recentBooks || [];

  return (
    <Box
      sx={{
        bgcolor: 'background.paper',
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 2,
        p: 2.5,
      }}
    >
      {/* Author header */}
      <Box display="flex" gap={2} alignItems="flex-start" mb={2}>
        <Avatar
          src={author.profileImageUrl}
          alt={author.name}
          sx={{ width: 56, height: 56, cursor: 'pointer', flexShrink: 0 }}
          onClick={() => navigate(buildRoute.authorProfile(author.id))}
        />
        <Box flex={1} minWidth={0}>
          <Typography
            variant="body1"
            fontWeight={700}
            sx={{ cursor: 'pointer', '&:hover': { color: 'primary.main' } }}
            onClick={() => navigate(buildRoute.authorProfile(author.id))}
          >
            {author.name}
          </Typography>
          <Typography
            variant="body2"
            color="text.secondary"
            sx={{
              display: '-webkit-box',
              WebkitLineClamp: 2,
              WebkitBoxOrient: 'vertical',
              overflow: 'hidden',
            }}
          >
            {author.biography}
          </Typography>
        </Box>
        <Button
          variant="outlined"
          size="small"
          color="error"
          startIcon={isUnfollowing ? <CircularProgress size={14} color="inherit" /> : <PersonRemoveIcon fontSize="small" />}
          onClick={() => onUnfollow(author.id)}
          disabled={isUnfollowing}
          sx={{ flexShrink: 0 }}
        >
          Unfollow
        </Button>
      </Box>

      {/* Recent books */}
      {recentBooks.length > 0 && (
        <>
          <Divider sx={{ mb: 2 }} />
          <Typography variant="caption" color="text.secondary" mb={1.5} display="block">
            Recent Books
          </Typography>
          <Box
            display="grid"
            gridTemplateColumns="repeat(auto-fill, minmax(140px, 1fr))"
            gap={1.5}
          >
            {recentBooks.slice(0, 3).map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </Box>
        </>
      )}
    </Box>
  );
}

// ─── My Writers Page ──────────────────────────────────────────────────────────

function MyWritersPage() {
  const queryClient = useQueryClient();
  const [page, setPage] = useState(0);
  const [unfollowingId, setUnfollowingId] = useState(null);
  const [snack, setSnack] = useState(null);

  const { data, isLoading, isError } = useQuery({
    queryKey: [...QUERY_KEYS.FOLLOWED_AUTHORS, page],
    queryFn: () => authorApi.getFollowedAuthors({ page, size: 10 }),
  });

  const authors = data?.data?.content || [];
  const totalPages = data?.data?.totalPages || 0;
  const totalElements = data?.data?.totalElements || 0;

  const unfollowMutation = useMutation({
    mutationFn: (authorId) => authorApi.unfollowAuthor(authorId),
    onMutate: (id) => setUnfollowingId(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.FOLLOWED_AUTHORS });
      setSnack({ type: 'success', msg: 'Author unfollowed.' });
    },
    onError: (err) => setSnack({ type: 'error', msg: err.response?.data?.message || 'Failed to unfollow.' }),
    onSettled: () => setUnfollowingId(null),
  });

  return (
    <Box maxWidth={900} mx="auto" px={{ xs: 2, md: 4 }} py={4}>
      <Typography variant="h4" fontWeight={700} mb={0.5}>
        My Writers
      </Typography>
      {totalElements > 0 && (
        <Typography variant="body2" color="text.secondary" mb={3}>
          Following {totalElements} {totalElements === 1 ? 'author' : 'authors'}
        </Typography>
      )}

      {isLoading ? (
        <LoadingSpinner />
      ) : isError ? (
        <ErrorMessage message="Failed to load followed authors." variant="page" />
      ) : authors.length === 0 ? (
        <EmptyState
          title="You're not following any authors"
          subtitle="Discover authors you love and follow them to stay updated on new releases."
        />
      ) : (
        <Stack spacing={2}>
          {authors.map((author) => (
            <FollowedAuthorCard
              key={author.id}
              author={author}
              onUnfollow={(id) => unfollowMutation.mutate(id)}
              isUnfollowing={unfollowingId === author.id}
            />
          ))}
        </Stack>
      )}

      <AppPagination page={page + 1} totalPages={totalPages} onChange={(_, p) => setPage(p - 1)} />

      <Snackbar
        open={!!snack}
        autoHideDuration={3000}
        onClose={() => setSnack(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        {snack ? (
          <Alert severity={snack.type} onClose={() => setSnack(null)} sx={{ width: '100%' }}>
            {snack.msg}
          </Alert>
        ) : <span />}
      </Snackbar>
    </Box>
  );
}

export default MyWritersPage;
