import axiosClient from './axiosClient';

export const homeApi = {
  getHomeSections: () =>
    axiosClient.get('/home').then((r) => r.data),

  getRecommended: () =>
    axiosClient.get('/home/recommended').then((r) => r.data),

  getBestsellers: () =>
    axiosClient.get('/home/bestsellers').then((r) => r.data),

  getNewLaunches: () =>
    axiosClient.get('/home/new-launches').then((r) => r.data),
};
