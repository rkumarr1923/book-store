import React, { createContext, useCallback, useEffect, useMemo, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { cartApi } from '../api/cartApi';
import { QUERY_KEYS } from '../constants/queryKeys';

export const CartContext = createContext(null);

export function CartProvider({ children }) {
  const queryClient = useQueryClient();
  const [cartCount, setCartCount] = useState(0);

  // Reset cart and all user-specific query caches on logout
  useEffect(() => {
    const handleReset = () => {
      setCartCount(0);
      // Remove all user-specific cached data so the next login starts fresh
      queryClient.removeQueries({ queryKey: QUERY_KEYS.CART });
      queryClient.removeQueries({ queryKey: QUERY_KEYS.WISHLIST });
      queryClient.removeQueries({ queryKey: QUERY_KEYS.ORDERS });
      queryClient.removeQueries({ queryKey: QUERY_KEYS.FOLLOWED_AUTHORS });
      queryClient.removeQueries({ queryKey: ['addresses'] });
    };
    window.addEventListener('cart:reset', handleReset);
    return () => window.removeEventListener('cart:reset', handleReset);
  }, [queryClient]);

  const updateCartCount = useCallback((count) => {
    setCartCount(count ?? 0);
  }, []);

  const invalidateCart = useCallback(() => {
    queryClient.invalidateQueries({ queryKey: QUERY_KEYS.CART });
  }, [queryClient]);

  // Fetch cart and sync count whenever CART query is settled
  const syncCartCount = useCallback(async () => {
    try {
      const data = await cartApi.getCart();
      setCartCount(data?.data?.itemCount ?? 0);
    } catch {
      // not logged in or cart unavailable — keep current count
    }
  }, []);

  const addToCart = useCallback(
    async (payload) => {
      try {
        const data = await cartApi.addItem(payload);
        invalidateCart();
        await syncCartCount();
        return { success: true, data };
      } catch (error) {
        return {
          success: false,
          message: error.response?.data?.message || 'Failed to add to cart.',
        };
      }
    },
    [invalidateCart, syncCartCount]
  );

  const removeFromCart = useCallback(
    async (cartItemId) => {
      try {
        await cartApi.removeItem(cartItemId);
        invalidateCart();
        await syncCartCount();
        return { success: true };
      } catch (error) {
        return {
          success: false,
          message: error.response?.data?.message || 'Failed to remove item.',
        };
      }
    },
    [invalidateCart, syncCartCount]
  );

  const value = useMemo(
    () => ({
      cartCount,
      updateCartCount,
      addToCart,
      removeFromCart,
      invalidateCart,
      syncCartCount,
    }),
    [cartCount, updateCartCount, addToCart, removeFromCart, invalidateCart, syncCartCount]
  );

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}
