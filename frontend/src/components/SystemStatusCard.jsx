import React, { useState, useEffect } from 'react';
import { fetchHealthStatus } from '../services/api';

export default function SystemStatusCard() {
  const [status, setStatus] = useState({
    loading: true,
    connected: false,
    data: null,
    error: null,
    checkedAt: null,
  });

  const checkStatus = async () => {
    setStatus((prev) => ({ ...prev, loading: true, error: null }));
    const result = await fetchHealthStatus();
    const now = new Date().toLocaleTimeString();

    if (result.success) {
      setStatus({
        loading: false,
        connected: true,
        data: result.data,
        error: null,
        checkedAt: now,
      });
    } else {
      setStatus({
        loading: false,
        connected: false,
        data: null,
        error: result.error,
        checkedAt: now,
      });
    }
  };

  useEffect(() => {
    checkStatus();
  }, []);

  return (
    <section id="system-status" className="py-8 md:py-12 border-t border-slate-800/80 bg-slate-900/30">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="rounded-2xl border border-slate-800 bg-slate-900/80 p-6 md:p-8 shadow-xl">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-800">
            <div>
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Integration Probe
                </span>
                <span className="text-slate-600">•</span>
                <span className="text-xs font-mono text-indigo-400">GET /api/health</span>
              </div>
              <h3 className="text-xl font-bold text-white">
                Spring Boot & Vite Proxy Link
              </h3>
            </div>

            <button
              id="refresh-health-btn"
              onClick={checkStatus}
              disabled={status.loading}
              className="inline-flex items-center justify-center px-4 py-2 rounded-lg text-xs font-medium bg-indigo-600 hover:bg-indigo-500 disabled:bg-slate-800 text-white disabled:text-slate-500 transition-colors cursor-pointer disabled:cursor-not-allowed shadow-md shadow-indigo-600/20"
            >
              {status.loading ? 'Checking...' : 'Refresh Health Status'}
            </button>
          </div>

          <div className="pt-6">
            {status.loading ? (
              <div className="flex items-center gap-3 text-slate-400 py-4">
                <div className="w-4 h-4 border-2 border-indigo-400 border-t-transparent rounded-full animate-spin" />
                <span className="text-sm">Querying Spring Boot health endpoint through Vite proxy...</span>
              </div>
            ) : status.connected ? (
              <div className="space-y-4">
                <div className="flex items-center gap-3 p-3 rounded-lg bg-emerald-950/40 border border-emerald-800/60 text-emerald-300 text-sm">
                  <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
                  <span>
                    <strong>Connected:</strong> Backend is running and responded successfully to health check.
                  </span>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 font-mono text-xs">
                  <div className="p-3 rounded-lg bg-slate-950 border border-slate-800">
                    <span className="text-slate-500 block mb-1">Service Status</span>
                    <span className="text-emerald-400 font-bold">{status.data?.status || 'UP'}</span>
                  </div>
                  <div className="p-3 rounded-lg bg-slate-950 border border-slate-800">
                    <span className="text-slate-500 block mb-1">Backend Service</span>
                    <span className="text-slate-200">{status.data?.service || 'AI ScholarTwin'}</span>
                  </div>
                  <div className="p-3 rounded-lg bg-slate-950 border border-slate-800">
                    <span className="text-slate-500 block mb-1">Last Verified</span>
                    <span className="text-slate-300">{status.checkedAt}</span>
                  </div>
                </div>

                {status.data?.database && (
                  <div className="p-3 rounded-lg bg-slate-950 border border-slate-800 font-mono text-xs flex items-center justify-between">
                    <span className="text-slate-400">Database Connection:</span>
                    <span className={status.data.database === 'CONNECTED' ? 'text-emerald-400 font-semibold' : 'text-amber-400 font-semibold'}>
                      {status.data.database}
                    </span>
                  </div>
                )}
              </div>
            ) : (
              <div className="p-4 rounded-lg bg-amber-950/30 border border-amber-800/40 text-sm space-y-2">
                <div className="flex items-center gap-2 text-amber-300 font-semibold">
                  <span>Backend Offline or Starting</span>
                </div>
                <p className="text-slate-400 text-xs leading-relaxed">
                  The frontend is running on <code className="text-indigo-300">http://localhost:5173</code>, but the backend on <code className="text-indigo-300">http://localhost:8080</code> is not yet running or not responding.
                </p>
                <div className="text-xs text-slate-400 font-mono pt-2 border-t border-amber-900/40">
                  To start the backend: <code className="text-emerald-400">cd backend; .\mvnw.cmd spring-boot:run</code>
                </div>
                {status.checkedAt && (
                  <div className="text-[11px] text-slate-500">
                    Last probe attempt: {status.checkedAt} ({status.error})
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </section>
  );
}
