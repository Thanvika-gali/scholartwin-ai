import React from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';
import Button from '../ui/Button';

export default function LearningOverviewTab({
  analytics,
  dailyPlan,
  revisionQueue,
  gaps,
  roadmaps,
  onNavigateTab,
  onOpenReviewModal,
  onOpenStudySessionModal,
  onToggleTaskComplete,
}) {
  return (
    <div className="space-y-6">
      {/* 1. High-Level KPI Metric Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3 sm:gap-4">
        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm hover:border-brand-teal/40 transition-all">
          <div className="flex items-center justify-between text-xs font-semibold text-text-muted mb-2">
            <span>Learning Hours</span>
            <span className="p-1.5 rounded-lg bg-brand-teal/10 text-brand-teal">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </span>
          </div>
          <div className="text-2xl font-extrabold text-text-primary tracking-tight">
            {analytics?.totalLearningHours ?? 0}
            <span className="text-xs text-text-muted font-normal ml-1">hrs</span>
          </div>
          <p className="text-[11px] text-text-muted mt-1">Verified study time</p>
        </div>

        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm hover:border-brand-gold/40 transition-all">
          <div className="flex items-center justify-between text-xs font-semibold text-text-muted mb-2">
            <span>Topics Done</span>
            <span className="p-1.5 rounded-lg bg-brand-gold/15 text-brand-gold">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </span>
          </div>
          <div className="text-2xl font-extrabold text-text-primary tracking-tight">
            {analytics?.completedTopicsCount ?? 0}
            <span className="text-xs text-text-muted font-normal ml-1">topics</span>
          </div>
          <p className="text-[11px] text-text-muted mt-1">Roadmap & DSA topics</p>
        </div>

        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm hover:border-orange-500/40 transition-all">
          <div className="flex items-center justify-between text-xs font-semibold text-text-muted mb-2">
            <span>Study Streak</span>
            <span className="p-1.5 rounded-lg bg-orange-500/10 text-orange-500">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343A7.975 7.975 0 0120 13a7.975 7.975 0 01-2.343 5.657z" />
              </svg>
            </span>
          </div>
          <div className="text-2xl font-extrabold text-text-primary tracking-tight">
            {analytics?.currentStreakDays ?? 0}
            <span className="text-xs text-text-muted font-normal ml-1">days</span>
          </div>
          <p className="text-[11px] text-text-muted mt-1">Continuous momentum</p>
        </div>

        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm hover:border-emerald-500/40 transition-all">
          <div className="flex items-center justify-between text-xs font-semibold text-text-muted mb-2">
            <span>Retention Rate</span>
            <span className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-500">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
            </span>
          </div>
          <div className="text-2xl font-extrabold text-text-primary tracking-tight">
            {analytics?.spacedRevisionRetentionRate ?? 0}%
          </div>
          <p className="text-[11px] text-text-muted mt-1">Memory decay defense</p>
        </div>

        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm hover:border-brand-teal/40 transition-all">
          <div className="flex items-center justify-between text-xs font-semibold text-text-muted mb-2">
            <span>Active Roadmaps</span>
            <span className="p-1.5 rounded-lg bg-brand-teal/10 text-brand-teal">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
              </svg>
            </span>
          </div>
          <div className="text-2xl font-extrabold text-text-primary tracking-tight">
            {analytics?.activeRoadmapsCount ?? 0}
          </div>
          <p className="text-[11px] text-text-muted mt-1">{analytics?.averageRoadmapCompletionPercentage ?? 0}% avg complete</p>
        </div>

        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm hover:border-rose-500/40 transition-all">
          <div className="flex items-center justify-between text-xs font-semibold text-text-muted mb-2">
            <span>Overdue Tasks</span>
            <span className="p-1.5 rounded-lg bg-rose-500/10 text-rose-500">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </span>
          </div>
          <div className="text-2xl font-extrabold text-text-primary tracking-tight text-rose-500">
            {analytics?.overdueTasksCount ?? 0}
          </div>
          <p className="text-[11px] text-text-muted mt-1">Requires rescheduling</p>
        </div>
      </div>

      {/* 2. Gap Radar Alert Banner */}
      {gaps && (
        <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-brand-navy via-brand-navy/95 to-slate-900 border border-brand-teal/30 p-5 sm:p-6 text-white shadow-lg">
          <div className="absolute top-0 right-0 -mt-6 -mr-6 w-48 h-48 bg-brand-teal/15 rounded-full blur-3xl pointer-events-none"></div>
          <div className="relative z-10 flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4">
            <div className="space-y-1.5 max-w-2xl">
              <div className="flex items-center gap-2">
                <span className="px-2 py-0.5 rounded text-[11px] font-bold tracking-wider uppercase bg-brand-gold text-brand-navy">
                  Adaptive Intelligence
                </span>
                <span className="text-xs text-text-muted-dark">Role Benchmark Target</span>
              </div>
              <h2 className="text-xl sm:text-2xl font-bold tracking-tight text-white">
                Target Role: {gaps.targetRole || 'Software Development Engineer'}
              </h2>
              <p className="text-xs sm:text-sm text-slate-300">
                You are currently at <strong className="text-brand-teal font-semibold">{gaps.overallReadinessPercentage}% readiness</strong>.
                {gaps.weakTopics?.length > 0 && (
                  <span> Identified key focus: <em>{gaps.weakTopics[0].topic}</em> ({gaps.weakTopics[0].rationale}).</span>
                )}
              </p>
            </div>
            <div className="flex flex-wrap items-center gap-3">
              <Button
                variant="secondary"
                size="sm"
                onClick={() => onNavigateTab('radar')}
                className="bg-white/10 hover:bg-white/20 text-white border-white/20 text-xs"
              >
                Inspect Gap Radar
              </Button>
              <Button
                variant="primary"
                size="sm"
                onClick={() => onNavigateTab('roadmaps')}
                className="bg-brand-teal hover:bg-brand-teal/90 text-white text-xs shadow-md"
              >
                Open Roadmaps
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* 3. Daily Study Planner Snapshot & Spaced Revision Queue */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Today's Study Planner */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-3">
            <div>
              <CardTitle className="text-base flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-brand-teal animate-pulse"></span>
                Today's Study Plan
              </CardTitle>
              <p className="text-xs text-text-muted mt-0.5">
                {dailyPlan?.date || 'Today'} • {dailyPlan?.completedMinutes || 0} / {dailyPlan?.plannedMinutes || 0} mins ({dailyPlan?.completionPercentage || 0}%)
              </p>
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={onOpenStudySessionModal}
                className="text-xs"
              >
                Log Session
              </Button>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => onNavigateTab('planner')}
                className="text-xs text-brand-teal"
              >
                View Full Plan →
              </Button>
            </div>
          </CardHeader>
          <CardContent className="space-y-3">
            {/* Progress Bar */}
            <div className="w-full bg-bg-surface-elevated rounded-full h-2 overflow-hidden border border-border-subtle">
              <div
                className="bg-brand-teal h-2 rounded-full transition-all duration-500"
                style={{ width: `${Math.min(100, dailyPlan?.completionPercentage || 0)}%` }}
              ></div>
            </div>

            {/* Tasks list */}
            {dailyPlan?.tasks && dailyPlan.tasks.length > 0 ? (
              <div className="space-y-2 mt-3">
                {dailyPlan.tasks.slice(0, 4).map((task) => (
                  <div
                    key={task.id}
                    className={`p-3 rounded-xl border flex items-center justify-between gap-3 transition-colors ${
                      task.status === 'COMPLETED'
                        ? 'bg-emerald-500/5 border-emerald-500/20 text-text-muted'
                        : 'bg-bg-surface-elevated/40 border-border-default hover:border-brand-teal/30'
                    }`}
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <button
                        type="button"
                        onClick={() => onToggleTaskComplete(task.id, task.status)}
                        className={`w-5 h-5 rounded-md border flex items-center justify-center transition-colors ${
                          task.status === 'COMPLETED'
                            ? 'bg-emerald-500 border-emerald-500 text-white'
                            : 'border-border-default hover:border-brand-teal'
                        }`}
                      >
                        {task.status === 'COMPLETED' && (
                          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                          </svg>
                        )}
                      </button>
                      <div className="min-w-0">
                        <p className={`text-xs font-semibold truncate ${task.status === 'COMPLETED' ? 'line-through text-text-muted' : 'text-text-primary'}`}>
                          {task.title}
                        </p>
                        <p className="text-[11px] text-text-muted flex items-center gap-2">
                          <span>{task.estimatedMinutes}m est</span>
                          <span>•</span>
                          <span className="uppercase text-[10px] tracking-wider">{task.taskType}</span>
                        </p>
                      </div>
                    </div>
                    <Badge
                      variant={
                        task.priority === 'HIGH' ? 'error' : task.priority === 'MEDIUM' ? 'warning' : 'neutral'
                      }
                      size="sm"
                    >
                      {task.priority}
                    </Badge>
                  </div>
                ))}
              </div>
            ) : (
              <div className="p-6 text-center text-text-muted text-xs">
                No tasks scheduled for today.
                <div className="mt-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onNavigateTab('planner')}
                    className="text-xs text-brand-teal"
                  >
                    Auto-populate Today's Schedule
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Spaced Revision Studio Radar */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-3">
            <div>
              <CardTitle className="text-base flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-brand-gold"></span>
                Spaced Revision Radar
              </CardTitle>
              <p className="text-xs text-text-muted mt-0.5">
                {revisionQueue?.dueTodayCount || 0} due today • {revisionQueue?.overdueCount || 0} overdue
              </p>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onNavigateTab('revision')}
              className="text-xs text-brand-teal"
            >
              Open Center →
            </Button>
          </CardHeader>
          <CardContent className="space-y-3">
            {revisionQueue?.dueToday && revisionQueue.dueToday.length > 0 ? (
              <div className="space-y-2">
                {revisionQueue.dueToday.slice(0, 3).map((item) => (
                  <div
                    key={item.id}
                    className="p-3 rounded-xl border border-border-default bg-bg-surface-elevated/40 flex items-center justify-between gap-3 hover:border-brand-gold/40 transition-colors"
                  >
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="px-1.5 py-0.5 rounded text-[10px] font-semibold bg-brand-gold/15 text-brand-gold uppercase">
                          Stage {item.stage}
                        </span>
                        <p className="text-xs font-bold text-text-primary truncate">{item.title}</p>
                      </div>
                      <p className="text-[11px] text-text-muted mt-0.5">
                        Interval: {item.intervalDays}d • Successes: {item.consecutiveSuccessCount}
                      </p>
                    </div>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => onOpenReviewModal(item)}
                      className="text-xs bg-brand-gold/15 text-brand-gold hover:bg-brand-gold hover:text-brand-navy transition-colors shrink-0"
                    >
                      Review Now
                    </Button>
                  </div>
                ))}
              </div>
            ) : revisionQueue?.upcoming && revisionQueue.upcoming.length > 0 ? (
              <div className="p-5 text-center text-xs text-text-muted space-y-1">
                <p className="font-semibold text-text-primary">All caught up for today!</p>
                <p>Next revision is scheduled for {revisionQueue.upcoming[0].scheduledDate}.</p>
              </div>
            ) : (
              <div className="p-6 text-center text-text-muted text-xs">
                No active spaced revisions tracked.
                <div className="mt-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => onNavigateTab('revision')}
                    className="text-xs text-brand-teal"
                  >
                    Add Revision Target
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* 4. Active Roadmaps Quick Glance */}
      <div>
        <div className="flex items-center justify-between mb-3">
          <h3 className="text-sm font-bold text-text-primary uppercase tracking-wider">Active Learning Curricula</h3>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onNavigateTab('roadmaps')}
            className="text-xs text-brand-teal"
          >
            Manage Roadmaps →
          </Button>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {roadmaps && roadmaps.length > 0 ? (
            roadmaps.map((roadmap) => (
              <Card key={roadmap.id} className="hover:border-brand-teal/40 transition-all">
                <CardContent className="p-5 space-y-3">
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                      <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-brand-teal/10 text-brand-teal">
                        {roadmap.goalType}
                      </span>
                      <h4 className="text-sm font-bold text-text-primary mt-1.5 truncate">{roadmap.title}</h4>
                      <p className="text-xs text-text-muted truncate mt-0.5">{roadmap.targetRole}</p>
                    </div>
                    <Badge variant={roadmap.status === 'COMPLETED' ? 'success' : 'primary'} size="sm">
                      {roadmap.status}
                    </Badge>
                  </div>

                  <div className="space-y-1 pt-1">
                    <div className="flex justify-between text-xs text-text-muted">
                      <span>Curriculum Progress</span>
                      <span className="font-semibold text-text-primary">{roadmap.completionPercentage}%</span>
                    </div>
                    <div className="w-full bg-bg-surface-elevated rounded-full h-1.5 overflow-hidden">
                      <div
                        className="bg-brand-teal h-1.5 rounded-full transition-all duration-300"
                        style={{ width: `${roadmap.completionPercentage}%` }}
                      ></div>
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-[11px] text-text-muted pt-2 border-t border-border-subtle">
                    <span>{roadmap.completedHours || 0} / {roadmap.estimatedHours || 0} hrs</span>
                    <span>Target: {roadmap.targetCompletionDate || 'Open'}</span>
                  </div>
                </CardContent>
              </Card>
            ))
          ) : (
            <div className="col-span-full p-8 text-center text-text-muted bg-bg-surface border border-border-default rounded-2xl">
              <p className="text-sm">No structured roadmap initialized yet.</p>
              <div className="mt-3">
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => onNavigateTab('roadmaps')}
                  className="bg-brand-teal text-white text-xs"
                >
                  Generate SDE / Full-Stack Roadmap
                </Button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
