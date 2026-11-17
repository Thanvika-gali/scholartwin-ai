import React, { useState, useEffect } from 'react';
import { fetchHealthStatus } from '../services/api';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from './ui/Card';
import Button from './ui/Button';
import Badge from './ui/Badge';

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
    <Card className="max-w-4xl mx-auto shadow-card">
      <CardHeader className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-[10px] font-bold uppercase tracking-wider text-text-muted">
              Service Diagnostics
            </span>
            <span className="text-text-muted">•</span>
            <span className="text-xs font-mono text-brand-teal">GET /api/health</span>
          </div>
          <CardTitle>System Connectivity Status</CardTitle>
          <CardDescription>
            Live verification between React client, Vite proxy, Spring Boot REST server, and MySQL database.
          </CardDescription>
        </div>

        <Button
          variant="secondary"
          size="sm"
          onClick={checkStatus}
          isLoading={status.loading}
        >
          {status.loading ? 'Verifying...' : 'Probe Backend'}
        </Button>
      </CardHeader>

      <CardContent className="space-y-4">
        {status.loading ? (
          <div className="flex items-center gap-3 text-text-muted py-3">
            <div className="w-4 h-4 border-2 border-brand-teal border-t-transparent rounded-full animate-spin" />
            <span className="text-xs">Querying Spring Boot health endpoint through Vite proxy...</span>
          </div>
        ) : status.connected ? (
          <div className="space-y-4">
            <div className="flex items-center gap-3 p-3.5 rounded-xl bg-status-success-bg border border-status-success-border text-status-success-text text-xs sm:text-sm">
              <span className="w-2.5 h-2.5 rounded-full bg-status-success animate-pulse shrink-0" />
              <span>
                <strong>Operational:</strong> Backend is responsive, health checks pass, and database is connected.
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 font-mono text-xs">
              <div className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default">
                <span className="text-text-muted block mb-1">REST API Status</span>
                <span className="text-status-success font-bold">{status.data?.status || 'UP'}</span>
              </div>
              <div className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default">
                <span className="text-text-muted block mb-1">Service Identifier</span>
                <span className="text-text-primary font-semibold">{status.data?.service || 'AI ScholarTwin'}</span>
              </div>
              <div className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default">
                <span className="text-text-muted block mb-1">Last Verified</span>
                <span className="text-text-secondary">{status.checkedAt}</span>
              </div>
            </div>

            {status.data?.database && (
              <div className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default font-mono text-xs flex items-center justify-between">
                <span className="text-text-muted">MySQL 8.x Persistence</span>
                <Badge variant={status.data.database === 'CONNECTED' ? 'success' : 'error'}>
                  {status.data.database}
                </Badge>
              </div>
            )}
          </div>
        ) : (
          <div className="p-4 rounded-xl bg-status-error-bg border border-status-error-border text-status-error-text text-xs sm:text-sm space-y-2">
            <div className="flex items-center gap-2 font-bold">
              <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <span>Backend Unreachable</span>
            </div>
            <p className="text-xs opacity-90 leading-relaxed">
              Unable to reach the Spring Boot backend at <code>http://localhost:8080/api/health</code>. Verify that the Spring Boot server process is active.
            </p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
