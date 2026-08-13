/**
 * Validates an email address.
 */
export const isValidEmail = (email) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

/**
 * Validates an Indian mobile phone number.
 * Accepts exactly 10 digits starting with 6-9 (without +91 prefix).
 */
export const isValidPhone = (phone) => {
  return /^[6-9]\d{9}$/.test(phone.replace(/^\+91/, '').replace(/\s/g, ''));
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

/**
 * Validates all address form fields.
 * Returns an object with field-level error messages (empty object = valid).
 * Used consistently across Checkout, AddressDialog (Add/Edit), etc.
 */
export const validateAddressForm = (addr) => {
  const e = {};
  if (!addr.firstName?.trim()) e.firstName = 'First name is required';
  if (!addr.lastName?.trim())  e.lastName  = 'Last name is required';
  if (!addr.addressLine1?.trim()) e.addressLine1 = 'Address Line 1 is required';
  if (!addr.email?.trim()) {
    e.email = 'Email address is required';
  } else if (!isValidEmail(addr.email.trim())) {
    e.email = 'Enter a valid email address';
  }
  if (!addr.city?.trim()) e.city = 'City is required';
  if (!addr.pinCode?.trim()) {
    e.pinCode = 'PIN code is required';
  } else if (!isValidPinCode(addr.pinCode.trim())) {
    e.pinCode = 'PIN code must be exactly 6 digits';
  }
  if (!addr.phoneNumber?.trim()) {
    e.phoneNumber = 'Phone number is required';
  } else if (!isValidPhone(addr.phoneNumber.trim())) {
    e.phoneNumber = 'Must start with 6, 7, 8, or 9 (10 digits)';
  }
  if (!addr.state?.trim()) e.state = 'State is required';
  return e;
};

/**
 * Validates profile name fields.
 * Returns an object with field-level error messages.
 */
export const validateProfileForm = (form) => {
  const e = {};
  if (!form.firstName?.trim()) e.firstName = 'First name is required';
  if (!form.lastName?.trim())  e.lastName  = 'Last name is required';
  if (form.phoneNumber?.trim() && !isValidPhone(form.phoneNumber.trim())) {
    e.phoneNumber = 'Must start with 6, 7, 8, or 9 (10 digits)';
  }
  return e;
};
