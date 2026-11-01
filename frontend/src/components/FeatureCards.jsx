import React from 'react';

const FUTURE_FEATURES = [
  {
    id: 'knowledge-vault',
    title: 'Knowledge Vault & Note Organization',
    milestone: 'Milestone 2',
    description: 'Ingest lecture slides, PDFs, notes, and research documents. Automatically categorize materials and enable context-aware AI retrieval.',
    tags: ['Document Ingestion', 'Semantic Search', 'Vector Knowledge Base'],
  },
  {
    id: 'dsa-tracker',
    title: 'DSA & Algorithmic Problem Tracker',
    milestone: 'Milestone 3',
    description: 'Track LeetCode and problem-solving patterns, log time complexities, identify topic weaknesses, and reinforce algorithmic concepts.',
    tags: ['Data Structures', 'Spaced Repetition', 'Weakness Diagnostic'],
  },
  {
    id: 'career-readiness',
    title: 'Career Readiness & Resume Analyzer',
    milestone: 'Milestone 4',
    description: 'Analyze software engineering resumes against target role requirements, detect skill gaps, and provide actionable technical project suggestions.',
    tags: ['Resume Scoring', 'Skill Gap Analysis', 'Role Benchmarks'],
  },
  {
    id: 'study-planner',
    title: 'Personalized Adaptive Study Engine',
    milestone: 'Milestone 5',
    description: 'Generate dynamic weekly study schedules that adapt based on upcoming exam dates, coding progress, and identified knowledge gaps.',
    tags: ['Curriculum Planning', 'Adaptive Schedules', 'Milestone Milestones'],
  },
];

export default function FeatureCards() {
  return (
    <section className="py-12 md:py-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10">
          <h2 className="text-2xl sm:text-3xl font-bold text-white mb-3">
            Architectural Roadmap
          </h2>
          <p className="text-slate-400 text-sm sm:text-base">
            The core modules designed for AI ScholarTwin. Foundation and communication protocols are established in Milestone 1; intelligence features will be developed in subsequent stages.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {FUTURE_FEATURES.map((feature) => (
            <div
              key={feature.id}
              className="rounded-2xl p-6 bg-slate-900/60 border border-slate-800 hover:border-slate-700/80 transition-all duration-200 relative group flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <span className="text-xs font-semibold uppercase tracking-wider px-2.5 py-1 rounded-full bg-slate-800 text-indigo-400 border border-indigo-900/40">
                    {feature.milestone}
                  </span>
                  <span className="text-xs text-slate-500 font-medium">
                    Future Module
                  </span>
                </div>

                <h3 className="text-lg font-semibold text-slate-100 mb-2 group-hover:text-white transition-colors">
                  {feature.title}
                </h3>
                <p className="text-slate-400 text-sm leading-relaxed mb-6">
                  {feature.description}
                </p>
              </div>

              <div className="flex flex-wrap gap-2 pt-4 border-t border-slate-800/80">
                {feature.tags.map((tag) => (
                  <span
                    key={tag}
                    className="text-xs px-2.5 py-1 rounded-md bg-slate-800/60 text-slate-300 border border-slate-700/50"
                  >
                    {tag}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
