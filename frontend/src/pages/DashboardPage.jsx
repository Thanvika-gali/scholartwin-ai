import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  fetchDashboardSummary,
  fetchDsaDashboard,
  fetchDsaRevisions,
  fetchCareerDashboardSummary,
  fetchCareerActions,
} from '../services/api';
import KnowledgeItemModal from '../components/KnowledgeItemModal';
import KnowledgeItemViewModal from '../components/KnowledgeItemViewModal';
import DocumentUploadModal from '../components/DocumentUploadModal';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import EmptyState from '../components/ui/EmptyState';
import Alert from '../components/ui/Alert';

export default function DashboardPage() {
  const { user } = useAuth();

  // Knowledge metrics state
  const [knowledgeSummary, setKnowledgeSummary] = useState({
    totalItems: 0,
    totalNotes: 0,
    totalLinks: 0,
    totalDocuments: 0,
    recentItems: [],
  });

  // DSA metrics state
  const [dsaSummary, setDsaSummary] = useState(null);
  const [dsaRevisions, setDsaRevisions] = useState({ overdue: [], dueToday: [] });

  // Career metrics state
  const [careerSummary, setCareerSummary] = useState(null);
  const [careerActions, setCareerActions] = useState([]);

  // Loading & Error states
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Quick Action Modal states
  const [noteModalOpen, setNoteModalOpen] = useState(false);
  const [noteModalType, setNoteModalType] = useState('NOTE');
  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [viewingItem, setViewingItem] = useState(null);

  const loadAllMetrics = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    const [kRes, dsaRes, revRes, careerRes, actionsRes] = await Promise.allSettled([
      fetchDashboardSummary(),
      fetchDsaDashboard(),
      fetchDsaRevisions(),
      fetchCareerDashboardSummary(),
      fetchCareerActions('PENDING'),
    ]);

    if (kRes.status === 'fulfilled') {
      setKnowledgeSummary(kRes.value || {});
    } else {
      console.warn('Dashboard knowledge summary failed to load:', kRes.reason);
    }

    if (dsaRes.status === 'fulfilled') {
      setDsaSummary(dsaRes.value || null);
    }

    if (revRes.status === 'fulfilled') {
      setDsaRevisions({
        overdue: revRes.value?.overdue || [],
        dueToday: revRes.value?.dueToday || [],
      });
    }

    if (careerRes.status === 'fulfilled') {
      setCareerSummary(careerRes.value || null);
    }

    if (actionsRes.status === 'fulfilled') {
      setCareerActions(actionsRes.value || []);
    }

    setIsLoading(false);
  }, []);

  useEffect(() => {
    loadAllMetrics();
  }, [loadAllMetrics]);

  const handleOpenNoteModal = (type) => {
    setNoteModalType(type);
    setNoteModalOpen(true);
  };

  const formatDate = (isoString) => {
    if (!isoString) return '';
    try {
      const d = new Date(isoString);
      return d.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
      });
    } catch {
      return '';
    }
  };

  const dueRevisionsTotal = (dsaRevisions.overdue.length || 0) + (dsaRevisions.dueToday.length || 0);

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-8 max-w-7xl mx-auto">
      {/* 1. Academic Command Center Hero Banner */}
      <section className="bg-bg-surface border border-border-default rounded-3xl p-6 sm:p-8 relative overflow-hidden shadow-card transition-colors">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 relative z-10">
          <div className="space-y-2.5 min-w-0">
            <div className="flex flex-wrap items-center gap-2">
              <Badge variant="teal" icon={<span className="w-1.5 h-1.5 rounded-full bg-brand-teal animate-pulse" />}>
                Academic Command Center
              </Badge>
              {dsaSummary?.currentStreakDays > 0 && (
                <Badge variant="gold" size="xs">
                  🔥 {dsaSummary.currentStreakDays}-Day Coding Streak
                </Badge>
              )}
              <span className="text-xs text-text-muted font-medium">Semester Trajectory</span>
            </div>

            <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-text-primary tracking-tight">
              Welcome back,{' '}
              <span className="text-brand-teal dark:text-brand-teal-light">
                {user?.fullName?.split(' ')[0] || 'Scholar'}
              </span>
            </h1>

            <p className="text-xs sm:text-sm text-text-secondary max-w-2xl leading-relaxed">
              Your personalized academic command center is synchronized. Review urgent spaced repetition revisions, explore your 100MB knowledge vault, and track progress toward your target technical role.
            </p>
          </div>

          {/* Quick Action Buttons */}
          <div className="flex flex-wrap items-center gap-2.5 shrink-0">
            <Button
              variant="primary"
              size="sm"
              onClick={() => setUploadModalOpen(true)}
              icon={
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                </svg>
              }
            >
              Upload Doc (100MB)
            </Button>

            <Button
              variant="secondary"
              size="sm"
              onClick={() => handleOpenNoteModal('NOTE')}
              icon={
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
              }
            >
              Add Note
            </Button>

            <Link to="/dsa">
              <Button
                variant="outline"
                size="sm"
                icon={
                  <svg className="w-4 h-4 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                  </svg>
                }
              >
                Log DSA
              </Button>
            </Link>

            <Link to="/career">
              <Button
                variant="gold"
                size="sm"
                icon={
                  <svg className="w-4 h-4 text-brand-navy" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                }
              >
                Scan Resume
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Error state alert if any */}
      {error && (
        <Alert variant="error" onClose={() => setError(null)}>
          <div className="flex items-center justify-between">
            <span>{error}</span>
            <Button variant="ghost" size="sm" onClick={loadAllMetrics}>
              Retry
            </Button>
          </div>
        </Alert>
      )}

      {/* 2. Unified Academic Command KPI Cards */}
      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Card 1: Knowledge Hub Vault */}
        <Card hover className="flex flex-col justify-between">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-text-muted uppercase tracking-wider">
                Knowledge Vault
              </span>
              <div className="w-9 h-9 rounded-xl bg-brand-teal/15 text-brand-teal flex items-center justify-center border border-brand-teal/20">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.75} d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                </svg>
              </div>
            </div>
            <div className="mt-3">
              <h3 className="text-3xl font-black text-text-primary">
                {isLoading ? '...' : (knowledgeSummary.totalItems || 0) + (knowledgeSummary.totalDocuments || 0)}
              </h3>
              <div className="flex items-center gap-2 mt-1 text-[11px] text-text-muted font-medium">
                <span>{knowledgeSummary.totalDocuments || 0} Docs</span>
                <span>•</span>
                <span>{knowledgeSummary.totalNotes || 0} Notes</span>
                <span>•</span>
                <span>{knowledgeSummary.totalLinks || 0} Links</span>
              </div>
            </div>
          </CardContent>
          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs rounded-b-2xl">
            <button
              type="button"
              onClick={() => setUploadModalOpen(true)}
              className="text-brand-teal hover:underline font-semibold cursor-pointer"
            >
              + Upload Paper
            </button>
            <Link to="/knowledge-hub" className="text-text-muted hover:text-text-primary font-medium">
              Open Vault →
            </Link>
          </div>
        </Card>

        {/* Card 2: DSA Problem Progress */}
        <Card hover className="flex flex-col justify-between">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-text-muted uppercase tracking-wider">
                DSA Mastery
              </span>
              <div className="w-9 h-9 rounded-xl bg-amber-500/15 text-amber-500 flex items-center justify-center border border-amber-500/20">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.75} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                </svg>
              </div>
            </div>
            <div className="mt-3">
              <h3 className="text-3xl font-black text-text-primary">
                {isLoading ? '...' : dsaSummary?.solvedProblems || 0}
                <span className="text-sm text-text-muted font-normal ml-1">
                  / {dsaSummary?.totalProblems || 0}
                </span>
              </h3>
              <p className="text-[11px] text-text-muted font-medium mt-1">
                Accuracy: <span className="text-brand-teal font-bold">{dsaSummary?.overallAccuracyPercent ?? 0}%</span> across logged attempts
              </p>
            </div>
          </CardContent>
          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs rounded-b-2xl">
            <span className="text-text-muted font-medium">
              {dsaSummary?.pendingRevisionsCount || 0} in Revision Queue
            </span>
            <Link to="/dsa" className="text-brand-teal hover:underline font-semibold">
              Practice →
            </Link>
          </div>
        </Card>

        {/* Card 3: Coding Practice Streak */}
        <Card hover className="flex flex-col justify-between">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-text-muted uppercase tracking-wider">
                Practice Streak
              </span>
              <div className="w-9 h-9 rounded-xl bg-rose-500/15 text-rose-500 flex items-center justify-center border border-rose-500/20">
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 .587l3.668 7.431 8.2 1.192-5.934 5.784 1.399 8.163-7.333-3.856-7.333 3.856 1.399-8.163-5.934-5.784 8.2-1.192zm0 5.702l-2.223 4.503-4.97.722 3.596 3.505-.848 4.947 4.445-2.337 4.445 2.337-.848-4.947 3.596-3.505-4.97-.722z" />
                </svg>
              </div>
            </div>
            <div className="mt-3">
              <div className="flex items-baseline gap-1.5">
                <h3 className="text-3xl font-black text-text-primary">
                  {isLoading ? '...' : dsaSummary?.currentStreakDays || 0}
                </h3>
                <span className="text-xs font-bold text-text-secondary uppercase">Days</span>
              </div>
              <p className="text-[11px] text-text-muted font-medium mt-1">
                All-time Best: <span className="text-brand-gold font-bold">{dsaSummary?.longestStreakDays || 0} days</span>
              </p>
            </div>
          </CardContent>
          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs rounded-b-2xl">
            <span className="text-text-muted font-medium">Daily Target: 1 problem</span>
            <Link to="/dsa?tab=revisions" className="text-brand-teal hover:underline font-semibold">
              Queue →
            </Link>
          </div>
        </Card>

        {/* Card 4: Career Readiness & ATS Diagnostics */}
        <Card hover className="flex flex-col justify-between">
          <CardContent className="p-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-text-muted uppercase tracking-wider">
                Career Readiness
              </span>
              <div className="w-9 h-9 rounded-xl bg-brand-gold/15 text-brand-gold flex items-center justify-center border border-brand-gold/20">
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.75} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </div>
            </div>
            <div className="mt-3">
              <h3 className="text-3xl font-black text-text-primary">
                {isLoading ? (
                  '...'
                ) : careerSummary?.primaryResumeScore != null ? (
                  `${careerSummary.primaryResumeScore}/100`
                ) : (
                  <span className="text-sm font-semibold text-text-muted">No Resume</span>
                )}
              </h3>
              <p className="text-[11px] text-text-muted font-medium mt-1 truncate">
                Target:{' '}
                <span className="text-brand-teal font-semibold">
                  {careerSummary?.targetRole || 'Software Development Engineer'}
                </span>
              </p>
            </div>
          </CardContent>
          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs rounded-b-2xl">
            <span className="text-text-muted font-medium">
              {careerSummary?.totalTrackedSkills || 0} Tracked Skills
            </span>
            <Link to="/career" className="text-brand-teal hover:underline font-semibold">
              Inspect ATS →
            </Link>
          </div>
        </Card>
      </section>

      {/* 3. Action Radar: Spaced Revisions & Career Priority Items */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left: Due DSA Spaced Revisions */}
        <Card className="flex flex-col justify-between">
          <CardHeader className="pb-3 border-b border-border-subtle flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-rose-500/15 text-rose-500 flex items-center justify-center">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div>
                <CardTitle className="text-base font-bold">Spaced Revision Queue</CardTitle>
                <CardDescription className="text-xs">
                  Algorithms due for active recall to prevent knowledge decay
                </CardDescription>
              </div>
            </div>
            {dueRevisionsTotal > 0 && (
              <Badge variant="error" size="xs">
                {dueRevisionsTotal} Due
              </Badge>
            )}
          </CardHeader>

          <CardContent className="p-4 sm:p-5 flex-1">
            {dueRevisionsTotal === 0 ? (
              <div className="py-8 text-center text-xs text-text-muted">
                <div className="w-10 h-10 rounded-full bg-status-success-bg text-status-success mx-auto flex items-center justify-center mb-2">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <p className="font-semibold text-text-primary">All caught up on revisions!</p>
                <p className="text-[11px] mt-0.5">Solve and flag new problems to schedule automated spaced reviews.</p>
                <Link to="/dsa" className="mt-3 inline-block">
                  <Button variant="outline" size="xs">
                    Browse DSA Problems
                  </Button>
                </Link>
              </div>
            ) : (
              <div className="space-y-2.5">
                {dsaRevisions.overdue.map((rev) => (
                  <div
                    key={`overdue-${rev.id}`}
                    className="p-3 rounded-xl bg-status-error-bg/30 border border-status-error-border flex items-center justify-between gap-3 text-xs"
                  >
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] font-bold px-1.5 py-0.2 rounded bg-rose-500 text-white uppercase">
                          Overdue
                        </span>
                        <span className="font-semibold text-text-primary truncate">
                          {rev.problemTitle}
                        </span>
                      </div>
                      <p className="text-[11px] text-text-muted mt-0.5">
                        {rev.primaryTopic} • Due {formatDate(rev.scheduledDate)}
                      </p>
                    </div>
                    <Link to="/dsa?tab=revisions">
                      <Button variant="primary" size="xs">
                        Review
                      </Button>
                    </Link>
                  </div>
                ))}

                {dsaRevisions.dueToday.map((rev) => (
                  <div
                    key={`today-${rev.id}`}
                    className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-between gap-3 text-xs"
                  >
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="text-[10px] font-bold px-1.5 py-0.2 rounded bg-amber-500 text-white uppercase">
                          Due Today
                        </span>
                        <span className="font-semibold text-text-primary truncate">
                          {rev.problemTitle}
                        </span>
                      </div>
                      <p className="text-[11px] text-text-muted mt-0.5">
                        {rev.primaryTopic}
                      </p>
                    </div>
                    <Link to="/dsa?tab=revisions">
                      <Button variant="secondary" size="xs">
                        Solve
                      </Button>
                    </Link>
                  </div>
                ))}
              </div>
            )}
          </CardContent>

          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs rounded-b-2xl">
            <span className="text-text-muted">Algorithm Retention Engine</span>
            <Link to="/dsa?tab=revisions" className="text-brand-teal hover:underline font-semibold">
              Open Revision Queue →
            </Link>
          </div>
        </Card>

        {/* Right: High-Priority Career Action Items */}
        <Card className="flex flex-col justify-between">
          <CardHeader className="pb-3 border-b border-border-subtle flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-brand-gold/15 text-brand-gold flex items-center justify-center">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                </svg>
              </div>
              <div>
                <CardTitle className="text-base font-bold">Career Action Plan</CardTitle>
                <CardDescription className="text-xs">
                  Prioritized steps to qualify for target technical positions
                </CardDescription>
              </div>
            </div>
            {careerActions.length > 0 && (
              <Badge variant="gold" size="xs">
                {careerActions.length} Pending
              </Badge>
            )}
          </CardHeader>

          <CardContent className="p-4 sm:p-5 flex-1">
            {careerActions.length === 0 ? (
              <div className="py-8 text-center text-xs text-text-muted">
                <p className="font-semibold text-text-primary">No pending career actions</p>
                <p className="text-[11px] mt-0.5">Upload a resume or run the Job Description matcher to generate high-impact career tasks.</p>
                <Link to="/career" className="mt-3 inline-block">
                  <Button variant="outline" size="xs">
                    Scan Resume in Career Hub
                  </Button>
                </Link>
              </div>
            ) : (
              <div className="space-y-2.5">
                {careerActions.slice(0, 3).map((act) => (
                  <div
                    key={act.id}
                    className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default flex items-center justify-between gap-3 text-xs"
                  >
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className={`text-[10px] font-bold px-1.5 py-0.2 rounded uppercase ${
                          act.priority === 'HIGH' ? 'bg-rose-500 text-white' : 'bg-brand-teal text-white'
                        }`}>
                          {act.priority || 'NORMAL'}
                        </span>
                        <span className="font-semibold text-text-primary truncate">
                          {act.title}
                        </span>
                      </div>
                      <p className="text-[11px] text-text-muted mt-0.5 truncate">
                        {act.category?.replace('_', ' ')} • Due {formatDate(act.dueDate) || 'Flexible'}
                      </p>
                    </div>
                    <Link to="/career?tab=actions">
                      <Button variant="secondary" size="xs">
                        Details
                      </Button>
                    </Link>
                  </div>
                ))}
              </div>
            )}
          </CardContent>

          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs rounded-b-2xl">
            <span className="text-text-muted">Interview Preparedness</span>
            <Link to="/career?tab=actions" className="text-brand-teal hover:underline font-semibold">
              Manage Career Plan →
            </Link>
          </div>
        </Card>
      </section>

      {/* 4. Dual Activity Streams: Recent Knowledge Resources & Recent Coding */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Left: Recent Knowledge Resources */}
        <Card className="flex flex-col justify-between">
          <CardHeader className="pb-3 border-b border-border-subtle flex items-center justify-between">
            <div>
              <CardTitle className="text-base font-bold">Recent Knowledge Resources</CardTitle>
              <CardDescription className="text-xs">
                Latest course materials, textbooks, and extracted notes
              </CardDescription>
            </div>
            <Link to="/knowledge-hub" className="text-xs text-brand-teal hover:underline font-semibold">
              View All Vault →
            </Link>
          </CardHeader>

          <CardContent className="p-4 sm:p-5 flex-1">
            {!knowledgeSummary.recentItems || knowledgeSummary.recentItems.length === 0 ? (
              <div className="py-8 text-center text-xs text-text-muted">
                <p className="font-semibold text-text-primary">Vault is empty</p>
                <p className="text-[11px] mt-0.5">Upload a PDF paper or create lecture notes to begin building your knowledge vault.</p>
                <button
                  type="button"
                  onClick={() => setUploadModalOpen(true)}
                  className="mt-3 text-brand-teal font-semibold hover:underline"
                >
                  + Upload First Document
                </button>
              </div>
            ) : (
              <div className="space-y-2.5">
                {knowledgeSummary.recentItems.slice(0, 4).map((item) => (
                  <div
                    key={item.id}
                    className="p-3 rounded-xl bg-bg-surface-elevated/60 border border-border-default flex items-center justify-between gap-3 text-xs hover:border-brand-teal/40 transition-colors"
                  >
                    <div className="min-w-0 flex items-center gap-3">
                      <div className="w-8 h-8 rounded-lg bg-brand-teal/10 text-brand-teal flex items-center justify-center shrink-0">
                        {item.type === 'DOCUMENT' ? (
                          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                          </svg>
                        ) : item.type === 'NOTE' ? (
                          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                          </svg>
                        ) : (
                          <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1" />
                          </svg>
                        )}
                      </div>
                      <div className="min-w-0">
                        <button
                          type="button"
                          onClick={() => setViewingItem(item)}
                          className="font-semibold text-text-primary hover:text-brand-teal truncate text-left block max-w-xs cursor-pointer"
                        >
                          {item.title}
                        </button>
                        <p className="text-[11px] text-text-muted mt-0.5">
                          {item.category || item.type} • {formatDate(item.createdAt)}
                        </p>
                      </div>
                    </div>
                    <Button variant="ghost" size="xs" onClick={() => setViewingItem(item)}>
                      Inspect
                    </Button>
                  </div>
                ))}
              </div>
            )}
          </CardContent>

          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs rounded-b-2xl">
            <span className="text-text-muted">Storage: MySQL 8 + PDFBox/POI</span>
            <Link to="/knowledge-hub" className="text-brand-teal hover:underline font-semibold">
              Explore All Resources →
            </Link>
          </div>
        </Card>

        {/* Right: Recent Coding Activity */}
        <Card className="flex flex-col justify-between">
          <CardHeader className="pb-3 border-b border-border-subtle flex items-center justify-between">
            <div>
              <CardTitle className="text-base font-bold">Recent Coding Activity</CardTitle>
              <CardDescription className="text-xs">
                Algorithmic problem practice and verdict history
              </CardDescription>
            </div>
            <Link to="/dsa?tab=problems" className="text-xs text-brand-teal hover:underline font-semibold">
              Problem Directory →
            </Link>
          </CardHeader>

          <CardContent className="p-4 sm:p-5 flex-1">
            {!dsaSummary?.recentProblems || dsaSummary.recentProblems.length === 0 ? (
              <div className="py-8 text-center text-xs text-text-muted">
                <p className="font-semibold text-text-primary">No problems solved yet</p>
                <p className="text-[11px] mt-0.5">Log your LeetCode or Codeforces problems to automatically build topic mastery.</p>
                <Link to="/dsa" className="mt-3 inline-block">
                  <Button variant="outline" size="xs">
                    Log First DSA Problem
                  </Button>
                </Link>
              </div>
            ) : (
              <div className="space-y-2.5">
                {dsaSummary.recentProblems.slice(0, 4).map((prob) => (
                  <div
                    key={prob.id}
                    className="p-3 rounded-xl bg-bg-surface-elevated/60 border border-border-default flex items-center justify-between gap-3 text-xs hover:border-brand-teal/40 transition-colors"
                  >
                    <div className="min-w-0 flex items-center gap-3">
                      <div className="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-500 flex items-center justify-center shrink-0">
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                        </svg>
                      </div>
                      <div className="min-w-0">
                        <Link
                          to={`/dsa?tab=problems&search=${encodeURIComponent(prob.title)}`}
                          className="font-semibold text-text-primary hover:text-brand-teal truncate block max-w-xs"
                        >
                          {prob.title}
                        </Link>
                        <p className="text-[11px] text-text-muted mt-0.5">
                          {prob.primaryTopic} • <span className={`font-semibold ${
                            prob.difficulty === 'HARD' ? 'text-rose-500' : prob.difficulty === 'MEDIUM' ? 'text-amber-500' : 'text-emerald-500'
                          }`}>{prob.difficulty}</span>
                        </p>
                      </div>
                    </div>
                    <Link to="/dsa">
                      <Button variant="secondary" size="xs">
                        Solve
                      </Button>
                    </Link>
                  </div>
                ))}
              </div>
            )}
          </CardContent>

          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs rounded-b-2xl">
            <span className="text-text-muted">Topics Covered: {dsaSummary?.topicsCoveredCount || 0}/20</span>
            <Link to="/dsa?tab=topics" className="text-brand-teal hover:underline font-semibold">
              Topic Mastery →
            </Link>
          </div>
        </Card>
      </section>

      {/* 5. AI Twin Contextual RAG Assistance Banner */}
      <section className="p-6 rounded-3xl bg-gradient-to-r from-brand-navy via-brand-navy-card to-brand-navy-deep border border-brand-teal/40 text-white relative overflow-hidden shadow-card">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-5 relative z-10">
          <div className="space-y-1.5 max-w-2xl">
            <div className="inline-flex items-center gap-2 px-2.5 py-0.5 rounded-full bg-brand-teal/20 text-brand-gold text-xs font-semibold">
              <span className="w-1.5 h-1.5 rounded-full bg-brand-teal animate-pulse" />
              Retrieval-Augmented Curriculum Co-pilot
            </div>
            <h3 className="text-xl sm:text-2xl font-bold tracking-tight text-white">
              Ground your study sessions in verified academic context
            </h3>
            <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
              Ask deep questions over your uploaded textbooks, lecture transcripts, and coding patterns without hallucinations. Citations link directly back to extracted page numbers and code snippets.
            </p>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            <Link to="/knowledge-hub">
              <Button variant="gold" size="md">
                Launch Vault RAG Q&A
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Modals */}
      <DocumentUploadModal
        isOpen={uploadModalOpen}
        onClose={() => setUploadModalOpen(false)}
        onUploadSuccess={loadAllMetrics}
      />

      <KnowledgeItemModal
        isOpen={noteModalOpen}
        onClose={() => setNoteModalOpen(false)}
        initialType={noteModalType}
        onSaved={loadAllMetrics}
      />

      <KnowledgeItemViewModal
        isOpen={Boolean(viewingItem)}
        onClose={() => setViewingItem(null)}
        item={viewingItem}
        onItemUpdated={loadAllMetrics}
        onItemDeleted={loadAllMetrics}
      />
    </div>
  );
}
