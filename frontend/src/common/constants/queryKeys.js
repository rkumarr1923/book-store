export const QUERY_KEYS = {
  // Auth
  ME: ['me'],

  // Home
  HOME: ['home'],
  HOME_RECOMMENDED: ['home', 'recommended'],
  HOME_BESTSELLERS: ['home', 'bestsellers'],
  HOME_NEW_LAUNCHES: ['home', 'newLaunches'],

  // Genres
  GENRES: ['genres'],

  // Books
  BOOKS: ['books'],
  BOOK_DETAIL: (bookId) => ['books', bookId],
  BOOK_RELATED: (bookId) => ['books', bookId, 'related'],

  // Authors
  AUTHOR: (authorId) => ['authors', authorId],
  FOLLOWED_AUTHORS: ['followedAuthors'],

  // Reviews
  REVIEWS: (bookId) => ['reviews', bookId],

  // Cart
  CART: ['cart'],

  // Wishlist
  WISHLIST: ['wishlist'],

  // Orders
  ORDERS: ['orders'],
  ORDER_DETAIL: (orderId) => ['orders', orderId],

  // Checkout
  CHECKOUT_SUMMARY: ['checkoutSummary'],
};
