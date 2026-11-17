import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import Button from './ui/Button';
import Badge from './ui/Badge';

export default function HeroSection() {
  const { isAuthenticated } = useAuth();

  return (
    <section className="relative overflow-hidden pt-16 pb-20 md:pt-24 md:pb-28 border-b border-border-default bg-bg-surface transition-colors">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10 space-y-6">
        <div className="inline-flex items-center gap-2">
          <Badge variant="teal" icon={<span className="w-1.5 h-1.5 rounded-full bg-brand-teal animate-pulse" />}>
            Intelligent Academic Companion
          </Badge>
        </div>

        <h1 className="text-4xl sm:text-5xl md:text-6xl font-extrabold tracking-tight text-text-primary leading-tight">
          Your Personalized{' '}
          <span className="text-brand-teal dark:text-brand-teal-light">
            Academic Knowledge
          </span>{' '}
          & AI Workspace
        </h1>

        <p className="max-w-3xl mx-auto text-base sm:text-lg md:text-xl text-text-secondary leading-relaxed font-normal">
          AI ScholarTwin is a dedicated workspace built for computer science students and engineers. Curate lecture notes, organize code concepts, archive verified learning resources, and prepare for grounded AI study trajectories.
        </p>

        <div className="flex flex-wrap items-center justify-center gap-3 pt-2">
          {isAuthenticated ? (
            <Link to="/dashboard">
              <Button variant="primary" size="lg">
                Go to Workspace →
              </Button>
            </Link>
          ) : (
            <>
              <Link to="/register">
                <Button variant="primary" size="lg">
                  Start Your Academic Vault
                </Button>
              </Link>
              <Link to="/login">
                <Button variant="secondary" size="lg">
                  Sign In to ScholarTwin
                </Button>
              </Link>
            </>
          )}
        </div>

        <div className="pt-8 flex flex-wrap items-center justify-center gap-4 text-xs text-text-muted">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-bg-surface-elevated border border-border-default">
            <span className="text-brand-teal font-semibold">Structured Vault:</span> Full CRUD Notes & Links
          </div>
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-bg-surface-elevated border border-border-default">
            <span className="text-brand-gold font-semibold">Zero Data Leakage:</span> Multi-Tenant Ownership
          </div>
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-bg-surface-elevated border border-border-default">
            <span className="text-status-success font-semibold">Live Storage:</span> MySQL 8.x + Spring Boot
          </div>
        </div>
      </div>
    </section>
  );
}
