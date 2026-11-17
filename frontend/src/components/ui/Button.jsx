import React from 'react';

export default function Button({
  children,
  variant = 'primary', // 'primary' | 'secondary' | 'gold' | 'ghost' | 'destructive'
  size = 'md', // 'sm' | 'md' | 'lg'
  isLoading = false,
  disabled = false,
  type = 'button',
  className = '',
  onClick,
  icon,
  ...props
}) {
  const baseStyles =
    'inline-flex items-center justify-center font-medium rounded-xl transition-all duration-150 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed cursor-pointer select-none';

  const sizeStyles = {
    sm: 'text-xs px-3 py-1.5 gap-1.5',
    md: 'text-xs sm:text-sm px-4 py-2 gap-2',
    lg: 'text-sm sm:text-base px-5 py-2.5 gap-2.5',
  };

  const variantStyles = {
    // Turquoise primary
    primary:
      'bg-brand-teal hover:bg-brand-teal-hover text-white shadow-sm shadow-brand-teal/20 focus:ring-brand-teal focus:ring-offset-bg-page',
    // Subtle surface outline/button
    secondary:
      'bg-bg-surface hover:bg-bg-surface-hover text-text-primary border border-border-default focus:ring-brand-teal focus:ring-offset-bg-page shadow-sm',
    // Warm gold accent
    gold:
      'bg-brand-gold hover:bg-brand-gold-hover text-brand-navy font-semibold shadow-sm shadow-brand-gold/20 focus:ring-brand-gold focus:ring-offset-bg-page',
    // Subtle ghost
    ghost:
      'bg-transparent hover:bg-bg-surface-hover text-text-secondary hover:text-text-primary border border-transparent focus:ring-brand-teal focus:ring-offset-bg-page',
    // Destructive
    destructive:
      'bg-status-error hover:bg-red-700 text-white shadow-sm shadow-red-600/20 focus:ring-status-error focus:ring-offset-bg-page',
  };

  return (
    <button
      type={type}
      disabled={disabled || isLoading}
      onClick={onClick}
      className={`${baseStyles} ${sizeStyles[size] || sizeStyles.md} ${
        variantStyles[variant] || variantStyles.primary
      } ${className}`}
      {...props}
    >
      {isLoading ? (
        <svg className="w-4 h-4 animate-spin shrink-0" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
      ) : icon ? (
        <span className="shrink-0">{icon}</span>
      ) : null}
      <span>{children}</span>
    </button>
  );
}

