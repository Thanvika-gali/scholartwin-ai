import React from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';

export default function LearningAnalyticsTab({ analytics }) {
  if (!analytics) {
    return (
      <div className="p-12 text-center text-xs text-text-muted bg-bg-surface border border-border-default rounded-2xl">
        Loading real-time learning telemetry...
      </div>
    );
  }

  const maxMinutes =
    analytics.dailyActivities && analytics.dailyActivities.length > 0
      ? Math.max(...analytics.dailyActivities.map((d) => d.minutesStudied), 60)
      : 60;

  return (
    <div className="space-y-6">
      {/* Metrics Row */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm">
          <span className="text-xs font-semibold text-text-muted block">Total Hours Learned</span>
          <span className="text-2xl font-extrabold text-brand-teal mt-1 block">
            {analytics.totalLearningHours} <span className="text-xs font-normal text-text-muted">hrs</span>
          </span>
          <p className="text-[11px] text-text-muted mt-1">From sessions & completed tasks</p>
        </div>

        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm">
          <span className="text-xs font-semibold text-text-muted block">Study Streak</span>
          <span className="text-2xl font-extrabold text-orange-500 mt-1 block">
            {analytics.currentStreakDays} <span className="text-xs font-normal text-text-muted">days</span>
          </span>
          <p className="text-[11px] text-text-muted mt-1">Consecutive learning days</p>
        </div>

        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm">
          <span className="text-xs font-semibold text-text-muted block">Revision Retention</span>
          <span className="text-2xl font-extrabold text-emerald-500 mt-1 block">
            {analytics.spacedRevisionRetentionRate}%
          </span>
          <p className="text-[11px] text-text-muted mt-1">EASY/MODERATE recall rate</p>
        </div>

        <div className="bg-bg-surface border border-border-default rounded-2xl p-4 shadow-sm">
          <span className="text-xs font-semibold text-text-muted block">Avg Assessment Score</span>
          <span className="text-2xl font-extrabold text-brand-gold mt-1 block">
            {analytics.averageAssessmentScore}%
          </span>
          <p className="text-[11px] text-text-muted mt-1">Across {analytics.totalAssessmentsTaken} completed tests</p>
        </div>
      </div>

      {/* 7-Day Activity Chart */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="text-base">7-Day Study Activity Breakdown</CardTitle>
            <p className="text-xs text-text-muted mt-0.5">
              Weekly focus: {analytics.weeklyStudyMinutes || 0} minutes total
            </p>
          </div>
          <Badge variant="primary" size="sm">Past 7 Days</Badge>
        </CardHeader>
        <CardContent>
          {analytics.dailyActivities && analytics.dailyActivities.length > 0 ? (
            <div className="h-56 flex items-end justify-between gap-2 pt-6 pb-2">
              {analytics.dailyActivities.map((day, idx) => {
                const heightPercent = Math.max(10, Math.round((day.minutesStudied / maxMinutes) * 100));
                const dayName = new Date(day.date).toLocaleDateString('en-US', { weekday: 'short' });
                return (
                  <div key={idx} className="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
                    <span className="text-[10px] text-text-muted font-mono opacity-0 group-hover:opacity-100 transition-opacity">
                      {day.minutesStudied}m
                    </span>
                    <div className="w-full max-w-[40px] bg-bg-surface-elevated rounded-t-xl overflow-hidden h-full flex items-end">
                      <div
                        className="w-full bg-gradient-to-t from-brand-teal to-brand-teal/80 group-hover:from-brand-teal group-hover:to-brand-gold rounded-t-xl transition-all duration-500"
                        style={{ height: `${heightPercent}%` }}
                      ></div>
                    </div>
                    <span className="text-xs font-semibold text-text-muted">{dayName}</span>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="p-8 text-center text-xs text-text-muted">No study activity recorded this week.</div>
          )}
        </CardContent>
      </Card>

      {/* Topic Mastery Distribution */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Domain Mastery Distribution</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {analytics.topicMetrics &&
            analytics.topicMetrics.map((topic, idx) => (
              <div key={idx} className="space-y-1.5">
                <div className="flex items-center justify-between text-xs">
                  <span className="font-bold text-text-primary">{topic.topic}</span>
                  <div className="flex items-center gap-3 text-text-muted">
                    <span>{topic.problemsSolved} Solved</span>
                    <span>•</span>
                    <span className="font-bold text-brand-teal">{topic.masteryPercentage}%</span>
                  </div>
                </div>
                <div className="w-full bg-bg-surface-elevated rounded-full h-2 overflow-hidden border border-border-subtle">
                  <div
                    className="bg-brand-teal h-2 rounded-full transition-all duration-700"
                    style={{ width: `${topic.masteryPercentage}%` }}
                  ></div>
                </div>
              </div>
            ))}
        </CardContent>
      </Card>
    </div>
  );
}
