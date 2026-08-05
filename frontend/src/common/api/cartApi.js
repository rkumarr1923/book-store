import axiosClient from './axiosClient';

export const cartApi = {
  getCart: () =>
    axiosClient.get('/cart').then((r) => r.data),

  addItem: (payload) =>
    axiosClient.post('/cart/items', payload).then((r) => r.data),

  updateItem: (cartItemId, payload) =>
    axiosClient.put(`/cart/items/${cartItemId}`, payload).then((r) => r.data),

  removeItem: (cartItemId) =>
    axiosClient.delete(`/cart/items/${cartItemId}`).then((r) => r.data),

  clearCart: () =>
    axiosClient.delete('/cart').then((r) => r.data),
};
