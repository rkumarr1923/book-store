import axiosClient from './axiosClient';

export const checkoutApi = {
  getSummary: () =>
    axiosClient.get('/checkout/summary').then((r) => r.data),

  validateCoupon: (payload) =>
    axiosClient.post('/checkout/validate-coupon', payload).then((r) => r.data),

  placeOrder: (payload) =>
    axiosClient.post('/checkout/place-order', payload).then((r) => r.data),
};
