import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';
import { fetchKnowledgeAnalyticsApi } from '../../services/api';

export default function KnowledgeAnalyticsModal({
  isOpen,
  onClose,
}) {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen) {
      setLoading(true);
      setError('');
      fetchKnowledgeAnalyticsApi()
        .then((data) => setAnalytics(data))
        .catch((err) => setError(err.message || 'Failed to load analytics'))
        .finally(() => setLoading(false));
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div
        className="bg-white dark:bg-slate-900 rounded-2xl max-w-3xl w-full shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[85vh]"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-teal-600 text-white flex items-center justify-center">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
              </svg>
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">
                Knowledge Hub 2.0 Intelligence & Analytics
              </h3>
              <p className="text-xs text-slate-500">
                Live metrics on your academic library, classification, and subject distributions.
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className="p-6 overflow-y-auto flex-1 space-y-6 text-xs scrollbar-thin">
          {error && (
            <div className="p-3 text-xs bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 rounded-lg">
              {error}
            </div>
          )}

          {loading ? (
            <div className="py-12 text-center text-slate-400">Loading library analytics...</div>
          ) : analytics ? (
            <>
              {/* Summary stat cards */}
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-800">
                  <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Total Resources</span>
                  <span className="text-xl font-extrabold text-slate-900 dark:text-white mt-1 block">
                    {analytics.totalResources || 0}
                  </span>
                  <span className="text-[11px] text-teal-600 mt-0.5 block">
                    {analytics.totalDocuments || 0} docs • {analytics.totalNotes || 0} notes • {analytics.totalLinks || 0} links
                  </span>
                </div>

                <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-800">
                  <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Folders & Collections</span>
                  <span className="text-xl font-extrabold text-teal-600 dark:text-teal-400 mt-1 block">
                    {analytics.totalFolders || 0}
                  </span>
                  <span className="text-[11px] text-slate-500 mt-0.5 block">
                    {analytics.totalCollections || 0} curated collections
                  </span>
                </div>

                <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-800">
                  <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Smart Inbox Pending</span>
                  <span className="text-xl font-extrabold text-amber-500 mt-1 block">
                    {analytics.inboxCount || 0}
                  </span>
                  <span className="text-[11px] text-slate-500 mt-0.5 block">
                    {analytics.favoriteCount || 0} favorited items
                  </span>
                </div>

                <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/60 dark:border-slate-800">
                  <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider block">Storage Used</span>
                  <span className="text-xl font-extrabold text-slate-900 dark:text-white mt-1 block">
                    {analytics.storageUsedFormatted || '0 B'}
                  </span>
                  <span className="text-[11px] text-slate-500 mt-0.5 block">
                    {analytics.totalStorageBytes || 0} bytes
                  </span>
                </div>
              </div>

              {/* Subject Breakdown */}
              {analytics.subjectBreakdown && Object.keys(analytics.subjectBreakdown).length > 0 && (
                <div>
                  <h4 className="font-bold text-slate-900 dark:text-white mb-2">
                    Academic Subject Distribution
                  </h4>
                  <div className="space-y-2">
                    {Object.entries(analytics.subjectBreakdown).map(([subject, count]) => {
                      const percentage = Math.round((count / (analytics.totalResources || 1)) * 100);
                      return (
                        <div key={subject}>
                          <div className="flex justify-between text-xs mb-1">
                            <span className="font-semibold text-slate-700 dark:text-slate-300">{subject}</span>
                            <span className="text-slate-500 font-mono">{count} ({percentage}%)</span>
                          </div>
                          <div className="w-full bg-slate-100 dark:bg-slate-800 h-2 rounded-full overflow-hidden">
                            <div
                              className="bg-teal-500 h-full rounded-full transition-all duration-300"
                              style={{ width: `${Math.max(5, percentage)}%` }}
                            />
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}

              {/* Top Active Folders */}
              {analytics.topActiveFolders && analytics.topActiveFolders.length > 0 && (
                <div>
                  <h4 className="font-bold text-slate-900 dark:text-white mb-2">
                    Most Active Folders
                  </h4>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                    {analytics.topActiveFolders.map((f) => (
                      <div
                        key={f.id}
                        className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/60 dark:border-slate-800 flex items-center justify-between"
                      >
                        <div className="flex items-center gap-2 truncate">
                          <span
                            className="w-3.5 h-3.5 flex-shrink-0"
                            style={{ color: f.color || '#178582' }}
                          >
                            <svg className="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20">
                              <path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z" />
                            </svg>
                          </span>
                          <span className="font-semibold text-slate-800 dark:text-slate-200 truncate">{f.name}</span>
                        </div>
                        <span className="text-xs font-mono font-bold text-teal-600 bg-teal-50 dark:bg-teal-950/60 px-2 py-0.5 rounded-full">
                          {f.totalCount || 0} items
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </>
          ) : null}
        </div>

        <div className="px-6 py-3 border-t border-slate-100 dark:border-slate-800 flex justify-end">
          <Button type="button" variant="primary" onClick={onClose}>
            Close
          </Button>
        </div>
      </div>
    </div>
  );
}

