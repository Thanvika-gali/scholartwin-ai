import React from 'react';
import Badge from './ui/Badge';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from './ui/Card';

const CORE_MODULES = [
  {
    id: 'knowledge-vault',
    title: 'Academic Notes & Concepts',
    status: 'Active Workspace',
    statusVariant: 'teal',
    description:
      'Capture course lectures, mathematical formulas, algorithm invariants, and architecture trade-offs with categorized full-text indexing.',
    tags: ['Categorized Notes', 'Fast Filtering', 'Grounded Storage'],
  },
  {
    id: 'learning-resources',
    title: 'Verified Resource Hub',
    status: 'Active Workspace',
    statusVariant: 'teal',
    description:
      'Curate official documentation, research papers, repositories, and technical articles with protocol safety and instant browser navigation.',
    tags: ['Safe External Links', 'Reference Library', 'Multi-tenant Vault'],
  },
  {
    id: 'ai-twin',
    title: 'AI Twin Chat & Course RAG',
    status: 'Upcoming',
    statusVariant: 'upcoming',
    description:
      'Conversational intelligence interface grounded strictly in your verified notes and curriculum materials using retrieval-augmented generation.',
    tags: ['Contextual RAG', 'Curriculum Grounding', 'Exam Revision'],
  },
  {
    id: 'dsa-tracker',
    title: 'DSA Practice & Problem Tracker',
    status: 'Upcoming',
    statusVariant: 'upcoming',
    description:
      'Master data structures and algorithmic patterns, diagnose topic weaknesses, and reinforce technical interview problem-solving.',
    tags: ['Spaced Repetition', 'Time Complexity Logs', 'Pattern Diagnostic'],
  },
];

export default function FeatureCards() {
  return (
    <section className="py-12 md:py-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="text-center max-w-2xl mx-auto mb-10 space-y-2">
          <Badge variant="gold">Platform Capabilities</Badge>
          <h2 className="text-2xl sm:text-3xl font-extrabold text-text-primary tracking-tight">
            Academic Knowledge Architecture
          </h2>
          <p className="text-text-muted text-xs sm:text-sm leading-relaxed">
            Engineered to provide a structured, calm, and grounded environment for deep computer science learning.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {CORE_MODULES.map((module) => (
            <Card key={module.id} hover className="flex flex-col justify-between">
              <div>
                <CardHeader>
                  <div className="flex items-center justify-between gap-2 mb-2">
                    <Badge variant={module.statusVariant}>
                      {module.status}
                    </Badge>
                  </div>
                  <CardTitle>{module.title}</CardTitle>
                  <CardDescription className="text-xs sm:text-sm">
                    {module.description}
                  </CardDescription>
                </CardHeader>
              </div>

              <div className="p-5 sm:p-6 pt-0">
                <div className="flex flex-wrap gap-2 pt-4 border-t border-border-subtle">
                  {module.tags.map((tag) => (
                    <Badge key={tag} variant="default" size="xs">
                      {tag}
                    </Badge>
                  ))}
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
}
