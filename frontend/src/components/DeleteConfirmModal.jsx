import React from 'react';
import Modal from './ui/Modal';
import Button from './ui/Button';

export default function DeleteConfirmModal({
  isOpen,
  onClose,
  onConfirm,
  title,
  isDeleting = false,
}) {
  if (!isOpen) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => !isDeleting && onClose()}
      title="Delete Knowledge Item"
      maxWidth="max-w-md"
      icon={
        <svg className="w-5 h-5 text-status-error" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
          />
        </svg>
      }
      footer={
        <>
          <Button
            variant="secondary"
            size="sm"
            disabled={isDeleting}
            onClick={onClose}
          >
            Cancel
          </Button>

          <Button
            variant="destructive"
            size="sm"
            isLoading={isDeleting}
            onClick={onConfirm}
          >
            Delete Permanently
          </Button>
        </>
      }
    >
      <p className="text-xs sm:text-sm text-text-secondary leading-relaxed py-2">
        Are you sure you want to permanently delete{' '}
        <strong className="text-text-primary">"{title}"</strong>? This action cannot be undone.
      </p>
    </Modal>
  );
}
