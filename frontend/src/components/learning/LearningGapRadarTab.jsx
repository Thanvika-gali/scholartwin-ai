import React from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';
import Button from '../ui/Button';

export default function LearningGapRadarTab({ gaps, onNavigateTab }) {
  if (!gaps) {
    return (
      <div className="p-12 text-center text-xs text-text-muted bg-bg-surface border border-border-default rounded-2xl">
        Loading cross-module gap analysis telemetry...
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Banner: Target Role & Readiness Score */}
      <div className="bg-gradient-to-r from-brand-navy via-brand-navy/95 to-slate-900 border border-brand-teal/30 rounded-2xl p-6 text-white shadow-lg space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="space-y-1">
            <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-brand-gold text-brand-navy">
              Cross-Module Intelligence
            </span>
            <h2 className="text-xl sm:text-2xl font-black text-white tracking-tight">
              {gaps.targetRole || 'Software Development Engineer'}
            </h2>
            <p className="text-xs text-slate-300">
              Synthesizing your verified career skills, DSA problem solve rates, Knowledge Hub documents, and assessment diagnostic results.
            </p>
          </div>

          <div className="flex items-center gap-4 bg-white/10 px-5 py-3 rounded-2xl border border-white/10 shrink-0">
            <div>
              <span className="text-[10px] uppercase font-bold text-slate-300 block">Overall Readiness</span>
              <span className="text-3xl font-black text-brand-teal">{gaps.overallReadinessPercentage}%</span>
            </div>
            <div className="w-12 h-12 rounded-full border-4 border-brand-teal flex items-center justify-center font-bold text-xs text-white">
              {gaps.overallReadinessPercentage >= 75 ? 'Tier-1' : gaps.overallReadinessPercentage >= 50 ? 'Mid' : 'Base'}
            </div>
          </div>
        </div>

        {/* Readiness Bar */}
        <div className="space-y-1 pt-2">
          <div className="flex justify-between text-xs text-slate-300">
            <span>Readiness Benchmark to Industry Standard</span>
            <span className="font-semibold text-brand-teal">{gaps.overallReadinessPercentage}%</span>
          </div>
          <div className="w-full bg-white/10 rounded-full h-2 overflow-hidden">
            <div
              className="bg-brand-teal h-2 rounded-full transition-all duration-700"
              style={{ width: `${gaps.overallReadinessPercentage}%` }}
            ></div>
          </div>
        </div>
      </div>

      {/* Prioritized Recommendations */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <span>🎯</span>
            Prioritized Action Recommendations
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {gaps.prioritizedRecommendations && gaps.prioritizedRecommendations.length > 0 ? (
            <div className="space-y-2.5">
              {gaps.prioritizedRecommendations.map((rec) => (
                <div
                  key={rec.priority}
                  className="p-4 rounded-xl border border-border-default bg-bg-surface-elevated/40 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:border-brand-teal/40 transition-all"
                >
                  <div className="flex items-start gap-3 min-w-0">
                    <span className="w-6 h-6 rounded-full bg-brand-teal text-white font-bold text-xs flex items-center justify-center shrink-0">
                      {rec.priority}
                    </span>
                    <div className="min-w-0 space-y-0.5">
                      <h4 className="text-xs sm:text-sm font-bold text-text-primary">{rec.actionItem}</h4>
                      <p className="text-xs text-text-muted">{rec.rationale}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 shrink-0 self-end sm:self-auto">
                    <span className="text-xs font-semibold text-brand-gold bg-brand-gold/10 px-2.5 py-1 rounded-lg">
                      ~{rec.estimatedHours} hrs
                    </span>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onNavigateTab('roadmaps')}
                      className="text-xs text-brand-teal"
                    >
                      Study Topic →
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-xs text-text-muted">No pending critical recommendations.</p>
          )}
        </CardContent>
      </Card>

      {/* Two Column: Strong Verified Areas vs Identified Knowledge Gaps */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Strong Areas */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base text-emerald-500 flex items-center gap-2">
              <span>✅</span> Strong & Verified Competencies ({gaps.strongTopics?.length || 0})
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2.5">
            {gaps.strongTopics && gaps.strongTopics.length > 0 ? (
              gaps.strongTopics.map((s, idx) => (
                <div
                  key={idx}
                  className="p-3 rounded-xl border border-emerald-500/20 bg-emerald-500/5 flex items-center justify-between text-xs"
                >
                  <div className="min-w-0">
                    <span className="font-bold text-text-primary block truncate">{s.topic}</span>
                    <span className="text-[11px] text-text-muted">{s.verificationEvidence}</span>
                  </div>
                  <Badge variant="success" size="sm">
                    {s.proficiency}
                  </Badge>
                </div>
              ))
            ) : (
              <p className="text-xs text-text-muted">No strong topics benchmarked yet.</p>
            )}
          </CardContent>
        </Card>

        {/* Weak / Gap Areas */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base text-rose-500 flex items-center gap-2">
              <span>⚠️</span> Identified Learning Gaps ({gaps.weakTopics?.length || 0})
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2.5">
            {gaps.weakTopics && gaps.weakTopics.length > 0 ? (
              gaps.weakTopics.map((g, idx) => (
                <div
                  key={idx}
                  className="p-3 rounded-xl border border-rose-500/20 bg-rose-500/5 space-y-1 text-xs"
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-text-primary">{g.topic}</span>
                    <Badge variant={g.riskRating === 'HIGH' ? 'error' : 'warning'} size="sm">
                      {g.riskRating} Risk
                    </Badge>
                  </div>
                  <p className="text-[11px] text-text-muted">{g.rationale}</p>
                  {g.prerequisites && g.prerequisites.length > 0 && (
                    <div className="text-[10px] text-brand-gold font-medium pt-1">
                      Prerequisites: {g.prerequisites.join(', ')}
                    </div>
                  )}
                </div>
              ))
            ) : (
              <p className="text-xs text-text-muted">Zero critical learning gaps found!</p>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Prerequisite Knowledge Graph */}
      {gaps.prerequisiteTopics && gaps.prerequisiteTopics.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2">
              <span>🧠</span> Foundational Prerequisite Dependencies
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {gaps.prerequisiteTopics.map((pre, idx) => (
                <span
                  key={idx}
                  className="px-3 py-1.5 rounded-xl border border-brand-teal/30 bg-brand-teal/10 text-brand-teal text-xs font-semibold"
                >
                  {pre}
                </span>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Data Sources Health Telemetry */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <span>📡</span> Multi-Module Telemetry Feeds
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3">
            {gaps.dataSources &&
              gaps.dataSources.map((ds, idx) => (
                <div
                  key={idx}
                  className="p-3 rounded-xl border border-border-default bg-bg-surface-elevated/40 space-y-1 text-xs"
                >
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-text-primary text-[11px]">{ds.sourceName}</span>
                    <span
                      className={`w-2 h-2 rounded-full ${
                        ds.available ? 'bg-emerald-500' : 'bg-amber-500'
                      }`}
                    ></span>
                  </div>
                  <p className="text-[10px] text-text-muted">{ds.details}</p>
                </div>
              ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
