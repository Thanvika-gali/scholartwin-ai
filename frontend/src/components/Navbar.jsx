import React from 'react';

export default function Navbar() {
  return (
    <header className="border-b border-slate-800 bg-slate-900/70 backdrop-blur-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 via-indigo-500 to-cyan-400 flex items-center justify-center shadow-lg shadow-indigo-500/20 text-white font-bold text-lg tracking-wider">
            ST
          </div>
          <div>
            <span className="text-xl font-bold bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
              AI ScholarTwin
            </span>
            <span className="hidden sm:inline-block ml-2 px-2 py-0.5 text-xs font-semibold rounded-full bg-indigo-950 text-indigo-300 border border-indigo-800/60">
              v0.1 Foundation
            </span>
          </div>
        </div>

        {/* Navigation / Info Badges */}
        <nav className="flex items-center space-x-3 sm:space-x-6 text-sm">
          <span className="text-slate-400 text-xs sm:text-sm font-medium">
            Milestone 1 Architecture
          </span>
          <a
            href="#system-status"
            className="px-3 py-1.5 rounded-lg text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 transition-colors"
          >
            System Status
          </a>
        </nav>
      </div>
    </header>
  );
}
