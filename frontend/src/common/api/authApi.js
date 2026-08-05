import axiosClient from './axiosClient';

export const authApi = {
  login: (credentials) =>
    axiosClient.post('/auth/login', credentials).then((r) => r.data),

  register: (payload) =>
    axiosClient.post('/auth/register', payload).then((r) => r.data),

  logout: () =>
    axiosClient.post('/auth/logout').then((r) => r.data),

  forgotPassword: (payload) =>
    axiosClient.post('/auth/forgot-password', payload).then((r) => r.data),

  resetPassword: (payload) =>
    axiosClient.post('/auth/reset-password', payload).then((r) => r.data),
};
