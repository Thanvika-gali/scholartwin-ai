import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import Sidebar from './Sidebar';
import ThemeToggle from './ThemeToggle';
import { useAuth } from '../hooks/useAuth';

export default function AppLayout({ children }) {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { user } = useAuth();
  const location = useLocation();

  const getPageTitle = () => {
    if (location.pathname.startsWith('/dashboard')) return 'Academic Dashboard';
    if (location.pathname.startsWith('/knowledge-hub')) return 'Knowledge Hub';
    if (location.pathname.startsWith('/profile')) return 'Profile & Settings';
    return 'AI ScholarTwin';
  };

  return (
    <div className="min-h-screen bg-bg-page flex flex-col md:flex-row text-text-primary transition-colors">
      {/* Sidebar Navigation */}
      <Sidebar isOpen={mobileMenuOpen} onClose={() => setMobileMenuOpen(false)} />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top Header Bar */}
        <header className="flex items-center justify-between px-4 sm:px-6 py-3.5 bg-bg-surface border-b border-border-default sticky top-0 z-20 transition-colors">
          <div className="flex items-center gap-3 min-w-0">
            {/* Mobile Hamburger Button */}
            <button
              type="button"
              onClick={() => setMobileMenuOpen(true)}
              className="md:hidden p-2 rounded-xl text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover transition-colors cursor-pointer"
              aria-label="Open navigation menu"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>

            <span className="text-sm font-bold text-text-primary tracking-tight truncate">
              {getPageTitle()}
            </span>
          </div>

          {/* Header Actions */}
          <div className="flex items-center space-x-2 sm:space-x-3">
            <ThemeToggle />

            <Link
              to="/profile"
              className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-xl bg-bg-surface-elevated hover:bg-bg-surface-hover border border-border-default transition-colors text-xs font-medium text-text-primary group"
              title="View student profile"
            >
              <div className="w-6 h-6 rounded-lg bg-brand-teal text-white text-[11px] font-bold flex items-center justify-center shrink-0">
                {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <span className="hidden sm:inline-block max-w-[120px] truncate">
                {user?.fullName || 'Profile'}
              </span>
            </Link>
          </div>
        </header>

        {/* Workspace Page Content */}
        <main className="flex-1 min-w-0">{children}</main>
      </div>
    </div>
  );
}
