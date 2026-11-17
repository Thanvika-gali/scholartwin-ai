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

  return (
    <aside className="w-96 flex-shrink-0 bg-white dark:bg-slate-900 border-l border-slate-200 dark:border-slate-800 flex flex-col h-[calc(100vh-4rem)] sticky top-16 shadow-xl z-20 overflow-hidden">
      {/* Top Header */}
      <div className="p-4 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
        <div className="flex items-center gap-2 min-w-0">
          <Badge variant="teal" size="sm">
            {resource.resourceType}
          </Badge>
          <span className="text-xs font-semibold text-slate-500 truncate">
            ID: #{resource.id}
          </span>
        </div>
        <button
          onClick={onClose}
          className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
          title="Close details"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      {/* Resource Title & Folder Selector */}
      <div className="p-4 border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/40">
        <h3 className="text-sm font-bold text-slate-900 dark:text-white line-clamp-2">
          {resource.title}
        </h3>

        <div className="mt-3 flex items-center gap-2">
          <label className="text-xs text-slate-500 whitespace-nowrap">Folder:</label>
          <select
            value={folderId}
            onChange={handleFolderChange}
            className="flex-1 px-2 py-1 text-xs rounded border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500 truncate"
          >
            <option value="">(Smart Inbox / Root)</option>
            {allFolders.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 dark:border-slate-800 text-xs font-semibold">
        <button
          onClick={() => setActiveTab('intelligence')}
          className={`flex-1 py-2.5 text-center transition-colors border-b-2 ${
            activeTab === 'intelligence'
              ? 'border-teal-600 text-teal-600 dark:text-teal-400'
              : 'border-transparent text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
          }`}
        >
          AI Intelligence
        </button>
        <button
          onClick={() => setActiveTab('related')}
          className={`flex-1 py-2.5 text-center transition-colors border-b-2 ${
            activeTab === 'related'
              ? 'border-teal-600 text-teal-600 dark:text-teal-400'
              : 'border-transparent text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
          }`}
        >
          Connections ({relatedResources.length})
        </button>
        <button
          onClick={() => setActiveTab('overview')}
          className={`flex-1 py-2.5 text-center transition-colors border-b-2 ${
            activeTab === 'overview'
              ? 'border-teal-600 text-teal-600 dark:text-teal-400'
              : 'border-transparent text-slate-500 hover:text-slate-800 dark:hover:text-slate-200'
          }`}
        >
          Details
        </button>
      </div>

      {/* Tab Contents */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4 text-xs scrollbar-thin">
        {activeTab === 'intelligence' && (
          <div className="space-y-4">
            {loadingIntelligence ? (
              <div className="py-8 text-center text-slate-400">Loading intelligence...</div>
            ) : intelligence ? (
              <>
                {/* Academic Subject Classification */}
                <div className="p-3 rounded-xl bg-teal-50/60 dark:bg-teal-950/30 border border-teal-200 dark:border-teal-900/60">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-bold text-teal-800 dark:text-teal-300 uppercase tracking-wide">
                      Academic Discipline
                    </span>
                    <button
                      type="button"
                      onClick={handleRegenerate}
                      disabled={isRegenerating}
                      className="text-[10px] font-semibold text-teal-600 hover:underline"
                    >
                      {isRegenerating ? 'Analyzing...' : 'Re-analyze'}
                    </button>
                  </div>
                  <p className="text-xs font-bold text-slate-900 dark:text-white mt-1">
                    {intelligence.estimatedSubject || 'General Academic'}
                  </p>
                </div>

                {/* Academic Extractive Summary */}
                <div>
                  <h5 className="font-bold text-slate-800 dark:text-slate-200 mb-1 flex items-center gap-1.5">
                    <svg className="w-3.5 h-3.5 text-teal-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                    Academic Summary
                  </h5>
                  <p className="text-slate-600 dark:text-slate-300 leading-relaxed bg-slate-50 dark:bg-slate-800/60 p-3 rounded-xl border border-slate-200/60 dark:border-slate-800">
                    {intelligence.summary || 'Summary could not be extracted.'}
                  </p>
                </div>

                {/* Main Topics with Page References */}
                {intelligence.mainTopics && intelligence.mainTopics.length > 0 && (
                  <div>
                    <h5 className="font-bold text-slate-800 dark:text-slate-200 mb-1.5">
                      Key Topics & Concepts
                    </h5>
                    <div className="space-y-1.5">
                      {intelligence.mainTopics.map((topic) => {
                        const pages = intelligence.conceptPageReferences ? intelligence.conceptPageReferences[topic] : null;
                        return (
                          <div
                            key={topic}
                            className="flex items-center justify-between p-2 rounded-lg bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/60"
                          >
                            <span className="font-medium text-slate-800 dark:text-slate-200">
                              {topic}
                            </span>
                            {pages && pages.length > 0 && (
                              <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-teal-50 dark:bg-teal-950/60 text-teal-700 dark:text-teal-300">
                                p. {pages.join(', ')}
                              </span>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Key Definitions */}
                {intelligence.keyDefinitions && intelligence.keyDefinitions.length > 0 && (
                  <div>
                    <h5 className="font-bold text-slate-800 dark:text-slate-200 mb-1.5">
                      Extracted Definitions
                    </h5>
                    <div className="space-y-1.5">
                      {intelligence.keyDefinitions.map((def, idx) => (
                        <div
                          key={idx}
                          className="p-2 rounded-lg bg-slate-50 dark:bg-slate-800/40 border border-slate-200 dark:border-slate-700/60 text-slate-600 dark:text-slate-300 italic"
                        >
                          "{def}"
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Formulas & Expressions */}
                {intelligence.formulas && intelligence.formulas.length > 0 && (
                  <div>
                    <h5 className="font-bold text-slate-800 dark:text-slate-200 mb-1.5">
                      Formulas & Mathematical Constructs
                    </h5>
                    <div className="space-y-1.5">
                      {intelligence.formulas.map((form, idx) => (
                        <div
                          key={idx}
                          className="p-2 rounded-lg bg-slate-100 dark:bg-slate-800 font-mono text-[11px] text-teal-800 dark:text-teal-200"
                        >
                          {form}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </>
            ) : (
              <div className="py-8 text-center text-slate-400">
                {resource.resourceType === 'DOCUMENT'
                  ? 'No intelligence report available for this document.'
                  : 'Document intelligence is available for uploaded PDFs and DOCX materials.'}
              </div>
            )}
          </div>
        )}

        {activeTab === 'related' && (
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-slate-500 font-medium">Connected Materials</span>
              <Button type="button" variant="outline" size="sm" onClick={onOpenConnectModal} className="text-xs">
                + Connect Item
              </Button>
            </div>

            {loadingRelated ? (
              <div className="py-8 text-center text-slate-400">Loading connections...</div>
            ) : relatedResources.length === 0 ? (
              <div className="py-8 text-center text-slate-400 border border-dashed border-slate-200 dark:border-slate-800 rounded-xl p-4">
                No related materials connected yet.
                <button
                  type="button"
                  onClick={onOpenConnectModal}
                  className="block mx-auto mt-2 text-teal-600 font-semibold hover:underline"
                >
                  Link a related document or note
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                {relatedResources.map((rel) => (
                  <div
                    key={`${rel.resourceType}-${rel.id}`}
                    className="p-3 rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700/60 flex items-start justify-between gap-2"
                  >
                    <div>
                      <div className="flex items-center gap-1.5 mb-1">
                        <Badge variant="teal" size="sm">
                          {rel.resourceType}
                        </Badge>
                        <span className="font-semibold text-slate-900 dark:text-white truncate max-w-[170px]">
                          {rel.title}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-500 dark:text-slate-400 line-clamp-1">
                        {rel.folderName || 'Smart Inbox'}
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleDisconnect(rel.resourceType, rel.id)}
                      className="text-slate-400 hover:text-rose-500 p-1"
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
              <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wide block mb-1">
                Content / Notes
              </label>
              <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/60 dark:border-slate-800 max-h-48 overflow-y-auto whitespace-pre-wrap text-slate-700 dark:text-slate-300">
                {resource.content || resource.description || 'No direct text content.'}
              </div>
            </div>

            {resource.url && (
              <div>
                <label className="text-[10px] font-bold text-slate-400 uppercase tracking-wide block mb-1">
                  Source URL
                </label>
                <a
                  href={resource.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-teal-600 dark:text-teal-400 hover:underline break-all block"
                >
                  {resource.url}
                </a>
              </div>
            )}

            <div className="pt-2 border-t border-slate-100 dark:border-slate-800 space-y-1.5 text-slate-500">
              <div className="flex justify-between">
                <span>Created:</span>
                <span className="font-mono text-slate-700 dark:text-slate-300">
                  {resource.createdAt ? new Date(resource.createdAt).toLocaleDateString() : '—'}
                </span>
              </div>
              <div className="flex justify-between">
                <span>Updated:</span>
                <span className="font-mono text-slate-700 dark:text-slate-300">
                  {resource.updatedAt ? new Date(resource.updatedAt).toLocaleDateString() : '—'}
                </span>
              </div>
              {resource.fileSizeBytes > 0 && (
                <div className="flex justify-between">
                  <span>File Size:</span>
                  <span className="font-mono text-slate-700 dark:text-slate-300">
                    {resource.fileSizeFormatted}
                  </span>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Footer Actions */}
      <div className="p-4 border-t border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-900/50 flex items-center justify-between gap-2">
        <Button
          type="button"
          variant="secondary"
          size="sm"
          onClick={handleDownload}
          className="text-xs"
        >
          {resource.resourceType === 'DOCUMENT' ? 'Download' : 'Open Resource'}
        </Button>

        <div className="flex items-center gap-2">
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={() => onToggleArchive(resource)}
            className="text-xs text-slate-500"
          >
            {resource.isArchived ? 'Unarchive' : 'Archive'}
          </Button>
          <Button
            type="button"
            variant="danger"
            size="sm"
            onClick={() => onDeleteResource(resource)}
            className="text-xs"
          >
            Delete
          </Button>
        </div>
      </div>
    </aside>
  );
}

