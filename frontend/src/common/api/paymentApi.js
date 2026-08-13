import axiosClient from './axiosClient';

export const paymentApi = {
  processPayment: (payload) =>
    axiosClient.post('/payments/process', payload).then((r) => r.data),

  getPaymentByOrder: (orderId) =>
    axiosClient.get(`/payments/${orderId}`).then((r) => r.data),
};
