import axiosClient from './axiosClient';

export const orderApi = {
  getOrders: (params) =>
    axiosClient.get('/orders', { params }).then((r) => r.data),

  getOrderById: (orderId) =>
    axiosClient.get(`/orders/${orderId}`).then((r) => r.data),

  buyAgain: (orderId) =>
    axiosClient.post(`/orders/${orderId}/buy-again`).then((r) => r.data),
};
