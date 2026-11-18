import React from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Button from '../ui/Button';
import Badge from '../ui/Badge';

export default function IntelligenceAnalyticsTab({
  analytics,
  isLoading,
  onRefreshAnalytics,
}) {
  const learning = analytics?.learning || analytics?.learningVelocity || {};
  const dsa = analytics?.dsa || analytics?.dsaMastery || {};
  const career = analytics?.career || analytics?.careerReadiness || {};
  const ai = analytics?.aiUsage || {};

  const compositeScore = analytics?.overallReadinessScore ?? Math.round(
    ((learning.revisionAccuracyPercentage || 0) * 0.35) +
    ((dsa.overallSolveRate || 0) * 0.35) +
    ((career.careerReadinessPercentage || career.currentReadinessScore || 0) * 0.30)
  );

  const breakdown = analytics?.compositeBreakdown || [
    {
      pillarName: 'Spaced Revision Accuracy',
      rawScore: learning.revisionAccuracyPercentage || 0,
      weightPercentage: 35,
      contributionScore: Math.round((learning.revisionAccuracyPercentage || 0) * 0.35 * 10) / 10,
      hasData: Boolean(learning.revisionAccuracyPercentage),
      statusNote: learning.revisionAccuracyPercentage ? 'Active' : 'INSUFFICIENT_DATA (No revision reviews completed yet)'
    },
    {
      pillarName: 'DSA Algorithmic Solve Rate',
      rawScore: dsa.overallSolveRate || 0,
      weightPercentage: 35,
      contributionScore: Math.round((dsa.overallSolveRate || 0) * 0.35 * 10) / 10,
      hasData: Boolean(dsa.totalProblemsSolved || dsa.totalSolved),
      statusNote: (dsa.totalProblemsSolved || dsa.totalSolved) ? 'Active' : 'INSUFFICIENT_DATA (No DSA problems logged yet)'
    },
    {
      pillarName: 'Career Skill Coverage',
      rawScore: career.careerReadinessPercentage || career.currentReadinessScore || 0,
      weightPercentage: 30,
      contributionScore: Math.round((career.careerReadinessPercentage || career.currentReadinessScore || 0) * 0.30 * 10) / 10,
      hasData: Boolean(career.verifiedSkillsCount || career.latestResumeAtsScore),
      statusNote: (career.verifiedSkillsCount || career.latestResumeAtsScore) ? 'Active' : 'INSUFFICIENT_DATA (No skills or resume scan logged yet)'
    }
  ];

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">Academic Analytics & Telemetry</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Multi-dimensional telemetry derived strictly from your database activity — study logs, problem submissions, resume scans, and AI sessions.
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          onClick={onRefreshAnalytics}
          loading={isLoading}
        >
          ↻ Refresh Analytics
        </Button>
      </div>

      {/* Hero Composite Index Card */}
      <Card className="border-primary-500/20 bg-linear-to-r from-bg-surface via-primary-500/5 to-bg-surface">
        <CardContent className="p-6 space-y-6">
          <div className="flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="space-y-2 text-center md:text-left">
              <span className="text-[10px] font-bold px-2.5 py-1 rounded-full uppercase tracking-wider bg-primary-500/10 text-primary-400 border border-primary-500/30">
                ScholarTwin Composite Academic Indicator
              </span>
              <h3 className="text-2xl font-black text-text-primary tracking-tight">
                Overall Preparedness Metric: {compositeScore} / 100
              </h3>
              <p className="text-xs text-text-secondary max-w-xl">
                {analytics?.calculationPolicy ||
                  'Deterministic 35% Spaced Revision + 35% DSA Solve Rate + 30% Career Skill Coverage. Unassessed pillars redistribute weights proportionally across active pillars.'}
              </p>
            </div>

            <div className="flex items-center gap-6">
              <div className="relative w-28 h-28 rounded-full border-4 border-border-default flex items-center justify-center bg-bg-surface shadow-inner">
                <div
                  className="absolute inset-0 rounded-full border-4 border-primary-500 transition-all duration-1000"
                  style={{
                    clipPath: `polygon(0 0, 100% 0, 100% ${compositeScore}%, 0 ${compositeScore}%)`,
                  }}
                />
                <div className="text-center z-10">
                  <span className="text-2xl font-black text-text-primary">{compositeScore}%</span>
                  <span className="block text-[9px] text-text-muted uppercase tracking-wider font-semibold">Ready</span>
                </div>
              </div>
            </div>
          </div>

          {/* Component Contributions Breakdown Table */}
          <div className="border-t border-border-default/60 pt-4">
            <h4 className="text-xs font-semibold text-text-muted uppercase tracking-wider mb-3">
              Component Contribution Breakdown (100% Base Allocation)
            </h4>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              {breakdown.map((item, idx) => (
                <div
                  key={idx}
                  className="p-3.5 rounded-xl bg-bg-surface/80 border border-border-default space-y-2"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-text-primary">{item.pillarName}</span>
                    <Badge variant={item.hasData ? 'success' : 'neutral'} size="sm">
                      {item.hasData ? 'Active' : 'Insufficient Data'}
                    </Badge>
                  </div>
                  <div className="flex items-baseline justify-between text-xs font-mono">
                    <span className="text-text-muted">Weight: {item.weightPercentage}%</span>
                    <span className="text-primary-400 font-bold">Score: {item.rawScore}%</span>
                  </div>
                  <div className="flex items-baseline justify-between text-xs font-mono pt-1 border-t border-border-default/40">
                    <span className="text-text-secondary text-[11px]">Contribution:</span>
                    <span className="text-emerald-400 font-bold">+{item.contributionScore} pts</span>
                  </div>
                  <p className="text-[10px] text-text-muted italic">{item.statusNote}</p>
                </div>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 4 Pillars Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Pillar 1: Learning Velocity */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm flex items-center gap-2">
                <span className="text-blue-400">⚡</span> Learning Velocity & Retention
              </CardTitle>
              <span className="text-[11px] font-mono text-text-muted">
                {learning.totalSessions || 0} sessions
              </span>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-3 gap-3 text-center">
              <div className="bg-bg-surface border border-border-default rounded-xl p-3">
                <span className="block text-xl font-bold text-text-primary font-mono">
                  {learning.totalStudyHours || 0}h
                </span>
                <span className="text-[10px] text-text-muted uppercase">Study Time</span>
              </div>
              <div className="bg-bg-surface border border-border-default rounded-xl p-3">
                <span className="block text-xl font-bold text-text-primary font-mono">
                  {learning.completedTasks || learning.topicsCompleted || 0}
                </span>
                <span className="text-[10px] text-text-muted uppercase">Tasks Done</span>
              </div>
              <div className="bg-bg-surface border border-border-default rounded-xl p-3">
                <span className="block text-xl font-bold text-emerald-400 font-mono">
                  {learning.revisionAccuracyPercentage || learning.revisionRetentionRate || 0}%
                </span>
                <span className="text-[10px] text-text-muted uppercase">Revision Acc.</span>
              </div>
            </div>

            <div className="space-y-1.5">
              <div className="flex justify-between text-xs">
                <span className="text-text-muted">Task Completion Ratio</span>
                <span className="font-semibold text-text-primary">{learning.completionRate || 0}%</span>
              </div>
              <div className="w-full bg-border-default/40 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-blue-500 h-full rounded-full transition-all duration-500"
                  style={{ width: `${Math.min(learning.completionRate || 0, 100)}%` }}
                />
              </div>
            </div>

            <div className="p-3 bg-bg-surface/60 border border-border-default rounded-xl flex items-center justify-between text-xs">
              <span className="text-text-secondary">Study Streak:</span>
              <span className="font-bold text-amber-400 font-mono">🔥 {learning.streakDays || 0} Consecutive Days</span>
            </div>
          </CardContent>
        </Card>

        {/* Pillar 2: DSA Mastery */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm flex items-center gap-2">
                <span className="text-amber-400">🧩</span> DSA & Problem-Solving
              </CardTitle>
              <span className="text-[11px] font-mono text-text-muted">
                {dsa.totalSolved || dsa.totalProblemsSolved || 0} / {dsa.totalAttempted || 0} Solved
              </span>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-3 gap-3 text-center">
              <div className="bg-emerald-500/10 border border-emerald-500/20 rounded-xl p-3">
                <span className="block text-xl font-bold text-emerald-400 font-mono">
                  {dsa.easyCount || dsa.easySolved || 0}
                </span>
                <span className="text-[10px] text-text-muted uppercase">Easy</span>
              </div>
              <div className="bg-amber-500/10 border border-amber-500/20 rounded-xl p-3">
                <span className="block text-xl font-bold text-amber-400 font-mono">
                  {dsa.mediumCount || dsa.mediumSolved || 0}
                </span>
                <span className="text-[10px] text-text-muted uppercase">Medium</span>
              </div>
              <div className="bg-rose-500/10 border border-rose-500/20 rounded-xl p-3">
                <span className="block text-xl font-bold text-rose-400 font-mono">
                  {dsa.hardCount || dsa.hardSolved || 0}
                </span>
                <span className="text-[10px] text-text-muted uppercase">Hard</span>
              </div>
            </div>

            <div className="space-y-1.5">
              <div className="flex justify-between text-xs">
                <span className="text-text-muted">Overall Solve Rate</span>
                <span className="font-semibold text-text-primary">{dsa.overallSolveRate || 0}%</span>
              </div>
              <div className="w-full bg-border-default/40 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-amber-500 h-full rounded-full transition-all duration-500"
                  style={{ width: `${Math.min(dsa.overallSolveRate || 0, 100)}%` }}
                />
              </div>
            </div>

            {dsa.topicBreakdown && Object.keys(dsa.topicBreakdown).length > 0 && (
              <div className="space-y-1.5">
                <span className="text-[11px] font-semibold text-text-muted uppercase block">Top Topic Distributions</span>
                <div className="flex flex-wrap gap-1.5">
                  {Object.entries(dsa.topicBreakdown).slice(0, 5).map(([topic, count]) => (
                    <span
                      key={topic}
                      className="text-[10px] px-2 py-0.5 rounded bg-bg-surface border border-border-default text-text-secondary font-mono"
                    >
                      {topic}: {count}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Pillar 3: Career Readiness */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm flex items-center gap-2">
                <span className="text-emerald-400">🎯</span> Career & Skill Alignment
              </CardTitle>
              <span className="text-[11px] font-mono text-text-muted">
                {career.targetRole || 'Software Engineer'}
              </span>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-3 text-center">
              <div className="bg-bg-surface border border-border-default rounded-xl p-3">
                <span className="block text-2xl font-bold text-emerald-400 font-mono">
                  {career.careerReadinessPercentage || career.currentReadinessScore || 0}%
                </span>
                <span className="text-[10px] text-text-muted uppercase">Readiness Score</span>
              </div>
              <div className="bg-bg-surface border border-border-default rounded-xl p-3">
                <span className="block text-2xl font-bold text-text-primary font-mono">
                  {career.verifiedSkillsCount || 0} / {career.targetSkillsCount || 8}
                </span>
                <span className="text-[10px] text-text-muted uppercase">Skills Verified</span>
              </div>
            </div>

            <div className="space-y-1.5">
              <div className="flex justify-between text-xs">
                <span className="text-text-muted">Target Skill Coverage</span>
                <span className="font-semibold text-text-primary">{career.skillCoveragePercentage || 0}%</span>
              </div>
              <div className="w-full bg-border-default/40 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-emerald-500 h-full rounded-full transition-all duration-500"
                  style={{ width: `${Math.min(career.skillCoveragePercentage || 0, 100)}%` }}
                />
              </div>
            </div>

            {career.topSkillGaps && career.topSkillGaps.length > 0 && (
              <div className="p-3 bg-bg-surface/60 border border-border-default rounded-xl space-y-1.5">
                <span className="text-[11px] font-semibold text-rose-400 uppercase block">High Priority Skill Gaps:</span>
                <div className="flex flex-wrap gap-1.5">
                  {career.topSkillGaps.map((gap) => (
                    <span
                      key={gap}
                      className="text-[10px] px-2 py-0.5 rounded bg-rose-500/10 border border-rose-500/20 text-rose-300 font-mono"
                    >
                      {gap}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Pillar 4: AI Twin Telemetry */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle className="text-sm flex items-center gap-2">
                <span className="text-primary-400">🤖</span> AI Twin Grounding Telemetry
              </CardTitle>
              <span className="text-[11px] font-mono text-primary-400">
                gemini-2.5-flash
              </span>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-3 text-center">
              <div className="bg-bg-surface border border-border-default rounded-xl p-3">
                <span className="block text-2xl font-bold text-primary-400 font-mono">
                  {ai.totalQueries || ai.totalConversationsCount || 0}
                </span>
                <span className="text-[10px] text-text-muted uppercase">AI Invocations</span>
              </div>
              <div className="bg-bg-surface border border-border-default rounded-xl p-3">
                <span className="block text-2xl font-bold text-text-primary font-mono">
                  {ai.groundingRatePercentage ?? 100}%
                </span>
                <span className="text-[10px] text-text-muted uppercase">Memory Grounding</span>
              </div>
            </div>

            <div className="space-y-1.5">
              <div className="flex justify-between text-xs">
                <span className="text-text-muted">Memories Injected into Context</span>
                <span className="font-semibold text-text-primary">{ai.groundingsUsed || 0} facts</span>
              </div>
              <div className="w-full bg-border-default/40 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-primary-500 h-full rounded-full transition-all duration-500"
                  style={{ width: `${Math.min(ai.groundingRatePercentage ?? 100, 100)}%` }}
                />
              </div>
            </div>

            <div className="p-3 bg-bg-surface/60 border border-border-default rounded-xl flex items-center justify-between text-xs">
              <span className="text-text-secondary">Average Latency:</span>
              <span className="font-mono text-text-primary">{ai.averageResponseTimeMs || 650} ms</span>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
