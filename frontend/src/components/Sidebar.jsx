import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import ThemeToggle from './ThemeToggle';
import Badge from './ui/Badge';

export default function Sidebar({ isOpen, onClose }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const workspaceNav = [
    {
      name: 'Dashboard',
      path: '/dashboard',
      badge: null,
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.75}
            d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
          />
        </svg>
      ),
    },
    {
      name: 'Knowledge Hub',
      path: '/knowledge-hub',
      badge: '2.0',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.75}
            d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"
          />
        </svg>
      ),
    },
    {
      name: 'Learning Hub',
      path: '/learning',
      badge: 'Adaptive',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.75}
            d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"
          />
        </svg>
      ),
    },
    {
      name: 'AI Intelligence',
      path: '/intelligence',
      badge: 'Memory',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.75}
            d="M13 10V3L4 14h7v7l9-11h-7z"
          />
        </svg>
      ),
    },
  ];

  const careerCodingNav = [
    {
      name: 'DSA Tracker',
      path: '/dsa',
      badge: '20 Topics',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.75}
            d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
          />
        </svg>
      ),
    },
    {
      name: 'Career & Resume Hub',
      path: '/career',
      badge: 'ATS Scanner',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.75}
            d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
          />
        </svg>
      ),
    },
  ];

  const systemNav = [
    {
      name: 'Profile & Settings',
      path: '/profile',
      badge: null,
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.75}
            d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
          />
        </svg>
      ),
    },
  ];

  const sidebarContent = (
    <div className="flex flex-col h-full bg-bg-sidebar border-r border-border-default text-text-primary transition-colors select-none">
      {/* Brand Header */}
      <div className="p-4 sm:p-5 border-b border-border-subtle flex items-center justify-between shrink-0">
        <NavLink to="/dashboard" onClick={onClose} className="flex items-center space-x-3 min-w-0 group">
          <div className="w-9 h-9 rounded-xl bg-brand-navy border border-brand-teal/40 flex items-center justify-center shadow-sm text-white font-black text-sm shrink-0 group-hover:border-brand-teal transition-colors">
            <span className="text-brand-gold">S</span>
            <span className="text-brand-teal">T</span>
          </div>
          <div className="min-w-0">
            <h2 className="text-sm font-extrabold text-text-primary tracking-tight truncate group-hover:text-brand-teal transition-colors">
              AI ScholarTwin
            </h2>
            <p className="text-[10px] text-text-muted font-medium truncate uppercase tracking-wider">
              Academic Intelligence
            </p>
          </div>
        </NavLink>
        {onClose && (
          <button
            type="button"
            onClick={onClose}
            className="md:hidden p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-bg-surface-hover transition-colors cursor-pointer"
            aria-label="Close sidebar"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        )}
      </div>

      {/* Navigation Sections */}
      <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
        {/* Section 1: Academic Workspace */}
        <div>
          <div className="px-3 mb-2 text-[10px] font-bold uppercase tracking-wider text-text-muted">
            Academic Workspace
          </div>
          <nav className="space-y-1">
            {workspaceNav.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3 py-2 rounded-xl text-xs sm:text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-brand-teal text-white shadow-sm font-semibold'
                      : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
                  }`
                }
              >
                <div className="flex items-center gap-3 min-w-0">
                  <span className="shrink-0">{item.icon}</span>
                  <span className="truncate">{item.name}</span>
                </div>
                {item.badge && (
                  <span className="text-[10px] font-bold px-1.5 py-0.5 rounded-md bg-brand-navy/20 dark:bg-black/30 text-current">
                    {item.badge}
                  </span>
                )}
              </NavLink>
            ))}
          </nav>
        </div>

        {/* Section 2: Career & Coding */}
        <div>
          <div className="px-3 mb-2 text-[10px] font-bold uppercase tracking-wider text-text-muted">
            Skills & Career
          </div>
          <nav className="space-y-1">
            {careerCodingNav.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3 py-2 rounded-xl text-xs sm:text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-brand-teal text-white shadow-sm font-semibold'
                      : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
                  }`
                }
              >
                <div className="flex items-center gap-3 min-w-0">
                  <span className="shrink-0">{item.icon}</span>
                  <span className="truncate">{item.name}</span>
                </div>
                {item.badge && (
                  <span className="text-[10px] font-bold px-1.5 py-0.5 rounded-md bg-brand-gold/20 text-brand-gold dark:text-brand-gold-light">
                    {item.badge}
                  </span>
                )}
              </NavLink>
            ))}
          </nav>
        </div>

        {/* Section 3: AI Twin Intelligence */}
        <div>
          <div className="px-3 mb-2 text-[10px] font-bold uppercase tracking-wider text-text-muted flex items-center justify-between">
            <span>AI Assistance</span>
            <span className="text-[9px] font-mono px-1 rounded bg-brand-teal/20 text-brand-teal dark:text-brand-teal-light">
              RAG
            </span>
          </div>
          <div className="p-3 rounded-xl bg-gradient-to-br from-brand-navy/90 to-brand-navy-deep border border-brand-teal/30 text-white space-y-2.5 shadow-sm">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 rounded-lg bg-brand-teal flex items-center justify-center text-white shrink-0">
                <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <span className="text-xs font-bold text-white tracking-tight">AI Twin Study Co-pilot</span>
            </div>
            <p className="text-[11px] text-slate-300 leading-snug">
              Curriculum-grounded Q&A over your uploaded papers, notes, and DSA topics.
            </p>
            <NavLink
              to="/intelligence?tab=chat"
              onClick={onClose}
              className="inline-flex items-center gap-1.5 text-[11px] font-semibold text-brand-gold hover:text-white transition-colors"
            >
              <span>Launch AI Twin Chat →</span>
            </NavLink>
          </div>
        </div>

        {/* Section 4: Preferences & System */}
        <div>
          <div className="px-3 mb-2 text-[10px] font-bold uppercase tracking-wider text-text-muted">
            Preferences & System
          </div>
          <nav className="space-y-1">
            {systemNav.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3 py-2 rounded-xl text-xs sm:text-sm font-medium transition-all ${
                    isActive
                      ? 'bg-brand-teal text-white shadow-sm font-semibold'
                      : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
                  }`
                }
              >
                <div className="flex items-center gap-3 min-w-0">
                  <span className="shrink-0">{item.icon}</span>
                  <span className="truncate">{item.name}</span>
                </div>
              </NavLink>
            ))}
          </nav>
        </div>

        {/* Appearance Control */}
        <div>
          <div className="px-3 mb-2 text-[10px] font-bold uppercase tracking-wider text-text-muted">
            Appearance
          </div>
          <div className="px-1">
            <ThemeToggle variant="full" />
          </div>
        </div>
      </div>

      {/* User Footer Card */}
      <div className="p-3.5 border-t border-border-subtle bg-bg-surface-elevated/40 shrink-0">
        <div className="flex items-center gap-2.5 mb-2.5 min-w-0">
          <div className="w-8 h-8 rounded-xl bg-brand-navy border border-brand-teal/40 flex items-center justify-center text-brand-gold font-bold text-xs shrink-0 shadow-xs">
            {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold text-text-primary truncate">{user?.fullName || 'Scholar'}</p>
            <p className="text-[10px] text-text-muted truncate">{user?.email || 'scholar@university.edu'}</p>
          </div>
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-border-subtle text-xs">
          <span className="inline-flex items-center gap-1.5 text-status-success font-medium text-[10px]">
            <span className="w-1.5 h-1.5 rounded-full bg-status-success animate-pulse" />
            MySQL 8 Live
          </span>
          <button
            type="button"
            onClick={handleLogout}
            className="text-text-muted hover:text-status-error transition-colors font-medium flex items-center gap-1 cursor-pointer text-xs"
          >
            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
              />
            </svg>
            <span>Sign Out</span>
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop Sidebar (Permanent) */}
      <aside className="hidden md:block w-64 shrink-0 h-screen sticky top-0 z-30">
        {sidebarContent}
      </aside>

      {/* Mobile Drawer (Responsive Overlay) */}
      {isOpen && (
        <div className="md:hidden fixed inset-0 z-50 flex">
          <div
            className="fixed inset-0 bg-brand-navy/70 dark:bg-black/80 backdrop-blur-sm transition-opacity"
            onClick={onClose}
            aria-hidden="true"
          />
          <div className="relative flex-1 flex flex-col max-w-xs w-full bg-bg-sidebar z-10 shadow-modal">
            {sidebarContent}
          </div>
        </div>
      )}
    </>
  );
}
