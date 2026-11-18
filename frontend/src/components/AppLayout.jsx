import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import ThemeToggle from './ThemeToggle';
import { useAuth } from '../hooks/useAuth';
import Badge from './ui/Badge';

export default function AppLayout({ children }) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchModalOpen, setSearchModalOpen] = useState(false);
  const { user } = useAuth();
  const location = useLocation();

  const getPageInfo = () => {
    if (location.pathname.startsWith('/dashboard')) {
      return {
        title: 'Academic Command Center',
        section: 'Dashboard',
        badge: 'Live Overview',
      };
    }
    if (location.pathname.startsWith('/knowledge-hub')) {
      return {
        title: 'Smart Knowledge Hub 2.0',
        section: 'Knowledge Vault',
        badge: '100MB + OCR',
      };
    }
    if (location.pathname.startsWith('/learning')) {
      return {
        title: 'Personalized Learning & Revision Engine',
        section: 'Adaptive Engine',
        badge: 'Milestone 8',
      };
    }
    if (location.pathname.startsWith('/intelligence')) {
      return {
        title: 'AI Insights, Memory & Analytics',
        section: 'Cognitive Engine',
        badge: 'Milestone 9',
      };
    }
    if (location.pathname.startsWith('/dsa')) {
      return {
        title: 'DSA & Coding Progress Tracker',
        section: 'Algorithmic Mastery',
        badge: 'Spaced Repetition',
      };
    }
    if (location.pathname.startsWith('/career')) {
      return {
        title: 'Career & Resume Intelligence',
        section: 'Career Hub',
        badge: '100-pt ATS',
      };
    }
    if (location.pathname.startsWith('/profile')) {
      return {
        title: 'Profile & Settings',
        section: 'Preferences',
        badge: 'Customization',
      };
    }
    return {
      title: 'AI ScholarTwin',
      section: 'Workspace',
      badge: null,
    };
  };

  const pageInfo = getPageInfo();

  return (
    <div className="min-h-screen bg-bg-page flex flex-col md:flex-row text-text-primary transition-colors">
      {/* Sidebar Navigation */}
      <Sidebar isOpen={mobileMenuOpen} onClose={() => setMobileMenuOpen(false)} />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top Header Bar */}
        <header className="flex items-center justify-between px-4 sm:px-6 py-3 bg-bg-surface/90 backdrop-blur-md border-b border-border-default sticky top-0 z-30 transition-colors">
          <div className="flex items-center gap-3 min-w-0">
            {/* Mobile Hamburger Button */}
            <button
              type="button"
              onClick={() => setMobileMenuOpen(true)}
              className="md:hidden p-2 rounded-xl text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover transition-colors cursor-pointer shrink-0"
              aria-label="Open navigation menu"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>

            {/* Breadcrumb & Title */}
            <div className="min-w-0 flex items-center gap-2">
              <span className="hidden sm:inline-block text-xs font-semibold uppercase tracking-wider text-text-muted">
                {pageInfo.section}
              </span>
              <span className="hidden sm:inline-block text-border-default">/</span>
              <h1 className="text-sm sm:text-base font-bold text-text-primary tracking-tight truncate">
                {pageInfo.title}
              </h1>
              {pageInfo.badge && (
                <span className="hidden lg:inline-flex items-center">
                  <Badge variant="teal" size="xs">
                    {pageInfo.badge}
                  </Badge>
                </span>
              )}
            </div>
          </div>

          {/* Header Right Actions */}
          <div className="flex items-center space-x-2 sm:space-x-3 shrink-0">
            {/* Quick Search Entry */}
            <Link
              to="/knowledge-hub"
              className="hidden lg:flex items-center gap-2 px-3 py-1.5 rounded-xl bg-bg-surface-elevated hover:bg-bg-surface-hover border border-border-default text-xs text-text-muted hover:text-text-primary transition-colors"
              title="Search knowledge vault, documents, and notes"
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <span>Quick Search...</span>
              <kbd className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-bg-surface border border-border-subtle text-text-muted">
                /
              </kbd>
            </Link>

            {/* System Theme Toggle */}
            <ThemeToggle />

            {/* User Profile Pill */}
            <Link
              to="/profile"
              className="flex items-center gap-2 pl-1.5 pr-3 py-1.5 rounded-xl bg-bg-surface-elevated hover:bg-bg-surface-hover border border-border-default transition-all text-xs font-medium text-text-primary group shadow-xs"
              title="Student Profile & Preferences"
            >
              <div className="w-7 h-7 rounded-lg bg-brand-navy border border-brand-teal/40 text-brand-gold text-xs font-bold flex items-center justify-center shrink-0">
                {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <div className="hidden sm:flex flex-col text-left leading-tight min-w-0">
                <span className="font-semibold text-text-primary max-w-[120px] truncate">
                  {user?.fullName?.split(' ')[0] || 'Scholar'}
                </span>
                <span className="text-[10px] text-text-muted">Account</span>
              </div>
            </Link>
          </div>
        </header>

        {/* Workspace Page Content */}
        <main className="flex-1 min-w-0">{children}</main>
      </div>
    </div>
  );
}
