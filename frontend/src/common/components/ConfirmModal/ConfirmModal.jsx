import React from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from '@mui/material';

/**
 * Generic confirmation modal.
 *
 * @param {boolean}   open          - Whether the dialog is open.
 * @param {string}    title         - Dialog title.
 * @param {string}    message       - Confirmation message body.
 * @param {string}    confirmLabel  - Confirm button label (default: "Confirm").
 * @param {string}    cancelLabel   - Cancel button label (default: "Cancel").
 * @param {string}    confirmColor  - Confirm button MUI color (default: "primary").
 * @param {function}  onConfirm     - Called when the user confirms.
 * @param {function}  onCancel      - Called when the user cancels or closes.
 */
function ConfirmModal({
  open,
  title = 'Confirm Action',
  message = 'Are you sure you want to proceed?',
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  confirmColor = 'primary',
  onConfirm,
  onCancel,
}) {
  return (
    <Dialog open={open} onClose={onCancel} maxWidth="xs" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{message}</DialogContentText>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onCancel} variant="outlined" color="inherit">
          {cancelLabel}
        </Button>
        <Button onClick={onConfirm} variant="contained" color={confirmColor}>
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default ConfirmModal;
