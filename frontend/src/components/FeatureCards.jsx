import React from 'react';
import { Link } from 'react-router-dom';
import Badge from './ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from './ui/Card';

const CORE_MODULES = [
  {
    id: 'knowledge-vault',
    title: 'Smart Knowledge Hub 2.0',
    status: 'Live Workspace',
    statusVariant: 'teal',
    route: '/knowledge-hub',
    description:
      'Centralized academic repository supporting up to 100MB PDFs, textbooks, DOCX lecture notes, and OCR extraction. Organize with hierarchical folders, smart inbox AI suggestions, and curated collections.',
    highlights: [
      '100MB Multi-format Extraction',
      'Tesseract OCR for Scanned Pages',
      'Hierarchical Folders & Smart Inbox',
      'Full-text & Deep Chunk Search',
    ],
  },
  {
    id: 'dsa-tracker',
    title: 'DSA & Coding Progress Tracker',
    status: 'Live Workspace',
    statusVariant: 'teal',
    route: '/dsa',
    description:
      'Master data structures and algorithms across 20 canonical CS topics. Track solving streaks, log code attempts with time/space complexity analysis, and reinforce retention with spaced repetition revision schedules.',
    highlights: [
      '20 Standard DSA Topic Mastery',
      'LeetCode & Platform Auto-detection',
      'Automated Spaced Repetition Queue',
      'Interactive Activity & Accuracy Charts',
    ],
  },
  {
    id: 'career-hub',
    title: 'Career & Resume Intelligence',
    status: 'Live Workspace',
    statusVariant: 'teal',
    route: '/career',
    description:
      'Turn technical coursework and DSA progress into interview readiness. Features a 100-point rubric ATS resume scanner, JD skill-gap analysis against top tech roles (SDE, Backend, Frontend), and prioritized action plans.',
    highlights: [
      '100-Point ATS Diagnostic Score',
      'Role Benchmark Simulator (SDE, AI, DevOps)',
      'Job Description Matcher & Skill Gap',
      'Prioritized Action Plan Roadmaps',
    ],
  },
  {
    id: 'ai-twin',
    title: 'Personalized AI Twin & Course RAG',
    status: 'Interactive RAG',
    statusVariant: 'gold',
    route: '/knowledge-hub',
    description:
      'Contextual AI study assistant grounded strictly in your verified course notes, textbook chunks, and coding attempts using Gemini API and vector retrieval for accurate, hallucination-free exam revision.',
    highlights: [
      'Curriculum-grounded Semantic RAG',
      'Zero-hallucination Citations',
      'Personalized Exam Question Generation',
      'Multi-modal Document Understanding',
    ],
  },
];

export default function FeatureCards() {
  return (
    <section className="py-8 sm:py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-3xl mx-auto mb-10 space-y-2.5">
          <Badge variant="gold">Unified Academic Platform</Badge>
          <h2 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-text-primary tracking-tight">
            Designed for Serious Computer Science Scholars
          </h2>
          <p className="text-text-muted text-xs sm:text-sm leading-relaxed">
            Every module is tightly integrated with persistent MySQL storage, authenticated tenant isolation, and modern high-performance micro-interactions.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {CORE_MODULES.map((module) => (
            <Card key={module.id} hover className="flex flex-col justify-between border-border-default/90">
              <div>
                <CardHeader className="pb-3">
                  <div className="flex items-center justify-between gap-2 mb-2">
                    <Badge variant={module.statusVariant} size="xs">
                      {module.status}
                    </Badge>
                    <Link
                      to={module.route}
                      className="text-xs font-semibold text-brand-teal hover:underline inline-flex items-center gap-1"
                    >
                      Open Workspace →
                    </Link>
                  </div>
                  <CardTitle className="text-lg sm:text-xl">{module.title}</CardTitle>
                  <CardDescription className="text-xs sm:text-sm leading-relaxed mt-2">
                    {module.description}
                  </CardDescription>
                </CardHeader>

                <div className="px-5 sm:px-6 py-2">
                  <ul className="space-y-1.5 text-xs text-text-secondary">
                    {module.highlights.map((highlight) => (
                      <li key={highlight} className="flex items-center gap-2">
                        <svg className="w-4 h-4 text-brand-teal shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
                        </svg>
                        <span>{highlight}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              </div>

              <div className="p-5 sm:p-6 pt-3 border-t border-border-subtle mt-4 bg-bg-surface-elevated/30 rounded-b-2xl flex items-center justify-between text-xs">
                <span className="text-text-muted font-medium">Enterprise Java 17 + Spring Boot 3</span>
                <Link
                  to={module.route}
                  className="font-semibold text-text-primary hover:text-brand-teal transition-colors"
                >
                  Explore Features →
                </Link>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}
