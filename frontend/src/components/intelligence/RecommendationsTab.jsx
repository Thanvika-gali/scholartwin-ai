import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Button from '../ui/Button';
import Badge from '../ui/Badge';
import { getPriorityBadgeStyle } from '../../utils/intelligenceValidation';

export default function RecommendationsTab({
  recommendations,
  isLoading,
  onRefreshRecommendations,
  onUpdateStatus,
  onNavigateTab,
}) {
  const navigate = useNavigate();
  const [statusFilter, setStatusFilter] = useState('ACTIVE');
  const [priorityFilter, setPriorityFilter] = useState('ALL');
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  const filtered = recommendations.filter((rec) => {
    // Status filter
    if (statusFilter === 'ACTIVE') {
      if (rec.status !== 'PENDING' && rec.status !== 'ACCEPTED') return false;
    } else if (statusFilter !== 'ALL' && rec.status !== statusFilter) {
      return false;
    }

    // Priority filter
    if (priorityFilter !== 'ALL' && rec.priority !== priorityFilter) {
      return false;
    }

    // Category filter
    if (categoryFilter !== 'ALL' && rec.category !== categoryFilter) {
      return false;
    }

    return true;
  });

  const handleActionClick = (target) => {
    if (!target) return;
    if (target.startsWith('/intelligence')) {
      const url = new URL(target, window.location.origin);
      const tab = url.searchParams.get('tab');
      if (tab && onNavigateTab) {
        onNavigateTab(tab);
        return;
      }
    }
    navigate(target);
  };

  const getCategoryColor = (cat) => {
    switch (cat) {
      case 'LEARNING':
        return 'bg-blue-500/10 text-blue-400 border-blue-500/20';
      case 'DSA':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/20';
      case 'CAREER':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
      case 'KNOWLEDGE':
        return 'bg-purple-500/10 text-purple-400 border-purple-500/20';
      case 'PRODUCTIVITY':
        return 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20';
      default:
        return 'bg-border-default/40 text-text-secondary border-border-default';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">AI Prioritized Recommendation Queue</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Algorithmic action plans synthesized from your active study sessions, DSA accuracy, and job readiness gaps.
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          onClick={onRefreshRecommendations}
          loading={isLoading}
        >
          ↻ Regenerate Queue
        </Button>
      </div>

      {/* Filter Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-bg-surface/50 border border-border-default rounded-xl p-3">
        <div className="flex flex-wrap items-center gap-2">
          <span className="text-xs font-semibold text-text-muted uppercase tracking-wider mr-1">Status:</span>
          {['ACTIVE', 'ALL', 'PENDING', 'ACCEPTED', 'COMPLETED', 'DISMISSED'].map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`text-xs px-2.5 py-1 rounded-lg border font-medium transition-all ${
                statusFilter === status
                  ? 'bg-primary-500/15 border-primary-500/40 text-primary-400 shadow-xs'
                  : 'bg-bg-surface border-border-default/60 text-text-muted hover:text-text-primary hover:border-border-default'
              }`}
            >
              {status === 'ACTIVE' ? 'Active (Pending & In-Progress)' : status.charAt(0) + status.slice(1).toLowerCase()}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2">
          {/* Priority filter dropdown */}
          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="text-xs bg-bg-surface border border-border-default rounded-lg px-2.5 py-1 text-text-secondary focus:outline-none focus:border-primary-500"
          >
            <option value="ALL">All Priorities</option>
            <option value="URGENT">Urgent</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          {/* Category filter dropdown */}
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="text-xs bg-bg-surface border border-border-default rounded-lg px-2.5 py-1 text-text-secondary focus:outline-none focus:border-primary-500"
          >
            <option value="ALL">All Categories</option>
            <option value="LEARNING">Learning</option>
            <option value="DSA">DSA</option>
            <option value="CAREER">Career</option>
            <option value="KNOWLEDGE">Knowledge</option>
            <option value="PRODUCTIVITY">Productivity</option>
          </select>
        </div>
      </div>

      {/* Recommendations List */}
      {filtered.length === 0 ? (
        <Card className="text-center py-12">
          <CardContent>
            <div className="w-12 h-12 rounded-2xl bg-primary-500/10 text-primary-400 border border-primary-500/20 flex items-center justify-center mx-auto mb-3 text-xl">
              ✓
            </div>
            <h3 className="text-base font-semibold text-text-primary">No recommendations matching filters</h3>
            <p className="text-xs text-text-muted mt-1 max-w-sm mx-auto">
              You are all caught up on recommendations for this criteria. Click "Regenerate Queue" to re-evaluate your learning and practice progress.
            </p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {filtered.map((rec) => {
            const priorityBadge = getPriorityBadgeStyle(rec.priority);

            return (
              <Card
                key={rec.id}
                className={`transition-all hover:border-border-default/80 ${
                  rec.priority === 'URGENT'
                    ? 'border-l-4 border-l-rose-500'
                    : rec.priority === 'HIGH'
                    ? 'border-l-4 border-l-amber-500'
                    : 'border-l-4 border-l-primary-500'
                }`}
              >
                <CardContent className="p-5">
                  <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
                    <div className="space-y-2 flex-1">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded border uppercase tracking-wider ${priorityBadge.color}`}>
                          {priorityBadge.label}
                        </span>
                        <span className={`text-[10px] font-bold px-2 py-0.5 rounded border uppercase tracking-wider ${getCategoryColor(rec.category)}`}>
                          {rec.category}
                        </span>
                        <span className="text-[10px] px-2 py-0.5 rounded bg-bg-surface border border-border-default text-text-muted font-mono">
                          Status: {rec.status}
                        </span>
                        {rec.estimatedMinutes && (
                          <span className="text-[11px] text-text-muted flex items-center gap-1 font-mono">
                            ⏱ ~{rec.estimatedMinutes} mins
                          </span>
                        )}
                      </div>

                      <h3 className="text-base font-bold text-text-primary tracking-tight">
                        {rec.title}
                      </h3>

                      <p className="text-xs text-text-secondary leading-relaxed">
                        {rec.description}
                      </p>

                      {rec.rationale && (
                        <div className="bg-bg-surface/80 border border-border-default/60 rounded-xl p-3 text-xs text-text-muted">
                          <span className="font-semibold text-text-primary block mb-0.5">Underlying Evidence & Rationale:</span>
                          {rec.rationale}
                        </div>
                      )}
                    </div>

                    {/* Actions Panel */}
                    <div className="flex flex-row md:flex-col items-center md:items-end justify-between gap-2 shrink-0 border-t md:border-t-0 pt-3 md:pt-0 border-border-default/60">
                      {rec.actionTarget && (
                        <Button
                          variant="primary"
                          size="sm"
                          onClick={() => handleActionClick(rec.actionTarget)}
                          className="w-full md:w-auto"
                        >
                          Execute Plan →
                        </Button>
                      )}

                      <div className="flex items-center gap-1.5 w-full md:w-auto justify-end">
                        {rec.status === 'PENDING' && (
                          <Button
                            variant="secondary"
                            size="xs"
                            onClick={() => onUpdateStatus(rec.id, 'ACCEPTED')}
                            title="Accept this recommendation into your active plan"
                          >
                            Accept
                          </Button>
                        )}

                        {rec.status === 'ACCEPTED' && (
                          <Button
                            variant="secondary"
                            size="xs"
                            className="bg-emerald-500/10 text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/20"
                            onClick={() => onUpdateStatus(rec.id, 'COMPLETED')}
                            title="Mark as completed"
                          >
                            ✓ Complete
                          </Button>
                        )}

                        {rec.status !== 'DISMISSED' && rec.status !== 'COMPLETED' && (
                          <Button
                            variant="ghost"
                            size="xs"
                            className="text-text-muted hover:text-rose-400"
                            onClick={() => onUpdateStatus(rec.id, 'DISMISSED')}
                            title="Dismiss recommendation"
                          >
                            Dismiss
                          </Button>
                        )}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}
    </div>
  );
}
