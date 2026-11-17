import React, { useState, useEffect } from 'react';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import Alert from '../ui/Alert';
import Card from '../ui/Card';
import EditExtractedDataModal from './EditExtractedDataModal';
import {
  downloadResumeApi,
  reanalyzeResumeApi,
  setPrimaryResumeApi,
  deleteResumeApi,
  importSkillsFromResumeApi,
  fetchRoleReadiness,
  fetchResumeDetail,
} from '../../services/api';
import { getScoreVariant } from '../../utils/careerValidation';

export default function ResumeDetailView({
  resume,
  allResumes = [],
  onBack,
  onRefresh,
}) {
  const [activeTab, setActiveTab] = useState('rubric'); // 'rubric' | 'benchmark' | 'compare' | 'extracted' | 'raw'
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [isImporting, setIsImporting] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [feedbackMsg, setFeedbackMsg] = useState(null);
  const [errorMsg, setErrorMsg] = useState(null);

  // Role benchmark simulator state
  const [selectedRoleId, setSelectedRoleId] = useState('sde');
  const [roleReadiness, setRoleReadiness] = useState(null);
  const [isLoadingRole, setIsLoadingRole] = useState(false);

  // Version comparison state
  const otherResumes = allResumes.filter((r) => r.id !== resume.id);
  const [compareResumeId, setCompareResumeId] = useState(otherResumes.length > 0 ? otherResumes[0].id : null);
  const [compareResumeDetail, setCompareResumeDetail] = useState(null);
  const [isLoadingCompare, setIsLoadingCompare] = useState(false);

  useEffect(() => {
    if (activeTab === 'benchmark') {
      loadRoleBenchmark(selectedRoleId);
    }
  }, [activeTab, selectedRoleId]);

  useEffect(() => {
    if (activeTab === 'compare' && compareResumeId) {
      loadCompareResume(compareResumeId);
    }
  }, [activeTab, compareResumeId]);

  const loadRoleBenchmark = async (roleId) => {
    setIsLoadingRole(true);
    try {
      const data = await fetchRoleReadiness(roleId);
      setRoleReadiness(data);
    } catch (_) {
    } finally {
      setIsLoadingRole(false);
    }
  };

  const loadCompareResume = async (targetId) => {
    setIsLoadingCompare(true);
    try {
      const data = await fetchResumeDetail(targetId);
      setCompareResumeDetail(data);
    } catch (_) {
    } finally {
      setIsLoadingCompare(false);
    }
  };

  if (!resume) return null;

  const analysis = resume.latestAnalysis;
  const extracted = resume.extractedData || {};

  const handleDownload = async () => {
    setIsDownloading(true);
    setErrorMsg(null);
    try {
      await downloadResumeApi(resume.id, resume.originalFileName);
    } catch (err) {
      setErrorMsg(err.message || 'Failed to download resume file.');
    } finally {
      setIsDownloading(false);
    }
  };

  const handleReanalyze = async () => {
    setIsAnalyzing(true);
    setErrorMsg(null);
    setFeedbackMsg(null);
    try {
      await reanalyzeResumeApi(resume.id);
      setFeedbackMsg('100-point diagnostic analysis refreshed successfully.');
      if (onRefresh) onRefresh();
    } catch (err) {
      setErrorMsg(err.message || 'Failed to re-run analysis.');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const handleSetPrimary = async () => {
    setErrorMsg(null);
    setFeedbackMsg(null);
    try {
      await setPrimaryResumeApi(resume.id);
      setFeedbackMsg('This resume version is now marked as your primary active resume.');
      if (onRefresh) onRefresh();
    } catch (err) {
      setErrorMsg(err.message || 'Failed to set as primary.');
    }
  };

  const handleImportSkills = async () => {
    setIsImporting(true);
    setErrorMsg(null);
    setFeedbackMsg(null);
    try {
      const result = await importSkillsFromResumeApi(resume.id);
      const count = Array.isArray(result) ? result.length : (result.importedCount || 0);
      setFeedbackMsg(`Successfully imported ${count} skills into your Skills Inventory!`);
      if (onRefresh) onRefresh();
    } catch (err) {
      setErrorMsg(err.message || 'Failed to import skills.');
    } finally {
      setIsImporting(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm(`Are you sure you want to delete "${resume.title || resume.originalFileName}"? This action cannot be undone.`)) {
      return;
    }
    setErrorMsg(null);
    try {
      await deleteResumeApi(resume.id);
      if (onBack) onBack();
    } catch (err) {
      setErrorMsg(err.message || 'Failed to delete resume.');
    }
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* Top Navigation & Action Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-border-default">
        <div className="flex items-center gap-3">
          <button
            onClick={onBack}
            className="p-2 rounded-xl border border-border-default bg-bg-surface hover:bg-bg-surface-hover text-text-secondary hover:text-text-primary transition-colors cursor-pointer"
            title="Back to Resumes"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
            </svg>
          </button>
          <div>
            <div className="flex items-center gap-2.5 flex-wrap">
              <h2 className="text-xl font-bold text-text-primary">
                {resume.title || resume.originalFileName}
              </h2>
              <Badge variant="teal" size="xs">v{resume.versionNumber}</Badge>
              {resume.isPrimary && (
                <Badge variant="gold" size="xs" icon={<span>★</span>}>
                  Primary Resume
                </Badge>
              )}
              <Badge variant={resume.processingStatus === 'PARSED' ? 'success' : 'warning'} size="xs">
                {resume.processingStatus}
              </Badge>
            </div>
            <p className="text-xs text-text-muted mt-1">
              {resume.originalFileName} • {(resume.fileSizeBytes / 1024).toFixed(1)} KB • Uploaded on{' '}
              {new Date(resume.createdAt).toLocaleDateString()}
            </p>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-wrap items-center gap-2">
          {!resume.isPrimary && (
            <Button variant="secondary" size="sm" onClick={handleSetPrimary}>
              Set as Primary
            </Button>
          )}
          <Button
            variant="secondary"
            size="sm"
            onClick={handleDownload}
            isLoading={isDownloading}
            icon={
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
            }
          >
            Download
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={handleImportSkills}
            isLoading={isImporting}
            icon={
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 4H6a2 2 0 00-2 2v12a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-2m-4-1v8m0 0l3-3m-3 3L9 8" />
              </svg>
            }
          >
            Import Skills
          </Button>
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setIsEditModalOpen(true)}
            icon={
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
            }
          >
            Edit Info
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={handleReanalyze}
            isLoading={isAnalyzing}
            icon={
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
              </svg>
            }
          >
            Re-Analyze
          </Button>
          <Button
            variant="destructive"
            size="sm"
            onClick={handleDelete}
            icon={
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>
            }
          >
            Delete
          </Button>
        </div>
      </div>

      {/* Notifications */}
      {feedbackMsg && (
        <Alert variant="success" dismissible onDismiss={() => setFeedbackMsg(null)}>
          {feedbackMsg}
        </Alert>
      )}
      {errorMsg && (
        <Alert variant="error" dismissible onDismiss={() => setErrorMsg(null)}>
          {errorMsg}
        </Alert>
      )}

      {/* View Switcher Tabs */}
      <div className="flex border-b border-border-default gap-6 text-sm font-medium overflow-x-auto">
        <button
          onClick={() => setActiveTab('rubric')}
          className={`pb-3 border-b-2 transition-colors cursor-pointer shrink-0 ${
            activeTab === 'rubric'
              ? 'border-brand-teal text-brand-teal font-semibold'
              : 'border-transparent text-text-muted hover:text-text-primary'
          }`}
        >
          100-Point Rubric Diagnostics
        </button>
        <button
          onClick={() => setActiveTab('benchmark')}
          className={`pb-3 border-b-2 transition-colors cursor-pointer shrink-0 ${
            activeTab === 'benchmark'
              ? 'border-brand-teal text-brand-teal font-semibold'
              : 'border-transparent text-text-muted hover:text-text-primary'
          }`}
        >
          Role Benchmark Simulator
        </button>
        <button
          onClick={() => setActiveTab('compare')}
          className={`pb-3 border-b-2 transition-colors cursor-pointer shrink-0 ${
            activeTab === 'compare'
              ? 'border-brand-teal text-brand-teal font-semibold'
              : 'border-transparent text-text-muted hover:text-text-primary'
          }`}
        >
          Version Comparison {otherResumes.length > 0 ? `(${otherResumes.length})` : ''}
        </button>
        <button
          onClick={() => setActiveTab('extracted')}
          className={`pb-3 border-b-2 transition-colors cursor-pointer shrink-0 ${
            activeTab === 'extracted'
              ? 'border-brand-teal text-brand-teal font-semibold'
              : 'border-transparent text-text-muted hover:text-text-primary'
          }`}
        >
          Parsed Profile & Sections ({extracted.skills?.length || 0} skills)
        </button>
        <button
          onClick={() => setActiveTab('raw')}
          className={`pb-3 border-b-2 transition-colors cursor-pointer shrink-0 ${
            activeTab === 'raw'
              ? 'border-brand-teal text-brand-teal font-semibold'
              : 'border-transparent text-text-muted hover:text-text-primary'
          }`}
        >
          Raw Text Preview
        </button>
      </div>

      {/* TAB 1: 100-POINT RUBRIC DIAGNOSTICS */}
      {activeTab === 'rubric' && (
        <div className="space-y-6">
          {analysis ? (
            <>
              {/* Score Summary Banner */}
              <div className="p-6 rounded-2xl bg-gradient-to-r from-bg-surface-elevated via-bg-surface to-bg-surface-elevated border border-border-default flex flex-col md:flex-row items-center justify-between gap-6 shadow-sm">
                <div className="flex items-center gap-5">
                  <div className="w-20 h-20 rounded-2xl bg-brand-navy border border-brand-teal/40 flex flex-col items-center justify-center text-white shadow-inner shrink-0">
                    <span className="text-3xl font-extrabold text-brand-gold">{analysis.overallScore}</span>
                    <span className="text-[10px] text-brand-teal uppercase font-semibold">/ 100 pts</span>
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="text-lg font-bold text-text-primary">Resume Readiness Score</h3>
                      <Badge variant={getScoreVariant(analysis.overallScore)} size="sm">
                        {analysis.overallScore >= 80 ? 'Production Ready' : analysis.overallScore >= 65 ? 'Competitive' : 'Needs Polish'}
                      </Badge>
                    </div>
                    <p className="text-xs text-text-muted mt-1 max-w-xl">
                      Transparent 7-dimension scoring evaluated across modern software engineering standards, verified project impact, and ATS best practices.
                    </p>
                  </div>
                </div>

                <div className="text-xs text-text-muted text-right space-y-1 self-end md:self-auto">
                  <div>Model: <span className="font-medium text-text-primary">ScholarTwin Hybrid Rubric</span></div>
                  <div>Analyzed: <span className="font-medium text-text-primary">{new Date(analysis.analyzedAt).toLocaleString()}</span></div>
                </div>
              </div>

              {/* 7 Dimensions Grid */}
              <div>
                <h4 className="text-sm font-bold uppercase tracking-wider text-text-secondary mb-3">
                  Dimension Breakdown
                </h4>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {analysis.dimensionScores &&
                    Object.entries(analysis.dimensionScores).map(([key, dim]) => (
                      <div
                        key={key}
                        className="p-4 rounded-xl bg-bg-surface border border-border-default hover:border-brand-teal/40 transition-all space-y-2.5"
                      >
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-bold text-text-primary">{dim.dimensionName}</span>
                          <span className="text-xs font-extrabold text-brand-teal">
                            {dim.earnedScore} / {dim.weight}
                          </span>
                        </div>
                        {/* Progress Bar */}
                        <div className="w-full bg-bg-surface-elevated rounded-full h-1.5 overflow-hidden">
                          <div
                            className="bg-brand-teal h-1.5 rounded-full"
                            style={{ width: `${Math.round((dim.earnedScore / dim.weight) * 100)}%` }}
                          />
                        </div>
                        {/* Evidence Badge */}
                        <p className="text-[11px] text-text-muted leading-relaxed">{dim.evidence}</p>
                      </div>
                    ))}
                </div>
              </div>

              {/* Strengths & Weaknesses */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="p-5 rounded-xl bg-status-success-bg/25 border border-status-success-border/60 space-y-3">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-status-success-text flex items-center gap-2">
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                    </svg>
                    Demonstrated Strengths ({analysis.strengths?.length || 0})
                  </h4>
                  <ul className="space-y-2">
                    {analysis.strengths?.map((st, i) => (
                      <li key={i} className="text-xs text-text-primary flex items-start gap-2">
                        <span className="text-status-success-text font-bold shrink-0">✓</span>
                        <span>{st}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="p-5 rounded-xl bg-status-warning-bg/25 border border-status-warning-border/60 space-y-3">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-status-warning-text flex items-center gap-2">
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                    Areas for Improvement ({analysis.weaknesses?.length || 0})
                  </h4>
                  <ul className="space-y-2">
                    {analysis.weaknesses?.map((wk, i) => (
                      <li key={i} className="text-xs text-text-primary flex items-start gap-2">
                        <span className="text-status-warning-text font-bold shrink-0">!</span>
                        <span>{wk}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>

              {/* Missing Information & Recommendations */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {analysis.missingInformation?.length > 0 && (
                  <div className="p-5 rounded-xl bg-status-error-bg/20 border border-status-error-border/50 space-y-3">
                    <h4 className="text-xs font-bold uppercase tracking-wider text-status-error-text">
                      Missing Information Identified
                    </h4>
                    <ul className="space-y-2">
                      {analysis.missingInformation.map((item, i) => (
                        <li key={i} className="text-xs text-text-primary flex items-start gap-2">
                          <span className="text-status-error-text font-bold shrink-0">•</span>
                          <span>{item}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}

                <div className="p-5 rounded-xl bg-bg-surface border border-border-default space-y-3">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-brand-teal">
                    Actionable Next Steps
                  </h4>
                  <ul className="space-y-2">
                    {analysis.recommendations?.map((rec, i) => (
                      <li key={i} className="text-xs text-text-secondary flex items-start gap-2">
                        <span className="text-brand-gold font-bold shrink-0">→</span>
                        <span>{rec}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>
            </>
          ) : (
            <Card className="text-center py-10 space-y-3">
              <p className="text-sm text-text-muted">No diagnostic analysis generated yet for this resume.</p>
              <Button variant="primary" size="sm" onClick={handleReanalyze} isLoading={isAnalyzing}>
                Run 100-Point Diagnostic Analysis
              </Button>
            </Card>
          )}
        </div>
      )}

      {/* TAB 2: ROLE BENCHMARK SIMULATOR */}
      {activeTab === 'benchmark' && (
        <div className="space-y-6">
          <div className="p-5 rounded-2xl bg-bg-surface border border-border-default flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm">
            <div>
              <h3 className="text-sm font-bold text-text-primary">8 Benchmark Roles Simulator</h3>
              <p className="text-xs text-text-muted mt-0.5">
                Simulate how this resume matches industry benchmarks across 4 skill dimensions.
              </p>
            </div>
            <div className="w-full sm:w-auto">
              <select
                value={selectedRoleId}
                onChange={(e) => setSelectedRoleId(e.target.value)}
                className="w-full sm:w-64 px-3 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-brand-teal"
              >
                <option value="sde">Software Development Engineer (SDE)</option>
                <option value="java-developer">Java Developer</option>
                <option value="full-stack">Full Stack Developer</option>
                <option value="frontend-developer">Frontend Developer</option>
                <option value="backend-developer">Backend Developer</option>
                <option value="ai-ml-engineer">AI/ML Engineer</option>
                <option value="data-analyst">Data Analyst</option>
                <option value="devops-engineer">DevOps Engineer</option>
              </select>
            </div>
          </div>

          {isLoadingRole ? (
            <div className="py-12 text-center text-xs text-text-muted">Loading role benchmark evaluation...</div>
          ) : roleReadiness ? (
            <>
              {/* Role Score Header */}
              <div className="p-6 rounded-2xl bg-gradient-to-r from-bg-surface-elevated via-bg-surface to-bg-surface-elevated border border-border-default flex flex-col md:flex-row items-center justify-between gap-6 shadow-sm">
                <div>
                  <div className="flex items-center gap-3">
                    <span className="text-3xl font-extrabold text-brand-teal">{roleReadiness.readinessPercentage}%</span>
                    <Badge variant={getScoreVariant(roleReadiness.readinessPercentage)} size="sm">
                      {roleReadiness.readinessPercentage >= 75 ? 'Role-Ready' : roleReadiness.readinessPercentage >= 50 ? 'Moderate Readiness' : 'Skills Gap'}
                    </Badge>
                  </div>
                  <h4 className="text-sm font-bold text-text-primary mt-1">{roleReadiness.roleName} Readiness</h4>
                  <p className="text-xs text-text-muted mt-0.5">
                    {roleReadiness.requiredMatchedCount} of {roleReadiness.requiredTotalCount} required skills matched • {roleReadiness.preferredMatchedCount} of {roleReadiness.preferredTotalCount} preferred skills matched
                  </p>
                </div>
                {roleReadiness.dsaSupportingEvidence && (
                  <div className="p-3 rounded-xl bg-brand-navy/30 border border-brand-teal/30 text-xs text-text-secondary max-w-sm">
                    <span className="text-brand-gold font-bold block mb-1">Algorithmic Evidence:</span>
                    {roleReadiness.dsaSupportingEvidence}
                  </div>
                )}
              </div>

              {/* 4 Categorized Skill Dimension Cards */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* 1. Resume Skills */}
                <div className="p-4 rounded-xl bg-bg-surface border border-border-default space-y-2.5">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-text-primary flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-status-success"></span>
                    Resume Skills ({roleReadiness.resumeSkills?.length || 0})
                  </h4>
                  <p className="text-[11px] text-text-muted">Skills explicitly detected in this resume document.</p>
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {roleReadiness.resumeSkills?.length > 0 ? (
                      roleReadiness.resumeSkills.map((s) => (
                        <Badge key={s} variant="success" size="xs">{s}</Badge>
                      ))
                    ) : (
                      <span className="text-xs text-text-muted">None detected</span>
                    )}
                  </div>
                </div>

                {/* 2. Verified Inventory Skills */}
                <div className="p-4 rounded-xl bg-bg-surface border border-border-default space-y-2.5">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-brand-teal flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-brand-teal"></span>
                    Verified Profile Inventory Skills ({roleReadiness.verifiedSkills?.length || 0})
                  </h4>
                  <p className="text-[11px] text-text-muted">Validated competencies in your ScholarTwin career profile.</p>
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {roleReadiness.verifiedSkills?.length > 0 ? (
                      roleReadiness.verifiedSkills.map((s) => (
                        <Badge key={s} variant="teal" size="xs">{s}</Badge>
                      ))
                    ) : (
                      <span className="text-xs text-text-muted">None saved</span>
                    )}
                  </div>
                </div>

                {/* 3. Inferred Skills */}
                <div className="p-4 rounded-xl bg-bg-surface border border-border-default space-y-2.5">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-brand-gold flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-brand-gold"></span>
                    Inferred Skills ({roleReadiness.inferredSkills?.length || 0})
                  </h4>
                  <p className="text-[11px] text-text-muted">Inferred from technical projects & DSA coding problem solving.</p>
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {roleReadiness.inferredSkills?.length > 0 ? (
                      roleReadiness.inferredSkills.map((s) => (
                        <Badge key={s} variant="gold" size="xs">{s}</Badge>
                      ))
                    ) : (
                      <span className="text-xs text-text-muted">Solve problems in DSA Tracker to earn inferred evidence</span>
                    )}
                  </div>
                </div>

                {/* 4. Missing Skills */}
                <div className="p-4 rounded-xl bg-bg-surface border border-border-default space-y-2.5">
                  <h4 className="text-xs font-bold uppercase tracking-wider text-status-error-text flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-status-error"></span>
                    Missing Skills for Role ({roleReadiness.missingSkills?.length || 0})
                  </h4>
                  <p className="text-[11px] text-text-muted">Key competencies required or preferred for this target benchmark.</p>
                  <div className="flex flex-wrap gap-1.5 pt-1">
                    {roleReadiness.missingSkills?.length > 0 ? (
                      roleReadiness.missingSkills.map((s) => (
                        <Badge key={s} variant="error" size="xs">{s}</Badge>
                      ))
                    ) : (
                      <span className="text-xs text-status-success-text font-semibold">All benchmark competencies satisfied!</span>
                    )}
                  </div>
                </div>
              </div>
            </>
          ) : null}
        </div>
      )}

      {/* TAB 3: VERSION COMPARISON */}
      {activeTab === 'compare' && (
        <div className="space-y-6">
          {otherResumes.length === 0 ? (
            <Card className="text-center py-10 space-y-3">
              <div className="w-12 h-12 rounded-2xl bg-brand-teal/15 text-brand-teal mx-auto flex items-center justify-center">
                <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                </svg>
              </div>
              <h3 className="text-sm font-bold text-text-primary">Single Resume Version</h3>
              <p className="text-xs text-text-muted max-w-sm mx-auto">
                Upload a revised version of your resume to compare before-and-after scores, newly acquired skills, and improvements side-by-side.
              </p>
            </Card>
          ) : (
            <div className="space-y-6">
              <div className="p-5 rounded-2xl bg-bg-surface border border-border-default flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm">
                <div>
                  <h3 className="text-sm font-bold text-text-primary">Resume Version Comparison</h3>
                  <p className="text-xs text-text-muted mt-0.5">
                    Compare Current v{resume.versionNumber} against previous revisions.
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-text-muted font-medium">Compare with:</span>
                  <select
                    value={compareResumeId || ''}
                    onChange={(e) => setCompareResumeId(Number(e.target.value))}
                    className="px-3 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-brand-teal"
                  >
                    {otherResumes.map((r) => (
                      <option key={r.id} value={r.id}>
                        {r.title || r.originalFileName} (v{r.versionNumber})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {isLoadingCompare ? (
                <div className="py-12 text-center text-xs text-text-muted">Loading version comparison...</div>
              ) : compareResumeDetail && compareResumeDetail.latestAnalysis ? (
                <>
                  {/* Delta Score Header */}
                  {(() => {
                    const currentScore = analysis?.overallScore || 0;
                    const prevScore = compareResumeDetail.latestAnalysis?.overallScore || 0;
                    const delta = currentScore - prevScore;
                    return (
                      <div className="p-6 rounded-2xl bg-bg-surface border border-border-default flex flex-col sm:flex-row items-center justify-between gap-6 shadow-sm">
                        <div className="flex items-center gap-6">
                          <div className="text-center">
                            <span className="text-[11px] uppercase tracking-wider text-text-muted block">Current v{resume.versionNumber}</span>
                            <span className="text-3xl font-extrabold text-brand-teal">{currentScore}</span>
                          </div>
                          <span className="text-xl text-text-muted font-bold">vs</span>
                          <div className="text-center">
                            <span className="text-[11px] uppercase tracking-wider text-text-muted block">v{compareResumeDetail.versionNumber}</span>
                            <span className="text-3xl font-extrabold text-text-secondary">{prevScore}</span>
                          </div>
                        </div>

                        <div>
                          <Badge variant={delta >= 0 ? 'success' : 'error'} size="md">
                            {delta >= 0 ? `+${delta} pts Score Improvement` : `${delta} pts Lower`}
                          </Badge>
                        </div>
                      </div>
                    );
                  })()}

                  {/* Dimension Comparison Table */}
                  <div className="p-5 rounded-2xl bg-bg-surface border border-border-default space-y-3">
                    <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
                      Dimension-by-Dimension Progress
                    </h4>
                    <div className="overflow-x-auto">
                      <table className="w-full text-xs text-left">
                        <thead className="text-[11px] uppercase text-text-muted border-b border-border-subtle">
                          <tr>
                            <th className="py-2 font-semibold">Rubric Dimension</th>
                            <th className="py-2 text-center font-semibold">v{resume.versionNumber} (Current)</th>
                            <th className="py-2 text-center font-semibold">v{compareResumeDetail.versionNumber}</th>
                            <th className="py-2 text-right font-semibold">Delta</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-border-subtle">
                          {analysis?.dimensionBreakdown?.map((dim) => {
                            const prevDim = compareResumeDetail.latestAnalysis?.dimensionBreakdown?.find(
                              (d) => d.dimensionId === dim.dimensionId
                            );
                            const prevScore = prevDim ? prevDim.earnedScore : 0;
                            const diff = dim.earnedScore - prevScore;
                            return (
                              <tr key={dim.dimensionId} className="hover:bg-bg-surface-elevated/40">
                                <td className="py-2.5 font-medium text-text-primary">{dim.dimensionName}</td>
                                <td className="py-2.5 text-center font-bold text-brand-teal">{dim.earnedScore} / {dim.maxScore}</td>
                                <td className="py-2.5 text-center text-text-muted">{prevScore} / {dim.maxScore}</td>
                                <td className="py-2.5 text-right font-bold">
                                  <span className={diff > 0 ? 'text-status-success' : diff < 0 ? 'text-status-error' : 'text-text-muted'}>
                                    {diff > 0 ? `+${diff}` : diff}
                                  </span>
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </>
              ) : null}
            </div>
          )}
        </div>
      )}

      {/* TAB 4: EXTRACTED SECTIONS */}
      {activeTab === 'extracted' && (
        <div className="space-y-6">
          {/* Contact Details Card */}
          <div className="p-5 rounded-xl bg-bg-surface border border-border-default space-y-4">
            <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
              Contact & Digital Presence
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 text-xs">
              <div>
                <span className="text-text-muted block text-[11px]">Name</span>
                <span className="font-semibold text-text-primary">{extracted.fullName || 'Not detected'}</span>
              </div>
              <div>
                <span className="text-text-muted block text-[11px]">Email</span>
                <span className="font-semibold text-text-primary">{extracted.email || 'Not detected'}</span>
              </div>
              <div>
                <span className="text-text-muted block text-[11px]">Phone</span>
                <span className="font-semibold text-text-primary">{extracted.phone || 'Not detected'}</span>
              </div>
              <div>
                <span className="text-text-muted block text-[11px]">Location</span>
                <span className="font-semibold text-text-primary">{extracted.location || 'Not detected'}</span>
              </div>
            </div>

            <div className="flex flex-wrap gap-4 pt-2 border-t border-border-subtle text-xs">
              {extracted.githubUrl && (
                <a
                  href={extracted.githubUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-brand-teal hover:underline flex items-center gap-1.5"
                >
                  <span>GitHub:</span> {extracted.githubUrl}
                </a>
              )}
              {extracted.linkedinUrl && (
                <a
                  href={extracted.linkedinUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-brand-teal hover:underline flex items-center gap-1.5"
                >
                  <span>LinkedIn:</span> {extracted.linkedinUrl}
                </a>
              )}
              {extracted.portfolioUrl && (
                <a
                  href={extracted.portfolioUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="text-brand-teal hover:underline flex items-center gap-1.5"
                >
                  <span>Portfolio:</span> {extracted.portfolioUrl}
                </a>
              )}
            </div>
          </div>

          {/* Professional Summary */}
          {extracted.summary && (
            <div className="p-5 rounded-xl bg-bg-surface border border-border-default space-y-2">
              <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
                Professional Summary / Objective
              </h4>
              <p className="text-xs text-text-secondary leading-relaxed whitespace-pre-line">
                {extracted.summary}
              </p>
            </div>
          )}

          {/* Extracted Skills */}
          <div className="p-5 rounded-xl bg-bg-surface border border-border-default space-y-3">
            <div className="flex items-center justify-between">
              <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
                Extracted Technical & Domain Skills ({extracted.skills?.length || 0})
              </h4>
              <Button variant="secondary" size="sm" onClick={handleImportSkills} isLoading={isImporting}>
                Import All to Inventory
              </Button>
            </div>
            <div className="flex flex-wrap gap-1.5">
              {extracted.skills?.length > 0 ? (
                extracted.skills.map((skill) => (
                  <Badge key={skill} variant="teal" size="sm">
                    {skill}
                  </Badge>
                ))
              ) : (
                <span className="text-xs text-text-muted">No skills parsed from this document.</span>
              )}
            </div>
          </div>

          {/* Experience List */}
          {extracted.experience?.length > 0 && (
            <div className="p-5 rounded-xl bg-bg-surface border border-border-default space-y-4">
              <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
                Work Experience ({extracted.experience.length})
              </h4>
              <div className="space-y-4">
                {extracted.experience.map((exp, idx) => (
                  <div key={idx} className="pb-3 border-b border-border-subtle last:border-0 last:pb-0">
                    <div className="flex justify-between items-baseline flex-wrap gap-1">
                      <span className="text-xs font-bold text-text-primary">{exp.role || exp.title}</span>
                      <span className="text-[11px] text-text-muted">{exp.duration || exp.dates}</span>
                    </div>
                    <span className="text-xs text-brand-teal font-medium block">{exp.company}</span>
                    {exp.description && (
                      <p className="text-xs text-text-secondary mt-1 whitespace-pre-line leading-relaxed">
                        {exp.description}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Projects List */}
          {extracted.projects?.length > 0 && (
            <div className="p-5 rounded-xl bg-bg-surface border border-border-default space-y-4">
              <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
                Projects ({extracted.projects.length})
              </h4>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {extracted.projects.map((proj, idx) => {
                  const tech = proj.technologies || proj.techStack;
                  const projectLink = proj.url || proj.liveUrl || proj.githubUrl;
                  return (
                    <div key={idx} className="p-4 rounded-xl bg-bg-surface-elevated border border-border-default space-y-2">
                      <div className="flex justify-between items-start gap-2">
                        <span className="text-xs font-bold text-text-primary">{proj.name || proj.title}</span>
                        <div className="flex items-center gap-2 shrink-0">
                          {proj.githubUrl && (
                            <a href={proj.githubUrl} target="_blank" rel="noreferrer" className="text-[11px] text-brand-gold hover:underline font-semibold flex items-center gap-1">
                              GitHub ↗
                            </a>
                          )}
                          {(proj.liveUrl || (proj.url && proj.url !== proj.githubUrl)) && (
                            <a href={proj.liveUrl || proj.url} target="_blank" rel="noreferrer" className="text-[11px] text-brand-teal hover:underline font-semibold flex items-center gap-1">
                              Live Demo ↗
                            </a>
                          )}
                        </div>
                      </div>
                      {proj.description && (
                        <p className="text-xs text-text-secondary leading-relaxed">{proj.description}</p>
                      )}
                      {proj.highlights?.length > 0 && (
                        <ul className="space-y-1 pt-1">
                          {proj.highlights.map((hl, hIdx) => (
                            <li key={hIdx} className="text-[11px] text-text-secondary flex items-start gap-1.5">
                              <span className="text-brand-teal font-bold shrink-0">•</span>
                              <span>{hl}</span>
                            </li>
                          ))}
                        </ul>
                      )}
                      {tech && (
                        <div className="flex flex-wrap gap-1 pt-1">
                          {Array.isArray(tech) ? (
                            tech.map((t) => <Badge key={t} variant="gold" size="xs">{t}</Badge>)
                          ) : typeof tech === 'string' && tech.includes(',') ? (
                            tech.split(',').map((t) => <Badge key={t.trim()} variant="gold" size="xs">{t.trim()}</Badge>)
                          ) : (
                            <span className="text-[11px] text-text-muted">{tech}</span>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          )}

          {/* Education List */}
          {extracted.education?.length > 0 && (
            <div className="p-5 rounded-xl bg-bg-surface border border-border-default space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
                Education
              </h4>
              <div className="space-y-3">
                {extracted.education.map((edu, idx) => (
                  <div key={idx} className="flex justify-between items-baseline flex-wrap gap-1 text-xs">
                    <div>
                      <span className="font-bold text-text-primary block">{edu.institution || edu.school}</span>
                      <span className="text-text-muted">{edu.degree} {edu.fieldOfStudy ? `in ${edu.fieldOfStudy}` : ''}</span>
                    </div>
                    <span className="text-text-muted text-[11px]">{edu.year || edu.dates}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* TAB 3: RAW TEXT PREVIEW */}
      {activeTab === 'raw' && (
        <div className="p-5 rounded-xl bg-bg-surface border border-border-default space-y-2">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
              Extracted Raw Plaintext ({resume.extractedText?.length || 0} characters)
            </h4>
            <span className="text-[11px] text-text-muted">Read-only extracted text stream</span>
          </div>
          <pre className="p-4 rounded-lg bg-bg-surface-elevated border border-border-default text-xs font-mono text-text-secondary whitespace-pre-wrap max-h-96 overflow-y-auto leading-relaxed">
            {resume.extractedText || 'No text content available.'}
          </pre>
        </div>
      )}

      {/* Edit Extracted Data Modal */}
      <EditExtractedDataModal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        resume={resume}
        onSuccess={() => {
          setFeedbackMsg('Resume details updated successfully.');
          if (onRefresh) onRefresh();
        }}
      />
    </div>
  );
}
