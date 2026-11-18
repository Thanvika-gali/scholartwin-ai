import React from 'react';

export default function Badge({
  children,
  variant = 'default', // 'default' | 'teal' | 'gold' | 'success' | 'warning' | 'error' | 'upcoming'
  size = 'sm', // 'xs' | 'sm'
  className = '',
  icon,
}) {
  const sizeStyles = {
    xs: 'text-[10px] px-2 py-0.5 gap-1',
    sm: 'text-xs px-2.5 py-0.5 gap-1.5',
    md: 'text-xs sm:text-sm px-3 py-1 gap-1.5',
  };

  const variantStyles = {
    default:
      'bg-bg-surface-elevated text-text-secondary border border-border-default',
    teal:
      'bg-brand-teal/15 text-brand-teal dark:text-brand-teal-light border border-brand-teal/30 font-medium',
    gold:
      'bg-brand-gold/15 text-amber-800 dark:text-brand-gold-light border border-brand-gold/30 font-medium',
    success:
      'bg-status-success-bg text-status-success-text border border-status-success-border font-medium',
    warning:
      'bg-status-warning-bg text-status-warning-text border border-status-warning-border font-medium',
    error:
      'bg-status-error-bg text-status-error-text border border-status-error-border font-medium',
    upcoming:
      'bg-bg-surface text-text-muted border border-border-default/80 font-medium tracking-wide uppercase text-[10px]',
  };

  return (
    <span
      className={`inline-flex items-center rounded-full font-medium ${
        sizeStyles[size] || sizeStyles.sm
      } ${variantStyles[variant] || variantStyles.default} ${className}`}
    >
      {icon && <span className="shrink-0">{icon}</span>}
      <span>{children}</span>
    </span>
  );
}

