import axiosClient from './axiosClient';

export const authorApi = {
  getAuthorById: (authorId, params) =>
    axiosClient.get(`/authors/${authorId}`, { params }).then((r) => r.data),

  followAuthor: (authorId) =>
    axiosClient.post(`/authors/${authorId}/follow`).then((r) => r.data),

  unfollowAuthor: (authorId) =>
    axiosClient.delete(`/authors/${authorId}/follow`).then((r) => r.data),

  getFollowedAuthors: (params) =>
    axiosClient.get('/users/me/followed-authors', { params }).then((r) => r.data),
};
