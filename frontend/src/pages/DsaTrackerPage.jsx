import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';
import Badge from '../components/ui/Badge';
import EmptyState from '../components/ui/EmptyState';
import AddEditProblemModal from '../components/dsa/AddEditProblemModal';
import ProblemDetailModal from '../components/dsa/ProblemDetailModal';
import LogAttemptModal from '../components/dsa/LogAttemptModal';
import ScheduleRevisionModal from '../components/dsa/ScheduleRevisionModal';
import { DifficultyDonutChart, ActivityTimelineBarChart } from '../components/dsa/DsaAnalyticsCharts';
import {
  fetchDsaDashboard,
  fetchDsaProblems,
  fetchDsaProblem,
  createDsaProblem,
  updateDsaProblem,
  deleteDsaProblem,
  fetchDsaTopics,
  fetchDsaTopicProblems,
  logDsaAttempt,
  fetchDsaRevisions,
  scheduleDsaRevision,
  updateDsaRevision,
  deleteDsaRevision,
  fetchDsaDifficultyAnalytics,
  fetchDsaActivityTimeline,
} from '../services/api';
import {
  DSA_TOPICS,
  DSA_PLATFORMS,
  DSA_DIFFICULTIES,
  DSA_STATUSES,
} from '../constants/dsaConstants';

export default function DsaTrackerPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const currentTab = searchParams.get('tab') || 'dashboard';

  const setTab = (tab) => {
    setSearchParams({ tab });
  };

  // Global State
  const [dashboardData, setDashboardData] = useState(null);
  const [loadingDashboard, setLoadingDashboard] = useState(true);

  // Problems List State
  const [problemsPage, setProblemsPage] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 });
  const [loadingProblems, setLoadingProblems] = useState(false);
  const [problemFilters, setProblemFilters] = useState({
    search: '',
    topic: '',
    difficulty: '',
    platform: '',
    status: '',
    revisionRequired: '',
    page: 0,
    size: 15,
  });

  // Topics State
  const [topics, setTopics] = useState([]);
  const [loadingTopics, setLoadingTopics] = useState(false);
  const [selectedTopic, setSelectedTopic] = useState(null);
  const [topicProblems, setTopicProblems] = useState([]);
  const [loadingTopicProblems, setLoadingTopicProblems] = useState(false);

  // Revisions State
  const [revisionsQueue, setRevisionsQueue] = useState({
    overdue: [],
    dueToday: [],
    upcoming: [],
    completed: [],
    totalPendingCount: 0,
    overdueCount: 0,
  });
  const [loadingRevisions, setLoadingRevisions] = useState(false);
  const [revisionFilter, setRevisionFilter] = useState('pending'); // 'pending' | 'overdue' | 'dueToday' | 'upcoming' | 'completed'

  // Analytics State
  const [difficultyAnalytics, setDifficultyAnalytics] = useState(null);
  const [activityTimeline, setActivityTimeline] = useState([]);
  const [loadingAnalytics, setLoadingAnalytics] = useState(false);

  // Modals
  const [isAddEditOpen, setIsAddEditOpen] = useState(false);
  const [editingProblem, setEditingProblem] = useState(null);
  const [isSavingProblem, setIsSavingProblem] = useState(false);

  const [isDetailOpen, setIsDetailOpen] = useState(false);
  const [activeProblemDetail, setActiveProblemDetail] = useState(null);
  const [loadingDetail, setLoadingDetail] = useState(false);

  const [isAttemptOpen, setIsAttemptOpen] = useState(false);
  const [targetProblemForAttempt, setTargetProblemForAttempt] = useState(null);
  const [isSavingAttempt, setIsSavingAttempt] = useState(false);

  const [isRevisionOpen, setIsRevisionOpen] = useState(false);
  const [targetProblemForRevision, setTargetProblemForRevision] = useState(null);
  const [isSavingRevision, setIsSavingRevision] = useState(false);

  const [feedbackMessage, setFeedbackMessage] = useState(null);

  const showFeedback = (text, type = 'success') => {
    setFeedbackMessage({ text, type });
    setTimeout(() => setFeedbackMessage(null), 4000);
  };

  // Load Dashboard Summary
  const loadDashboard = useCallback(async () => {
    setLoadingDashboard(true);
    try {
      const data = await fetchDsaDashboard();
      setDashboardData(data);
    } catch (err) {
      console.error('Failed to load DSA dashboard summary', err);
    } finally {
      setLoadingDashboard(false);
    }
  }, []);

  // Load Problems
  const loadProblems = useCallback(async () => {
    setLoadingProblems(true);
    try {
      const data = await fetchDsaProblems(problemFilters);
      setProblemsPage(data || { content: [], totalElements: 0, totalPages: 0, number: 0 });
    } catch (err) {
      console.error('Failed to load DSA problems', err);
    } finally {
      setLoadingProblems(false);
    }
  }, [problemFilters]);

  // Load Topics
  const loadTopics = useCallback(async () => {
    setLoadingTopics(true);
    try {
      const data = await fetchDsaTopics();
      setTopics(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Failed to load DSA topics', err);
    } finally {
      setLoadingTopics(false);
    }
  }, []);

  // Load Revisions
  const loadRevisions = useCallback(async () => {
    setLoadingRevisions(true);
    try {
      const data = await fetchDsaRevisions();
      setRevisionsQueue(
        data || {
          overdue: [],
          dueToday: [],
          upcoming: [],
          completed: [],
          totalPendingCount: 0,
          overdueCount: 0,
        }
      );
    } catch (err) {
      console.error('Failed to load DSA revisions queue', err);
    } finally {
      setLoadingRevisions(false);
    }
  }, []);

  // Load Analytics
  const loadAnalytics = useCallback(async () => {
    setLoadingAnalytics(true);
    try {
      const [diffData, actData] = await Promise.all([
        fetchDsaDifficultyAnalytics().catch(() => null),
        fetchDsaActivityTimeline(14).catch(() => []),
      ]);
      setDifficultyAnalytics(diffData);
      setActivityTimeline(Array.isArray(actData) ? actData : []);
    } catch (err) {
      console.error('Failed to load DSA analytics', err);
    } finally {
      setLoadingAnalytics(false);
    }
  }, []);

  // Sync tab loading
  useEffect(() => {
    if (currentTab === 'dashboard') loadDashboard();
    if (currentTab === 'problems') loadProblems();
    if (currentTab === 'topics') loadTopics();
    if (currentTab === 'revisions') loadRevisions();
    if (currentTab === 'analytics') loadAnalytics();
  }, [currentTab, loadDashboard, loadProblems, loadTopics, loadRevisions, loadAnalytics]);

  // Topic Drilldown
  const handleSelectTopic = async (topicName) => {
    setSelectedTopic(topicName);
    setLoadingTopicProblems(true);
    try {
      const proms = await fetchDsaTopicProblems(topicName);
      setTopicProblems(Array.isArray(proms) ? proms : []);
    } catch (err) {
      console.error('Failed to load topic problems', err);
      setTopicProblems([]);
    } finally {
      setLoadingTopicProblems(false);
    }
  };

  // Open Problem Detail Modal
  const openProblemDetail = async (id) => {
    setIsDetailOpen(true);
    setLoadingDetail(true);
    try {
      const detail = await fetchDsaProblem(id);
      setActiveProblemDetail(detail);
    } catch (err) {
      console.error('Failed to load problem detail', err);
    } finally {
      setLoadingDetail(false);
    }
  };

  // Save Problem (Create or Update)
  const handleSaveProblem = async (problemData) => {
    setIsSavingProblem(true);
    try {
      if (editingProblem && editingProblem.id) {
        await updateDsaProblem(editingProblem.id, problemData);
        showFeedback('Problem updated successfully');
      } else {
        await createDsaProblem(problemData);
        showFeedback('New problem tracked in library');
      }
      setIsAddEditOpen(false);
      setEditingProblem(null);
      loadDashboard();
      if (currentTab === 'problems') loadProblems();
      if (currentTab === 'topics' && selectedTopic) handleSelectTopic(selectedTopic);
    } catch (err) {
      alert(err.message || 'Failed to save problem');
    } finally {
      setIsSavingProblem(false);
    }
  };

  // Delete Problem
  const handleDeleteProblem = async (problem) => {
    if (!window.confirm(`Are you sure you want to delete "${problem.title}"?`)) return;
    try {
      await deleteDsaProblem(problem.id);
      showFeedback('Problem removed from tracker');
      setIsDetailOpen(false);
      loadDashboard();
      if (currentTab === 'problems') loadProblems();
      if (currentTab === 'topics' && selectedTopic) handleSelectTopic(selectedTopic);
    } catch (err) {
      alert(err.message || 'Failed to delete problem');
    }
  };

  // Save Practice Attempt
  const handleSaveAttempt = async (attemptData) => {
    if (!targetProblemForAttempt) return;
    setIsSavingAttempt(true);
    try {
      await logDsaAttempt(targetProblemForAttempt.id, attemptData);
      showFeedback('Practice attempt recorded');
      setIsAttemptOpen(false);
      loadDashboard();
      if (currentTab === 'problems') loadProblems();
      if (isDetailOpen && activeProblemDetail?.problem?.id === targetProblemForAttempt.id) {
        openProblemDetail(targetProblemForAttempt.id);
      }
    } catch (err) {
      alert(err.message || 'Failed to log attempt');
    } finally {
      setIsSavingAttempt(false);
    }
  };

  // Schedule Revision
  const handleSaveRevision = async (revData) => {
    if (!targetProblemForRevision) return;
    setIsSavingRevision(true);
    try {
      await scheduleDsaRevision(targetProblemForRevision.id, revData);
      showFeedback('Spaced revision scheduled in queue');
      setIsRevisionOpen(false);
      loadDashboard();
      if (currentTab === 'revisions') loadRevisions();
      if (isDetailOpen && activeProblemDetail?.problem?.id === targetProblemForRevision.id) {
        openProblemDetail(targetProblemForRevision.id);
      }
    } catch (err) {
      alert(err.message || 'Failed to schedule revision');
    } finally {
      setIsSavingRevision(false);
    }
  };

  // Complete a revision
  const handleCompleteRevision = async (revisionId) => {
    try {
      await updateDsaRevision(revisionId, {
        status: 'COMPLETED',
        revisionNotes: 'Completed spaced review',
      });
      showFeedback('Revision marked as completed');
      loadDashboard();
      if (currentTab === 'revisions') loadRevisions();
      if (isDetailOpen && activeProblemDetail?.problem?.id) {
        openProblemDetail(activeProblemDetail.problem.id);
      }
    } catch (err) {
      alert(err.message || 'Failed to complete revision');
    }
  };

  // Delete a revision
  const handleDeleteRevision = async (revisionId) => {
    try {
      await deleteDsaRevision(revisionId);
      showFeedback('Revision removed from queue');
      loadDashboard();
      if (currentTab === 'revisions') loadRevisions();
      if (isDetailOpen && activeProblemDetail?.problem?.id) {
        openProblemDetail(activeProblemDetail.problem.id);
      }
    } catch (err) {
      alert(err.message || 'Failed to delete revision');
    }
  };

  // Render Difficulty Pill
  const renderDifficultyBadge = (diff) => {
    const map = {
      EASY: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
      MEDIUM: 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30',
      HARD: 'bg-rose-500/15 text-rose-600 dark:text-rose-400 border-rose-500/30',
    };
    return (
      <span className={`text-[11px] font-semibold px-2 py-0.5 rounded-md border ${map[diff] || ''}`}>
        {diff}
      </span>
    );
  };

  // Render Status Pill
  const renderStatusBadge = (status) => {
    const map = {
      NOT_STARTED: 'bg-bg-surface-elevated text-text-muted border-border-default',
      ATTEMPTED: 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30',
      SOLVED: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
    };
    return (
      <span className={`text-[11px] font-semibold px-2 py-0.5 rounded-md border ${map[status] || ''}`}>
        {status?.replace('_', ' ')}
      </span>
    );
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-6 max-w-7xl mx-auto">
      {/* Toast Feedback */}
      {feedbackMessage && (
        <div
          role="status"
          aria-live="polite"
          className="fixed bottom-6 right-6 z-50 bg-brand-navy border border-brand-teal text-white text-xs sm:text-sm font-medium py-3 px-4 rounded-xl shadow-modal flex items-center gap-2 animate-in fade-in slide-in-from-bottom-2 duration-200"
        >
          <span className="w-2 h-2 rounded-full bg-brand-teal animate-pulse" />
          {feedbackMessage.text}
        </div>
      )}

      {/* Top Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-border-default pb-5">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-xl bg-brand-teal/15 text-brand-teal flex items-center justify-center border border-brand-teal/20">
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
              </svg>
            </div>
            <h1 className="text-xl sm:text-2xl font-bold text-text-primary tracking-tight">
              DSA & Coding Progress Tracker
            </h1>
          </div>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Algorithmic mastery across 20 canonical CS topics, daily practice streaks, and spaced repetition revisions.
          </p>
        </div>

        <div className="flex items-center gap-2.5 shrink-0">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => {
              loadDashboard();
              if (currentTab === 'problems') loadProblems();
              if (currentTab === 'topics') loadTopics();
              if (currentTab === 'revisions') loadRevisions();
              if (currentTab === 'analytics') loadAnalytics();
            }}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => {
              setEditingProblem(null);
              setIsAddEditOpen(true);
            }}
            icon={
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 4v16m8-8H4" />
              </svg>
            }
          >
            Add Problem
          </Button>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="flex border-b border-border-default gap-2 overflow-x-auto pb-px">
        {[
          { id: 'dashboard', label: 'Dashboard & Streaks' },
          { id: 'problems', label: 'All Problems' },
          { id: 'topics', label: 'Topic Mastery' },
          {
            id: 'revisions',
            label: 'Revision Queue',
            badge: revisionsQueue?.totalPendingCount > 0 ? revisionsQueue.totalPendingCount : null,
          },
          { id: 'analytics', label: 'Visual Analytics' },
        ].map((tab) => {
          const isActive = currentTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-2.5 text-xs sm:text-sm font-semibold rounded-t-xl transition-all cursor-pointer whitespace-nowrap ${
                isActive
                  ? 'bg-bg-surface text-brand-teal border-t-2 border-t-brand-teal border-x border-border-default shadow-xs -mb-px font-bold'
                  : 'text-text-muted hover:text-text-primary hover:bg-bg-surface-elevated/40'
              }`}
            >
              <span>{tab.label}</span>
              {tab.badge && (
                <span className="text-[10px] font-bold px-1.5 py-0.2 rounded-full bg-brand-gold text-brand-navy">
                  {tab.badge}
                </span>
              )}
            </button>
          );
        })}
      </div>

      {/* =========================================================================
          TAB 1: DASHBOARD & STREAKS
         ========================================================================= */}
      {currentTab === 'dashboard' && (
        <div className="space-y-6">
          {/* Key Metric Banner */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {/* Total Problems Tracked */}
            <Card hover>
              <CardContent className="p-4 sm:p-5 flex items-center justify-between">
                <div>
                  <span className="text-xs font-bold text-text-muted uppercase tracking-wider block">
                    Total Tracked
                  </span>
                  <span className="text-2xl font-black text-text-primary mt-1 block">
                    {dashboardData?.totalProblems || 0}
                  </span>
                  <span className="text-[11px] text-text-muted mt-0.5 block">
                    {dashboardData?.solvedProblems || 0} solved ({dashboardData?.totalProblems ? Math.round((dashboardData.solvedProblems / dashboardData.totalProblems) * 100) : 0}%)
                  </span>
                </div>
                <div className="w-10 h-10 rounded-xl bg-brand-teal/15 text-brand-teal flex items-center justify-center border border-brand-teal/20">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
              </CardContent>
            </Card>

            {/* Current Streak */}
            <Card hover>
              <CardContent className="p-4 sm:p-5 flex items-center justify-between">
                <div>
                  <span className="text-xs font-bold text-text-muted uppercase tracking-wider block">
                    Practice Streak
                  </span>
                  <div className="flex items-baseline gap-1 mt-1">
                    <span className="text-2xl font-black text-amber-500">
                      {dashboardData?.currentStreakDays || 0}
                    </span>
                    <span className="text-xs font-bold text-text-secondary uppercase">days</span>
                  </div>
                  <span className="text-[11px] text-text-muted mt-0.5 block">
                    Best: {dashboardData?.longestStreakDays || 0} days
                  </span>
                </div>
                <div className="w-10 h-10 rounded-xl bg-amber-500/15 text-amber-500 flex items-center justify-center border border-amber-500/20">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 .587l3.668 7.431 8.2 1.192-5.934 5.784 1.399 8.163-7.333-3.856-7.333 3.856 1.399-8.163-5.934-5.784 8.2-1.192zm0 5.702l-2.223 4.503-4.97.722 3.596 3.505-.848 4.947 4.445-2.337 4.445 2.337-.848-4.947 3.596-3.505-4.97-.722z" />
                  </svg>
                </div>
              </CardContent>
            </Card>

            {/* Overall Accuracy */}
            <Card hover>
              <CardContent className="p-4 sm:p-5 flex items-center justify-between">
                <div>
                  <span className="text-xs font-bold text-text-muted uppercase tracking-wider block">
                    Overall Accuracy
                  </span>
                  <span className="text-2xl font-black text-brand-teal mt-1 block">
                    {dashboardData?.overallAccuracyPercent !== undefined
                      ? `${dashboardData.overallAccuracyPercent}%`
                      : '0%'}
                  </span>
                  <span className="text-[11px] text-text-muted mt-0.5 block">
                    Across logged attempts
                  </span>
                </div>
                <div className="w-10 h-10 rounded-xl bg-brand-teal/15 text-brand-teal flex items-center justify-center border border-brand-teal/20">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
              </CardContent>
            </Card>

            {/* Revisions Queued */}
            <Card hover>
              <CardContent className="p-4 sm:p-5 flex items-center justify-between">
                <div>
                  <span className="text-xs font-bold text-text-muted uppercase tracking-wider block">
                    Due Revisions
                  </span>
                  <span className="text-2xl font-black text-brand-gold mt-1 block">
                    {dashboardData?.pendingRevisionsCount || 0}
                  </span>
                  <span className="text-[11px] text-rose-500 font-semibold mt-0.5 block">
                    {dashboardData?.overdueRevisionsCount || 0} overdue
                  </span>
                </div>
                <div className="w-10 h-10 rounded-xl bg-brand-gold/15 text-brand-gold flex items-center justify-center border border-brand-gold/20">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Difficulty Tier Progress Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-bg-surface border border-emerald-500/30 rounded-2xl p-4 flex flex-col justify-between shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-emerald-600 dark:text-emerald-400 uppercase tracking-wider">
                  Easy Problems
                </span>
                <span className="text-sm font-bold text-text-primary">
                  {dashboardData?.easySolved || 0} / {dashboardData?.easyTotal || 0}
                </span>
              </div>
              <div className="w-full h-2 rounded-full bg-emerald-500/15 overflow-hidden">
                <div
                  style={{
                    width: `${dashboardData?.easyTotal ? (dashboardData.easySolved / dashboardData.easyTotal) * 100 : 0}%`,
                  }}
                  className="h-full bg-emerald-500 rounded-full transition-all"
                />
              </div>
            </div>

            <div className="bg-bg-surface border border-amber-500/30 rounded-2xl p-4 flex flex-col justify-between shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-amber-600 dark:text-amber-400 uppercase tracking-wider">
                  Medium Problems
                </span>
                <span className="text-sm font-bold text-text-primary">
                  {dashboardData?.mediumSolved || 0} / {dashboardData?.mediumTotal || 0}
                </span>
              </div>
              <div className="w-full h-2 rounded-full bg-amber-500/15 overflow-hidden">
                <div
                  style={{
                    width: `${dashboardData?.mediumTotal ? (dashboardData.mediumSolved / dashboardData.mediumTotal) * 100 : 0}%`,
                  }}
                  className="h-full bg-amber-500 rounded-full transition-all"
                />
              </div>
            </div>

            <div className="bg-bg-surface border border-rose-500/30 rounded-2xl p-4 flex flex-col justify-between shadow-xs">
              <div className="flex items-center justify-between mb-2">
                <span className="text-xs font-bold text-rose-600 dark:text-rose-400 uppercase tracking-wider">
                  Hard Problems
                </span>
                <span className="text-sm font-bold text-text-primary">
                  {dashboardData?.hardSolved || 0} / {dashboardData?.hardTotal || 0}
                </span>
              </div>
              <div className="w-full h-2 rounded-full bg-rose-500/15 overflow-hidden">
                <div
                  style={{
                    width: `${dashboardData?.hardTotal ? (dashboardData.hardSolved / dashboardData.hardTotal) * 100 : 0}%`,
                  }}
                  className="h-full bg-rose-500 rounded-full transition-all"
                />
              </div>
            </div>
          </div>

          {/* Quick Problem Launch & Practice Radar */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Recent Problems Card */}
            <Card>
              <CardHeader className="flex items-center justify-between pb-3">
                <CardTitle className="text-base font-bold">Recent Tracked Problems</CardTitle>
                <button
                  onClick={() => setTab('problems')}
                  className="text-xs font-semibold text-brand-teal hover:underline cursor-pointer"
                >
                  View All Problems →
                </button>
              </CardHeader>
              <CardContent className="p-4 pt-1">
                {!dashboardData?.recentProblems || dashboardData.recentProblems.length === 0 ? (
                  <div className="py-8 text-center text-xs text-text-muted">
                    No problems logged yet. Click &quot;Add Problem&quot; to begin tracking.
                  </div>
                ) : (
                  <div className="divide-y divide-border-subtle">
                    {dashboardData.recentProblems.slice(0, 5).map((p) => (
                      <div
                        key={p.id}
                        className="py-2.5 flex items-center justify-between gap-3 hover:bg-bg-surface-hover px-2 rounded-lg transition-colors"
                      >
                        <div className="min-w-0">
                          <button
                            type="button"
                            onClick={() => openProblemDetail(p.id)}
                            className="text-xs font-bold text-text-primary hover:text-brand-teal truncate block text-left cursor-pointer"
                          >
                            {p.title}
                          </button>
                          <div className="flex items-center gap-2 mt-0.5">
                            <span className="text-[10px] text-text-muted">{p.primaryTopic}</span>
                            <span>•</span>
                            <span className="text-[10px] text-text-muted">{p.platform}</span>
                          </div>
                        </div>
                        <div className="flex items-center gap-2 shrink-0">
                          {renderDifficultyBadge(p.difficulty)}
                          {renderStatusBadge(p.status)}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Practice Recommendations & Spaced Repetition Alert */}
            <Card>
              <CardHeader className="pb-3">
                <CardTitle className="text-base font-bold">Spaced Repetition Radar</CardTitle>
                <CardDescription className="text-xs">
                  Active recall triggers based on past attempts and review dates
                </CardDescription>
              </CardHeader>
              <CardContent className="p-4 pt-1">
                {dashboardData?.overdueRevisionsCount > 0 ? (
                  <div className="p-4 rounded-xl bg-status-error-bg/30 border border-status-error-border space-y-2">
                    <div className="flex items-center gap-2 text-status-error-text font-bold text-xs">
                      <span className="w-2 h-2 rounded-full bg-rose-500 animate-pulse" />
                      {dashboardData.overdueRevisionsCount} Overdue Revision Tasks
                    </div>
                    <p className="text-[11px] text-text-muted leading-relaxed">
                      You have spaced repetition reviews that were scheduled for earlier dates. Completing them reinforces long-term algorithm retention for coding interviews.
                    </p>
                    <Button
                      variant="primary"
                      size="xs"
                      onClick={() => setTab('revisions')}
                    >
                      Open Revision Queue
                    </Button>
                  </div>
                ) : (
                  <div className="py-8 text-center text-xs text-text-muted space-y-2">
                    <p className="font-semibold text-text-primary">No overdue review alerts</p>
                    <p className="text-[11px]">Flag tricky LeetCode problems with &quot;Revision Required&quot; to automate your recall schedule.</p>
                    <Button
                      variant="outline"
                      size="xs"
                      onClick={() => setTab('problems')}
                    >
                      Browse Problems
                    </Button>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      )}

      {/* =========================================================================
          TAB 2: ALL PROBLEMS (RESPONSIVE DUAL PRESENTATION)
         ========================================================================= */}
      {currentTab === 'problems' && (
        <div className="space-y-4">
          {/* Filter Bar */}
          <Card>
            <CardContent className="p-4 space-y-3">
              {/* Search + Topic + Platform + Difficulty */}
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
                {/* Search */}
                <div className="relative">
                  <input
                    type="text"
                    placeholder="Search problems..."
                    value={problemFilters.search}
                    onChange={(e) =>
                      setProblemFilters({ ...problemFilters, search: e.target.value, page: 0 })
                    }
                    className="w-full bg-bg-surface-elevated border border-border-default rounded-xl pl-9 pr-3 py-2 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
                  />
                  <svg
                    className="w-4 h-4 text-text-muted absolute left-3 top-2.5"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                </div>

                {/* Topic */}
                <div>
                  <select
                    value={problemFilters.topic}
                    onChange={(e) =>
                      setProblemFilters({ ...problemFilters, topic: e.target.value, page: 0 })
                    }
                    className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3 py-2 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
                  >
                    <option value="">All Topics (20 Topics)</option>
                    {DSA_TOPICS.map((topic) => (
                      <option key={topic} value={topic}>
                        {topic}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Difficulty */}
                <div>
                  <select
                    value={problemFilters.difficulty}
                    onChange={(e) =>
                      setProblemFilters({ ...problemFilters, difficulty: e.target.value, page: 0 })
                    }
                    className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3 py-2 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
                  >
                    <option value="">All Difficulties</option>
                    {DSA_DIFFICULTIES.map((d) => (
                      <option key={d.value} value={d.value}>
                        {d.label}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Status */}
                <div>
                  <select
                    value={problemFilters.status}
                    onChange={(e) =>
                      setProblemFilters({ ...problemFilters, status: e.target.value, page: 0 })
                    }
                    className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3 py-2 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
                  >
                    <option value="">All Statuses</option>
                    {DSA_STATUSES.map((s) => (
                      <option key={s.value} value={s.value}>
                        {s.label}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="flex items-center justify-between pt-2 border-t border-border-subtle text-xs text-text-muted">
                <label className="flex items-center gap-1.5 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={problemFilters.revisionRequired === true}
                    onChange={(e) =>
                      setProblemFilters({
                        ...problemFilters,
                        revisionRequired: e.target.checked ? true : '',
                        page: 0,
                      })
                    }
                    className="rounded border-border-default text-brand-teal focus:ring-brand-teal cursor-pointer"
                  />
                  <span>Revision Required Only</span>
                </label>

                <span>
                  Showing {problemsPage.content?.length || 0} of {problemsPage.totalElements || 0} problems
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Desktop Table View (>= md:) */}
          <div className="hidden md:block">
            <Card>
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-bg-surface-elevated/70 border-b border-border-default text-text-secondary uppercase tracking-wider font-semibold">
                    <tr>
                      <th className="py-3 px-4">Status</th>
                      <th className="py-3 px-4">Title</th>
                      <th className="py-3 px-4">Difficulty</th>
                      <th className="py-3 px-4">Topic</th>
                      <th className="py-3 px-4">Platform</th>
                      <th className="py-3 px-4">Attempts / Acc</th>
                      <th className="py-3 px-4 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border-subtle">
                    {loadingProblems ? (
                      <tr>
                        <td colSpan={7} className="py-12 text-center text-text-muted">
                          Loading problems...
                        </td>
                      </tr>
                    ) : problemsPage.content?.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="py-12 text-center">
                          <EmptyState
                            title="No problems found"
                            description="Try clearing your filters or track a new DSA problem."
                            actionLabel="Add Problem"
                            onAction={() => {
                              setEditingProblem(null);
                              setIsAddEditOpen(true);
                            }}
                          />
                        </td>
                      </tr>
                    ) : (
                      problemsPage.content.map((p) => (
                        <tr key={p.id} className="hover:bg-bg-surface-hover transition-colors">
                          <td className="py-3.5 px-4 whitespace-nowrap">
                            {renderStatusBadge(p.status)}
                          </td>
                          <td className="py-3.5 px-4 font-semibold text-text-primary">
                            <button
                              type="button"
                              onClick={() => openProblemDetail(p.id)}
                              className="hover:text-brand-teal text-left cursor-pointer truncate max-w-xs block font-bold"
                            >
                              {p.title}
                            </button>
                          </td>
                          <td className="py-3.5 px-4 whitespace-nowrap">
                            {renderDifficultyBadge(p.difficulty)}
                          </td>
                          <td className="py-3.5 px-4 whitespace-nowrap text-text-secondary">
                            {p.primaryTopic}
                          </td>
                          <td className="py-3.5 px-4 whitespace-nowrap text-text-muted">
                            {p.platform}
                          </td>
                          <td className="py-3.5 px-4 whitespace-nowrap text-text-secondary font-mono text-[11px]">
                            {p.attemptCount || 0} att. ({p.successRatePercent || 0}%)
                          </td>
                          <td className="py-3.5 px-4 text-right whitespace-nowrap">
                            <div className="flex items-center justify-end gap-1.5">
                              <Button
                                variant="outline"
                                size="xs"
                                onClick={() => {
                                  setTargetProblemForAttempt(p);
                                  setIsAttemptOpen(true);
                                }}
                              >
                                Log
                              </Button>
                              <Button
                                variant="teal-subtle"
                                size="xs"
                                onClick={() => {
                                  setTargetProblemForRevision(p);
                                  setIsRevisionOpen(true);
                                }}
                              >
                                Review
                              </Button>
                              <Button
                                variant="ghost"
                                size="xs"
                                onClick={() => openProblemDetail(p.id)}
                              >
                                Details
                              </Button>
                            </div>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </Card>
          </div>

          {/* Mobile Card Layout (< md:) */}
          <div className="md:hidden space-y-3">
            {loadingProblems ? (
              <div className="py-8 text-center text-xs text-text-muted">Loading problems...</div>
            ) : problemsPage.content?.length === 0 ? (
              <EmptyState
                title="No problems found"
                description="Try clearing your filters or add a new DSA problem."
                actionLabel="Add Problem"
                onAction={() => {
                  setEditingProblem(null);
                  setIsAddEditOpen(true);
                }}
              />
            ) : (
              problemsPage.content.map((p) => (
                <div
                  key={p.id}
                  className="p-4 rounded-2xl bg-bg-surface border border-border-default shadow-card space-y-2.5"
                >
                  <div className="flex items-start justify-between gap-2">
                    <button
                      type="button"
                      onClick={() => openProblemDetail(p.id)}
                      className="text-sm font-bold text-text-primary hover:text-brand-teal text-left cursor-pointer truncate flex-1"
                    >
                      {p.title}
                    </button>
                    {renderDifficultyBadge(p.difficulty)}
                  </div>

                  <div className="flex items-center gap-2 flex-wrap text-[11px]">
                    {renderStatusBadge(p.status)}
                    <span className="px-2 py-0.5 rounded-md bg-bg-surface-elevated text-text-secondary border border-border-subtle font-medium">
                      {p.primaryTopic}
                    </span>
                    <span className="text-text-muted">{p.platform}</span>
                    <span className="text-text-muted font-mono ml-auto">
                      {p.attemptCount || 0} att. ({p.successRatePercent || 0}%)
                    </span>
                  </div>

                  <div className="flex items-center justify-end gap-2 pt-2 border-t border-border-subtle">
                    <Button
                      variant="outline"
                      size="xs"
                      onClick={() => {
                        setTargetProblemForAttempt(p);
                        setIsAttemptOpen(true);
                      }}
                    >
                      Log Attempt
                    </Button>
                    <Button
                      variant="primary"
                      size="xs"
                      onClick={() => openProblemDetail(p.id)}
                    >
                      Details →
                    </Button>
                  </div>
                </div>
              ))
            )}
          </div>

          {/* Pagination Controls */}
          {problemsPage.totalPages > 1 && (
            <div className="flex items-center justify-between p-3 bg-bg-surface border border-border-default rounded-xl text-xs">
              <Button
                variant="outline"
                size="xs"
                disabled={problemsPage.number === 0}
                onClick={() =>
                  setProblemFilters({ ...problemFilters, page: problemsPage.number - 1 })
                }
              >
                Previous Page
              </Button>
              <span className="text-text-muted">
                Page {problemsPage.number + 1} of {problemsPage.totalPages}
              </span>
              <Button
                variant="outline"
                size="xs"
                disabled={problemsPage.number + 1 >= problemsPage.totalPages}
                onClick={() =>
                  setProblemFilters({ ...problemFilters, page: problemsPage.number + 1 })
                }
              >
                Next Page
              </Button>
            </div>
          )}
        </div>
      )}

      {/* =========================================================================
          TAB 3: TOPIC MASTERY
         ========================================================================= */}
      {currentTab === 'topics' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
            {loadingTopics ? (
              <div className="col-span-full py-12 text-center text-text-muted text-xs">
                Loading 20 CS topics...
              </div>
            ) : topics.length === 0 ? (
              <div className="col-span-full py-12 text-center text-text-muted text-xs">
                No topics loaded yet.
              </div>
            ) : (
              topics.map((t) => {
                const completion = t.completionPercentage || 0;
                const isSelected = selectedTopic === t.topicName;
                return (
                  <div
                    key={t.topicName}
                    onClick={() => handleSelectTopic(t.topicName)}
                    className={`p-4 rounded-2xl border transition-all cursor-pointer shadow-card hover:shadow-card-hover ${
                      isSelected
                        ? 'bg-brand-teal/10 border-brand-teal ring-2 ring-brand-teal/20'
                        : 'bg-bg-surface border-border-default hover:border-brand-teal/40'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <h4 className="text-xs sm:text-sm font-bold text-text-primary truncate">
                        {t.topicName}
                      </h4>
                      <span className="text-xs font-bold text-brand-teal shrink-0">
                        {completion}%
                      </span>
                    </div>

                    <div className="mt-3 w-full h-2 rounded-full bg-bg-surface-elevated overflow-hidden border border-border-subtle">
                      <div
                        style={{ width: `${completion}%` }}
                        className="h-full bg-brand-teal rounded-full transition-all duration-300"
                      />
                    </div>

                    <div className="flex items-center justify-between text-[11px] text-text-muted mt-2.5">
                      <span>
                        {t.solvedProblems} / {t.totalProblems} solved
                      </span>
                      <span>{t.attemptedProblems > 0 ? `${t.attemptedProblems} att.` : ''}</span>
                    </div>
                  </div>
                );
              })
            )}
          </div>

          {/* Drilldown Section for Selected Topic */}
          {selectedTopic && (
            <Card>
              <CardHeader className="flex items-center justify-between">
                <div>
                  <CardTitle className="text-base font-bold">{selectedTopic} Problem Set</CardTitle>
                  <CardDescription className="text-xs">
                    Drill-down problem set and practice history for {selectedTopic}
                  </CardDescription>
                </div>
                <Button
                  size="xs"
                  variant="primary"
                  onClick={() => {
                    setEditingProblem({ primaryTopic: selectedTopic });
                    setIsAddEditOpen(true);
                  }}
                >
                  + Add to {selectedTopic}
                </Button>
              </CardHeader>
              <CardContent className="p-4 pt-1">
                {loadingTopicProblems ? (
                  <div className="py-8 text-center text-text-muted text-xs">
                    Loading topic problems...
                  </div>
                ) : topicProblems.length === 0 ? (
                  <div className="py-8 text-center text-text-muted text-xs">
                    No problems tracked under {selectedTopic} yet. Click &quot;Add to {selectedTopic}&quot; to start.
                  </div>
                ) : (
                  <div className="divide-y divide-border-subtle">
                    {topicProblems.map((p) => (
                      <div
                        key={p.id}
                        className="py-3 flex items-center justify-between gap-3 hover:bg-bg-surface-hover px-2 rounded-lg transition-colors"
                      >
                        <div className="min-w-0">
                          <button
                            type="button"
                            onClick={() => openProblemDetail(p.id)}
                            className="text-xs sm:text-sm font-semibold text-text-primary hover:text-brand-teal truncate block text-left cursor-pointer"
                          >
                            {p.title}
                          </button>
                          <div className="flex items-center gap-2 mt-1">
                            {renderStatusBadge(p.status)}
                            {renderDifficultyBadge(p.difficulty)}
                            <span className="text-[11px] text-text-muted">{p.platform}</span>
                          </div>
                        </div>

                        <div className="flex items-center gap-2">
                          <Button
                            size="xs"
                            variant="secondary"
                            onClick={() => {
                              setTargetProblemForAttempt(p);
                              setIsAttemptOpen(true);
                            }}
                          >
                            Log
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {/* =========================================================================
          TAB 4: REVISION QUEUE
         ========================================================================= */}
      {currentTab === 'revisions' && (
        <div className="space-y-4">
          {/* Sub Tabs for Revisions */}
          <div className="flex gap-2 border-b border-border-default pb-2 overflow-x-auto">
            {[
              { id: 'pending', label: 'All Pending', count: revisionsQueue.totalPendingCount },
              { id: 'overdue', label: 'Overdue', count: revisionsQueue.overdueCount, alert: true },
              { id: 'dueToday', label: 'Due Today', count: revisionsQueue.dueToday?.length || 0 },
              { id: 'upcoming', label: 'Upcoming', count: revisionsQueue.upcoming?.length || 0 },
              { id: 'completed', label: 'Completed History', count: revisionsQueue.completed?.length || 0 },
            ].map((sub) => {
              const isActive = revisionFilter === sub.id;
              return (
                <button
                  key={sub.id}
                  type="button"
                  onClick={() => setRevisionFilter(sub.id)}
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer whitespace-nowrap ${
                    isActive
                      ? 'bg-brand-teal text-white shadow-xs font-bold'
                      : 'bg-bg-surface text-text-secondary border border-border-default hover:text-text-primary'
                  }`}
                >
                  <span>{sub.label}</span>
                  <span
                    className={`text-[10px] px-1.5 py-0.2 rounded-full ${
                      sub.alert && sub.count > 0
                        ? 'bg-rose-500 text-white font-bold'
                        : isActive
                        ? 'bg-white/20 text-white'
                        : 'bg-bg-surface-elevated text-text-muted'
                    }`}
                  >
                    {sub.count}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Revision Items List */}
          <Card>
            <CardContent className="p-4">
              {loadingRevisions ? (
                <div className="py-12 text-center text-text-muted text-xs">
                  Loading spaced revision queue...
                </div>
              ) : (
                (() => {
                  let items = [];
                  if (revisionFilter === 'pending') {
                    items = [...(revisionsQueue.overdue || []), ...(revisionsQueue.dueToday || []), ...(revisionsQueue.upcoming || [])];
                  } else if (revisionFilter === 'overdue') {
                    items = revisionsQueue.overdue || [];
                  } else if (revisionFilter === 'dueToday') {
                    items = revisionsQueue.dueToday || [];
                  } else if (revisionFilter === 'upcoming') {
                    items = revisionsQueue.upcoming || [];
                  } else if (revisionFilter === 'completed') {
                    items = revisionsQueue.completed || [];
                  }

                  if (items.length === 0) {
                    return (
                      <EmptyState
                        title="No revisions in this queue"
                        description="Schedule spaced repetition for difficult problems from the problem details view."
                      />
                    );
                  }

                  return (
                    <div className="space-y-3">
                      {items.map((rev) => (
                        <div
                          key={rev.id}
                          className={`p-4 rounded-xl border flex flex-col sm:flex-row sm:items-center justify-between gap-3 ${
                            rev.isOverdue && rev.status !== 'COMPLETED'
                              ? 'bg-rose-500/10 border-rose-500/30'
                              : 'bg-bg-surface-elevated/40 border-border-default'
                          }`}
                        >
                          <div className="space-y-1 min-w-0">
                            <div className="flex items-center gap-2">
                              <span
                                className={`text-[10px] font-bold uppercase px-2 py-0.5 rounded ${
                                  rev.status === 'COMPLETED'
                                    ? 'bg-emerald-500/15 text-emerald-600'
                                    : rev.isOverdue
                                    ? 'bg-rose-500 text-white'
                                    : 'bg-brand-teal/15 text-brand-teal'
                                }`}
                              >
                                {rev.status === 'COMPLETED'
                                  ? 'Done'
                                  : rev.isOverdue
                                  ? 'Overdue'
                                  : 'Due'}
                              </span>
                              <span className="text-xs font-semibold text-text-muted">
                                Target: {rev.scheduledDate}
                              </span>
                            </div>

                            <button
                              type="button"
                              onClick={() => openProblemDetail(rev.problemId)}
                              className="text-sm font-bold text-text-primary hover:text-brand-teal text-left block cursor-pointer truncate"
                            >
                              {rev.problemTitle}
                            </button>

                            {rev.revisionNotes && (
                              <p className="text-xs text-text-muted bg-bg-surface p-2 rounded-lg border border-border-subtle">
                                {rev.revisionNotes}
                              </p>
                            )}
                          </div>

                          <div className="flex items-center gap-2 shrink-0">
                            {rev.status === 'PENDING' && (
                              <Button
                                size="xs"
                                variant="primary"
                                onClick={() => handleCompleteRevision(rev.id)}
                              >
                                Mark Done
                              </Button>
                            )}
                            <button
                              type="button"
                              onClick={() => handleDeleteRevision(rev.id)}
                              className="p-1.5 text-text-muted hover:text-rose-500 transition-colors cursor-pointer"
                              title="Delete from Queue"
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
                  );
                })()
              )}
            </CardContent>
          </Card>
        </div>
      )}

      {/* =========================================================================
          TAB 5: VISUAL ANALYTICS
         ========================================================================= */}
      {currentTab === 'analytics' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Difficulty Donut Chart */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base font-bold">Difficulty Breakdown</CardTitle>
                <CardDescription className="text-xs">Distribution of tracked problems by difficulty tier</CardDescription>
              </CardHeader>
              <CardContent>
                <DifficultyDonutChart difficultyData={difficultyAnalytics} />
              </CardContent>
            </Card>

            {/* 14-Day Activity Bar Chart */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base font-bold">14-Day Activity Timeline</CardTitle>
                <CardDescription className="text-xs">Daily solving attempts and completed problems</CardDescription>
              </CardHeader>
              <CardContent>
                <ActivityTimelineBarChart timelineData={activityTimeline} />
              </CardContent>
            </Card>
          </div>
        </div>
      )}

      {/* =========================================================================
          MODALS
         ========================================================================= */}
      <AddEditProblemModal
        isOpen={isAddEditOpen}
        onClose={() => {
          setIsAddEditOpen(false);
          setEditingProblem(null);
        }}
        onSave={handleSaveProblem}
        initialData={editingProblem}
        isSaving={isSavingProblem}
      />

      <ProblemDetailModal
        isOpen={isDetailOpen}
        onClose={() => {
          setIsDetailOpen(false);
          setActiveProblemDetail(null);
        }}
        problemDetail={activeProblemDetail}
        isLoading={loadingDetail}
        onEdit={(problem) => {
          setIsDetailOpen(false);
          setEditingProblem(problem);
          setIsAddEditOpen(true);
        }}
        onDelete={handleDeleteProblem}
        onLogAttempt={(problem) => {
          setTargetProblemForAttempt(problem);
          setIsAttemptOpen(true);
        }}
        onScheduleRevision={(problem) => {
          setTargetProblemForRevision(problem);
          setIsRevisionOpen(true);
        }}
        onCompleteRevision={handleCompleteRevision}
        onDeleteRevision={handleDeleteRevision}
      />

      <LogAttemptModal
        isOpen={isAttemptOpen}
        onClose={() => {
          setIsAttemptOpen(false);
          setTargetProblemForAttempt(null);
        }}
        onSave={handleSaveAttempt}
        problem={targetProblemForAttempt}
        isSaving={isSavingAttempt}
      />

      <ScheduleRevisionModal
        isOpen={isRevisionOpen}
        onClose={() => {
          setIsRevisionOpen(false);
          setTargetProblemForRevision(null);
        }}
        onSave={handleSaveRevision}
        problem={targetProblemForRevision}
        isSaving={isSavingRevision}
      />
    </div>
  );
}
