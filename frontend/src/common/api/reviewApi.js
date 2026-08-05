import axiosClient from './axiosClient';

export const reviewApi = {
  getReviews: (bookId, params) =>
    axiosClient.get(`/books/${bookId}/reviews`, { params }).then((r) => r.data),

  submitReview: (bookId, payload) =>
    axiosClient.post(`/books/${bookId}/reviews`, payload).then((r) => r.data),
};
