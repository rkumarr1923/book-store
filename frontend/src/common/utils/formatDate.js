/**
 * Formats a date string or Date object for delivery display.
 * e.g. "2025-07-21" → "Mon, 21 Jul"
 */
export const formatDeliveryDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-IN', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
  });
};

/**
 * Formats a date string as a human-readable long date.
 * e.g. "2025-07-21" → "21 July 2025"
 */
export const formatLongDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
};
