import axiosClient from './axiosClient';

export const wishlistApi = {
  getWishlist: (params) =>
    axiosClient.get('/wishlist', { params }).then((r) => r.data),

  addToWishlist: (payload) =>
    axiosClient.post('/wishlist/items', payload).then((r) => r.data),

  removeFromWishlist: (wishlistItemId) =>
    axiosClient.delete(`/wishlist/items/${wishlistItemId}`).then((r) => r.data),

  moveToCart: (wishlistItemId) =>
    axiosClient
      .post(`/wishlist/items/${wishlistItemId}/move-to-cart`)
      .then((r) => r.data),
};
