import axiosClient from './axiosClient';

export const userApi = {
  getMe: () =>
    axiosClient.get('/users/me').then((r) => r.data),

  updateMe: (payload) =>
    axiosClient.put('/users/me', payload).then((r) => r.data),

  getAddresses: () =>
    axiosClient.get('/users/me/addresses').then((r) => r.data),

  addAddress: (payload) =>
    axiosClient.post('/users/me/addresses', payload).then((r) => r.data),

  updateAddress: (addressId, payload) =>
    axiosClient
      .put(`/users/me/addresses/${addressId}`, payload)
      .then((r) => r.data),

  deleteAddress: (addressId) =>
    axiosClient.delete(`/users/me/addresses/${addressId}`).then((r) => r.data),

  setDefaultAddress: (addressId) =>
    axiosClient
      .patch(`/users/me/addresses/${addressId}/default`)
      .then((r) => r.data),
};
