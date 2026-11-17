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

  const navItems = [
    {
      name: 'Dashboard',
      path: '/dashboard',
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
      name: 'DSA Tracker',
      path: '/dsa',
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
    {
      name: 'Profile & Settings',
      path: '/profile',
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

  const upcomingModules = [
    {
      name: 'AI Twin Chat',
      description: 'Conversational assistant & personal course RAG',
      icon: (
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.75}
            d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
          />
        </svg>
      ),
    },
  ];

  const sidebarContent = (
    <div className="flex flex-col h-full bg-bg-sidebar border-r border-border-default text-text-primary transition-colors">
      {/* Brand Header */}
      <div className="p-5 border-b border-border-subtle flex items-center justify-between">
        <div className="flex items-center space-x-3 min-w-0">
          <div className="w-9 h-9 rounded-xl bg-brand-navy border border-brand-teal/40 flex items-center justify-center shadow-sm text-white font-extrabold text-sm shrink-0">
            <span className="text-brand-gold">S</span>
            <span className="text-brand-teal">T</span>
          </div>
          <div className="min-w-0">
            <h2 className="text-sm font-bold text-text-primary tracking-tight truncate">AI ScholarTwin</h2>
            <p className="text-[11px] text-text-muted font-medium truncate">Academic Co-pilot</p>
          </div>
        </div>
        {onClose && (
          <button
            type="button"
            onClick={onClose}
            className="md:hidden p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-bg-surface-hover transition-colors"
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
        {/* Core Workspace Navigation */}
        <div>
          <div className="px-3 mb-2 text-[10px] font-bold uppercase tracking-wider text-text-muted">
            Workspace
          </div>
          <nav className="space-y-1">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3 py-2.5 rounded-xl text-xs sm:text-sm font-medium transition-all ${
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

        {/* Future Modules */}
        <div>
          <div className="px-3 mb-2 flex items-center justify-between">
            <span className="text-[10px] font-bold uppercase tracking-wider text-text-muted">
              Planned Capabilities
            </span>
          </div>
          <div className="space-y-1">
            {upcomingModules.map((item) => (
              <div
                key={item.name}
                className="flex items-center justify-between px-3 py-2.5 rounded-xl text-xs text-text-muted bg-bg-surface/40 border border-transparent select-none group"
                title={`${item.name}: ${item.description}`}
              >
                <div className="flex items-center gap-3 min-w-0">
                  <span className="shrink-0 opacity-60">{item.icon}</span>
                  <div className="min-w-0">
                    <p className="font-medium text-text-secondary truncate">{item.name}</p>
                  </div>
                </div>
                <Badge variant="upcoming">Upcoming</Badge>
              </div>
            ))}
          </div>
        </div>

        {/* Theme Settings Control */}
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
      <div className="p-4 border-t border-border-subtle bg-bg-surface-elevated/40">
        <div className="flex items-center gap-3 mb-3 min-w-0">
          <div className="w-9 h-9 rounded-xl bg-brand-navy border border-brand-teal/40 flex items-center justify-center text-brand-gold font-bold text-xs shrink-0 shadow-sm">
            {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold text-text-primary truncate">{user?.fullName || 'Scholar'}</p>
            <p className="text-[11px] text-text-muted truncate">{user?.email || 'scholar@university.edu'}</p>
          </div>
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-border-subtle text-xs">
          <span className="inline-flex items-center gap-1.5 text-status-success font-medium text-[11px]">
            <span className="w-1.5 h-1.5 rounded-full bg-status-success" />
            Connected
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
            className="fixed inset-0 bg-brand-navy/70 dark:bg-black/75 backdrop-blur-sm transition-opacity"
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
