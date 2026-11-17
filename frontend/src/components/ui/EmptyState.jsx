import React from 'react';

export default function EmptyState({
  icon,
  title,
  description,
  action,
  secondaryAction,
  className = '',
}) {
  return (
    <div
      className={`py-12 sm:py-16 px-4 bg-bg-surface border border-border-default rounded-2xl text-center max-w-xl mx-auto space-y-4 shadow-card ${className}`}
    >
      <div className="w-14 h-14 rounded-2xl bg-bg-surface-elevated border border-border-default flex items-center justify-center mx-auto text-brand-teal">
        {icon || (
          <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
            />
          </svg>
        )}
      </div>

      <div className="space-y-1.5">
        <h3 className="text-base sm:text-lg font-bold text-text-primary">{title}</h3>
        {description && (
          <p className="text-xs sm:text-sm text-text-muted max-w-md mx-auto leading-relaxed">
            {description}
          </p>
        )}
      </div>

      {(action || secondaryAction) && (
        <div className="pt-2 flex flex-wrap items-center justify-center gap-3">
          {action}
          {secondaryAction}
        </div>
      )}
    </div>
  );
}

