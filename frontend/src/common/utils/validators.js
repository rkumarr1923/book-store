/**
 * Validates an email address.
 */
export const isValidEmail = (email) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

/**
 * Validates an Indian mobile phone number (10 digits, optionally starting with +91).
 */
export const isValidPhone = (phone) => {
  return /^(\+91)?[6-9]\d{9}$/.test(phone.replace(/\s/g, ''));
};

/**
 * Validates that a password meets minimum requirements.
 * At least 8 chars, one uppercase letter, one digit, one special character.
 * Matches backend RegisterRequest validation.
 */
export const isValidPassword = (password) => {
  return /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,}$/.test(password);
};

/**
 * Validates an Indian PIN code (6 digits).
 */
export const isValidPinCode = (pin) => {
  return /^\d{6}$/.test(pin);
};

/**
 * Returns a required field error message if the value is empty.
 */
export const requiredError = (value, label = 'This field') => {
  if (!value || String(value).trim() === '') {
    return `${label} is required`;
  }
  return '';
};
