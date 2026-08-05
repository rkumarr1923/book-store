import axiosClient from './axiosClient';

export const bookApi = {
  getBooks: (params) =>
    axiosClient.get('/books', { params }).then((r) => r.data),

  getBookById: (bookId) =>
    axiosClient.get(`/books/${bookId}`).then((r) => r.data),

  getRelatedBooks: (bookId) =>
    axiosClient.get(`/books/${bookId}/related`).then((r) => r.data),
};
