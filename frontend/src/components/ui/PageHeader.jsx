import React from 'react';

export default function PageHeader({
  title,
  description,
  badge,
  actions,
  className = '',
}) {
  return (
    <div
      className={`flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-border-subtle ${className}`}
    >
      <div className="space-y-1.5 min-w-0">
        {badge && <div className="mb-2">{badge}</div>}
        <h1 className="text-2xl sm:text-3xl font-extrabold text-text-primary tracking-tight">
          {title}
        </h1>
        {description && (
          <p className="text-xs sm:text-sm text-text-muted max-w-2xl leading-relaxed">
            {description}
          </p>
        )}
      </div>

      {actions && <div className="flex flex-wrap items-center gap-3 shrink-0">{actions}</div>}
    </div>
  );
}

