import React from 'react';

export default function Footer() {
  return (
    <footer className="border-t border-slate-800 bg-slate-950 py-8 text-center text-xs text-slate-500">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-3">
        <p className="text-slate-400">
          AI ScholarTwin &copy; {new Date().getFullYear()} — Computer Science Engineering Project.
        </p>
        <p className="flex flex-wrap items-center justify-center gap-2 text-slate-500">
          <span>React 19</span>
          <span>•</span>
          <span>Vite</span>
          <span>•</span>
          <span>Tailwind CSS v4</span>
          <span>•</span>
          <span>Spring Boot 3</span>
          <span>•</span>
          <span>Java 17 LTS</span>
          <span>•</span>
          <span>MySQL 8.x</span>
        </p>
        <p className="text-[11px] text-slate-600">
          Milestone 1 Architecture & Foundation. All future AI components are safely modularized.
        </p>
      </div>
    </footer>
  );
}
