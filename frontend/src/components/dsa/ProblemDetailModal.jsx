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
      footer={
        <div className="flex items-center justify-between w-full">
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
              Done
            </Button>
          </div>
        </div>
      }
    >
      {isLoading || !problem ? (
        <div className="p-8 text-center text-text-muted">Loading problem specifications...</div>
      ) : (
        <div className="space-y-4">
          {/* Top metadata header bar */}
          <div className="p-3 sm:p-4 bg-bg-surface-elevated/50 border border-border-default rounded-xl flex flex-wrap items-center justify-between gap-3">
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

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="xs"
                onClick={() => onLogAttempt(problem)}
              >
                + Log Attempt
              </Button>
              <Button
                variant="teal-subtle"
                size="xs"
                onClick={() => onScheduleRevision(problem)}
              >
                + Schedule Review
              </Button>
              {problem.problemUrl && (
                <a
                  href={problem.problemUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="inline-flex items-center gap-1 text-xs font-semibold text-brand-teal hover:underline ml-1"
                >
                  Solve →
                </a>
              )}
            </div>
          </div>

          {/* Quick Stats Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
            <div className="bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-center">
              <span className="text-[10px] text-text-muted uppercase tracking-wider font-semibold block">
                Attempts
              </span>
              <span className="text-lg font-bold text-text-primary">{stats.totalAttempts}</span>
            </div>
            <div className="bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-center">
              <span className="text-[10px] text-text-muted uppercase tracking-wider font-semibold block">
                Accuracy
              </span>
              <span className="text-lg font-bold text-brand-teal">
                {stats.successRatePercent !== undefined ? `${stats.successRatePercent}%` : '0%'}
              </span>
            </div>
            <div className="bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-center">
              <span className="text-[10px] text-text-muted uppercase tracking-wider font-semibold block">
                Avg Time
              </span>
              <span className="text-lg font-bold text-text-primary">
                {stats.averageTimeSpentMinutes ? `${stats.averageTimeSpentMinutes}m` : '—'}
              </span>
            </div>
            <div className="bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-center">
              <span className="text-[10px] text-text-muted uppercase tracking-wider font-semibold block">
                Revisions
              </span>
              <span className="text-lg font-bold text-brand-gold">{revisions.length}</span>
            </div>
          </div>

          {/* Navigation Tabs */}
          <div className="flex border-b border-border-subtle gap-4 text-xs font-semibold">
            <button
              type="button"
              onClick={() => setActiveTab('overview')}
              className={`pb-2 transition-colors cursor-pointer ${
                activeTab === 'overview'
                  ? 'border-b-2 border-brand-teal text-brand-teal font-bold'
                  : 'text-text-muted hover:text-text-primary'
              }`}
            >
              Overview & Invariants
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('attempts')}
              className={`pb-2 transition-colors cursor-pointer ${
                activeTab === 'attempts'
                  ? 'border-b-2 border-brand-teal text-brand-teal font-bold'
                  : 'text-text-muted hover:text-text-primary'
              }`}
            >
              Attempts History ({attempts.length})
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('revisions')}
              className={`pb-2 transition-colors cursor-pointer ${
                activeTab === 'revisions'
                  ? 'border-b-2 border-brand-teal text-brand-teal font-bold'
                  : 'text-text-muted hover:text-text-primary'
              }`}
            >
              Spaced Revisions ({revisions.length})
            </button>
          </div>

          {/* Tab 1: Overview */}
          {activeTab === 'overview' && (
            <div className="space-y-3 text-xs">
              {/* Complexities */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div className="p-3 bg-bg-surface-elevated border border-border-default rounded-xl">
                  <span className="text-[10px] uppercase font-bold text-text-muted block mb-0.5">
                    Time Complexity
                  </span>
                  <span className="font-mono text-sm font-semibold text-text-primary">
                    {problem.timeComplexity || 'Not specified'}
                  </span>
                </div>
                <div className="p-3 bg-bg-surface-elevated border border-border-default rounded-xl">
                  <span className="text-[10px] uppercase font-bold text-text-muted block mb-0.5">
                    Space Complexity
                  </span>
                  <span className="font-mono text-sm font-semibold text-text-primary">
                    {problem.spaceComplexity || 'Not specified'}
                  </span>
                </div>
              </div>

              {/* Solution Approach */}
              <div>
                <span className="text-[10px] uppercase font-bold text-text-muted block mb-1">
                  Optimal Solution Approach & Invariants
                </span>
                <div className="p-3.5 bg-bg-surface-elevated border border-border-default rounded-xl text-text-primary whitespace-pre-wrap leading-relaxed">
                  {problem.solutionApproach || 'No solution approach logged yet.'}
                </div>
              </div>

              {/* Personal Notes */}
              <div>
                <span className="text-[10px] uppercase font-bold text-text-muted block mb-1">
                  Personal Takeaways & Edge Cases
                </span>
                <div className="p-3.5 bg-bg-surface-elevated border border-border-default rounded-xl text-text-primary whitespace-pre-wrap leading-relaxed">
                  {problem.personalNotes || 'No personal notes logged yet.'}
                </div>
              </div>

              {/* Additional Topics */}
              {problem.additionalTopics && problem.additionalTopics.length > 0 && (
                <div>
                  <span className="text-[10px] uppercase font-bold text-text-muted block mb-1">
                    Related Algorithmic Patterns
                  </span>
                  <div className="flex flex-wrap gap-1.5">
                    {problem.additionalTopics.map((top) => (
                      <span
                        key={top}
                        className="text-[10px] px-2 py-0.5 rounded-md bg-bg-surface-elevated border border-border-default text-text-secondary"
                      >
                        {top}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Tab 2: Attempts */}
          {activeTab === 'attempts' && (
            <div className="space-y-2 text-xs">
              {attempts.length === 0 ? (
                <div className="py-8 text-center text-text-muted">
                  <p>No practice attempts logged for this problem.</p>
                  <Button
                    variant="outline"
                    size="xs"
                    onClick={() => onLogAttempt(problem)}
                    className="mt-2"
                  >
                    Log First Attempt
                  </Button>
                </div>
              ) : (
                attempts.map((att) => (
                  <div
                    key={att.id}
                    className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default flex items-start justify-between gap-3"
                  >
                    <div>
                      <div className="flex items-center gap-2 mb-1">
                        <span
                          className={`text-[10px] font-bold px-1.5 py-0.2 rounded uppercase ${
                            att.successful
                              ? 'bg-status-success-bg text-status-success-text border border-status-success-border'
                              : 'bg-status-error-bg text-status-error-text border border-status-error-border'
                          }`}
                        >
                          {att.successful ? 'SOLVED' : 'FAILED'}
                        </span>
                        <span className="text-[11px] text-text-muted">
                          {new Date(att.attemptedAt).toLocaleDateString()}
                        </span>
                        {att.timeSpentMinutes && (
                          <span className="text-[11px] text-text-muted font-mono">
                            • {att.timeSpentMinutes} mins
                          </span>
                        )}
                      </div>
                      {att.approachUsed && (
                        <p className="text-xs text-text-secondary font-medium">{att.approachUsed}</p>
                      )}
                      {att.notes && <p className="text-[11px] text-text-muted mt-0.5">{att.notes}</p>}
                    </div>
                  </div>
                ))
              )}
            </div>
          )}

          {/* Tab 3: Revisions */}
          {activeTab === 'revisions' && (
            <div className="space-y-2 text-xs">
              {revisions.length === 0 ? (
                <div className="py-8 text-center text-text-muted">
                  <p>No spaced repetition reviews scheduled.</p>
                  <Button
                    variant="outline"
                    size="xs"
                    onClick={() => onScheduleRevision(problem)}
                    className="mt-2"
                  >
                    Schedule Revision
                  </Button>
                </div>
              ) : (
                revisions.map((rev) => (
                  <div
                    key={rev.id}
                    className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default flex items-center justify-between gap-3"
                  >
                    <div>
                      <div className="flex items-center gap-2 mb-0.5">
                        <span
                          className={`text-[10px] font-bold px-1.5 py-0.2 rounded uppercase ${
                            rev.status === 'COMPLETED'
                              ? 'bg-status-success-bg text-status-success-text border border-status-success-border'
                              : 'bg-amber-500/20 text-amber-700 dark:text-amber-300 border border-amber-500/30'
                          }`}
                        >
                          {rev.status}
                        </span>
                        <span className="text-xs font-semibold text-text-primary">
                          Scheduled: {rev.scheduledDate}
                        </span>
                      </div>
                      {rev.revisionNotes && (
                        <p className="text-[11px] text-text-muted">{rev.revisionNotes}</p>
                      )}
                    </div>

                    <div className="flex items-center gap-2">
                      {rev.status === 'PENDING' && (
                        <Button
                          variant="primary"
                          size="xs"
                          onClick={() => onCompleteRevision(rev.id)}
                        >
                          Mark Done
                        </Button>
                      )}
                      <button
                        type="button"
                        onClick={() => onDeleteRevision(rev.id)}
                        className="p-1 text-text-muted hover:text-status-error transition-colors cursor-pointer"
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
                ))
              )}
            </div>
          )}
        </div>
      )}
    </Modal>
  );
}
