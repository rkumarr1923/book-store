import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Avatar,
  Box,
  Button,
  CircularProgress,
  Divider,
  Stack,
  Typography,
} from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import PersonRemoveIcon from '@mui/icons-material/PersonRemove';

import { authorApi } from '../../common/api/authorApi';
import { QUERY_KEYS } from '../../common/constants/queryKeys';
import LoadingSpinner from '../../common/components/LoadingSpinner/LoadingSpinner';
import ErrorMessage from '../../common/components/ErrorMessage/ErrorMessage';
import EmptyState from '../../common/components/EmptyState/EmptyState';
import BookCard from '../../common/components/BookCard/BookCard';
import AppPagination from '../../common/components/Pagination/AppPagination';
import { useAuth } from '../../common/hooks/useAuth';
import { useToast } from '../../common/context/ToastContext';

function AuthorProfilePage() {
  const { authorId } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const toast = useToast();
  const queryClient = useQueryClient();

  const [page, setPage] = useState(0);

  const { data, isLoading, isError } = useQuery({
    queryKey: [...QUERY_KEYS.AUTHOR(authorId), page],
    queryFn: () => authorApi.getAuthorById(authorId, { page, size: 12 }),
    enabled: !!authorId,
  });

  const author = data?.data;
  const books = author?.books?.content || [];
  const totalPages = author?.books?.totalPages || 0;
  const isFollowed = author?.isFollowed || false;

  const followMutation = useMutation({
    mutationFn: () => authorApi.followAuthor(authorId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.AUTHOR(authorId) });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.FOLLOWED_AUTHORS });
      toast.success('Author followed! You\'ll see their books in My Writers.');
    },
    onError: (err) => {
      toast.error(err.response?.data?.message || 'Failed to follow author.');
    },
  });

  const unfollowMutation = useMutation({
    mutationFn: () => authorApi.unfollowAuthor(authorId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.AUTHOR(authorId) });
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.FOLLOWED_AUTHORS });
      toast.info('Author unfollowed.');
    },
    onError: (err) => {
      toast.error(err.response?.data?.message || 'Failed to unfollow author.');
    },
  });

  const handleFollowToggle = () => {
    if (!isAuthenticated) {
      window.dispatchEvent(new Event('open:login'));
      return;
    }
    if (isFollowed) {
      unfollowMutation.mutate();
    } else {
      followMutation.mutate();
    }
  };

  const isMutating = followMutation.isPending || unfollowMutation.isPending;

  if (isLoading) return <LoadingSpinner sx={{ mt: 8 }} />;
  if (isError || !author)
    return (
      <ErrorMessage
        message="Author not found or failed to load."
        variant="page"
        action={<Button onClick={() => navigate(-1)}>Go Back</Button>}
      />
    );

  return (
    <Box maxWidth={1200} mx="auto" px={{ xs: 2, md: 4 }} py={4}>
      {/* Author header */}
      <Box
        display="flex"
        gap={3}
        flexDirection={{ xs: 'column', sm: 'row' }}
        alignItems={{ xs: 'flex-start', sm: 'center' }}
        mb={3}
      >
        <Avatar
          src={author.profileImageUrl}
          alt={author.name}
          sx={{ width: 100, height: 100, flexShrink: 0 }}
        />
        <Box flex={1}>
          <Typography variant="h4" fontWeight={700} mb={0.5}>
            {author.name}
          </Typography>
          <Typography variant="body1" color="text.secondary" mb={2} sx={{ maxWidth: 700 }}>
            {author.biography}
          </Typography>

          <Button
            variant={isFollowed ? 'outlined' : 'contained'}
            color={isFollowed ? 'inherit' : 'primary'}
            startIcon={
              isMutating ? (
                <CircularProgress size={16} color="inherit" />
              ) : isFollowed ? (
                <PersonRemoveIcon />
              ) : (
                <PersonAddIcon />
              )
            }
            onClick={handleFollowToggle}
            disabled={isMutating}
            sx={{ minWidth: 160 }}
          >
            {isMutating ? 'Updating…' : isFollowed ? 'Unfollow' : 'Follow Author'}
          </Button>
        </Box>
      </Box>

      <Divider sx={{ mb: 4 }} />

      {/* Books by this author */}
      <Typography variant="h5" fontWeight={700} mb={2.5}>
        Books by {author.name}
      </Typography>

      {books.length === 0 ? (
        <EmptyState title="No books available" subtitle="This author has no books listed yet." />
      ) : (
        <>
          <Box
            display="grid"
            gridTemplateColumns="repeat(auto-fill, minmax(172px, 1fr))"
            gap={2}
            mb={2}
          >
            {books.map((book) => (
              <BookCard key={book.id} book={book} />
            ))}
          </Box>
          <AppPagination
            page={page + 1}
            totalPages={totalPages}
            onChange={(_, p) => setPage(p - 1)}
          />
        </>
      )}
    </Box>
  );
}

export default AuthorProfilePage;
