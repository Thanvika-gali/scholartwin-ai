import React, { useState } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Badge from '../ui/Badge';

export default function ProblemDetailModal({
  isOpen,
  onClose,
  problemDetail,
  isLoading = false,
  onEdit,
  onDelete,
  onLogAttempt,
  onScheduleRevision,
  onCompleteRevision,
  onDeleteRevision,
}) {
  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'attempts' | 'revisions'

  if (!isOpen) return null;

  const problem = problemDetail?.problem;
  const attempts = problemDetail?.attempts || [];
  const revisions = problemDetail?.revisions || [];
  const stats = problemDetail?.stats || {
    totalAttempts: 0,
    successfulAttempts: 0,
    failedAttempts: 0,
    successRatePercent: 0,
    averageTimeSpentMinutes: null,
  };

  const difficultyColors = {
    EASY: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
    MEDIUM: 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30',
    HARD: 'bg-rose-500/15 text-rose-600 dark:text-rose-400 border-rose-500/30',
  };

  const statusColors = {
    NOT_STARTED: 'bg-slate-500/15 text-slate-600 dark:text-slate-400 border-slate-500/30',
    ATTEMPTED: 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30',
    SOLVED: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={problem?.title || 'Problem Details'}
      subtitle={problem ? `${problem.platform} • ${problem.primaryTopic}` : ''}
      maxWidth="max-w-3xl"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
          />
        </svg>
      }
    >
      {isLoading || !problem ? (
        <div className="p-8 text-center text-text-muted">Loading problem details...</div>
      ) : (
        <div className="flex flex-col max-h-[75vh]">
          {/* Top metadata header bar */}
          <div className="px-6 py-4 bg-bg-surface-elevated/40 border-b border-border-subtle flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap items-center gap-2">
              <span
                className={`text-xs px-2.5 py-0.5 rounded-full font-semibold border ${
                  difficultyColors[problem.difficulty] || ''
                }`}
              >
                {problem.difficulty}
              </span>
              <span
                className={`text-xs px-2.5 py-0.5 rounded-full font-semibold border ${
                  statusColors[problem.status] || ''
                }`}
              >
                {problem.status.replace('_', ' ')}
              </span>
              {problem.revisionRequired && (
                <span className="text-xs px-2.5 py-0.5 rounded-full font-semibold border bg-brand-gold/15 text-amber-800 dark:text-brand-gold-light border-brand-gold/30">
                  Revision Queued
                </span>
              )}
            </div>

            {problem.problemUrl && (
              <a
                href={problem.problemUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1.5 text-xs font-semibold text-brand-teal hover:underline"
              >
                Open on {problem.platform}
                <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
                  />
                </svg>
              </a>
            )}
          </div>

          {/* Quick Stats Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 p-6 pb-2">
            <div className="bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-center">
              <span className="text-[11px] text-text-muted uppercase tracking-wider font-semibold block">
                Attempts
              </span>
              <span className="text-lg font-bold text-text-primary">{stats.totalAttempts}</span>
            </div>
            <div className="bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-center">
              <span className="text-[11px] text-text-muted uppercase tracking-wider font-semibold block">
                Success Rate
              </span>
              <span className="text-lg font-bold text-brand-teal">{stats.successRatePercent}%</span>
            </div>
            <div className="bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-center">
              <span className="text-[11px] text-text-muted uppercase tracking-wider font-semibold block">
                Avg Time
              </span>
              <span className="text-lg font-bold text-text-primary">
                {stats.averageTimeSpentMinutes !== null ? `${stats.averageTimeSpentMinutes}m` : '—'}
              </span>
            </div>
            <div className="bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-center">
              <span className="text-[11px] text-text-muted uppercase tracking-wider font-semibold block">
                Revisions
              </span>
              <span className="text-lg font-bold text-brand-gold">{revisions.length}</span>
            </div>
          </div>

          {/* Navigation Sub-Tabs */}
          <div className="px-6 flex gap-4 border-b border-border-subtle mt-2">
            <button
              type="button"
              onClick={() => setActiveTab('overview')}
              className={`pb-2.5 text-xs font-semibold border-b-2 transition-all cursor-pointer ${
                activeTab === 'overview'
                  ? 'border-brand-teal text-brand-teal'
                  : 'border-transparent text-text-muted hover:text-text-primary'
              }`}
            >
              Overview & Solution
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('attempts')}
              className={`pb-2.5 text-xs font-semibold border-b-2 transition-all cursor-pointer ${
                activeTab === 'attempts'
                  ? 'border-brand-teal text-brand-teal'
                  : 'border-transparent text-text-muted hover:text-text-primary'
              }`}
            >
              Attempt History ({attempts.length})
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('revisions')}
              className={`pb-2.5 text-xs font-semibold border-b-2 transition-all cursor-pointer ${
                activeTab === 'revisions'
                  ? 'border-brand-teal text-brand-teal'
                  : 'border-transparent text-text-muted hover:text-text-primary'
              }`}
            >
              Revision Schedule ({revisions.length})
            </button>
          </div>

          {/* Tab Contents */}
          <div className="p-6 overflow-y-auto space-y-4">
            {activeTab === 'overview' && (
              <>
                {/* Topic tags */}
                <div>
                  <h4 className="text-xs font-semibold text-text-secondary uppercase tracking-wider mb-2">
                    Topics & Patterns
                  </h4>
                  <div className="flex flex-wrap gap-1.5">
                    <span className="text-xs font-semibold px-2.5 py-1 rounded-lg bg-brand-teal text-white">
                      {problem.primaryTopic}
                    </span>
                    {problem.additionalTopics?.map((t) => (
                      <span
                        key={t}
                        className="text-xs font-medium px-2.5 py-1 rounded-lg bg-bg-surface-elevated text-text-secondary border border-border-default"
                      >
                        {t}
                      </span>
                    ))}
                  </div>
                </div>

                {/* Complexities */}
                {(problem.timeComplexity || problem.spaceComplexity) && (
                  <div className="grid grid-cols-2 gap-3">
                    <div className="p-3 bg-bg-surface-elevated border border-border-default rounded-xl">
                      <span className="text-[11px] text-text-muted font-semibold uppercase block">
                        Time Complexity
                      </span>
                      <span className="text-sm font-mono font-medium text-text-primary">
                        {problem.timeComplexity || 'Not specified'}
                      </span>
                    </div>
                    <div className="p-3 bg-bg-surface-elevated border border-border-default rounded-xl">
                      <span className="text-[11px] text-text-muted font-semibold uppercase block">
                        Space Complexity
                      </span>
                      <span className="text-sm font-mono font-medium text-text-primary">
                        {problem.spaceComplexity || 'Not specified'}
                      </span>
                    </div>
                  </div>
                )}

                {/* Solution Approach */}
                <div>
                  <h4 className="text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
                    Optimal Solution Approach
                  </h4>
                  <div className="p-3.5 bg-bg-surface-elevated border border-border-default rounded-xl text-sm text-text-primary leading-relaxed whitespace-pre-wrap">
                    {problem.solutionApproach || (
                      <span className="text-text-muted italic">No solution approach recorded yet.</span>
                    )}
                  </div>
                </div>

                {/* Personal Notes */}
                <div>
                  <h4 className="text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
                    Personal Takeaways & Gotchas
                  </h4>
                  <div className="p-3.5 bg-bg-surface-elevated border border-border-default rounded-xl text-sm text-text-primary leading-relaxed whitespace-pre-wrap">
                    {problem.personalNotes || (
                      <span className="text-text-muted italic">No personal notes recorded yet.</span>
                    )}
                  </div>
                </div>
              </>
            )}

            {activeTab === 'attempts' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h4 className="text-xs font-semibold text-text-secondary uppercase tracking-wider">
                    Past Practice Sessions
                  </h4>
                  <Button size="sm" variant="primary" onClick={() => onLogAttempt(problem)}>
                    + Log New Attempt
                  </Button>
                </div>

                {attempts.length === 0 ? (
                  <div className="text-center py-8 text-text-muted text-sm bg-bg-surface-elevated rounded-xl border border-border-default">
                    No attempts logged for this problem yet. Click above to log your first attempt!
                  </div>
                ) : (
                  <div className="space-y-2.5">
                    {attempts.map((att) => (
                      <div
                        key={att.id}
                        className="p-3.5 bg-bg-surface-elevated border border-border-default rounded-xl flex flex-col gap-2"
                      >
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <span
                              className={`text-xs px-2 py-0.5 rounded-md font-bold uppercase ${
                                att.outcome === 'SUCCESS'
                                  ? 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border border-emerald-500/20'
                                  : 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border border-amber-500/20'
                              }`}
                            >
                              {att.outcome}
                            </span>
                            <span className="text-xs text-text-muted">
                              {att.attemptDate} • {att.programmingLanguage || 'Code'}
                            </span>
                          </div>
                          {att.timeSpentMinutes && (
                            <span className="text-xs font-medium text-text-secondary">
                              ⏱️ {att.timeSpentMinutes} mins
                            </span>
                          )}
                        </div>

                        {att.learningNotes && (
                          <p className="text-xs text-text-primary bg-bg-surface p-2.5 rounded-lg border border-border-subtle">
                            {att.learningNotes}
                          </p>
                        )}
                        {att.difficultyEncountered && (
                          <p className="text-[11px] text-amber-600 dark:text-amber-400">
                            <strong>Challenge:</strong> {att.difficultyEncountered}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {activeTab === 'revisions' && (
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h4 className="text-xs font-semibold text-text-secondary uppercase tracking-wider">
                    Scheduled Spaced Reviews
                  </h4>
                  <Button size="sm" variant="gold" onClick={() => onScheduleRevision(problem)}>
                    + Schedule Revision
                  </Button>
                </div>

                {revisions.length === 0 ? (
                  <div className="text-center py-8 text-text-muted text-sm bg-bg-surface-elevated rounded-xl border border-border-default">
                    No revisions scheduled. Add spaced repetition to cement this algorithmic pattern!
                  </div>
                ) : (
                  <div className="space-y-2.5">
                    {revisions.map((rev) => (
                      <div
                        key={rev.id}
                        className="p-3.5 bg-bg-surface-elevated border border-border-default rounded-xl flex items-center justify-between gap-3"
                      >
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span
                              className={`text-xs px-2 py-0.5 rounded-md font-semibold ${
                                rev.status === 'COMPLETED'
                                  ? 'bg-emerald-500/15 text-emerald-600 border border-emerald-500/20'
                                  : rev.isOverdue
                                  ? 'bg-rose-500/15 text-rose-600 border border-rose-500/20'
                                  : 'bg-brand-teal/15 text-brand-teal border border-brand-teal/20'
                              }`}
                            >
                              {rev.status === 'COMPLETED'
                                ? 'Completed'
                                : rev.isOverdue
                                ? 'Overdue'
                                : 'Scheduled'}
                            </span>
                            <span className="text-xs font-medium text-text-primary">
                              Target: {rev.scheduledDate}
                            </span>
                          </div>
                          {rev.revisionNotes && (
                            <p className="text-xs text-text-muted">{rev.revisionNotes}</p>
                          )}
                        </div>

                        <div className="flex items-center gap-2">
                          {rev.status === 'PENDING' && (
                            <button
                              type="button"
                              onClick={() => onCompleteRevision(rev.id)}
                              className="text-xs font-semibold px-2.5 py-1 rounded-lg bg-emerald-500 text-white hover:bg-emerald-600 transition-colors cursor-pointer"
                            >
                              Mark Done
                            </button>
                          )}
                          <button
                            type="button"
                            onClick={() => onDeleteRevision(rev.id)}
                            className="p-1 text-text-muted hover:text-rose-500 transition-colors cursor-pointer"
                            title="Delete revision"
                          >
                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                              />
                            </svg>
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Modal Footer */}
          <div className="p-6 pt-3 border-t border-border-subtle flex items-center justify-between">
            <Button
              variant="destructive"
              size="sm"
              onClick={() => onDelete(problem)}
            >
              Delete Problem
            </Button>

            <div className="flex items-center gap-2">
              <Button variant="secondary" size="sm" onClick={() => onEdit(problem)}>
                Edit Details
              </Button>
              <Button variant="primary" size="sm" onClick={onClose}>
                Close
              </Button>
            </div>
          </div>
        </div>
      )}
    </Modal>
  );
}

