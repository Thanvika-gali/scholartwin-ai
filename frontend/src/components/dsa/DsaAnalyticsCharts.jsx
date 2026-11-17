import React from 'react';

/**
 * Pure SVG Donut Chart for Difficulty Analytics
 */
export function DifficultyDonutChart({ difficultyData }) {
  if (!difficultyData) return null;

  const easy = difficultyData.easyTotal || 0;
  const medium = difficultyData.mediumTotal || 0;
  const hard = difficultyData.hardTotal || 0;
  const total = easy + medium + hard;

  if (total === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center text-text-muted">
        <svg className="w-12 h-12 stroke-current mb-2 opacity-40" viewBox="0 0 24 24" fill="none">
          <circle cx="12" cy="12" r="10" strokeWidth="2" strokeDasharray="4 4" />
        </svg>
        <span className="text-xs">No problems tracked yet to display difficulty breakdown.</span>
      </div>
    );
  }

  const radius = 64;
  const strokeWidth = 20;
  const circumference = 2 * Math.PI * radius;

  const easyPercent = easy / total;
  const mediumPercent = medium / total;
  const hardPercent = hard / total;

  const easyDash = easyPercent * circumference;
  const mediumDash = mediumPercent * circumference;
  const hardDash = hardPercent * circumference;

  const easyOffset = 0;
  const mediumOffset = -easyDash;
  const hardOffset = -(easyDash + mediumDash);

  return (
    <div className="flex flex-col sm:flex-row items-center justify-around gap-6 py-2">
      <div className="relative w-44 h-44 flex items-center justify-center shrink-0">
        <svg className="w-full h-full -rotate-90 transform" viewBox="0 0 160 160">
          {/* Base track */}
          <circle
            cx="80"
            cy="80"
            r={radius}
            fill="transparent"
            stroke="currentColor"
            className="text-border-default/40"
            strokeWidth={strokeWidth}
          />
          {/* Easy segment */}
          {easy > 0 && (
            <circle
              cx="80"
              cy="80"
              r={radius}
              fill="transparent"
              stroke="#10b981"
              strokeWidth={strokeWidth}
              strokeDasharray={`${easyDash} ${circumference}`}
              strokeDashoffset={easyOffset}
            />
          )}
          {/* Medium segment */}
          {medium > 0 && (
            <circle
              cx="80"
              cy="80"
              r={radius}
              fill="transparent"
              stroke="#f59e0b"
              strokeWidth={strokeWidth}
              strokeDasharray={`${mediumDash} ${circumference}`}
              strokeDashoffset={mediumOffset}
            />
          )}
          {/* Hard segment */}
          {hard > 0 && (
            <circle
              cx="80"
              cy="80"
              r={radius}
              fill="transparent"
              stroke="#f43f5e"
              strokeWidth={strokeWidth}
              strokeDasharray={`${hardDash} ${circumference}`}
              strokeDashoffset={hardOffset}
            />
          )}
        </svg>
        <div className="absolute flex flex-col items-center justify-center pointer-events-none">
          <span className="text-2xl font-bold text-text-primary">{total}</span>
          <span className="text-[10px] uppercase tracking-wider text-text-muted font-semibold">Total</span>
        </div>
      </div>

      {/* Legend & Stats */}
      <div className="w-full sm:w-auto space-y-3">
        <div className="flex items-center justify-between sm:justify-start gap-4">
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-emerald-500 shrink-0" />
            <span className="text-xs font-medium text-text-secondary">Easy</span>
          </div>
          <span className="text-xs font-bold text-text-primary">
            {difficultyData.easySolved} / {easy}{' '}
            <span className="text-[11px] font-normal text-text-muted">
              ({difficultyData.easySolvedPercentage}%)
            </span>
          </span>
        </div>

        <div className="flex items-center justify-between sm:justify-start gap-4">
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-amber-500 shrink-0" />
            <span className="text-xs font-medium text-text-secondary">Medium</span>
          </div>
          <span className="text-xs font-bold text-text-primary">
            {difficultyData.mediumSolved} / {medium}{' '}
            <span className="text-[11px] font-normal text-text-muted">
              ({difficultyData.mediumSolvedPercentage}%)
            </span>
          </span>
        </div>

        <div className="flex items-center justify-between sm:justify-start gap-4">
          <div className="flex items-center gap-2">
            <span className="w-3 h-3 rounded-full bg-rose-500 shrink-0" />
            <span className="text-xs font-medium text-text-secondary">Hard</span>
          </div>
          <span className="text-xs font-bold text-text-primary">
            {difficultyData.hardSolved} / {hard}{' '}
            <span className="text-[11px] font-normal text-text-muted">
              ({difficultyData.hardSolvedPercentage}%)
            </span>
          </span>
        </div>
      </div>
    </div>
  );
}

/**
 * Pure SVG Bar Chart for 14-day Activity Timeline
 */
export function ActivityTimelineBarChart({ timelineData = [] }) {
  if (!timelineData || timelineData.length === 0) {
    return (
      <div className="py-8 text-center text-xs text-text-muted">
        No recent activity history recorded. Log an attempt or solve a problem to see your 14-day timeline.
      </div>
    );
  }

  const maxVal = Math.max(
    1,
    ...timelineData.map((d) => Math.max(d.attemptsCount || 0, d.solvedCount || 0))
  );

  const chartHeight = 120;

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-end gap-4 text-[11px]">
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-sm bg-brand-teal" />
          <span className="text-text-muted">Solved</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="w-2.5 h-2.5 rounded-sm bg-brand-gold" />
          <span className="text-text-muted">Attempts</span>
        </div>
      </div>

      <div className="flex items-end justify-between gap-1 sm:gap-2 h-36 pt-4 border-b border-border-default">
        {timelineData.map((item, idx) => {
          const attemptsHeight = ((item.attemptsCount || 0) / maxVal) * chartHeight;
          const solvedHeight = ((item.solvedCount || 0) / maxVal) * chartHeight;
          const dayLabel = item.date ? item.date.slice(5) : '';

          return (
            <div key={item.date || idx} className="flex-1 flex flex-col items-center gap-1 group relative">
              {/* Tooltip */}
              <div className="absolute -top-10 opacity-0 group-hover:opacity-100 transition-opacity bg-brand-navy text-white text-[10px] py-1 px-2 rounded pointer-events-none whitespace-nowrap z-10 shadow-md">
                {item.date}: {item.attemptsCount} attempts, {item.solvedCount} solved
              </div>

              {/* Bars container */}
              <div className="w-full flex items-end justify-center gap-0.5 sm:gap-1 h-[120px]">
                {/* Solved bar */}
                <div
                  style={{ height: `${Math.max(4, solvedHeight)}px` }}
                  className={`w-1.5 sm:w-2.5 rounded-t-sm transition-all ${
                    item.solvedCount > 0 ? 'bg-brand-teal' : 'bg-transparent'
                  }`}
                />
                {/* Attempts bar */}
                <div
                  style={{ height: `${Math.max(4, attemptsHeight)}px` }}
                  className={`w-1.5 sm:w-2.5 rounded-t-sm transition-all ${
                    item.attemptsCount > 0 ? 'bg-brand-gold' : 'bg-border-default/40'
                  }`}
                />
              </div>

              {/* Date label */}
              <span className="text-[9px] text-text-muted truncate max-w-full">
                {idx % 2 === 0 ? dayLabel : ''}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

