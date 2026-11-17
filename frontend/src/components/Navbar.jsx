import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import ThemeToggle from './ThemeToggle';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <header className="border-b border-border-default bg-bg-surface/95 backdrop-blur-md sticky top-0 z-40 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
        {/* Brand */}
        <Link
          to={isAuthenticated ? '/dashboard' : '/'}
          className="flex items-center space-x-3 group focus:outline-none focus:ring-2 focus:ring-brand-teal rounded-xl"
        >
          <div className="w-10 h-10 rounded-xl bg-brand-navy border border-brand-teal/40 flex items-center justify-center shadow-sm text-white font-extrabold text-base tracking-wider group-hover:border-brand-teal transition-colors">
            <span className="text-brand-gold">S</span>
            <span className="text-brand-teal">T</span>
          </div>
          <div>
            <span className="text-lg font-bold text-text-primary tracking-tight block leading-tight">
              AI ScholarTwin
            </span>
            <span className="text-[11px] text-text-muted font-medium hidden sm:block">
              Academic Productivity Workspace
            </span>
          </div>
        </Link>

        {/* Navigation & Controls */}
        <nav className="flex items-center space-x-2 sm:space-x-3 text-sm">
          {isAuthenticated ? (
            <>
              <Link
                to="/dashboard"
                className={`hidden sm:inline-flex px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                  isActive('/dashboard')
                    ? 'bg-brand-teal/15 text-brand-teal dark:text-brand-teal-light'
                    : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
                }`}
              >
                Dashboard
              </Link>
              <Link
                to="/knowledge-hub"
                className={`hidden sm:inline-flex px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                  isActive('/knowledge-hub')
                    ? 'bg-brand-teal/15 text-brand-teal dark:text-brand-teal-light'
                    : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
                }`}
              >
                Knowledge Hub
              </Link>

              {/* Theme Toggle */}
              <ThemeToggle />

              {/* User Profile */}
              <Link
                to="/profile"
                className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                  isActive('/profile')
                    ? 'bg-brand-teal/15 text-brand-teal border-brand-teal/40'
                    : 'bg-bg-surface-elevated hover:bg-bg-surface-hover text-text-primary border-border-default'
                }`}
              >
                <div className="w-5 h-5 rounded-full bg-brand-teal text-white text-[10px] font-bold flex items-center justify-center">
                  {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
                </div>
                <span className="max-w-[110px] truncate">{user?.fullName || 'Profile'}</span>
              </Link>

              {/* Sign Out */}
              <button
                type="button"
                onClick={handleLogout}
                className="px-3 py-1.5 rounded-lg text-xs font-medium text-text-muted hover:text-status-error hover:bg-status-error-bg/30 border border-transparent hover:border-status-error/30 transition-colors cursor-pointer"
              >
                Sign Out
              </button>
            </>
          ) : (
            <>
              {/* Theme Toggle */}
              <ThemeToggle />

              <Link
                to="/login"
                className="px-3.5 py-1.5 rounded-lg text-xs font-medium text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover transition-colors"
              >
                Sign In
              </Link>
              <Link
                to="/register"
                className="px-4 py-1.5 rounded-lg text-xs font-semibold bg-brand-teal hover:bg-brand-teal-hover text-white shadow-sm shadow-brand-teal/20 transition-all"
              >
                Get Started
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
