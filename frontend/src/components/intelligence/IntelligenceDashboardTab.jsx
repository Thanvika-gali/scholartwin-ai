import React from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';
import Button from '../ui/Button';

export default function IntelligenceDashboardTab({
  overview,
  insights,
  recommendations,
  onNavigateTab,
  onTriggerSync,
}) {
  const readinessScore = overview?.overallReadinessScore ?? 0;

  return (
    <div className="space-y-6">
      {/* Top Banner: Composite Intelligence Score */}
      <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-brand-navy via-brand-navy-card to-brand-navy-deep border border-brand-teal/40 text-white p-6 sm:p-8 shadow-card">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 relative z-10">
          <div className="space-y-2 max-w-2xl">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-brand-teal/20 text-brand-gold text-xs font-semibold">
              <span className="w-2 h-2 rounded-full bg-brand-teal animate-pulse" />
              Unified Academic Intelligence & Memory Hub
            </div>
            <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
              Grounded Cross-Module Intelligence Engine
            </h2>
            <p className="text-xs sm:text-sm text-slate-300 leading-relaxed">
              AI ScholarTwin synthesizes your study sessions, DSA problem accuracy, resume ATS diagnostics, and persistent memories into continuous, evidence-backed academic guidance.
            </p>
          </div>

          <div className="flex items-center gap-4 bg-white/5 backdrop-blur-md px-6 py-4 rounded-2xl border border-white/10 shrink-0">
            <div className="relative w-16 h-16 flex items-center justify-center">
              <svg className="w-full h-full transform -rotate-90" viewBox="0 0 36 36">
                <path
                  className="text-white/10"
                  strokeWidth="3.5"
                  stroke="currentColor"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
                <path
                  className="text-brand-teal"
                  strokeDasharray={`${readinessScore}, 100`}
                  strokeWidth="3.5"
                  strokeLinecap="round"
                  stroke="currentColor"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
              </svg>
              <span className="absolute text-base font-bold text-white font-mono">{readinessScore}%</span>
            </div>
            <div>
              <p className="text-[11px] uppercase tracking-wider text-slate-300 font-semibold">Readiness Index</p>
              <p className="text-sm font-bold text-brand-gold">Role-Aligned</p>
            </div>
          </div>
        </div>
      </div>

      {/* 4 Pillars Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card className="hover:border-brand-teal/50 transition-all">
          <CardContent className="p-5 space-y-2">
            <div className="flex items-center justify-between text-xs font-semibold text-text-muted">
              <span>Learning Velocity</span>
              <span className="text-brand-teal font-mono">
                {overview?.learning?.totalStudyHours || 0} hrs
              </span>
            </div>
            <p className="text-2xl font-bold text-text-primary tracking-tight">
              {overview?.learning?.topicsCompleted || 0} Topics
            </p>
            <p className="text-xs text-text-muted">
              {overview?.learning?.roadmapCompletionRate || 0}% avg roadmap progress
            </p>
          </CardContent>
        </Card>

        <Card className="hover:border-brand-teal/50 transition-all">
          <CardContent className="p-5 space-y-2">
            <div className="flex items-center justify-between text-xs font-semibold text-text-muted">
              <span>DSA Pattern Accuracy</span>
              <span className="text-blue-500 font-mono">
                {overview?.dsa?.accuracyPercentage ?? 0}%
              </span>
            </div>
            <p className="text-2xl font-bold text-text-primary tracking-tight">
              {overview?.dsa?.totalProblemsSolved || 0} Solved
            </p>
            <p className="text-xs text-text-muted">
              Top: {overview?.dsa?.topTopic || 'None recorded'}
            </p>
          </CardContent>
        </Card>

        <Card className="hover:border-brand-teal/50 transition-all">
          <CardContent className="p-5 space-y-2">
            <div className="flex items-center justify-between text-xs font-semibold text-text-muted">
              <span>Career Readiness</span>
              <span className="text-brand-gold font-mono">
                {overview?.career?.careerReadinessPercentage ?? 0}%
              </span>
            </div>
            <p className="text-2xl font-bold text-text-primary tracking-tight">
              {overview?.career?.latestResumeAtsScore ?? 0} ATS
            </p>
            <p className="text-xs text-text-muted">
              {overview?.career?.verifiedSkillsCount || 0} verified skills
            </p>
          </CardContent>
        </Card>

        <Card className="hover:border-brand-teal/50 transition-all">
          <CardContent className="p-5 space-y-2">
            <div className="flex items-center justify-between text-xs font-semibold text-text-muted">
              <span>AI Memory Records</span>
              <span className="text-purple-500 font-mono">
                {overview?.aiUsage?.pinnedMemoriesCount || 0} Pinned
              </span>
            </div>
            <p className="text-2xl font-bold text-text-primary tracking-tight">
              {overview?.aiUsage?.activeMemoriesCount || 0} Active
            </p>
            <p className="text-xs text-text-muted">
              {overview?.aiUsage?.totalConversationsCount || 0} conversations logged
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Strengths & Weaknesses Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
              Verified Core Strengths
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {overview?.topStrengths && overview.topStrengths.length > 0 ? (
              overview.topStrengths.map((str, idx) => (
                <div
                  key={idx}
                  className="flex items-start gap-3 p-3 rounded-xl bg-bg-surface border border-border-default hover:border-emerald-500/40 transition-colors"
                >
                  <span className="text-emerald-500 font-bold text-xs mt-0.5">✓</span>
                  <span className="text-xs sm:text-sm text-text-primary leading-snug">{str}</span>
                </div>
              ))
            ) : (
              <p className="text-xs text-text-muted italic">Complete roadmap tasks and DSA challenges to record verified strengths.</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-amber-500" />
              Diagnosed Growth Areas
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {overview?.topWeaknesses && overview.topWeaknesses.length > 0 ? (
              overview.topWeaknesses.map((weak, idx) => (
                <div
                  key={idx}
                  className="flex items-start gap-3 p-3 rounded-xl bg-bg-surface border border-border-default hover:border-amber-500/40 transition-colors"
                >
                  <span className="text-amber-500 font-bold text-xs mt-0.5">!</span>
                  <span className="text-xs sm:text-sm text-text-primary leading-snug">{weak}</span>
                </div>
              ))
            ) : (
              <p className="text-xs text-text-muted italic">No critical weak areas identified.</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Quick Launchpad to Memory & Chat */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
        <div
          onClick={() => onNavigateTab('memories')}
          className="p-5 rounded-2xl bg-bg-surface border border-border-default hover:border-brand-teal cursor-pointer transition-all space-y-2 group shadow-sm"
        >
          <div className="flex items-center justify-between">
            <h4 className="font-bold text-sm text-text-primary group-hover:text-brand-teal transition-colors">
              Memory Center →
            </h4>
            <span className="text-[10px] px-2 py-0.5 rounded-md bg-brand-teal/15 text-brand-teal font-semibold">
              5 Categories
            </span>
          </div>
          <p className="text-xs text-text-muted leading-relaxed">
            Manage your persistent user facts, edit inaccurate recollections, archive entries, and adjust privacy preferences.
          </p>
        </div>

        <div
          onClick={() => onNavigateTab('chat')}
          className="p-5 rounded-2xl bg-bg-surface border border-border-default hover:border-brand-teal cursor-pointer transition-all space-y-2 group shadow-sm"
        >
          <div className="flex items-center justify-between">
            <h4 className="font-bold text-sm text-text-primary group-hover:text-brand-teal transition-colors">
              Context-Aware AI Twin →
            </h4>
            <span className="text-[10px] px-2 py-0.5 rounded-md bg-brand-gold/15 text-brand-gold font-semibold">
              Gemini + Memory
            </span>
          </div>
          <p className="text-xs text-text-muted leading-relaxed">
            Consult your personalized academic co-pilot. Grounded in your target role, weak topics, and verified vault notes.
          </p>
        </div>

        <div
          onClick={() => onNavigateTab('graph')}
          className="p-5 rounded-2xl bg-bg-surface border border-border-default hover:border-brand-teal cursor-pointer transition-all space-y-2 group shadow-sm"
        >
          <div className="flex items-center justify-between">
            <h4 className="font-bold text-sm text-text-primary group-hover:text-brand-teal transition-colors">
              Knowledge Graph →
            </h4>
            <span className="text-[10px] px-2 py-0.5 rounded-md bg-blue-500/15 text-blue-500 font-semibold">
              Visual Explorer
            </span>
          </div>
          <p className="text-xs text-text-muted leading-relaxed">
            Explore interconnected nodes linking your skills, target career roles, DSA topics, and Knowledge Hub notes.
          </p>
        </div>
      </div>
    </div>
  );
}
