import React from 'react';

export function Input({
  label,
  id,
  error,
  helperText,
  className = '',
  required = false,
  ...props
}) {
  const inputId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="w-full space-y-1.5">
      {label && (
        <label
          htmlFor={inputId}
          className="block text-xs font-semibold uppercase tracking-wider text-text-secondary"
        >
          {label} {required && <span className="text-status-error">*</span>}
        </label>
      )}
      <input
        id={inputId}
        required={required}
        className={`w-full px-3.5 py-2.5 rounded-xl bg-bg-surface border ${
          error ? 'border-status-error focus:ring-status-error' : 'border-border-default focus:border-brand-teal focus:ring-brand-teal'
        } text-text-primary placeholder:text-text-muted text-sm focus:outline-none focus:ring-1 transition-colors disabled:opacity-60 disabled:cursor-not-allowed ${className}`}
        {...props}
      />
      {error ? (
        <p className="text-xs text-status-error font-medium">{error}</p>
      ) : helperText ? (
        <p className="text-xs text-text-muted">{helperText}</p>
      ) : null}
    </div>
  );
}

export function Textarea({
  label,
  id,
  error,
  helperText,
  characterCount,
  maxLength,
  className = '',
  required = false,
  rows = 4,
  ...props
}) {
  const textareaId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="w-full space-y-1.5">
      <div className="flex items-center justify-between">
        {label && (
          <label
            htmlFor={textareaId}
            className="block text-xs font-semibold uppercase tracking-wider text-text-secondary"
          >
            {label} {required && <span className="text-status-error">*</span>}
          </label>
        )}
        {maxLength !== undefined && characterCount !== undefined && (
          <span className="text-[11px] text-text-muted">
            {characterCount}/{maxLength}
          </span>
        )}
      </div>
      <textarea
        id={textareaId}
        rows={rows}
        maxLength={maxLength}
        required={required}
        className={`w-full px-3.5 py-2.5 rounded-xl bg-bg-surface border ${
          error ? 'border-status-error focus:ring-status-error' : 'border-border-default focus:border-brand-teal focus:ring-brand-teal'
        } text-text-primary placeholder:text-text-muted text-sm focus:outline-none focus:ring-1 transition-colors leading-relaxed font-sans disabled:opacity-60 disabled:cursor-not-allowed ${className}`}
        {...props}
      />
      {error ? (
        <p className="text-xs text-status-error font-medium">{error}</p>
      ) : helperText ? (
        <p className="text-xs text-text-muted">{helperText}</p>
      ) : null}
    </div>
  );
}

export function Select({
  label,
  id,
  error,
  helperText,
  children,
  className = '',
  required = false,
  ...props
}) {
  const selectId = id || (label ? label.toLowerCase().replace(/\s+/g, '-') : undefined);

  return (
    <div className="w-full space-y-1.5">
      {label && (
        <label
          htmlFor={selectId}
          className="block text-xs font-semibold uppercase tracking-wider text-text-secondary"
        >
          {label} {required && <span className="text-status-error">*</span>}
        </label>
      )}
      <select
        id={selectId}
        required={required}
        className={`w-full px-3.5 py-2.5 rounded-xl bg-bg-surface border ${
          error ? 'border-status-error focus:ring-status-error' : 'border-border-default focus:border-brand-teal focus:ring-brand-teal'
        } text-text-primary text-sm focus:outline-none focus:ring-1 transition-colors cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed ${className}`}
        {...props}
      >
        {children}
      </select>
      {error ? (
        <p className="text-xs text-status-error font-medium">{error}</p>
      ) : helperText ? (
        <p className="text-xs text-text-muted">{helperText}</p>
      ) : null}
    </div>
  );
}

export default Input;


