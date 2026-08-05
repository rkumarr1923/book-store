import axiosClient from './axiosClient';

export const genreApi = {
  getAllGenres: () =>
    axiosClient.get('/genres').then((r) => r.data),
};
