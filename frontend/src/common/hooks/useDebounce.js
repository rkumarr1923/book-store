import { useEffect, useState } from 'react';

/**
 * Debounces a value by the given delay (ms).
 * Useful for delaying search queries while the user types.
 */
export function useDebounce(value, delay = 400) {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debouncedValue;
}
