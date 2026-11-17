import React from 'react';
import { Link } from 'react-router-dom';

export default function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="border-t border-border-default bg-bg-surface text-text-muted py-8 text-xs transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center space-x-2">
          <div className="w-5 h-5 rounded-md bg-brand-navy border border-brand-teal/40 flex items-center justify-center text-[10px] font-bold text-brand-gold">
            ST
          </div>
          <span className="font-semibold text-text-primary">AI ScholarTwin</span>
          <span>— Personalized Academic & Engineering Learning Assistant</span>
        </div>

        <div className="flex items-center space-x-6">
          <Link to="/dashboard" className="hover:text-brand-teal transition-colors">
            Workspace
          </Link>
          <Link to="/knowledge-hub" className="hover:text-brand-teal transition-colors">
            Knowledge Hub
          </Link>
          <span>© {currentYear} AI ScholarTwin. All rights reserved.</span>
        </div>
      </div>
    </footer>
  );
}
