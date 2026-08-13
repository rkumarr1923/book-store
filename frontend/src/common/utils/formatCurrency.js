/**
 * Formats a number as Indian Rupee currency string.
 * e.g. 1234.5 → ₹1,234.50
 */
export const formatCurrency = (amount) => {
  if (amount == null || isNaN(amount)) return '₹0.00';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(amount);
};
