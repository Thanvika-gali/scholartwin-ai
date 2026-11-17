import React, { useState, useEffect } from 'react';
import Modal from './ui/Modal';
import Button from './ui/Button';
import Badge from './ui/Badge';
import Alert from './ui/Alert';
import { fetchDocumentContent, fetchDocumentChunks, downloadDocumentApi, retryDocumentApi } from '../services/api';
import { formatFileSize } from '../utils/documentValidation';

export default function DocumentViewModal({
  isOpen,
  onClose,
  document,
  onDeleted,
  onUpdated,
}) {
  const [activeTab, setActiveTab] = useState('overview'); // 'overview' | 'content' | 'chunks'
  const [contentData, setContentData] = useState(null);
  const [chunks, setChunks] = useState([]);
  const [isLoadingContent, setIsLoadingContent] = useState(false);
  const [isRetrying, setIsRetrying] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);
  const [copied, setCopied] = useState(false);
  const [error, setError] = useState('');
  const [chunkSearch, setChunkSearch] = useState('');

  useEffect(() => {
    if (isOpen && document?.id) {
      setActiveTab('overview');
      setContentData(null);
      setChunks([]);
      setError('');
      loadDetails();
    }
  }, [isOpen, document?.id]);

  const loadDetails = async () => {
    if (!document?.id) return;
    setIsLoadingContent(true);
    try {
      const [content, chunksData] = await Promise.all([
        fetchDocumentContent(document.id).catch(() => null),
        fetchDocumentChunks(document.id, 0, 100).catch(() => []),
      ]);
      setContentData(content);
      setChunks(chunksData || []);
    } catch (err) {
      setError(err.message || 'Failed to load document content');
    } finally {
      setIsLoadingContent(false);
    }
  };

  const handleDownload = async () => {
    if (!document?.id || isDownloading) return;
    setIsDownloading(true);
    try {
      await downloadDocumentApi(document.id, document.originalFilename);
    } catch (err) {
      setError(err.message || 'Failed to download original document.');
    } finally {
      setIsDownloading(false);
    }
  };

  const handleRetry = async () => {
    if (!document?.id || isRetrying) return;
    setIsRetrying(true);
    setError('');
    try {
      const updated = await retryDocumentApi(document.id);
      if (onUpdated) onUpdated(updated);
      await loadDetails();
    } catch (err) {
      setError(err.message || 'Retry processing failed.');
    } finally {
      setIsRetrying(false);
    }
  };

  const handleCopyText = () => {
    if (!contentData?.fullText) return;
    navigator.clipboard.writeText(contentData.fullText);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  if (!isOpen || !document) return null;

  const isFailed = document.status === 'FAILED';
  const isCompleted = document.status === 'COMPLETED';

  const filteredChunks = chunks.filter((c) =>
    chunkSearch ? c.text.toLowerCase().includes(chunkSearch.toLowerCase()) : true
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={document.originalFilename}
      subtitle={`Uploaded on ${new Date(document.createdAt).toLocaleDateString()} at ${new Date(document.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`}
      maxWidth="max-w-4xl"
      icon={
        <div className={`w-8 h-8 rounded-lg flex items-center justify-center font-bold text-xs ${
          document.format === 'PDF'
            ? 'bg-red-500/15 text-red-600 dark:text-red-400'
            : 'bg-blue-500/15 text-blue-600 dark:text-blue-400'
        }`}>
          {document.format}
        </div>
      }
    >
      <div className="flex flex-col h-[75vh]">
        {/* Navigation Tabs */}
        <div className="px-6 pt-3 border-b border-border-subtle flex items-center justify-between gap-4 shrink-0 bg-bg-surface">
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setActiveTab('overview')}
              className={`pb-3 px-3 text-sm font-semibold border-b-2 transition-colors ${
                activeTab === 'overview'
                  ? 'border-brand-teal text-brand-teal'
                  : 'border-transparent text-text-muted hover:text-text-primary'
              }`}
            >
              Overview & Metadata
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('content')}
              className={`pb-3 px-3 text-sm font-semibold border-b-2 transition-colors ${
                activeTab === 'content'
                  ? 'border-brand-teal text-brand-teal'
                  : 'border-transparent text-text-muted hover:text-text-primary'
              }`}
            >
              Extracted Content
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('chunks')}
              className={`pb-3 px-3 text-sm font-semibold border-b-2 transition-colors flex items-center gap-1.5 ${
                activeTab === 'chunks'
                  ? 'border-brand-teal text-brand-teal'
                  : 'border-transparent text-text-muted hover:text-text-primary'
              }`}
            >
              <span>Semantic Chunks</span>
              <span className="px-1.5 py-0.5 text-xs rounded-full bg-bg-subtle text-text-secondary">
                {document.chunkCount || chunks.length}
              </span>
            </button>
          </div>

          <div className="pb-3 flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleDownload}
              isLoading={isDownloading}
              title="Download original file"
            >
              <svg className="w-4 h-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              Download
            </Button>
          </div>
        </div>

        {/* Tab Content Body */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {error && (
            <Alert variant="danger" onClose={() => setError('')}>
              {error}
            </Alert>
          )}

          {/* OVERVIEW TAB */}
          {activeTab === 'overview' && (
            <div className="space-y-6">
              {/* Status Header Banner */}
              <div className="rounded-2xl border border-border-subtle bg-bg-subtle/50 p-5 flex flex-wrap items-center justify-between gap-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2.5">
                    <span className="text-sm font-semibold text-text-primary">Extraction Status:</span>
                    <Badge
                      variant={
                        isCompleted ? 'success' : isFailed ? 'danger' : 'warning'
                      }
                      size="md"
                    >
                      {document.status}
                    </Badge>
                    {document.ocrRequired && (
                      <Badge variant="warning" size="sm">
                        OCR Required
                      </Badge>
                    )}
                    {document.ocrUsed && (
                      <Badge variant="info" size="sm">
                        OCR Applied
                      </Badge>
                    )}
                  </div>
                  {isFailed && document.errorMessage && (
                    <p className="text-xs text-red-600 dark:text-red-400 font-medium">
                      Error: {document.errorMessage}
                    </p>
                  )}
                </div>

                {isFailed && (
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={handleRetry}
                    isLoading={isRetrying}
                  >
                    Retry Extraction
                  </Button>
                )}
              </div>

              {/* Metrics Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
                <div className="p-4 rounded-xl border border-border-subtle bg-bg-surface shadow-xs">
                  <p className="text-xs font-medium text-text-muted uppercase tracking-wider">File Size</p>
                  <p className="text-lg font-bold text-text-primary mt-1">
                    {formatFileSize(document.fileSize)}
                  </p>
                </div>

                <div className="p-4 rounded-xl border border-border-subtle bg-bg-surface shadow-xs">
                  <p className="text-xs font-medium text-text-muted uppercase tracking-wider">Pages</p>
                  <p className="text-lg font-bold text-text-primary mt-1">
                    {document.pageCount || 0}
                  </p>
                </div>

                <div className="p-4 rounded-xl border border-border-subtle bg-bg-surface shadow-xs">
                  <p className="text-xs font-medium text-text-muted uppercase tracking-wider">Characters</p>
                  <p className="text-lg font-bold text-text-primary mt-1">
                    {document.characterCount ? document.characterCount.toLocaleString() : 0}
                  </p>
                </div>

                <div className="p-4 rounded-xl border border-border-subtle bg-bg-surface shadow-xs">
                  <p className="text-xs font-medium text-text-muted uppercase tracking-wider">Chunks</p>
                  <p className="text-lg font-bold text-brand-teal mt-1">
                    {document.chunkCount || 0}
                  </p>
                </div>
              </div>

              {/* Technical Details List */}
              <div className="rounded-xl border border-border-subtle divide-y divide-border-subtle bg-bg-surface">
                <div className="p-4 flex items-center justify-between text-sm">
                  <span className="text-text-muted">Original Filename</span>
                  <span className="font-medium text-text-primary truncate max-w-xs">{document.originalFilename}</span>
                </div>
                <div className="p-4 flex items-center justify-between text-sm">
                  <span className="text-text-muted">Document Format</span>
                  <span className="font-medium text-text-primary">{document.format}</span>
                </div>
                <div className="p-4 flex items-center justify-between text-sm">
                  <span className="text-text-muted">MIME Content Type</span>
                  <span className="font-mono text-xs text-text-secondary">{document.contentType}</span>
                </div>
                <div className="p-4 flex items-center justify-between text-sm">
                  <span className="text-text-muted">Processing Time</span>
                  <span className="text-xs text-text-secondary">
                    {document.completedAt
                      ? `${new Date(document.completedAt).toLocaleTimeString()}`
                      : 'Pending'}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* EXTRACTED TEXT TAB */}
          {activeTab === 'content' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between gap-4">
                <p className="text-xs text-text-muted">
                  Full normalized text extracted via Apache PDFBox / POI pipeline.
                </p>
                {contentData?.fullText && (
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={handleCopyText}
                  >
                    {copied ? 'Copied!' : 'Copy All Text'}
                  </Button>
                )}
              </div>

              {isLoadingContent ? (
                <div className="p-12 text-center text-sm text-text-muted animate-pulse">
                  Loading extracted content...
                </div>
              ) : contentData?.fullText ? (
                <div className="p-4 rounded-xl border border-border-subtle bg-bg-subtle/40 font-mono text-xs text-text-primary whitespace-pre-wrap leading-relaxed max-h-[50vh] overflow-y-auto select-text">
                  {contentData.fullText}
                </div>
              ) : (
                <div className="p-8 text-center text-sm text-text-muted">
                  No text content available for this document.
                </div>
              )}
            </div>
          )}

          {/* CHUNKS TAB */}
          {activeTab === 'chunks' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between gap-4">
                <input
                  type="text"
                  placeholder="Filter chunks by keyword..."
                  value={chunkSearch}
                  onChange={(e) => setChunkSearch(e.target.value)}
                  className="px-3 py-1.5 text-xs rounded-lg border border-border-default bg-bg-surface text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal w-full max-w-xs"
                />
                <span className="text-xs text-text-muted shrink-0">
                  Showing {filteredChunks.length} of {chunks.length} chunks
                </span>
              </div>

              {isLoadingContent ? (
                <div className="p-12 text-center text-sm text-text-muted animate-pulse">
                  Loading chunks...
                </div>
              ) : filteredChunks.length > 0 ? (
                <div className="space-y-3 max-h-[50vh] overflow-y-auto pr-1">
                  {filteredChunks.map((chunk) => (
                    <div
                      key={chunk.id || chunk.chunkIndex}
                      className="p-4 rounded-xl border border-border-subtle bg-bg-surface hover:border-brand-teal/40 transition-colors shadow-xs space-y-2"
                    >
                      <div className="flex items-center justify-between text-xs">
                        <span className="font-bold text-brand-teal">
                          Chunk #{chunk.chunkIndex}
                        </span>
                        <div className="flex items-center gap-2 text-text-muted">
                          <span>
                            {chunk.startPage === chunk.endPage
                              ? `Page ${chunk.startPage}`
                              : `Pages ${chunk.startPage}–${chunk.endPage}`}
                          </span>
                          <span>•</span>
                          <span>{chunk.characterCount} chars</span>
                        </div>
                      </div>
                      <p className="text-xs text-text-secondary leading-relaxed whitespace-pre-wrap select-text">
                        {chunk.text}
                      </p>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="p-8 text-center text-sm text-text-muted">
                  No chunks matched your search.
                </div>
              )}
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-4 border-t border-border-subtle flex items-center justify-between gap-3 shrink-0 bg-bg-surface">
          <Button
            variant="danger"
            size="sm"
            onClick={() => onDeleted(document)}
          >
            Delete Document
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={onClose}
          >
            Close
          </Button>
        </div>
      </div>
    </Modal>
  );
}

