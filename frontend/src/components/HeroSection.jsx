import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import Button from './ui/Button';
import Badge from './ui/Badge';

export default function HeroSection() {
  const { isAuthenticated } = useAuth();

  return (
    <section className="relative overflow-hidden pt-12 pb-16 md:pt-20 md:pb-24 border-b border-border-default bg-bg-surface transition-colors">
      {/* Decorative Background Glows */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[350px] bg-brand-teal/5 dark:bg-brand-teal/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute top-1/3 right-10 w-[400px] h-[300px] bg-brand-gold/5 dark:bg-brand-gold/10 rounded-full blur-3xl pointer-events-none" />

      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10 space-y-6">
        <div className="inline-flex items-center gap-2">
          <Badge variant="teal" icon={<span className="w-1.5 h-1.5 rounded-full bg-brand-teal animate-pulse" />}>
            Milestones 1–7 Live & Production-Ready
          </Badge>
        </div>

        <h1 className="text-3xl sm:text-5xl md:text-6xl font-black tracking-tight text-text-primary leading-tight max-w-4xl mx-auto">
          Academic Knowledge, Algorithmic Mastery &{' '}
          <span className="text-brand-teal dark:text-brand-teal-light">
            Career Intelligence
          </span>
        </h1>

        <p className="max-w-3xl mx-auto text-sm sm:text-base md:text-lg text-text-secondary leading-relaxed font-normal">
          AI ScholarTwin is a unified workspace engineered for computer science students. Ingest 100MB textbooks and lecture notes with OCR, track 20 standard DSA topics with spaced repetition, and optimize your resume with 100-point ATS diagnostic intelligence.
        </p>

        {/* Call to Actions */}
        <div className="flex flex-wrap items-center justify-center gap-3 pt-3">
          {isAuthenticated ? (
            <Link to="/dashboard">
              <Button variant="primary" size="lg" className="shadow-md">
                Open Academic Command Center →
              </Button>
            </Link>
          ) : (
            <>
              <Link to="/register">
                <Button variant="primary" size="lg" className="shadow-md">
                  Get Started Free
                </Button>
              </Link>
              <Link to="/login">
                <Button variant="secondary" size="lg">
                  Sign In to Workspace
                </Button>
              </Link>
            </>
          )}
          <Link to={isAuthenticated ? '/knowledge-hub' : '/login'}>
            <Button variant="outline" size="lg">
              Explore Knowledge Vault
            </Button>
          </Link>
        </div>

        {/* Feature Badges Grid */}
        <div className="pt-6 grid grid-cols-2 sm:grid-cols-4 gap-3 max-w-4xl mx-auto text-left">
          <div className="p-3.5 rounded-xl bg-bg-surface-elevated border border-border-default flex flex-col justify-between">
            <span className="text-[10px] font-bold text-brand-teal uppercase tracking-wider">
              Knowledge Hub 2.0
            </span>
            <p className="text-xs font-semibold text-text-primary mt-1">
              100MB PDF / DOCX & OCR Extraction
            </p>
          </div>

          <div className="p-3.5 rounded-xl bg-bg-surface-elevated border border-border-default flex flex-col justify-between">
            <span className="text-[10px] font-bold text-brand-gold uppercase tracking-wider">
              Coding Tracker
            </span>
            <p className="text-xs font-semibold text-text-primary mt-1">
              20 Topics & Spaced Revision Queue
            </p>
          </div>

          <div className="p-3.5 rounded-xl bg-bg-surface-elevated border border-border-default flex flex-col justify-between">
            <span className="text-[10px] font-bold text-status-success uppercase tracking-wider">
              Career Hub
            </span>
            <p className="text-xs font-semibold text-text-primary mt-1">
              100-Pt ATS Diagnostic & Role Readiness
            </p>
          </div>

          <div className="p-3.5 rounded-xl bg-bg-surface-elevated border border-border-default flex flex-col justify-between">
            <span className="text-[10px] font-bold text-brand-teal-light uppercase tracking-wider">
              AI Study Twin
            </span>
            <p className="text-xs font-semibold text-text-primary mt-1">
              Curriculum Grounding & RAG Q&A
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}
