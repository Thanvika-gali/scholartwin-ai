import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Alert from '../ui/Alert';
import Badge from '../ui/Badge';
import { analyzeJobDescriptionApi } from '../../services/api';
import { getScoreVariant } from '../../utils/careerValidation';

export default function JobDescriptionModal({ isOpen, onClose, onSuccess, resumes = [], viewJob = null }) {
  const [jobTitle, setJobTitle] = useState('');
  const [company, setCompany] = useState('');
  const [jobDescriptionText, setJobDescriptionText] = useState('');
  const [selectedResumeId, setSelectedResumeId] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [analysisResult, setAnalysisResult] = useState(null);

  useEffect(() => {
    if (isOpen) {
      if (viewJob) {
        setAnalysisResult(viewJob);
        setError(null);
        return;
      }
      setJobTitle('');
      setCompany('');
      setJobDescriptionText('');
      setError(null);
      setAnalysisResult(null);

      // Default to primary resume or first resume
      const primary = resumes.find((r) => r.isPrimary);
      if (primary) {
        setSelectedResumeId(primary.id);
      } else if (resumes.length > 0) {
        setSelectedResumeId(resumes[0].id);
      } else {
        setSelectedResumeId('');
      }
    }
  }, [isOpen, viewJob, resumes]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!jobTitle.trim()) {
      setError('Please provide a job title.');
      return;
    }
    if (!jobDescriptionText.trim() || jobDescriptionText.trim().length < 30) {
      setError('Please paste a substantial job description (at least 30 characters).');
      return;
    }

    setIsLoading(true);
    setError(null);

    const payload = {
      jobTitle: jobTitle.trim(),
      company: company.trim() || null,
      jobDescriptionText: jobDescriptionText.trim(),
      resumeId: selectedResumeId ? Number(selectedResumeId) : null,
    };

    try {
      const result = await analyzeJobDescriptionApi(payload);
      setAnalysisResult(result);
      setIsLoading(false);
      if (onSuccess) {
        onSuccess(result);
      }
    } catch (err) {
      setIsLoading(false);
      setError(err.message || 'Failed to analyze job description. Please try again.');
    }
  };

  const score = analysisResult ? (analysisResult.matchScore ?? analysisResult.matchPercentage ?? 0) : 0;
  const companyName = analysisResult ? (analysisResult.company ?? analysisResult.companyName ?? '') : '';
  const missingKeywordsList = analysisResult ? (analysisResult.missingKeywords ?? analysisResult.keywordCoverage ?? []) : [];
  const recsList = analysisResult ? (analysisResult.recommendations ?? analysisResult.tailoredSuggestions ?? []) : [];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={analysisResult ? 'JD Match & Skill Gap Diagnostics' : 'Analyze Job Description'}
      subtitle={
        analysisResult
          ? `${analysisResult.jobTitle}${companyName ? ` at ${companyName}` : ''}`
          : 'Compare your resume against any tech job specification'
      }
      maxWidth="max-w-2xl"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
          />
        </svg>
      }
    >
      {analysisResult ? (
        <div className="space-y-5 py-1">
          {/* Top Score Banner */}
          <div className="flex flex-col sm:flex-row items-center justify-between p-4 rounded-xl bg-bg-surface-elevated border border-border-default gap-4">
            <div className="text-center sm:text-left">
              <span className="text-xs uppercase tracking-wider font-semibold text-text-muted">Overall Match Score</span>
              <div className="flex items-center gap-3 mt-1">
                <span className="text-3xl font-extrabold text-brand-teal">{score}%</span>
                <Badge variant={getScoreVariant(score)} size="sm">
                  {score >= 75 ? 'Strong Match' : score >= 50 ? 'Moderate Match' : 'Gap Identified'}
                </Badge>
              </div>
            </div>
            <div className="text-xs text-text-muted text-center sm:text-right space-y-1">
              <div>Required Skills: <span className="font-semibold text-text-primary">{analysisResult.matchedRequiredSkills?.length || 0} matched</span> / {(analysisResult.matchedRequiredSkills?.length || 0) + (analysisResult.missingRequiredSkills?.length || 0)} total</div>
              <div>Preferred Skills: <span className="font-semibold text-text-primary">{analysisResult.matchedPreferredSkills?.length || 0} matched</span></div>
            </div>
          </div>

          {/* Required Skills Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="p-4 rounded-xl bg-status-success-bg/30 border border-status-success-border/60">
              <h4 className="text-xs font-bold uppercase tracking-wider text-status-success-text mb-2.5 flex items-center gap-1.5">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                </svg>
                Matched Required ({analysisResult.matchedRequiredSkills?.length || 0})
              </h4>
              <div className="flex flex-wrap gap-1.5">
                {analysisResult.matchedRequiredSkills?.length > 0 ? (
                  analysisResult.matchedRequiredSkills.map((sk) => (
                    <Badge key={sk} variant="success" size="xs">
                      {sk}
                    </Badge>
                  ))
                ) : (
                  <span className="text-xs text-text-muted">No required skills matched yet</span>
                )}
              </div>
            </div>

            <div className="p-4 rounded-xl bg-status-error-bg/30 border border-status-error-border/60">
              <h4 className="text-xs font-bold uppercase tracking-wider text-status-error-text mb-2.5 flex items-center gap-1.5">
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
                </svg>
                Missing Required ({analysisResult.missingRequiredSkills?.length || 0})
              </h4>
              <div className="flex flex-wrap gap-1.5">
                {analysisResult.missingRequiredSkills?.length > 0 ? (
                  analysisResult.missingRequiredSkills.map((sk) => (
                    <Badge key={sk} variant="error" size="xs">
                      {sk}
                    </Badge>
                  ))
                ) : (
                  <span className="text-xs text-status-success-text font-medium">All required skills present!</span>
                )}
              </div>
            </div>
          </div>

          {/* Missing Keywords / Domain Terms */}
          {missingKeywordsList?.length > 0 && (
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wider text-brand-gold mb-2">
                Missing Keywords & Terminology
              </h4>
              <div className="flex flex-wrap gap-1.5">
                {missingKeywordsList.map((kw) => (
                  <Badge key={kw} variant="gold" size="xs">
                    {kw}
                  </Badge>
                ))}
              </div>
            </div>
          )}

          {/* Recommendations */}
          {recsList?.length > 0 && (
            <div>
              <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary mb-2">
                Tailoring Recommendations
              </h4>
              <ul className="space-y-1.5">
                {recsList.map((rec, i) => (
                  <li key={i} className="text-xs text-text-secondary flex items-start gap-2 bg-bg-surface-elevated p-2 rounded-lg border border-border-default">
                    <span className="text-brand-teal font-bold shrink-0">•</span>
                    <span>{rec}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div className="flex justify-between pt-3 border-t border-border-subtle">
            <Button variant="secondary" onClick={() => setAnalysisResult(null)}>
              Analyze Another JD
            </Button>
            <Button variant="primary" onClick={onClose}>
              Done
            </Button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <Alert variant="error" dismissible onDismiss={() => setError(null)}>
              {error}
            </Alert>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Target Role / Job Title"
              placeholder="e.g. Software Engineer (Backend)"
              value={jobTitle}
              onChange={(e) => setJobTitle(e.target.value)}
              required
              disabled={isLoading}
            />
            <Input
              label="Company / Employer"
              placeholder="e.g. Google, Microsoft, Startup"
              value={company}
              onChange={(e) => setCompany(e.target.value)}
              disabled={isLoading}
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
              Select Resume to Compare Against
            </label>
            <select
              value={selectedResumeId}
              onChange={(e) => setSelectedResumeId(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
              disabled={isLoading}
            >
              <option value="">Compare with All Verified Skills (Profile & Inventory)</option>
              {resumes.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.title || r.originalFileName} (v{r.versionNumber}) {r.isPrimary ? '★ Primary' : ''}
                </option>
              ))}
            </select>
            <p className="text-[11px] text-text-muted mt-1">
              Select a specific resume version or compare against all verified skills in your profile inventory.
            </p>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
              Job Description Text
            </label>
            <textarea
              rows={6}
              placeholder="Paste the full job description, qualifications, and responsibilities..."
              value={jobDescriptionText}
              onChange={(e) => setJobDescriptionText(e.target.value)}
              required
              disabled={isLoading}
              className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal resize-none"
            />
            <p className="text-[11px] text-text-muted mt-1">
              Minimum 30 characters. The parser extracts required skills, preferred qualifications, and keywords automatically.
            </p>
          </div>

          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="secondary" onClick={onClose} disabled={isLoading}>
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              isLoading={isLoading}
              disabled={isLoading}
              icon={
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                </svg>
              }
            >
              Run Diagnostic Match
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
}

