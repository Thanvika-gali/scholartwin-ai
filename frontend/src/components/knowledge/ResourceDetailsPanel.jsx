import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';
import Badge from '../ui/Badge';
import {
  fetchDocumentIntelligenceApi,
  regenerateDocumentIntelligenceApi,
  fetchRelatedResourcesApi,
  connectResourceApi,
  disconnectResourceApi,
  downloadDocumentApi,
} from '../../services/api';

export default function ResourceDetailsPanel({
  resource,
  isOpen,
  onClose,
  allFolders = [],
  onMoveToFolder,
  onToggleFavorite,
  onToggleArchive,
  onDeleteResource,
  onOpenConnectModal,
}) {
  const [activeTab, setActiveTab] = useState('intelligence'); // 'intelligence' | 'overview' | 'related'
  const [intelligence, setIntelligence] = useState(null);
  const [loadingIntelligence, setLoadingIntelligence] = useState(false);
  const [relatedResources, setRelatedResources] = useState([]);
  const [loadingRelated, setLoadingRelated] = useState(false);
  const [isRegenerating, setIsRegenerating] = useState(false);
  const [folderId, setFolderId] = useState('');

  useEffect(() => {
    if (!resource) return;

    setFolderId(resource.folderId ? String(resource.folderId) : '');

    // If it's a document, load intelligence data
    if (resource.resourceType === 'DOCUMENT') {
      setLoadingIntelligence(true);
      fetchDocumentIntelligenceApi(resource.id)
        .then((res) => {
          setIntelligence(res);
        })
        .catch(() => setIntelligence(null))
        .finally(() => setLoadingIntelligence(false));
    } else {
      setIntelligence(null);
    }

    // Load related resources
    setLoadingRelated(true);
    fetchRelatedResourcesApi(resource.resourceType, resource.id)
      .then((res) => {
        setRelatedResources(Array.isArray(res) ? res : []);
      })
      .catch(() => setRelatedResources([]))
      .finally(() => setLoadingRelated(false));
  }, [resource]);

  if (!isOpen || !resource) return null;

  const handleFolderChange = (e) => {
    const val = e.target.value;
    setFolderId(val);
    onMoveToFolder(resource, val ? Number(val) : null);
  };

  const handleRegenerate = async () => {
    if (resource.resourceType !== 'DOCUMENT') return;
    setIsRegenerating(true);
    try {
      const updated = await regenerateDocumentIntelligenceApi(resource.id);
      setIntelligence(updated);
    } catch (err) {
      console.error(err);
    } finally {
      setIsRegenerating(false);
    }
  };

  const handleDisconnect = async (targetType, targetId) => {
    try {
      await disconnectResourceApi(resource.resourceType, resource.id, targetType, targetId);
      setRelatedResources((prev) => prev.filter((r) => r.id !== targetId || r.resourceType !== targetType));
    } catch (err) {
      console.error(err);
    }
  };

  const handleDownload = async () => {
    if (resource.resourceType === 'DOCUMENT') {
      await downloadDocumentApi(resource.id, resource.title);
    } else if (resource.url) {
      window.open(resource.url, '_blank', 'noopener,noreferrer');
    }
  };

  const panelContent = (
    <div className="flex flex-col h-full bg-bg-surface border-l border-border-default shadow-modal select-none overflow-hidden">
      {/* Top Header */}
      <div className="p-4 border-b border-border-subtle flex items-start justify-between gap-3 shrink-0 bg-bg-surface">
        <div className="min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <Badge variant="teal" size="xs">
              {resource.resourceType}
            </Badge>
            {resource.folderName && (
              <span className="text-[11px] text-text-muted truncate">
                in {resource.folderName}
              </span>
            )}
          </div>
          <h3 className="text-sm sm:text-base font-bold text-text-primary truncate" title={resource.title}>
            {resource.title}
          </h3>
        </div>

        <button
          onClick={onClose}
          className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-bg-surface-hover transition-colors cursor-pointer"
          aria-label="Close inspector panel"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-border-subtle px-4 text-xs font-semibold gap-4 shrink-0 bg-bg-surface-elevated/30">
        <button
          onClick={() => setActiveTab('intelligence')}
          className={`py-2.5 border-b-2 transition-colors cursor-pointer ${
            activeTab === 'intelligence'
              ? 'border-brand-teal text-brand-teal font-bold'
              : 'border-transparent text-text-muted hover:text-text-primary'
          }`}
        >
          Intelligence
        </button>
        <button
          onClick={() => setActiveTab('related')}
          className={`py-2.5 border-b-2 transition-colors cursor-pointer ${
            activeTab === 'related'
              ? 'border-brand-teal text-brand-teal font-bold'
              : 'border-transparent text-text-muted hover:text-text-primary'
          }`}
        >
          Connections ({relatedResources.length})
        </button>
        <button
          onClick={() => setActiveTab('overview')}
          className={`py-2.5 border-b-2 transition-colors cursor-pointer ${
            activeTab === 'overview'
              ? 'border-brand-teal text-brand-teal font-bold'
              : 'border-transparent text-text-muted hover:text-text-primary'
          }`}
        >
          Overview
        </button>
      </div>

      {/* Body Content */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 text-xs">
        {/* Quick Folder Move Selector */}
        <div>
          <label className="text-[10px] font-bold text-text-muted uppercase tracking-wider block mb-1">
            Current Folder
          </label>
          <select
            value={folderId}
            onChange={handleFolderChange}
            className="w-full px-2.5 py-1.5 text-xs rounded-xl border border-border-default bg-bg-surface-elevated text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
          >
            <option value="">Smart Inbox (Root)</option>
            {allFolders.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </select>
        </div>

        {activeTab === 'intelligence' && (
          <div className="space-y-4">
            {resource.resourceType !== 'DOCUMENT' ? (
              <div className="py-8 text-center text-text-muted">
                Document intelligence is available for parsed PDF and DOCX documents.
              </div>
            ) : loadingIntelligence ? (
              <div className="py-8 text-center text-text-muted">Analyzing document metadata...</div>
            ) : !intelligence ? (
              <div className="py-6 text-center text-text-muted space-y-2">
                <p>No intelligence summary generated yet.</p>
                <Button
                  type="button"
                  variant="outline"
                  size="xs"
                  onClick={handleRegenerate}
                  isLoading={isRegenerating}
                >
                  Generate Intelligence
                </Button>
              </div>
            ) : (
              <div className="space-y-4">
                {/* Executive Summary */}
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <span className="text-[10px] font-bold text-text-muted uppercase tracking-wider">
                      Key Takeaway
                    </span>
                    <button
                      onClick={handleRegenerate}
                      disabled={isRegenerating}
                      className="text-[10px] text-brand-teal hover:underline font-semibold cursor-pointer"
                    >
                      {isRegenerating ? 'Refreshing...' : 'Refresh'}
                    </button>
                  </div>
                  <div className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary leading-relaxed">
                    {intelligence.executiveSummary || 'Summary not available.'}
                  </div>
                </div>

                {/* Key Concepts */}
                {intelligence.keyConcepts && intelligence.keyConcepts.length > 0 && (
                  <div>
                    <span className="text-[10px] font-bold text-text-muted uppercase tracking-wider block mb-1">
                      Key Concepts & Invariants
                    </span>
                    <div className="flex flex-wrap gap-1.5">
                      {intelligence.keyConcepts.map((concept, idx) => (
                        <span
                          key={idx}
                          className="px-2 py-0.5 rounded-md bg-brand-teal/10 text-brand-teal text-[11px] font-medium border border-brand-teal/20"
                        >
                          {concept}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {/* Study Guide / Action Points */}
                {intelligence.actionItems && intelligence.actionItems.length > 0 && (
                  <div>
                    <span className="text-[10px] font-bold text-text-muted uppercase tracking-wider block mb-1">
                      Study Questions & Action Items
                    </span>
                    <ul className="space-y-1.5">
                      {intelligence.actionItems.map((item, idx) => (
                        <li key={idx} className="flex items-start gap-1.5 text-text-secondary leading-snug">
                          <span className="text-brand-teal font-bold mt-0.5">•</span>
                          <span>{item}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {activeTab === 'related' && (
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-bold text-text-muted uppercase tracking-wider">
                Cross-Linked Items
              </span>
              <Button type="button" variant="outline" size="xs" onClick={onOpenConnectModal}>
                + Connect Item
              </Button>
            </div>

            {loadingRelated ? (
              <div className="py-8 text-center text-text-muted">Loading connections...</div>
            ) : relatedResources.length === 0 ? (
              <div className="py-8 text-center text-text-muted border border-dashed border-border-default rounded-xl p-4">
                No related materials connected yet.
                <button
                  type="button"
                  onClick={onOpenConnectModal}
                  className="block mx-auto mt-2 text-brand-teal font-semibold hover:underline cursor-pointer"
                >
                  Link a related document or note
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                {relatedResources.map((rel) => (
                  <div
                    key={`${rel.resourceType}-${rel.id}`}
                    className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default flex items-start justify-between gap-2"
                  >
                    <div className="min-w-0">
                      <div className="flex items-center gap-1.5 mb-1">
                        <Badge variant="teal" size="xs">
                          {rel.resourceType}
                        </Badge>
                        <span className="font-semibold text-text-primary truncate max-w-[170px]">
                          {rel.title}
                        </span>
                      </div>
                      <p className="text-[11px] text-text-muted line-clamp-1">
                        {rel.folderName || 'Smart Inbox'}
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleDisconnect(rel.resourceType, rel.id)}
                      className="text-text-muted hover:text-status-error p-1 cursor-pointer"
                      title="Disconnect relation"
                    >
                      <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'overview' && (
          <div className="space-y-3">
            <div>
              <label className="text-[10px] font-bold text-text-muted uppercase tracking-wider block mb-1">
                Content / Notes
              </label>
              <div className="p-3 rounded-xl bg-bg-surface-elevated border border-border-default max-h-48 overflow-y-auto whitespace-pre-wrap text-text-secondary leading-relaxed">
                {resource.content || resource.description || 'No direct text content.'}
              </div>
            </div>

            {resource.url && (
              <div>
                <label className="text-[10px] font-bold text-text-muted uppercase tracking-wider block mb-1">
                  Source URL
                </label>
                <a
                  href={resource.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-brand-teal hover:underline break-all block"
                >
                  {resource.url}
                </a>
              </div>
            )}

            <div className="pt-2 border-t border-border-subtle space-y-1.5 text-text-muted">
              <div className="flex justify-between">
                <span>Created:</span>
                <span className="font-mono text-text-primary">
                  {resource.createdAt ? new Date(resource.createdAt).toLocaleDateString() : '—'}
                </span>
              </div>
              <div className="flex justify-between">
                <span>Updated:</span>
                <span className="font-mono text-text-primary">
                  {resource.updatedAt ? new Date(resource.updatedAt).toLocaleDateString() : '—'}
                </span>
              </div>
              {resource.fileSizeBytes > 0 && (
                <div className="flex justify-between">
                  <span>File Size:</span>
                  <span className="font-mono text-text-primary">
                    {resource.fileSizeFormatted}
                  </span>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Footer Actions */}
      <div className="p-3.5 border-t border-border-subtle bg-bg-surface-elevated/40 flex items-center justify-between gap-2 shrink-0">
        <Button
          type="button"
          variant="secondary"
          size="xs"
          onClick={handleDownload}
        >
          {resource.resourceType === 'DOCUMENT' ? 'Download' : 'Open Link'}
        </Button>

        <div className="flex items-center gap-1.5">
          <Button
            type="button"
            variant="ghost"
            size="xs"
            onClick={() => onToggleArchive(resource)}
          >
            {resource.isArchived ? 'Unarchive' : 'Archive'}
          </Button>
          <Button
            type="button"
            variant="destructive"
            size="xs"
            onClick={() => onDeleteResource(resource)}
          >
            Delete
          </Button>
        </div>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop Docked Sidebar (on xl screens) */}
      <aside className="hidden xl:flex w-96 flex-shrink-0 flex-col h-[calc(100vh-3.5rem)] sticky top-14 z-20">
        {panelContent}
      </aside>

      {/* Slide-over Drawer for smaller screens (< xl) */}
      <div className="xl:hidden fixed inset-0 z-50 flex justify-end">
        <div
          className="fixed inset-0 bg-brand-navy/60 dark:bg-black/75 backdrop-blur-sm transition-opacity"
          onClick={onClose}
          aria-hidden="true"
        />
        <div className="relative w-full max-w-sm sm:max-w-md h-full z-10 animate-in slide-in-from-right duration-200">
          {panelContent}
        </div>
      </div>
    </>
  );
}
