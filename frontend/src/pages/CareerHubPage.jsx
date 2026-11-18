import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import PageHeader from '../components/ui/PageHeader';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import Alert from '../components/ui/Alert';
import EmptyState from '../components/ui/EmptyState';
import LoadingSkeleton from '../components/ui/LoadingSkeleton';

import ResumeUploadModal from '../components/career/ResumeUploadModal';
import AddEditSkillModal from '../components/career/AddEditSkillModal';
import AddEditActionModal from '../components/career/AddEditActionModal';
import JobDescriptionModal from '../components/career/JobDescriptionModal';
import ResumeDetailView from '../components/career/ResumeDetailView';

import {
  fetchCareerDashboardSummary,
  fetchResumes,
  fetchResumeDetail,
  fetchCareerSkills,
  deleteCareerSkill,
  fetchAvailableRoles,
  fetchRoleReadiness,
  fetchJobAnalyses,
  deleteJobAnalysisApi,
  fetchCareerActions,
  updateCareerAction,
  deleteCareerAction,
} from '../services/api';

import {
  getScoreVariant,
  formatSkillCategory,
  formatProficiency,
  formatActionPriority,
  formatActionCategory,
  formatActionStatus,
} from '../utils/careerValidation';

export default function CareerHubPage() {
  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'resumes' | 'skills' | 'roles' | 'jobs' | 'actions'
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Data states
  const [dashboardSummary, setDashboardSummary] = useState(null);
  const [resumes, setResumes] = useState([]);
  const [skills, setSkills] = useState([]);
  const [roles, setRoles] = useState([]);
  const [selectedRoleId, setSelectedRoleId] = useState('SDE');
  const [roleReadiness, setRoleReadiness] = useState(null);
  const [isRoleLoading, setIsRoleLoading] = useState(false);
  const [jobAnalyses, setJobAnalyses] = useState([]);
  const [actions, setActions] = useState([]);

  // Subview / Selection states
  const [selectedResumeId, setSelectedResumeId] = useState(null);
  const [selectedResumeDetail, setSelectedResumeDetail] = useState(null);
  const [isResumeDetailLoading, setIsResumeDetailLoading] = useState(false);

  // Modals
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);
  const [isSkillModalOpen, setIsSkillModalOpen] = useState(false);
  const [editingSkill, setEditingSkill] = useState(null);
  const [isActionModalOpen, setIsActionModalOpen] = useState(false);
  const [editingAction, setEditingAction] = useState(null);
  const [isJobModalOpen, setIsJobModalOpen] = useState(false);
  const [viewJobAnalysis, setViewJobAnalysis] = useState(null);

  // Filters
  const [skillCategoryFilter, setSkillCategoryFilter] = useState('ALL');
  const [skillSearch, setSkillSearch] = useState('');
  const [actionStatusFilter, setActionStatusFilter] = useState('ALL');

  // Load core dashboard data
  const loadDashboardData = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [summaryRes, resumesRes, skillsRes, rolesRes, jobsRes, actionsRes] = await Promise.allSettled([
        fetchCareerDashboardSummary(),
        fetchResumes(),
        fetchCareerSkills(),
        fetchAvailableRoles(),
        fetchJobAnalyses(),
        fetchCareerActions(),
      ]);

      if (summaryRes.status === 'fulfilled') setDashboardSummary(summaryRes.value);
      if (resumesRes.status === 'fulfilled') setResumes(resumesRes.value || []);
      if (skillsRes.status === 'fulfilled') setSkills(skillsRes.value || []);
      if (rolesRes.status === 'fulfilled') setRoles(rolesRes.value || []);
      if (jobsRes.status === 'fulfilled') setJobAnalyses(jobsRes.value || []);
      if (actionsRes.status === 'fulfilled') setActions(actionsRes.value || []);
    } catch (err) {
      setError('Failed to load career data. Please check your connection.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboardData();
  }, [loadDashboardData]);

  // Load role readiness on demand
  useEffect(() => {
    let isMounted = true;
    async function loadRole() {
      if (!selectedRoleId) return;
      setIsRoleLoading(true);
      try {
        const readiness = await fetchRoleReadiness(selectedRoleId);
        if (isMounted) setRoleReadiness(readiness);
      } catch (_) {
        // fallback
      } finally {
        if (isMounted) setIsRoleLoading(false);
      }
    }
    loadRole();
    return () => {
      isMounted = false;
    };
  }, [selectedRoleId]);

  // Load detailed resume when selected
  const handleSelectResume = async (id) => {
    setSelectedResumeId(id);
    setIsResumeDetailLoading(true);
    try {
      const detail = await fetchResumeDetail(id);
      setSelectedResumeDetail(detail);
    } catch (err) {
      setError('Failed to load resume details.');
    } finally {
      setIsResumeDetailLoading(false);
    }
  };

  const handleBackToResumeList = () => {
    setSelectedResumeId(null);
    setSelectedResumeDetail(null);
    loadDashboardData();
  };

  // Toggle Action Status
  const handleToggleActionStatus = async (action) => {
    const nextStatus = action.status === 'COMPLETED' ? 'NOT_STARTED' : 'COMPLETED';
    try {
      const updated = await updateCareerAction(action.id, {
        title: action.title,
        status: nextStatus,
      });
      setActions((prev) => prev.map((a) => (a.id === updated.id ? updated : a)));
      // refresh summary metrics
      fetchCareerDashboardSummary().then(setDashboardSummary).catch(() => {});
    } catch (_) {}
  };

  // Delete Action
  const handleDeleteAction = async (id) => {
    try {
      await deleteCareerAction(id);
      setActions((prev) => prev.filter((a) => a.id !== id));
      fetchCareerDashboardSummary().then(setDashboardSummary).catch(() => {});
    } catch (_) {}
  };

  // Delete Skill
  const handleDeleteSkill = async (id) => {
    try {
      await deleteCareerSkill(id);
      setSkills((prev) => prev.filter((s) => s.id !== id));
      fetchCareerDashboardSummary().then(setDashboardSummary).catch(() => {});
    } catch (_) {}
  };

  // Delete Job Analysis
  const handleDeleteJobAnalysis = async (id) => {
    try {
      await deleteJobAnalysisApi(id);
      setJobAnalyses((prev) => prev.filter((j) => j.id !== id));
    } catch (_) {}
  };

  // Filtered Skills
  const filteredSkills = skills.filter((sk) => {
    const matchesCategory = skillCategoryFilter === 'ALL' || sk.category === skillCategoryFilter;
    const matchesSearch = !skillSearch.trim() || sk.skillName.toLowerCase().includes(skillSearch.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  // Filtered Actions
  const filteredActions = actions.filter((act) => {
    if (actionStatusFilter === 'ALL') return true;
    return act.status === actionStatusFilter;
  });

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 sm:px-6 py-6 font-sans">
      {/* Page Header */}
      <PageHeader
        title="Career & Resume Hub"
        subtitle="AI-driven resume diagnostics, skills inventory, role readiness, and career growth roadmap"
        actions={
          <div className="flex flex-wrap items-center gap-2">
            <Button
              variant="secondary"
              size="sm"
              onClick={() => setIsJobModalOpen(true)}
              icon={
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                </svg>
              }
            >
              Analyze JD
            </Button>
            <Button
              variant="secondary"
              size="sm"
              onClick={() => {
                setEditingSkill(null);
                setIsSkillModalOpen(true);
              }}
              icon={
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
              }
            >
              Add Skill
            </Button>
            <Button
              variant="primary"
              size="sm"
              onClick={() => setIsUploadModalOpen(true)}
              icon={
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                </svg>
              }
            >
              Upload Resume
            </Button>
          </div>
        }
      />

      {error && (
        <Alert variant="error" dismissible onDismiss={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* If drilling down into a single Resume */}
      {selectedResumeId ? (
        isResumeDetailLoading ? (
          <LoadingSkeleton count={5} height="h-28" />
        ) : selectedResumeDetail ? (
          <ResumeDetailView
            resume={selectedResumeDetail}
            allResumes={resumes}
            onBack={handleBackToResumeList}
            onRefresh={() => handleSelectResume(selectedResumeId)}
          />
        ) : (
          <EmptyState
            title="Resume not found"
            description="The requested resume could not be retrieved."
            action={<Button onClick={handleBackToResumeList}>Back to Resumes</Button>}
          />
        )
      ) : (
        <>
          {/* Main Module Tabs */}
          <div className="flex border-b border-border-default overflow-x-auto gap-2 sm:gap-6 text-sm font-medium scrollbar-none">
            {[
              { id: 'overview', label: 'Overview & Insights' },
              { id: 'resumes', label: `Resume Library (${resumes.length})` },
              { id: 'skills', label: `Skills Inventory (${skills.length})` },
              { id: 'roles', label: 'Role Readiness' },
              { id: 'jobs', label: `Job Matcher (${jobAnalyses.length})` },
              { id: 'actions', label: `Action Plan (${actions.length})` },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`pb-3 px-1 border-b-2 whitespace-nowrap transition-colors cursor-pointer ${
                  activeTab === tab.id
                    ? 'border-brand-teal text-brand-teal font-semibold'
                    : 'border-transparent text-text-muted hover:text-text-primary'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>

          {isLoading ? (
            <LoadingSkeleton count={4} height="h-32" />
          ) : (
            <>
              {/* TAB 1: OVERVIEW & INSIGHTS */}
              {activeTab === 'overview' && (
                <div className="space-y-6">
                  {/* Onboarding Guide Card when user has no resumes yet */}
                  {resumes.length === 0 && (
                    <div className="p-6 rounded-2xl bg-gradient-to-br from-brand-navy via-brand-navy-light to-brand-navy text-white border border-brand-teal/40 shadow-lg relative overflow-hidden">
                      <div className="absolute top-0 right-0 w-96 h-96 bg-brand-teal/10 rounded-full blur-3xl pointer-events-none" />
                      <div className="relative z-10 space-y-4">
                        <div className="flex items-center gap-3">
                          <span className="px-2.5 py-1 rounded-full text-[11px] font-bold tracking-wider uppercase bg-brand-gold/20 text-brand-gold border border-brand-gold/30">
                            Career Onboarding Guide
                          </span>
                          <span className="text-xs text-gray-300">Fast-track your tech career preparation</span>
                        </div>
                        <div>
                          <h3 className="text-lg sm:text-xl font-bold text-white tracking-tight">
                            Accelerate Your SDE Career Readiness in 3 Steps
                          </h3>
                          <p className="text-xs sm:text-sm text-gray-300 mt-1 max-w-2xl leading-relaxed">
                            Unlock automated ATS scoring, benchmark your verified skills against software engineering roles, and match against real job descriptions.
                          </p>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 pt-2">
                          <div className="p-4 rounded-xl bg-white/5 border border-white/10 hover:border-brand-teal/50 transition-all flex flex-col justify-between space-y-3">
                            <div className="space-y-1.5">
                              <div className="flex items-center gap-2">
                                <span className="w-6 h-6 rounded-lg bg-brand-teal text-white flex items-center justify-center text-xs font-bold">
                                  1
                                </span>
                                <h4 className="text-xs font-bold text-white uppercase tracking-wider">
                                  Upload Resume
                                </h4>
                              </div>
                              <p className="text-xs text-gray-300 leading-relaxed">
                                Upload PDF or DOCX for instant 100-pt ATS diagnostics, layout scoring, and automated skill parsing.
                              </p>
                            </div>
                            <Button
                              variant="primary"
                              size="sm"
                              onClick={() => setIsUploadModalOpen(true)}
                              className="w-full text-xs"
                            >
                              Upload Resume
                            </Button>
                          </div>

                          <div className="p-4 rounded-xl bg-white/5 border border-white/10 hover:border-brand-gold/50 transition-all flex flex-col justify-between space-y-3">
                            <div className="space-y-1.5">
                              <div className="flex items-center gap-2">
                                <span className="w-6 h-6 rounded-lg bg-brand-gold text-brand-navy flex items-center justify-center text-xs font-bold">
                                  2
                                </span>
                                <h4 className="text-xs font-bold text-white uppercase tracking-wider">
                                  Role Benchmark
                                </h4>
                              </div>
                              <p className="text-xs text-gray-300 leading-relaxed">
                                Compare your verified skills and DSA progress against industry benchmarks for SDE, Backend, Frontend & AI.
                              </p>
                            </div>
                            <Button
                              variant="secondary"
                              size="sm"
                              onClick={() => setActiveTab('roles')}
                              className="w-full text-xs bg-white/10 hover:bg-white/20 text-white border-white/20"
                            >
                              Explore Benchmark Roles
                            </Button>
                          </div>

                          <div className="p-4 rounded-xl bg-white/5 border border-white/10 hover:border-brand-teal/50 transition-all flex flex-col justify-between space-y-3">
                            <div className="space-y-1.5">
                              <div className="flex items-center gap-2">
                                <span className="w-6 h-6 rounded-lg bg-brand-teal/30 text-brand-teal-light flex items-center justify-center text-xs font-bold">
                                  3
                                </span>
                                <h4 className="text-xs font-bold text-white uppercase tracking-wider">
                                  JD Matcher
                                </h4>
                              </div>
                              <p className="text-xs text-gray-300 leading-relaxed">
                                Paste any job description to discover required skill coverage, missing keywords, and tailored interview guidance.
                              </p>
                            </div>
                            <Button
                              variant="secondary"
                              size="sm"
                              onClick={() => setIsJobModalOpen(true)}
                              className="w-full text-xs bg-white/10 hover:bg-white/20 text-white border-white/20"
                            >
                              Match Job Spec
                            </Button>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {/* Top 4 KPI Metrics */}
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                    <Card className="flex items-center gap-4 p-5">
                      <div className="w-12 h-12 rounded-xl bg-brand-teal/15 text-brand-teal flex items-center justify-center shrink-0 border border-brand-teal/30">
                        <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.75} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                      </div>
                      <div>
                        <span className="text-xs uppercase font-semibold text-text-muted">Primary Resume Score</span>
                        <div className="flex items-center gap-2 mt-0.5">
                          <span className="text-2xl font-extrabold text-text-primary">
                            {dashboardSummary?.primaryResumeScore != null ? `${dashboardSummary.primaryResumeScore}` : '—'}
                          </span>
                          {dashboardSummary?.primaryResumeScore != null && (
                            <Badge variant={getScoreVariant(dashboardSummary.primaryResumeScore)} size="xs">
                              / 100
                            </Badge>
                          )}
                        </div>
                      </div>
                    </Card>

                    <Card className="flex items-center gap-4 p-5">
                      <div className="w-12 h-12 rounded-xl bg-brand-gold/15 text-brand-gold flex items-center justify-center shrink-0 border border-brand-gold/30">
                        <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.75} d="M13 10V3L4 14h7v7l9-11h-7z" />
                        </svg>
                      </div>
                      <div>
                        <span className="text-xs uppercase font-semibold text-text-muted">Target Role Readiness</span>
                        <div className="flex items-center gap-2 mt-0.5">
                          <span className="text-2xl font-extrabold text-text-primary">
                            {dashboardSummary?.targetRoleReadinessPercentage != null ? `${dashboardSummary.targetRoleReadinessPercentage}%` : '—'}
                          </span>
                          <span className="text-[11px] text-brand-teal font-semibold">
                            {dashboardSummary?.targetRole || 'SDE'}
                          </span>
                        </div>
                      </div>
                    </Card>

                    <Card className="flex items-center gap-4 p-5">
                      <div className="w-12 h-12 rounded-xl bg-blue-500/15 text-blue-500 flex items-center justify-center shrink-0 border border-blue-500/30">
                        <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.75} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
                        </svg>
                      </div>
                      <div>
                        <span className="text-xs uppercase font-semibold text-text-muted">Tracked Skills</span>
                        <div className="flex items-center gap-2 mt-0.5">
                          <span className="text-2xl font-extrabold text-text-primary">
                            {dashboardSummary?.totalSkillsCount || 0}
                          </span>
                          <span className="text-[11px] text-text-muted">competencies</span>
                        </div>
                      </div>
                    </Card>

                    <Card className="flex items-center gap-4 p-5">
                      <div className="w-12 h-12 rounded-xl bg-amber-500/15 text-amber-500 flex items-center justify-center shrink-0 border border-amber-500/30">
                        <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.75} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                        </svg>
                      </div>
                      <div>
                        <span className="text-xs uppercase font-semibold text-text-muted">Active Goals</span>
                        <div className="flex items-center gap-2 mt-0.5">
                          <span className="text-2xl font-extrabold text-text-primary">
                            {dashboardSummary?.inProgressActionsCount || 0}
                          </span>
                          <span className="text-[11px] text-status-success-text font-semibold">
                            ({dashboardSummary?.completedActionsCount || 0} done)
                          </span>
                        </div>
                      </div>
                    </Card>
                  </div>

                  {/* Personalized AI Career Insights */}
                  {dashboardSummary?.careerInsights?.length > 0 && (
                    <div className="p-5 rounded-2xl bg-gradient-to-r from-brand-navy via-brand-navy-light to-brand-navy text-white border border-brand-teal/30 space-y-3 shadow-md">
                      <div className="flex items-center gap-2">
                        <div className="w-6 h-6 rounded-lg bg-brand-gold/20 text-brand-gold flex items-center justify-center text-xs font-bold">
                          ★
                        </div>
                        <h3 className="text-sm font-bold uppercase tracking-wider text-brand-gold">
                          Personalized Career Diagnostics
                        </h3>
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        {dashboardSummary.careerInsights.map((insight, idx) => (
                          <div
                            key={idx}
                            className="p-3 rounded-xl bg-white/5 border border-white/10 text-xs text-gray-200 leading-relaxed flex items-start gap-2"
                          >
                            <span className="text-brand-teal font-extrabold mt-0.5">▸</span>
                            <span>{insight}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Cross-Platform DSA Tracker Integration Banner */}
                  <div className="p-5 rounded-xl bg-bg-surface border border-brand-teal/30 flex flex-col md:flex-row items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-brand-teal/15 text-brand-teal flex items-center justify-center font-bold">
                        &lt;/&gt;
                      </div>
                      <div>
                        <h4 className="text-xs font-bold uppercase tracking-wider text-text-primary">
                          Algorithmic Verification from DSA Tracker
                        </h4>
                        <p className="text-xs text-text-muted mt-0.5">
                          Your solved DSA problems directly strengthen your SDE & Backend role readiness score.
                        </p>
                      </div>
                    </div>
                    <Link to="/dsa">
                      <Button variant="secondary" size="sm">
                        Open DSA Tracker ↗
                      </Button>
                    </Link>
                  </div>

                  {/* Urgent Actions Section */}
                  <div className="space-y-3">
                    <div className="flex items-center justify-between">
                      <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
                        Priority Career Actions
                      </h4>
                      <button
                        onClick={() => setActiveTab('actions')}
                        className="text-xs text-brand-teal hover:underline font-semibold"
                      >
                        View all ({actions.length}) →
                      </button>
                    </div>

                    {actions.length === 0 ? (
                      <Card className="text-center py-6">
                        <p className="text-xs text-text-muted">No career action items set up yet.</p>
                        <Button
                          variant="secondary"
                          size="sm"
                          className="mt-2"
                          onClick={() => {
                            setEditingAction(null);
                            setIsActionModalOpen(true);
                          }}
                        >
                          Create First Goal
                        </Button>
                      </Card>
                    ) : (
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                        {actions.slice(0, 4).map((action) => {
                          const priorityInfo = formatActionPriority(action.priority);
                          const isDone = action.status === 'COMPLETED';
                          return (
                            <div
                              key={action.id}
                              className={`p-3.5 rounded-xl bg-bg-surface border transition-all flex items-start justify-between gap-3 ${
                                isDone
                                  ? 'border-status-success-border/40 opacity-75'
                                  : 'border-border-default hover:border-brand-teal/50'
                              }`}
                            >
                              <div className="flex items-start gap-3 min-w-0">
                                <input
                                  type="checkbox"
                                  checked={isDone}
                                  onChange={() => handleToggleActionStatus(action)}
                                  className="mt-1 rounded text-brand-teal focus:ring-brand-teal cursor-pointer"
                                />
                                <div className="min-w-0">
                                  <p
                                    className={`text-xs font-bold truncate ${
                                      isDone ? 'line-through text-text-muted' : 'text-text-primary'
                                    }`}
                                  >
                                    {action.title}
                                  </p>
                                  <div className="flex items-center gap-2 mt-1 flex-wrap">
                                    <Badge variant={priorityInfo.variant} size="xs">
                                      {priorityInfo.label}
                                    </Badge>
                                    <span className="text-[10px] text-text-muted">
                                      {formatActionCategory(action.category)}
                                    </span>
                                    {action.category === 'DSA_PRACTICE' && (
                                      <Link
                                        to="/dsa"
                                        className="text-[10px] text-brand-teal font-semibold hover:underline"
                                      >
                                        Go to DSA ↗
                                      </Link>
                                    )}
                                  </div>
                                </div>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* TAB 2: RESUME LIBRARY */}
              {activeTab === 'resumes' && (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-bold text-text-primary">Resume Versions & ATS Profiles</h3>
                      <p className="text-xs text-text-muted mt-0.5">
                        Manage your resume iterations, view diagnostic breakdowns, and designate your primary document.
                      </p>
                    </div>
                    <Button variant="primary" size="sm" onClick={() => setIsUploadModalOpen(true)}>
                      Upload New Version
                    </Button>
                  </div>

                  {resumes.length === 0 ? (
                    <EmptyState
                      title="No resumes uploaded yet"
                      description="Upload your latest PDF or DOCX resume to extract structured data and generate a 100-point diagnostic analysis."
                      action={
                        <Button variant="primary" onClick={() => setIsUploadModalOpen(true)}>
                          Upload Resume
                        </Button>
                      }
                    />
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      {resumes.map((res) => {
                        return (
                          <div
                            key={res.id}
                            className={`p-5 rounded-2xl bg-bg-surface border transition-all flex flex-col justify-between space-y-4 hover:shadow-sm ${
                              res.isPrimary
                                ? 'border-brand-teal ring-1 ring-brand-teal/40'
                                : 'border-border-default hover:border-brand-teal/40'
                            }`}
                          >
                            <div className="space-y-2">
                              <div className="flex items-start justify-between gap-2">
                                <div className="min-w-0">
                                  <h4 className="text-sm font-bold text-text-primary truncate">
                                    {res.title || res.originalFileName}
                                  </h4>
                                  <p className="text-[11px] text-text-muted mt-0.5 truncate">
                                    {res.originalFileName} • {(res.fileSizeBytes / 1024).toFixed(1)} KB
                                  </p>
                                </div>
                                <Badge variant="teal" size="xs">
                                  v{res.versionNumber}
                                </Badge>
                              </div>

                              <div className="flex items-center gap-2 flex-wrap pt-1">
                                {res.isPrimary ? (
                                  <Badge variant="gold" size="xs" icon={<span>★</span>}>
                                    Primary Active
                                  </Badge>
                                ) : null}
                                <Badge
                                  variant={res.processingStatus === 'PARSED' ? 'success' : 'warning'}
                                  size="xs"
                                >
                                  {res.processingStatus}
                                </Badge>
                                {res.score != null && (
                                  <Badge variant={getScoreVariant(res.score)} size="xs">
                                    Score: {res.score}/100
                                  </Badge>
                                )}
                              </div>
                            </div>

                            <div className="pt-3 border-t border-border-subtle flex items-center justify-between text-xs text-text-muted">
                              <span>{new Date(res.createdAt).toLocaleDateString()}</span>
                              <Button
                                variant="secondary"
                                size="sm"
                                onClick={() => handleSelectResume(res.id)}
                              >
                                View Diagnostics →
                              </Button>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 3: SKILLS INVENTORY */}
              {activeTab === 'skills' && (
                <div className="space-y-4">
                  {/* Top Bar with Category Filter & Search */}
                  <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
                    <div className="flex flex-wrap items-center gap-2">
                      <input
                        type="text"
                        placeholder="Search skills..."
                        value={skillSearch}
                        onChange={(e) => setSkillSearch(e.target.value)}
                        className="px-3 py-1.5 text-xs rounded-xl bg-bg-surface border border-border-default text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal w-44"
                      />
                      <select
                        value={skillCategoryFilter}
                        onChange={(e) => setSkillCategoryFilter(e.target.value)}
                        className="px-3 py-1.5 text-xs rounded-xl bg-bg-surface border border-border-default text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
                      >
                        <option value="ALL">All Categories ({skills.length})</option>
                        <option value="PROGRAMMING_LANGUAGES">Programming Languages</option>
                        <option value="FRONTEND">Frontend Development</option>
                        <option value="BACKEND">Backend Development</option>
                        <option value="DATABASES">Databases & Storage</option>
                        <option value="CLOUD_DEVOPS">Cloud & DevOps</option>
                        <option value="AI_ML">AI & Machine Learning</option>
                        <option value="TESTING">Testing & QA</option>
                        <option value="TOOLS">Developer Tools</option>
                        <option value="CORE_CS">Core Computer Science</option>
                        <option value="OTHER">Other Skills</option>
                      </select>
                    </div>

                    <div className="flex items-center gap-2">
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => {
                          setEditingSkill(null);
                          setIsSkillModalOpen(true);
                        }}
                      >
                        Add Skill
                      </Button>
                    </div>
                  </div>

                  {filteredSkills.length === 0 ? (
                    <EmptyState
                      title="No skills found"
                      description={
                        skillSearch || skillCategoryFilter !== 'ALL'
                          ? 'No skills match your active filter.'
                          : 'Add skills manually or import them directly from your uploaded resume.'
                      }
                      action={
                        <Button
                          variant="secondary"
                          onClick={() => {
                            setSkillSearch('');
                            setSkillCategoryFilter('ALL');
                          }}
                        >
                          Clear Filters
                        </Button>
                      }
                    />
                  ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                      {filteredSkills.map((sk) => (
                        <div
                          key={sk.id}
                          className="p-3.5 rounded-xl bg-bg-surface border border-border-default hover:border-brand-teal/40 transition-all flex flex-col justify-between space-y-2 group"
                        >
                          <div className="flex items-start justify-between gap-2">
                            <span className="text-xs font-bold text-text-primary truncate">
                              {sk.skillName}
                            </span>
                            <Badge variant="teal" size="xs">
                              {formatProficiency(sk.proficiency)}
                            </Badge>
                          </div>

                          <div className="space-y-1">
                            <span className="text-[10px] uppercase font-semibold text-text-muted block">
                              {formatSkillCategory(sk.category)}
                            </span>
                            {sk.verificationEvidence && (
                              <p className="text-[11px] text-brand-gold truncate">
                                ✓ {sk.verificationEvidence}
                              </p>
                            )}
                          </div>

                          <div className="flex items-center justify-end gap-2 pt-2 border-t border-border-subtle opacity-90 group-hover:opacity-100">
                            <button
                              onClick={() => {
                                setEditingSkill(sk);
                                setIsSkillModalOpen(true);
                              }}
                              className="text-[11px] text-text-muted hover:text-brand-teal transition-colors"
                            >
                              Edit
                            </button>
                            <button
                              onClick={() => handleDeleteSkill(sk.id)}
                              className="text-[11px] text-text-muted hover:text-red-500 transition-colors"
                            >
                              Delete
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 4: ROLE READINESS */}
              {activeTab === 'roles' && (
                <div className="space-y-6">
                  {/* Role Selector Grid */}
                  <div>
                    <h3 className="text-xs font-bold uppercase tracking-wider text-text-secondary mb-3">
                      Select Benchmark Target Role
                    </h3>
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
                      {roles.map((r) => (
                        <button
                          key={r.roleId}
                          onClick={() => setSelectedRoleId(r.roleId)}
                          className={`p-3 rounded-xl border text-left transition-all cursor-pointer ${
                            selectedRoleId === r.roleId
                              ? 'bg-brand-teal/15 border-brand-teal text-brand-teal font-bold shadow-sm'
                              : 'bg-bg-surface border-border-default text-text-secondary hover:border-brand-teal/40'
                          }`}
                        >
                          <span className="text-xs font-bold block">{r.roleName}</span>
                          <span className="text-[10px] text-text-muted">{r.roleId}</span>
                        </button>
                      ))}
                    </div>
                  </div>

                  {/* Readiness Breakdown */}
                  {isRoleLoading ? (
                    <LoadingSkeleton count={3} height="h-28" />
                  ) : roleReadiness ? (
                    <div className="space-y-6">
                      {/* Overall Readiness Card */}
                      <div className="p-6 rounded-2xl bg-gradient-to-r from-bg-surface-elevated via-bg-surface to-bg-surface-elevated border border-border-default flex flex-col md:flex-row items-center justify-between gap-6 shadow-sm">
                        <div className="flex items-center gap-5">
                          <div className="w-20 h-20 rounded-2xl bg-brand-navy border border-brand-teal/40 flex flex-col items-center justify-center text-white shrink-0">
                            <span className="text-3xl font-extrabold text-brand-teal">
                              {roleReadiness.readinessPercentage}%
                            </span>
                            <span className="text-[9px] uppercase font-bold text-brand-gold">Readiness</span>
                          </div>
                          <div>
                            <div className="flex items-center gap-2">
                              <h3 className="text-lg font-bold text-text-primary">
                                {roleReadiness.roleName} Readiness
                              </h3>
                              <Badge variant={getScoreVariant(roleReadiness.readinessPercentage)} size="sm">
                                {roleReadiness.readinessPercentage >= 75
                                  ? 'Interview Ready'
                                  : roleReadiness.readinessPercentage >= 50
                                  ? 'On Track'
                                  : 'Significant Gap'}
                              </Badge>
                            </div>
                            <p className="text-xs text-text-muted mt-1 max-w-xl">
                              Calculated from your verified skills inventory, DSA problem solving achievements, and active resume qualifications.
                            </p>
                          </div>
                        </div>

                        {/* DSA Evidence Badge */}
                        {(roleReadiness.dsaSupportingEvidence || roleReadiness.algorithmicEvidence) && (
                          <div className="p-3.5 rounded-xl bg-brand-teal/10 border border-brand-teal/30 text-xs text-text-primary space-y-1">
                            <span className="text-[10px] font-bold uppercase text-brand-teal block">
                              Algorithmic Evidence
                            </span>
                            <p className="text-xs font-medium">
                              {roleReadiness.dsaSupportingEvidence || roleReadiness.algorithmicEvidence}
                            </p>
                          </div>
                        )}
                      </div>

                      {/* Required Skills Grid */}
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="p-5 rounded-xl bg-status-success-bg/25 border border-status-success-border/60 space-y-3">
                          <h4 className="text-xs font-bold uppercase tracking-wider text-status-success-text flex items-center gap-1.5">
                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                            </svg>
                            Required Skills Acquired ({roleReadiness.matchedRequiredSkills?.length || 0})
                          </h4>
                          <div className="flex flex-wrap gap-1.5">
                            {roleReadiness.matchedRequiredSkills?.map((s) => (
                              <Badge key={s} variant="success" size="xs">
                                {s}
                              </Badge>
                            ))}
                          </div>
                        </div>

                        <div className="p-5 rounded-xl bg-status-error-bg/25 border border-status-error-border/60 space-y-3">
                          <h4 className="text-xs font-bold uppercase tracking-wider text-status-error-text flex items-center gap-1.5">
                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
                            </svg>
                            Required Skills Missing ({roleReadiness.missingRequiredSkills?.length || 0})
                          </h4>
                          <div className="flex flex-wrap gap-1.5">
                            {roleReadiness.missingRequiredSkills?.length > 0 ? (
                              roleReadiness.missingRequiredSkills.map((s) => (
                                <Badge key={s} variant="error" size="xs">
                                  {s}
                                </Badge>
                              ))
                            ) : (
                              <span className="text-xs text-status-success-text font-medium">
                                All required skills achieved!
                              </span>
                            )}
                          </div>
                        </div>
                      </div>

                      {/* Recommended Focus Next Steps */}
                      {roleReadiness.recommendedNextSkills?.length > 0 && (
                        <div className="p-5 rounded-xl bg-bg-surface border border-border-default space-y-3">
                          <h4 className="text-xs font-bold uppercase tracking-wider text-brand-gold">
                            Recommended Next Skills to Master
                          </h4>
                          <div className="flex flex-wrap gap-2">
                            {roleReadiness.recommendedNextSkills.map((s) => (
                              <Badge key={s} variant="gold" size="sm">
                                + {s}
                              </Badge>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  ) : null}
                </div>
              )}

              {/* TAB 5: JOB MATCH DIAGNOSTICS */}
              {activeTab === 'jobs' && (
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-sm font-bold text-text-primary">Job Description Match History</h3>
                      <p className="text-xs text-text-muted mt-0.5">
                        Compare your resume and skills against real job descriptions to identify qualification gaps.
                      </p>
                    </div>
                    <Button variant="primary" size="sm" onClick={() => setIsJobModalOpen(true)}>
                      Analyze New JD
                    </Button>
                  </div>

                  {jobAnalyses.length === 0 ? (
                    <EmptyState
                      title="No job descriptions analyzed yet"
                      description="Paste a real software engineering job specification to run diagnostic keyword and qualification matching."
                      action={
                        <Button variant="primary" onClick={() => setIsJobModalOpen(true)}>
                          Analyze Job Description
                        </Button>
                      }
                    />
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                      {jobAnalyses.map((job) => {
                        const score = job.matchScore ?? job.matchPercentage ?? 0;
                        const company = job.company ?? job.companyName ?? 'Tech Company';
                        return (
                          <div
                            key={job.id}
                            className="p-5 rounded-2xl bg-bg-surface border border-border-default hover:border-brand-teal/40 transition-all flex flex-col justify-between space-y-4 shadow-sm"
                          >
                            <div className="space-y-2">
                              <div className="flex items-start justify-between gap-2">
                                <div className="min-w-0">
                                  <h4 className="text-sm font-bold text-text-primary truncate">
                                    {job.jobTitle}
                                  </h4>
                                  <p className="text-xs text-brand-teal font-medium truncate">
                                    {company}
                                  </p>
                                </div>
                                <span className="text-2xl font-extrabold text-brand-teal">
                                  {score}%
                                </span>
                              </div>

                              <div className="flex flex-wrap gap-1 pt-1">
                                <Badge variant={getScoreVariant(score)} size="xs">
                                  {job.matchedRequiredSkills?.length || 0} req matched
                                </Badge>
                                {job.missingRequiredSkills?.length > 0 && (
                                  <Badge variant="error" size="xs">
                                    {job.missingRequiredSkills.length} missing
                                  </Badge>
                                )}
                              </div>
                            </div>

                            <div className="pt-3 border-t border-border-subtle flex items-center justify-between text-xs text-text-muted">
                              <span>{new Date(job.createdAt).toLocaleDateString()}</span>
                              <div className="flex items-center gap-3">
                                <button
                                  onClick={() => {
                                    setViewJobAnalysis(job);
                                    setIsJobModalOpen(true);
                                  }}
                                  className="text-[11px] font-semibold text-brand-teal hover:underline transition-colors"
                                >
                                  View Diagnostics
                                </button>
                                <button
                                  onClick={() => handleDeleteJobAnalysis(job.id)}
                                  className="text-[11px] text-text-muted hover:text-red-500 transition-colors"
                                >
                                  Delete
                                </button>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}

              {/* TAB 6: ACTION PLAN */}
              {activeTab === 'actions' && (
                <div className="space-y-4">
                  <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
                    <div className="flex items-center gap-2">
                      <select
                        value={actionStatusFilter}
                        onChange={(e) => setActionStatusFilter(e.target.value)}
                        className="px-3 py-1.5 text-xs rounded-xl bg-bg-surface border border-border-default text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
                      >
                        <option value="ALL">All Statuses ({actions.length})</option>
                        <option value="NOT_STARTED">Not Started</option>
                        <option value="IN_PROGRESS">In Progress</option>
                        <option value="COMPLETED">Completed</option>
                      </select>
                    </div>

                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => {
                        setEditingAction(null);
                        setIsActionModalOpen(true);
                      }}
                    >
                      New Action Goal
                    </Button>
                  </div>

                  {filteredActions.length === 0 ? (
                    <EmptyState
                      title="No action goals found"
                      description="Create structured career milestones to close skill gaps and prepare for tech interviews."
                      action={
                        <Button
                          variant="primary"
                          onClick={() => {
                            setEditingAction(null);
                            setIsActionModalOpen(true);
                          }}
                        >
                          Add Action Item
                        </Button>
                      }
                    />
                  ) : (
                    <div className="space-y-3">
                      {filteredActions.map((action) => {
                        const priorityInfo = formatActionPriority(action.priority);
                        const statusInfo = formatActionStatus(action.status);
                        const isDone = action.status === 'COMPLETED';

                        return (
                          <div
                            key={action.id}
                            className={`p-4 rounded-xl bg-bg-surface border transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4 ${
                              isDone
                                ? 'border-status-success-border/40 bg-bg-surface/60'
                                : 'border-border-default hover:border-brand-teal/40'
                            }`}
                          >
                            <div className="flex items-start gap-3 min-w-0">
                              <input
                                type="checkbox"
                                checked={isDone}
                                onChange={() => handleToggleActionStatus(action)}
                                className="mt-1 rounded text-brand-teal focus:ring-brand-teal cursor-pointer"
                              />
                              <div className="min-w-0">
                                <h4
                                  className={`text-sm font-bold ${
                                    isDone ? 'line-through text-text-muted' : 'text-text-primary'
                                  }`}
                                >
                                  {action.title}
                                </h4>
                                {action.description && (
                                  <p className="text-xs text-text-secondary mt-0.5 leading-relaxed">
                                    {action.description}
                                  </p>
                                )}
                                <div className="flex items-center gap-2 mt-2 flex-wrap">
                                  <Badge variant={priorityInfo.variant} size="xs">
                                    {priorityInfo.label}
                                  </Badge>
                                  <Badge variant={statusInfo.variant} size="xs">
                                    {statusInfo.label}
                                  </Badge>
                                  <span className="text-[11px] text-text-muted">
                                    {formatActionCategory(action.category)}
                                  </span>
                                  {action.targetRoleOrSkill && (
                                    <span className="text-[11px] text-brand-teal font-semibold">
                                      Target: {action.targetRoleOrSkill}
                                    </span>
                                  )}
                                  {action.dueDate && (
                                    <span className="text-[11px] text-text-muted">
                                      Due: {action.dueDate.substring(0, 10)}
                                    </span>
                                  )}
                                  {action.category === 'DSA_PRACTICE' && (
                                    <Link
                                      to="/dsa"
                                      className="text-xs font-semibold text-brand-teal hover:underline"
                                    >
                                      Practice on DSA Tracker ↗
                                    </Link>
                                  )}
                                </div>
                              </div>
                            </div>

                            <div className="flex items-center gap-2 self-end sm:self-center shrink-0">
                              <button
                                onClick={() => {
                                  setEditingAction(action);
                                  setIsActionModalOpen(true);
                                }}
                                className="text-xs text-text-muted hover:text-brand-teal font-medium"
                              >
                                Edit
                              </button>
                              <button
                                onClick={() => handleDeleteAction(action.id)}
                                className="text-xs text-text-muted hover:text-red-500 font-medium"
                              >
                                Delete
                              </button>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}
            </>
          )}
        </>
      )}

      {/* Modals */}
      <ResumeUploadModal
        isOpen={isUploadModalOpen}
        onClose={() => setIsUploadModalOpen(false)}
        onSuccess={() => {
          loadDashboardData();
          setActiveTab('resumes');
        }}
      />

      <AddEditSkillModal
        isOpen={isSkillModalOpen}
        onClose={() => {
          setIsSkillModalOpen(false);
          setEditingSkill(null);
        }}
        initialData={editingSkill}
        onSuccess={() => loadDashboardData()}
      />

      <AddEditActionModal
        isOpen={isActionModalOpen}
        onClose={() => {
          setIsActionModalOpen(false);
          setEditingAction(null);
        }}
        initialData={editingAction}
        onSuccess={() => loadDashboardData()}
      />

      <JobDescriptionModal
        isOpen={isJobModalOpen}
        onClose={() => {
          setIsJobModalOpen(false);
          setViewJobAnalysis(null);
        }}
        resumes={resumes}
        viewJob={viewJobAnalysis}
        onSuccess={() => {
          loadDashboardData();
          setActiveTab('jobs');
        }}
      />
    </div>
  );
}
