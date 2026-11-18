import React, { useState } from 'react';
import Button from '../ui/Button';
import Badge from '../ui/Badge';

export default function SmartInboxBanner({
  suggestions = [],
  onAccept,
  onReject,
  onBulkAcceptHighConfidence,
  onNavigateInbox,
}) {
  const [isExpanded, setIsExpanded] = useState(true);
  const [loadingId, setLoadingId] = useState(null);

  if (!suggestions || suggestions.length === 0) return null;

  const handleAction = async (id, actionFn) => {
    setLoadingId(id);
    try {
      await actionFn();
    } finally {
      setLoadingId(null);
    }
  };

  const highConfidenceCount = suggestions.filter((s) => s.confidenceScore >= 0.85).length;

  return (
    <div className="mb-6 rounded-2xl border border-brand-teal/30 bg-bg-surface-elevated/50 p-4 shadow-card transition-colors">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0">
          <div className="w-8 h-8 rounded-xl bg-brand-teal text-white flex items-center justify-center shrink-0 shadow-sm">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M13 10V3L4 14h7v7l9-11h-7z"
              />
            </svg>
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-2 flex-wrap">
              <h4 className="text-xs sm:text-sm font-bold text-text-primary">
                Smart Inbox — {suggestions.length} Pending Organization {suggestions.length === 1 ? 'Suggestion' : 'Suggestions'}
              </h4>
              <Badge variant="teal" size="xs">
                AI & Academic Rules
              </Badge>
            </div>
            <p className="text-xs text-text-muted mt-0.5">
              ScholarTwin classified new materials automatically. Accept suggestions to organize into your hierarchy.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 shrink-0 self-end sm:self-center">
          {highConfidenceCount > 0 && (
            <Button
              type="button"
              variant="outline"
              size="xs"
              onClick={onBulkAcceptHighConfidence}
            >
              Accept High-Confidence ({highConfidenceCount})
            </Button>
          )}
          <button
            onClick={() => setIsExpanded((prev) => !prev)}
            className="p-1 rounded-lg text-text-muted hover:text-text-primary hover:bg-bg-surface-hover cursor-pointer"
            title={isExpanded ? 'Collapse' : 'Expand'}
          >
            <svg
              className={`w-4 h-4 transition-transform duration-200 ${isExpanded ? 'transform rotate-180' : ''}`}
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
            </svg>
          </button>
        </div>
      </div>

      {isExpanded && (
        <div className="mt-4 space-y-2.5">
          {suggestions.slice(0, 3).map((suggestion) => {
            const confidencePercent = Math.round((suggestion.confidenceScore || 0.8) * 100);

            return (
              <div
                key={suggestion.id}
                className="flex flex-col md:flex-row md:items-center justify-between gap-3 p-3 rounded-xl bg-bg-surface border border-border-default shadow-xs"
              >
                <div className="flex items-start gap-3 min-w-0">
                  <div className="p-2 rounded-lg bg-bg-surface-elevated text-text-secondary shrink-0">
                    {suggestion.resourceType === 'DOCUMENT' ? (
                      <svg className="w-4 h-4 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                    ) : (
                      <svg className="w-4 h-4 text-brand-gold" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                    )}
                  </div>

                  <div className="min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-xs font-bold text-text-primary">
                        {suggestion.resourceType}: #{suggestion.resourceId}
                      </span>
                      <span className="text-xs text-text-muted">→</span>
                      <span className="inline-flex items-center gap-1 text-xs font-semibold text-brand-teal bg-brand-teal/10 px-2 py-0.5 rounded-md border border-brand-teal/20">
                        <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                          <path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z" />
                        </svg>
                        {suggestion.suggestedFolderPath || 'Academic Library'}
                      </span>
                      <span
                        className={`text-[10px] px-1.5 py-0.2 rounded font-semibold ${
                          confidencePercent >= 85
                            ? 'bg-status-success-bg text-status-success-text border border-status-success-border'
                            : 'bg-status-warning-bg text-status-warning-text border border-status-warning-border'
                        }`}
                      >
                        {confidencePercent}% confidence
                      </span>
                    </div>

                    <p className="text-[11px] text-text-muted mt-1 leading-snug">
                      {suggestion.reasoning}
                    </p>

                    {suggestion.suggestedTags && suggestion.suggestedTags.length > 0 && (
                      <div className="flex flex-wrap gap-1 mt-1.5">
                        {suggestion.suggestedTags.map((tag) => (
                          <span
                            key={tag}
                            className="text-[10px] px-1.5 py-0.2 rounded bg-bg-surface-elevated text-text-secondary border border-border-subtle"
                          >
                            #{tag}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0 self-end md:self-center">
                  <Button
                    type="button"
                    variant="ghost"
                    size="xs"
                    disabled={loadingId === suggestion.id}
                    onClick={() => handleAction(suggestion.id, () => onReject(suggestion.id))}
                  >
                    Dismiss
                  </Button>
                  <Button
                    type="button"
                    variant="primary"
                    size="xs"
                    disabled={loadingId === suggestion.id}
                    onClick={() => handleAction(suggestion.id, () => onAccept(suggestion))}
                  >
                    {loadingId === suggestion.id ? 'Moving...' : 'Accept'}
                  </Button>
                </div>
              </div>
            );
          })}

          {suggestions.length > 3 && (
            <div className="text-center pt-2">
              <button
                type="button"
                onClick={onNavigateInbox}
                className="text-xs font-semibold text-brand-teal hover:underline cursor-pointer"
              >
                View all {suggestions.length} items in Smart Inbox →
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
