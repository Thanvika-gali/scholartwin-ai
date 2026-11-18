import React from 'react';

export default function Alert({
  variant = 'error', // 'error' | 'success' | 'warning' | 'info'
  title,
  children,
  onClose,
  onDismiss,
  dismissible,
  className = '',
}) {
  const handleClose = onClose || onDismiss;
  const variantStyles = {
    error: {
      wrapper: 'bg-status-error-bg border-status-error-border text-status-error-text',
      icon: (
        <svg className="w-4 h-4 text-status-error shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      ),
    },
    success: {
      wrapper: 'bg-status-success-bg border-status-success-border text-status-success-text',
      icon: (
        <svg className="w-4 h-4 text-status-success shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
        </svg>
      ),
    },
    warning: {
      wrapper: 'bg-status-warning-bg border-status-warning-border text-status-warning-text',
      icon: (
        <svg className="w-4 h-4 text-status-warning shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
      ),
    },
    info: {
      wrapper: 'bg-status-info-bg border-status-info-border text-status-info-text',
      icon: (
        <svg className="w-4 h-4 text-status-info shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      ),
    },
  };

  const current = variantStyles[variant] || variantStyles.error;

  return (
    <div
      role="alert"
      className={`p-3.5 rounded-xl border text-xs sm:text-sm flex items-start gap-3 ${current.wrapper} ${className}`}
    >
      {current.icon}
      <div className="flex-1 min-w-0">
        {title && <p className="font-semibold mb-0.5">{title}</p>}
        <div className="leading-relaxed">{children}</div>
      </div>
      {handleClose && (
        <button
          type="button"
          onClick={handleClose}
          className="text-current opacity-70 hover:opacity-100 p-0.5 cursor-pointer"
          aria-label="Dismiss alert"
        >
          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      )}
    </div>
  );
}

