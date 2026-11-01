import React from 'react';

export default function HeroSection() {
  return (
    <section className="relative overflow-hidden pt-12 pb-16 md:pt-20 md:pb-24 border-b border-slate-800/80">
      {/* Background glow effects */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[300px] bg-gradient-to-tr from-indigo-600/15 to-cyan-500/10 blur-3xl pointer-events-none rounded-full" />

      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-900 border border-slate-800 text-xs font-medium text-slate-300 mb-6 shadow-sm">
          <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          <span>Active Phase: Full-Stack Project Foundation & Verification</span>
        </div>

        <h1 className="text-4xl sm:text-5xl md:text-6xl font-extrabold tracking-tight text-white mb-6 leading-tight">
          Your Personalized{' '}
          <span className="bg-gradient-to-r from-indigo-400 via-purple-300 to-cyan-400 bg-clip-text text-transparent">
            AI Academic Twin
          </span>
        </h1>

        <p className="max-w-3xl mx-auto text-base sm:text-lg md:text-xl text-slate-300 mb-8 leading-relaxed font-normal">
          AI ScholarTwin is an intelligent companion built for computer science students. It is designed to index academic resources, answer technical questions grounded in your personal course notes, track coding & DSA progress, and construct customized learning trajectories.
        </p>

        <div className="flex flex-wrap items-center justify-center gap-4 text-xs sm:text-sm text-slate-400">
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-md bg-slate-900/80 border border-slate-800">
            <span className="text-indigo-400 font-semibold">Frontend:</span> React 19 + Tailwind CSS
          </div>
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-md bg-slate-900/80 border border-slate-800">
            <span className="text-emerald-400 font-semibold">Backend:</span> Spring Boot 3 + Java 17
          </div>
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-md bg-slate-900/80 border border-slate-800">
            <span className="text-cyan-400 font-semibold">Database:</span> MySQL 8.x
          </div>
        </div>
      </div>
    </section>
  );
}
