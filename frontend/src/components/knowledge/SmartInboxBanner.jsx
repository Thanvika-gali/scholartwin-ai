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
    <div className="mb-6 rounded-2xl border border-teal-200 dark:border-teal-900/60 bg-gradient-to-r from-teal-50/80 via-white to-amber-50/40 dark:from-slate-900 dark:via-slate-900/90 dark:to-teal-950/20 p-4 shadow-sm">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-xl bg-teal-600 text-white flex items-center justify-center flex-shrink-0 shadow-sm">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M13 10V3L4 14h7v7l9-11h-7z"
              />
            </svg>
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h4 className="text-sm font-bold text-slate-900 dark:text-white">
                Smart Inbox — {suggestions.length} Pending Organization {suggestions.length === 1 ? 'Suggestion' : 'Suggestions'}
              </h4>
              <span className="px-2 py-0.5 text-[10px] font-bold rounded-full bg-teal-100 dark:bg-teal-900/60 text-teal-800 dark:text-teal-200 uppercase tracking-wide">
                AI & Academic Rules
              </span>
            </div>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              ScholarTwin automatically classified your new materials. Review and accept suggestions to keep your library organized.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {highConfidenceCount > 0 && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={onBulkAcceptHighConfidence}
              className="text-xs border-teal-600 text-teal-700 dark:text-teal-300 hover:bg-teal-50 dark:hover:bg-teal-950/50"
            >
              Accept High-Confidence ({highConfidenceCount})
            </Button>
          )}
          <button
            onClick={() => setIsExpanded((prev) => !prev)}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
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
            const isAutoMoveRule = suggestion.reasoning && suggestion.reasoning.includes('user-defined rule');

            return (
              <div
                key={suggestion.id}
                className="flex flex-col md:flex-row md:items-center justify-between gap-3 p-3 rounded-xl bg-white/80 dark:bg-slate-800/80 border border-slate-200/70 dark:border-slate-700/60 shadow-xs"
              >
                <div className="flex items-start gap-3 min-w-0">
                  <div className="p-2 rounded-lg bg-slate-100 dark:bg-slate-700/50 text-slate-600 dark:text-slate-300 flex-shrink-0">
                    {suggestion.resourceType === 'DOCUMENT' ? (
                      <svg className="w-4 h-4 text-teal-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                    ) : suggestion.resourceType === 'LINK' ? (
                      <svg className="w-4 h-4 text-sky-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                      </svg>
                    ) : (
                      <svg className="w-4 h-4 text-amber-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                      </svg>
                    )}
                  </div>

                  <div className="min-w-0">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-xs font-bold text-slate-800 dark:text-slate-100">
                        {suggestion.resourceType}: #{suggestion.resourceId}
                      </span>
                      <span className="text-xs text-slate-400">→</span>
                      <span className="inline-flex items-center gap-1 text-xs font-semibold text-teal-700 dark:text-teal-300 bg-teal-50 dark:bg-teal-950/60 px-2 py-0.5 rounded-md">
                        <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                          <path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z" />
                        </svg>
                        {suggestion.suggestedFolderPath || 'Academic Library'}
                      </span>
                      <span
                        className={`text-[10px] px-1.5 py-0.2 rounded font-semibold ${
                          confidencePercent >= 85
                            ? 'bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300'
                            : 'bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300'
                        }`}
                      >
                        {confidencePercent}% confidence
                      </span>
                    </div>

                    <p className="text-[11px] text-slate-500 dark:text-slate-400 mt-1">
                      {suggestion.reasoning}
                    </p>

                    {suggestion.suggestedTags && suggestion.suggestedTags.length > 0 && (
                      <div className="flex flex-wrap gap-1 mt-1.5">
                        {suggestion.suggestedTags.map((tag) => (
                          <span
                            key={tag}
                            className="text-[10px] px-1.5 py-0.2 rounded bg-slate-100 dark:bg-slate-700/60 text-slate-600 dark:text-slate-300"
                          >
                            #{tag}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-2 flex-shrink-0 self-end md:self-center">
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    disabled={loadingId === suggestion.id}
                    onClick={() => handleAction(suggestion.id, () => onReject(suggestion.id))}
                    className="text-xs text-slate-500 hover:text-slate-700 dark:hover:text-slate-200"
                  >
                    Dismiss
                  </Button>
                  <Button
                    type="button"
                    variant="primary"
                    size="sm"
                    disabled={loadingId === suggestion.id}
                    onClick={() => handleAction(suggestion.id, () => onAccept(suggestion))}
                    className="text-xs bg-teal-600 hover:bg-teal-700 text-white"
                  >
                    {loadingId === suggestion.id ? 'Moving...' : 'Accept Suggestion'}
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
                className="text-xs font-semibold text-teal-600 dark:text-teal-400 hover:underline"
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

