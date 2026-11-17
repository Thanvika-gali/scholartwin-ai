import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { fetchDashboardSummary } from '../services/api';
import KnowledgeItemModal from '../components/KnowledgeItemModal';
import KnowledgeItemViewModal from '../components/KnowledgeItemViewModal';
import DocumentUploadModal from '../components/DocumentUploadModal';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import PageHeader from '../components/ui/PageHeader';
import EmptyState from '../components/ui/EmptyState';
import { CardSkeleton } from '../components/ui/LoadingSkeleton';
import Alert from '../components/ui/Alert';

export default function DashboardPage() {
  const { user } = useAuth();
  const [summary, setSummary] = useState({
    totalItems: 0,
    totalNotes: 0,
    totalLinks: 0,
    totalDocuments: 0,
    recentItems: [],
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Quick Action Modal states
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState('NOTE');
  const [uploadModalOpen, setUploadModalOpen] = useState(false);

  // View modal state
  const [viewingItem, setViewingItem] = useState(null);

  const loadSummary = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await fetchDashboardSummary();
      setSummary(data);
    } catch (err) {
      setError(err.message || 'Failed to fetch dashboard metrics.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadSummary();
  }, [loadSummary]);

  const handleOpenModal = (type) => {
    setModalType(type);
    setModalOpen(true);
  };

  const handleItemSaved = () => {
    loadSummary();
  };

  const formatDate = (isoString) => {
    if (!isoString) return '';
    try {
      const d = new Date(isoString);
      return d.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return '';
    }
  };

  return (
    <div className="p-4 sm:p-6 lg:p-8 space-y-8 max-w-7xl mx-auto">
      {/* Student Welcome Banner */}
      <section className="bg-bg-surface border border-border-default rounded-3xl p-6 sm:p-8 relative overflow-hidden shadow-card transition-colors">
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div className="space-y-2 min-w-0">
            <div className="flex items-center gap-2">
              <Badge variant="teal" icon={<span className="w-1.5 h-1.5 rounded-full bg-brand-teal animate-pulse" />}>
                Active Session
              </Badge>
              <span className="text-xs text-text-muted font-medium">Personalized Knowledge Vault</span>
            </div>

            <h1 className="text-2xl sm:text-3xl md:text-4xl font-extrabold text-text-primary tracking-tight">
              Welcome back,{' '}
              <span className="text-brand-teal dark:text-brand-teal-light">
                {user?.fullName || 'Scholar'}
              </span>
            </h1>

            <p className="text-xs sm:text-sm text-text-muted max-w-2xl leading-relaxed">
              Organize your academic materials, capture code concepts and formulas, and maintain verified learning links for targeted revision.
            </p>
          </div>

          {/* Quick Action Buttons */}
          <div className="flex flex-wrap items-center gap-3 shrink-0">
            <Button
              variant="primary"
              onClick={() => setUploadModalOpen(true)}
              icon={
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                </svg>
              }
            >
              Upload Document
            </Button>

            <Button
              variant="secondary"
              onClick={() => handleOpenModal('NOTE')}
              icon={
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
              }
            >
              Add Note
            </Button>

            <Button
              variant="secondary"
              onClick={() => handleOpenModal('LINK')}
              icon={
                <svg className="w-4 h-4 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
                  />
                </svg>
              }
            >
              Save Link
            </Button>

            <Link to="/knowledge-hub">
              <Button variant="ghost">
                Knowledge Hub →
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Error state */}
      {error && (
        <Alert variant="error" onClose={() => setError(null)}>
          <div className="flex items-center justify-between">
            <span>Failed to load live metrics: {error}</span>
            <Button variant="ghost" size="sm" onClick={loadSummary}>
              Retry
            </Button>
          </div>
        </Alert>
      )}

      {/* Real Live Metrics Cards */}
      <section className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Total Items */}
        <Card hover>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-text-muted">
                Total Knowledge Items
              </p>
              <h3 className="text-3xl font-extrabold text-text-primary mt-2">
                {isLoading ? (
                  <span className="inline-block w-8 h-8 bg-bg-surface-elevated rounded animate-pulse" />
                ) : (
                  summary.totalItems
                )}
              </h3>
              <p className="text-[11px] text-text-muted mt-1">Grounded in MySQL storage</p>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-bg-surface-elevated text-brand-teal flex items-center justify-center border border-border-default shrink-0">
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.75}
                  d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
                />
              </svg>
            </div>
          </CardContent>
          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs">
            <span className="text-text-muted">Active Vault</span>
            <Link to="/knowledge-hub" className="text-brand-teal hover:underline font-medium">
              View All →
            </Link>
          </div>
        </Card>

        {/* Documents */}
        <Card hover>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-text-muted">
                Documents & Papers
              </p>
              <h3 className="text-3xl font-extrabold text-brand-teal mt-2">
                {isLoading ? (
                  <span className="inline-block w-8 h-8 bg-bg-surface-elevated rounded animate-pulse" />
                ) : (
                  summary.totalDocuments || 0
                )}
              </h3>
              <p className="text-[11px] text-text-muted mt-1">PDF & DOCX Extracted</p>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-bg-surface-elevated text-brand-teal flex items-center justify-center border border-border-default shrink-0">
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.75}
                  d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                />
              </svg>
            </div>
          </CardContent>
          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs">
            <button
              type="button"
              onClick={() => setUploadModalOpen(true)}
              className="text-brand-teal hover:underline font-medium cursor-pointer"
            >
              + Upload Doc
            </button>
            <Link to="/knowledge-hub" className="text-text-muted hover:text-text-primary">
              View Vault →
            </Link>
          </div>
        </Card>

        {/* Notes Count */}
        <Card hover>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-text-muted">
                Academic Notes
              </p>
              <h3 className="text-3xl font-extrabold text-text-primary mt-2">
                {isLoading ? (
                  <span className="inline-block w-8 h-8 bg-bg-surface-elevated rounded animate-pulse" />
                ) : (
                  summary.totalNotes
                )}
              </h3>
              <p className="text-[11px] text-text-muted mt-1">Concepts & formulas</p>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-bg-surface-elevated text-brand-gold flex items-center justify-center border border-border-default shrink-0">
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.75}
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                />
              </svg>
            </div>
          </CardContent>
          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs">
            <button
              type="button"
              onClick={() => handleOpenModal('NOTE')}
              className="text-brand-gold hover:underline font-medium cursor-pointer"
            >
              + Quick Note
            </button>
            <Link to="/knowledge-hub" className="text-text-muted hover:text-text-primary">
              Filter Notes →
            </Link>
          </div>
        </Card>

        {/* Links Count */}
        <Card hover>
          <CardContent className="p-5 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-text-muted">
                Learning Resources
              </p>
              <h3 className="text-3xl font-extrabold text-text-primary mt-2">
                {isLoading ? (
                  <span className="inline-block w-8 h-8 bg-bg-surface-elevated rounded animate-pulse" />
                ) : (
                  summary.totalLinks
                )}
              </h3>
              <p className="text-[11px] text-text-muted mt-1">Verified documentation & repositories</p>
            </div>
            <div className="w-12 h-12 rounded-2xl bg-bg-surface-elevated text-brand-teal flex items-center justify-center border border-border-default shrink-0">
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.75}
                  d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
                />
              </svg>
            </div>
          </CardContent>
          <div className="px-5 py-3 border-t border-border-subtle bg-bg-surface-elevated/30 flex items-center justify-between text-xs">
            <button
              type="button"
              onClick={() => handleOpenModal('LINK')}
              className="text-brand-teal hover:underline font-medium cursor-pointer"
            >
              + Quick Link
            </button>
            <Link to="/knowledge-hub" className="text-text-muted hover:text-text-primary">
              Filter Links →
            </Link>
          </div>
        </Card>
      </section>

      {/* Recent Activity Section */}
      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-text-primary tracking-tight">Recent Knowledge Items</h2>
            <p className="text-xs text-text-muted">Your latest study notes and bookmarked resources</p>
          </div>
          <Link
            to="/knowledge-hub"
            className="text-xs font-semibold text-brand-teal hover:underline"
          >
            Open Knowledge Hub →
          </Link>
        </div>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <CardSkeleton />
            <CardSkeleton />
          </div>
        ) : summary.recentItems && summary.recentItems.length > 0 ? (
          <div className="bg-bg-surface border border-border-default rounded-2xl divide-y divide-border-subtle shadow-card overflow-hidden">
            {summary.recentItems.map((item) => {
              const isLink = item.type === 'LINK';
              return (
                <div
                  key={item.id}
                  className="p-4 sm:p-5 flex items-center justify-between gap-4 hover:bg-bg-surface-hover/50 transition-colors group"
                >
                  <div className="flex items-start gap-3.5 min-w-0">
                    <div
                      className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 mt-0.5 border ${
                        isLink
                          ? 'bg-brand-teal/15 border-brand-teal/30 text-brand-teal'
                          : 'bg-brand-gold/15 border-brand-gold/30 text-brand-gold'
                      }`}
                    >
                      {isLink ? (
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
                          />
                        </svg>
                      ) : (
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                          />
                        </svg>
                      )}
                    </div>

                    <div className="min-w-0">
                      <div className="flex items-center gap-2 mb-1 flex-wrap">
                        <Badge variant={isLink ? 'teal' : 'gold'}>
                          {isLink ? 'Resource Link' : 'Academic Note'}
                        </Badge>
                        {item.category && <Badge variant="default">{item.category}</Badge>}
                      </div>

                      <h4
                        onClick={() => setViewingItem(item)}
                        className="text-sm font-semibold text-text-primary group-hover:text-brand-teal transition-colors cursor-pointer truncate"
                      >
                        {item.title}
                      </h4>

                      <p className="text-xs text-text-muted truncate mt-0.5">
                        {isLink ? item.url : item.content}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 shrink-0">
                    <span className="text-[11px] font-mono text-text-muted hidden sm:inline-block">
                      {formatDate(item.updatedAt)}
                    </span>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => setViewingItem(item)}
                    >
                      View
                    </Button>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          /* Pure First-Use Zero State (No fake demo records) */
          <EmptyState
            title="Your Knowledge Vault is Empty"
            description="Start curating your computer science notes, algorithm invariants, system designs, or documentation links. Items saved here will power your personalized AI twin."
            action={
              <Button
                variant="primary"
                onClick={() => handleOpenModal('NOTE')}
                icon={
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                  </svg>
                }
              >
                Create First Note
              </Button>
            }
            secondaryAction={
              <Button
                variant="secondary"
                onClick={() => handleOpenModal('LINK')}
                icon={
                  <svg className="w-4 h-4 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
                    />
                  </svg>
                }
              >
                Save First Link
              </Button>
            }
          />
        )}
      </section>

      {/* Modals */}
      <DocumentUploadModal
        isOpen={uploadModalOpen}
        onClose={() => setUploadModalOpen(false)}
        onUploaded={() => {
          setUploadModalOpen(false);
          loadSummary();
        }}
      />

      <KnowledgeItemModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onSaved={handleItemSaved}
        defaultType={modalType}
      />

      <KnowledgeItemViewModal
        isOpen={Boolean(viewingItem)}
        onClose={() => setViewingItem(null)}
        item={viewingItem}
        onEdit={() => {
          window.location.href = '/knowledge-hub';
        }}
        onDelete={() => {
          window.location.href = '/knowledge-hub';
        }}
      />
    </div>
  );
}
