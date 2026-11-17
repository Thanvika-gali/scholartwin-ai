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
      setProblemsPage(data);
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
      setTopics(data || []);
    } catch (err) {
      console.error('Failed to load DSA topics', err);
    } finally {
      setLoadingTopics(false);
    }
  }, []);

  // Load Topic Problems
  const loadTopicProblems = async (topicName) => {
    setSelectedTopic(topicName);
    setLoadingTopicProblems(true);
    try {
      const data = await fetchDsaTopicProblems(topicName);
      setTopicProblems(data || []);
    } catch (err) {
      console.error('Failed to load problems for topic', err);
    } finally {
      setLoadingTopicProblems(false);
    }
  };

  // Load Revisions
  const loadRevisions = useCallback(async () => {
    setLoadingRevisions(true);
    try {
      const data = await fetchDsaRevisions();
      setRevisionsQueue(data);
    } catch (err) {
      console.error('Failed to load revisions queue', err);
    } finally {
      setLoadingRevisions(false);
    }
  }, []);

  // Load Analytics
  const loadAnalytics = useCallback(async () => {
    setLoadingAnalytics(true);
    try {
      const [diffData, activityData] = await Promise.all([
        fetchDsaDifficultyAnalytics(),
        fetchDsaActivityTimeline(14),
      ]);
      setDifficultyAnalytics(diffData);
      setActivityTimeline(activityData);
    } catch (err) {
      console.error('Failed to load analytics', err);
    } finally {
      setLoadingAnalytics(false);
    }
  }, []);

  // Trigger loading based on active tab
  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  useEffect(() => {
    if (currentTab === 'problems') {
      loadProblems();
    } else if (currentTab === 'topics') {
      loadTopics();
    } else if (currentTab === 'revisions') {
      loadRevisions();
    } else if (currentTab === 'analytics') {
      loadAnalytics();
    }
  }, [currentTab, loadProblems, loadTopics, loadRevisions, loadAnalytics]);

  // Handle Problem Save (Create or Update)
  const handleSaveProblem = async (formData) => {
    setIsSavingProblem(true);
    try {
      if (editingProblem && editingProblem.id) {
        await updateDsaProblem(editingProblem.id, formData);
        showFeedback('Problem updated successfully');
      } else {
        await createDsaProblem(formData);
        showFeedback('Problem tracked successfully');
      }
      setIsAddEditOpen(false);
      setEditingProblem(null);
      loadDashboard();
      if (currentTab === 'problems') loadProblems();
      if (currentTab === 'topics') loadTopics();
      if (activeProblemDetail && editingProblem?.id === activeProblemDetail.problem?.id) {
        openProblemDetail(editingProblem.id);
      }
    } catch (err) {
      alert(err.message || 'Failed to save problem');
    } finally {
      setIsSavingProblem(false);
    }
  };

  // Handle Problem Delete
  const handleDeleteProblem = async (problem) => {
    if (!window.confirm(`Are you sure you want to delete "${problem.title}"? All attempts and revisions will also be removed.`)) {
      return;
    }
    try {
      await deleteDsaProblem(problem.id);
      showFeedback('Problem removed');
      setIsDetailOpen(false);
      loadDashboard();
      if (currentTab === 'problems') loadProblems();
      if (currentTab === 'topics') {
        loadTopics();
        if (selectedTopic) loadTopicProblems(selectedTopic);
      }
    } catch (err) {
      alert(err.message || 'Failed to delete problem');
    }
  };

  // Open Problem Detail Modal
  const openProblemDetail = async (problemId) => {
    setIsDetailOpen(true);
    setLoadingDetail(true);
    try {
      const data = await fetchDsaProblem(problemId);
      setActiveProblemDetail(data);
    } catch (err) {
      alert(err.message || 'Could not fetch problem details');
      setIsDetailOpen(false);
    } finally {
      setLoadingDetail(false);
    }
  };

  // Handle Save Attempt
  const handleSaveAttempt = async (attemptData) => {
    if (!targetProblemForAttempt) return;
    setIsSavingAttempt(true);
    try {
      await logDsaAttempt(targetProblemForAttempt.id, attemptData);
      showFeedback(
        attemptData.outcome === 'SUCCESS'
          ? '🎉 Solved! Streak incremented and accuracy updated.'
          : 'Attempt recorded. Added to practice history.'
      );
      setIsAttemptOpen(false);
      loadDashboard();
      if (currentTab === 'problems') loadProblems();
      if (currentTab === 'topics') loadTopics();
      if (isDetailOpen && activeProblemDetail?.problem?.id === targetProblemForAttempt.id) {
        openProblemDetail(targetProblemForAttempt.id);
      }
    } catch (err) {
      alert(err.message || 'Failed to log attempt');
    } finally {
      setIsSavingAttempt(false);
    }
  };

  // Handle Save Revision
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
      NOT_STARTED: 'bg-slate-500/15 text-slate-600 dark:text-slate-400 border-slate-500/30',
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
    <div className="space-y-6 pb-12">
      {/* Toast Feedback */}
      {feedbackMessage && (
        <div
          role="status"
          aria-live="polite"
          className="fixed bottom-6 right-6 z-50 bg-brand-navy border border-brand-teal text-white text-xs sm:text-sm font-medium py-3 px-4 rounded-xl shadow-lg flex items-center gap-2 animate-in fade-in slide-in-from-bottom-2 duration-200"
        >
          <span className="w-2 h-2 rounded-full bg-brand-teal" />
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
              DSA & Coding Tracker
            </h1>
          </div>
          <p className="text-xs sm:text-sm text-text-muted mt-1">
            Systematic algorithmic mastery, daily coding streaks, and spaced repetition revisions.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
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
            badge: dashboardData?.pendingRevisionsCount > 0 ? dashboardData.pendingRevisionsCount : null,
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
                  ? 'bg-bg-surface text-brand-teal border-t-2 border-t-brand-teal border-x border-border-default shadow-sm -mb-px'
                  : 'text-text-muted hover:text-text-primary hover:bg-bg-surface/50'
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
          TAB 1: DASHBOARD
         ========================================================================= */}
      {currentTab === 'dashboard' && (
        <div className="space-y-6">
          {/* Key Stat Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {/* Total Problems Tracked */}
            <Card>
              <CardContent className="p-4 sm:p-5 flex items-center justify-between">
                <div>
                  <span className="text-xs font-semibold text-text-muted uppercase tracking-wider block">
                    Total Tracked
                  </span>
                  <span className="text-2xl font-bold text-text-primary mt-1 block">
                    {dashboardData?.totalProblems || 0}
                  </span>
                  <span className="text-[11px] text-text-muted mt-0.5 block">
                    {dashboardData?.solvedProblems || 0} solved (
                    {dashboardData?.totalProblems
                      ? Math.round((dashboardData.solvedProblems / dashboardData.totalProblems) * 100)
                      : 0}
                    %)
                  </span>
                </div>
                <div className="w-10 h-10 rounded-xl bg-brand-teal/15 text-brand-teal flex items-center justify-center">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
              </CardContent>
            </Card>

            {/* Current Streak */}
            <Card>
              <CardContent className="p-4 sm:p-5 flex items-center justify-between">
                <div>
                  <span className="text-xs font-semibold text-text-muted uppercase tracking-wider block">
                    Practice Streak
                  </span>
                  <div className="flex items-baseline gap-1 mt-1">
                    <span className="text-2xl font-bold text-amber-500">
                      {dashboardData?.currentStreakDays || 0}
                    </span>
                    <span className="text-xs font-semibold text-text-secondary">days</span>
                  </div>
                  <span className="text-[11px] text-text-muted mt-0.5 block">
                    Best: {dashboardData?.longestStreakDays || 0} days
                  </span>
                </div>
                <div className="w-10 h-10 rounded-xl bg-amber-500/15 text-amber-500 flex items-center justify-center">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
                    <path d="M12 .587l3.668 7.431 8.2 1.192-5.934 5.784 1.399 8.163-7.333-3.856-7.333 3.856 1.399-8.163-5.934-5.784 8.2-1.192zm0 5.702l-2.223 4.503-4.97.722 3.596 3.505-.848 4.947 4.445-2.337 4.445 2.337-.848-4.947 3.596-3.505-4.97-.722z" />
                  </svg>
                </div>
              </CardContent>
            </Card>

            {/* Overall Accuracy */}
            <Card>
              <CardContent className="p-4 sm:p-5 flex items-center justify-between">
                <div>
                  <span className="text-xs font-semibold text-text-muted uppercase tracking-wider block">
                    Overall Accuracy
                  </span>
                  <span className="text-2xl font-bold text-brand-teal mt-1 block">
                    {dashboardData?.overallAccuracyPercent !== undefined
                      ? `${dashboardData.overallAccuracyPercent}%`
                      : '0%'}
                  </span>
                  <span className="text-[11px] text-text-muted mt-0.5 block">
                    Across all logged attempts
                  </span>
                </div>
                <div className="w-10 h-10 rounded-xl bg-brand-teal/15 text-brand-teal flex items-center justify-center">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
              </CardContent>
            </Card>

            {/* Revisions Queued */}
            <Card>
              <CardContent className="p-4 sm:p-5 flex items-center justify-between">
                <div>
                  <span className="text-xs font-semibold text-text-muted uppercase tracking-wider block">
                    Due Revisions
                  </span>
                  <span className="text-2xl font-bold text-brand-gold mt-1 block">
                    {dashboardData?.pendingRevisionsCount || 0}
                  </span>
                  <span className="text-[11px] text-rose-500 font-medium mt-0.5 block">
                    {dashboardData?.overdueRevisionsCount || 0} overdue
                  </span>
                </div>
                <div className="w-10 h-10 rounded-xl bg-brand-gold/15 text-brand-gold flex items-center justify-center">
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Difficulty Tier Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-bg-surface border border-emerald-500/20 rounded-2xl p-4 flex items-center justify-between">
              <div>
                <span className="text-xs font-semibold text-emerald-600 dark:text-emerald-400 uppercase tracking-wider">
                  Easy
                </span>
                <div className="text-xl font-bold text-text-primary mt-1">
                  {dashboardData?.easySolved || 0}{' '}
                  <span className="text-xs text-text-muted font-normal">/ {dashboardData?.easyTotal || 0}</span>
                </div>
              </div>
              <div className="w-12 h-2 rounded-full bg-emerald-500/20 overflow-hidden">
                <div
                  style={{
                    width: `${
                      dashboardData?.easyTotal
                        ? (dashboardData.easySolved / dashboardData.easyTotal) * 100
                        : 0
                    }%`,
                  }}
                  className="h-full bg-emerald-500"
                />
              </div>
            </div>

            <div className="bg-bg-surface border border-amber-500/20 rounded-2xl p-4 flex items-center justify-between">
              <div>
                <span className="text-xs font-semibold text-amber-600 dark:text-amber-400 uppercase tracking-wider">
                  Medium
                </span>
                <div className="text-xl font-bold text-text-primary mt-1">
                  {dashboardData?.mediumSolved || 0}{' '}
                  <span className="text-xs text-text-muted font-normal">/ {dashboardData?.mediumTotal || 0}</span>
                </div>
              </div>
              <div className="w-12 h-2 rounded-full bg-amber-500/20 overflow-hidden">
                <div
                  style={{
                    width: `${
                      dashboardData?.mediumTotal
                        ? (dashboardData.mediumSolved / dashboardData.mediumTotal) * 100
                        : 0
                    }%`,
                  }}
                  className="h-full bg-amber-500"
                />
              </div>
            </div>

            <div className="bg-bg-surface border border-rose-500/20 rounded-2xl p-4 flex items-center justify-between">
              <div>
                <span className="text-xs font-semibold text-rose-600 dark:text-rose-400 uppercase tracking-wider">
                  Hard
                </span>
                <div className="text-xl font-bold text-text-primary mt-1">
                  {dashboardData?.hardSolved || 0}{' '}
                  <span className="text-xs text-text-muted font-normal">/ {dashboardData?.hardTotal || 0}</span>
                </div>
              </div>
              <div className="w-12 h-2 rounded-full bg-rose-500/20 overflow-hidden">
                <div
                  style={{
                    width: `${
                      dashboardData?.hardTotal
                        ? (dashboardData.hardSolved / dashboardData.hardTotal) * 100
                        : 0
                    }%`,
                  }}
                  className="h-full bg-rose-500"
                />
              </div>
            </div>
          </div>

          {/* Rule-Based Dynamic Insights */}
          {dashboardData?.insights && dashboardData.insights.length > 0 && (
            <div className="bg-brand-navy border border-brand-teal/40 rounded-2xl p-5 text-white shadow-card">
              <div className="flex items-center gap-2 mb-3">
                <div className="w-7 h-7 rounded-lg bg-brand-teal text-brand-navy flex items-center justify-center font-bold text-xs">
                  AI
                </div>
                <h3 className="text-sm font-bold tracking-tight text-brand-gold-light">
                  Rule-Based Practice Insights
                </h3>
              </div>
              <div className="space-y-2">
                {dashboardData.insights.map((insight, idx) => (
                  <div key={idx} className="flex items-start gap-2.5 text-xs text-slate-200">
                    <span className="text-brand-teal font-bold mt-0.5">•</span>
                    <p className="leading-relaxed">{insight}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Split View: Recently Solved & Recent Attempts */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Recently Solved */}
            <Card>
              <CardHeader className="flex items-center justify-between">
                <div>
                  <CardTitle>Recently Solved</CardTitle>
                  <CardDescription>Problems successfully mastered</CardDescription>
                </div>
                <Button size="sm" variant="ghost" onClick={() => setTab('problems')}>
                  View All →
                </Button>
              </CardHeader>
              <CardContent className="space-y-3">
                {dashboardData?.recentlySolved && dashboardData.recentlySolved.length > 0 ? (
                  dashboardData.recentlySolved.map((p) => (
                    <div
                      key={p.id}
                      className="p-3 bg-bg-surface-elevated/50 border border-border-default rounded-xl flex items-center justify-between gap-3 hover:border-brand-teal/40 transition-colors"
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
                          {renderDifficultyBadge(p.difficulty)}
                          <span className="text-[11px] text-text-muted">{p.primaryTopic}</span>
                        </div>
                      </div>
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => {
                          setTargetProblemForAttempt(p);
                          setIsAttemptOpen(true);
                        }}
                      >
                        Log
                      </Button>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8 text-text-muted text-xs">
                    No solved problems yet. Solve your first problem to start your streak!
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Recent Attempts */}
            <Card>
              <CardHeader className="flex items-center justify-between">
                <div>
                  <CardTitle>Recent Attempts</CardTitle>
                  <CardDescription>Latest problem-solving activity</CardDescription>
                </div>
              </CardHeader>
              <CardContent className="space-y-3">
                {dashboardData?.recentlyAttempted && dashboardData.recentlyAttempted.length > 0 ? (
                  dashboardData.recentlyAttempted.map((att) => (
                    <div
                      key={att.id}
                      className="p-3 bg-bg-surface-elevated/50 border border-border-default rounded-xl flex items-center justify-between gap-3"
                    >
                      <div className="min-w-0">
                        <span className="text-xs font-semibold text-text-primary truncate block">
                          {att.problemTitle}
                        </span>
                        <div className="flex items-center gap-2 mt-1">
                          <span
                            className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                              att.outcome === 'SUCCESS'
                                ? 'bg-emerald-500/15 text-emerald-600'
                                : 'bg-amber-500/15 text-amber-600'
                            }`}
                          >
                            {att.outcome}
                          </span>
                          <span className="text-[11px] text-text-muted">
                            {att.attemptDate} {att.timeSpentMinutes ? `• ${att.timeSpentMinutes}m` : ''}
                          </span>
                        </div>
                      </div>
                      <span className="text-[11px] font-mono text-text-secondary">
                        {att.programmingLanguage || 'Code'}
                      </span>
                    </div>
                  ))
                ) : (
                  <div className="text-center py-8 text-text-muted text-xs">
                    No attempts logged yet. Select a problem and click &quot;Log Attempt&quot;.
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      )}

      {/* =========================================================================
          TAB 2: ALL PROBLEMS EXPLORER
         ========================================================================= */}
      {currentTab === 'problems' && (
        <div className="space-y-4">
          {/* Filter Bar */}
          <Card>
            <CardContent className="p-4 space-y-3">
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
                {/* Search */}
                <div>
                  <input
                    type="text"
                    placeholder="Search by title or notes..."
                    value={problemFilters.search}
                    onChange={(e) =>
                      setProblemFilters({ ...problemFilters, search: e.target.value, page: 0 })
                    }
                    className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3 py-2 text-xs text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
                  />
                </div>

                {/* Topic */}
                <div>
                  <select
                    value={problemFilters.topic}
                    onChange={(e) =>
                      setProblemFilters({ ...problemFilters, topic: e.target.value, page: 0 })
                    }
                    className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3 py-2 text-xs text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
                  >
                    <option value="">All Topics (20)</option>
                    {DSA_TOPICS.map((t) => (
                      <option key={t} value={t}>
                        {t}
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
                    className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3 py-2 text-xs text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
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
                    className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3 py-2 text-xs text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
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
                <div className="flex items-center gap-3">
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
                      className="rounded border-border-default text-brand-teal focus:ring-brand-teal"
                    />
                    <span>Revision Required Only</span>
                  </label>
                </div>

                <span>
                  Showing {problemsPage.content?.length || 0} of {problemsPage.totalElements || 0} problems
                </span>
              </div>
            </CardContent>
          </Card>

          {/* Problems Table */}
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
                          description="Try clearing your filters or add a new DSA problem to your tracker."
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
                            className="hover:text-brand-teal text-left cursor-pointer truncate max-w-xs block"
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
                        <td className="py-3.5 px-4 whitespace-nowrap text-text-secondary">
                          {p.attemptCount || 0} ({p.successRatePercent || 0}%)
                        </td>
                        <td className="py-3.5 px-4 text-right whitespace-nowrap">
                          <div className="flex items-center justify-end gap-1.5">
                            <button
                              type="button"
                              onClick={() => {
                                setTargetProblemForAttempt(p);
                                setIsAttemptOpen(true);
                              }}
                              className="px-2 py-1 rounded-md text-xs font-semibold bg-brand-teal/15 text-brand-teal hover:bg-brand-teal/25 transition-colors cursor-pointer"
                              title="Log Attempt"
                            >
                              Log
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                setTargetProblemForRevision(p);
                                setIsRevisionOpen(true);
                              }}
                              className="px-2 py-1 rounded-md text-xs font-semibold bg-brand-gold/15 text-amber-800 dark:text-brand-gold-light hover:bg-brand-gold/25 transition-colors cursor-pointer"
                              title="Schedule Revision"
                            >
                              Revise
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                setEditingProblem(p);
                                setIsAddEditOpen(true);
                              }}
                              className="p-1 text-text-muted hover:text-text-primary transition-colors cursor-pointer"
                              title="Edit"
                            >
                              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                              </svg>
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Pagination Controls */}
            {problemsPage.totalPages > 1 && (
              <div className="p-4 border-t border-border-subtle flex items-center justify-between text-xs">
                <Button
                  size="sm"
                  variant="secondary"
                  disabled={problemsPage.number === 0}
                  onClick={() =>
                    setProblemFilters({ ...problemFilters, page: problemFilters.page - 1 })
                  }
                >
                  Previous
                </Button>
                <span className="text-text-muted">
                  Page {problemsPage.number + 1} of {problemsPage.totalPages}
                </span>
                <Button
                  size="sm"
                  variant="secondary"
                  disabled={problemsPage.number >= problemsPage.totalPages - 1}
                  onClick={() =>
                    setProblemFilters({ ...problemFilters, page: problemFilters.page + 1 })
                  }
                >
                  Next
                </Button>
              </div>
            )}
          </Card>
        </div>
      )}

      {/* =========================================================================
          TAB 3: TOPIC MASTERY (20 TOPICS MATRIX)
         ========================================================================= */}
      {currentTab === 'topics' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {loadingTopics ? (
              <div className="col-span-full py-12 text-center text-text-muted text-xs">
                Loading topic mastery matrix...
              </div>
            ) : (
              topics.map((t) => {
                const completion = t.completionPercentage || 0;
                return (
                  <div
                    key={t.topicName}
                    onClick={() => loadTopicProblems(t.topicName)}
                    className={`p-4 bg-bg-surface border rounded-2xl cursor-pointer transition-all hover:shadow-card-hover ${
                      selectedTopic === t.topicName
                        ? 'border-brand-teal ring-2 ring-brand-teal/20 shadow-md'
                        : 'border-border-default hover:border-brand-teal/40'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <h4 className="text-sm font-bold text-text-primary truncate">
                        {t.topicName}
                      </h4>
                      <span className="text-xs font-bold text-brand-teal shrink-0">
                        {completion}%
                      </span>
                    </div>

                    <div className="mt-3 w-full h-2 rounded-full bg-bg-surface-elevated overflow-hidden border border-border-default/40">
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
                  <CardTitle>{selectedTopic} Problems</CardTitle>
                  <CardDescription>
                    Drill-down problem set and practice history for {selectedTopic}
                  </CardDescription>
                </div>
                <Button
                  size="sm"
                  variant="primary"
                  onClick={() => {
                    setEditingProblem({ primaryTopic: selectedTopic });
                    setIsAddEditOpen(true);
                  }}
                >
                  + Add to {selectedTopic}
                </Button>
              </CardHeader>
              <CardContent>
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
                            size="sm"
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
          <div className="flex gap-2 border-b border-border-default pb-2">
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
                  className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all cursor-pointer ${
                    isActive
                      ? 'bg-brand-teal text-white shadow-sm'
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
                                size="sm"
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
                <CardTitle>Difficulty Breakdown</CardTitle>
                <CardDescription>Distribution of tracked problems by difficulty tier</CardDescription>
              </CardHeader>
              <CardContent>
                <DifficultyDonutChart difficultyData={difficultyAnalytics} />
              </CardContent>
            </Card>

            {/* 14-Day Activity Bar Chart */}
            <Card>
              <CardHeader>
                <CardTitle>14-Day Activity Timeline</CardTitle>
                <CardDescription>Daily solving attempts and completed problems</CardDescription>
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

