import React from 'react';

export function Spinner({ size = 'md', className = '' }) {
  const sizeMap = {
    sm: 'w-4 h-4 border-2',
    md: 'w-6 h-6 border-2',
    lg: 'w-8 h-8 border-3',
  };

  return (
    <div
      className={`${sizeMap[size] || sizeMap.md} border-brand-teal border-t-transparent rounded-full animate-spin shrink-0 ${className}`}
      role="status"
      aria-label="Loading"
    />
  );
}

export function Skeleton({ className = '' }) {
  return (
    <div
      className={`animate-pulse bg-bg-surface-elevated/80 rounded-xl ${className}`}
      aria-hidden="true"
    />
  );
}

export function CardSkeleton() {
  return (
    <div className="bg-bg-surface border border-border-default rounded-2xl p-5 space-y-4 shadow-card">
      <div className="flex items-center justify-between">
        <Skeleton className="h-5 w-20" />
        <Skeleton className="h-4 w-16" />
      </div>
      <Skeleton className="h-6 w-3/4" />
      <Skeleton className="h-4 w-full" />
      <Skeleton className="h-4 w-2/3" />
      <div className="pt-3 border-t border-border-subtle flex items-center justify-between">
        <Skeleton className="h-4 w-24" />
        <Skeleton className="h-5 w-16" />
      </div>
    </div>
  );
}

export default function LoadingSkeleton({ count = 3, height = 'h-24' }) {
  return (
    <div className="space-y-3">
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton key={i} className={`w-full ${height}`} />
      ))}
    </div>
  );
}
