export const ROUTES = {
  HOME: '/',
  CATALOGUE: '/catalogue',
  BOOK_DETAIL: '/books/:bookId',
  AUTHOR_PROFILE: '/authors/:authorId',
  CHECKOUT: '/checkout',
  ORDERS: '/orders',
  ORDER_DETAIL: '/orders/:orderId',
  WISHLIST: '/wishlist',
  MY_WRITERS: '/my-writers',
  PROFILE: '/profile',
  ORDER_CONFIRMATION: '/order-confirmation',
  NOT_FOUND: '*',
};

export const buildRoute = {
  bookDetail: (bookId) => `/books/${bookId}`,
  authorProfile: (authorId) => `/authors/${authorId}`,
  orderDetail: (orderId) => `/orders/${orderId}`,
};
