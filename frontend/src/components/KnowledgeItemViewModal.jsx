import React, { useState } from 'react';
import Modal from './ui/Modal';
import Button from './ui/Button';
import Badge from './ui/Badge';

export default function KnowledgeItemViewModal({
  isOpen,
  onClose,
  item,
  onEdit,
  onDelete,
}) {
  const [copied, setCopied] = useState(false);

  if (!isOpen || !item) return null;

  const isLink = item.type === 'LINK';

  const handleCopy = async (text) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // Fallback
    }
  };

  const formatDate = (isoString) => {
    if (!isoString) return 'N/A';
    try {
      const date = new Date(isoString);
      return date.toLocaleDateString(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return isoString;
    }
  };

  const icon = isLink ? (
    <svg className="w-5 h-5 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
      />
    </svg>
  ) : (
    <svg className="w-5 h-5 text-brand-gold" fill="none" viewBox="0 0 24 24" stroke="currentColor">
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeWidth={2}
        d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
      />
    </svg>
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={item.title}
      subtitle={
        <span className="flex items-center gap-2 mt-1">
          <Badge variant={isLink ? 'teal' : 'gold'}>
            {isLink ? 'Learning Resource' : 'Academic Note'}
          </Badge>
          {item.category && (
            <Badge variant="default">{item.category}</Badge>
          )}
        </span>
      }
      icon={icon}
    >
      <div className="p-6 space-y-6">
        {/* Link URL Banner (if LINK) */}
        {isLink && item.url && (
          <div className="p-4 rounded-xl bg-bg-surface-elevated/80 border border-border-default flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="min-w-0 flex-1">
              <span className="text-[10px] font-bold text-text-muted uppercase tracking-wider block mb-1">
                Target Resource URL
              </span>
              <p className="text-xs sm:text-sm font-mono text-brand-teal break-all">{item.url}</p>
            </div>

            <div className="flex items-center gap-2 shrink-0">
              <Button
                variant="secondary"
                size="sm"
                onClick={() => handleCopy(item.url)}
              >
                {copied ? 'Copied!' : 'Copy Link'}
              </Button>

              <a
                href={item.url}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-brand-teal hover:bg-brand-teal-hover text-white text-xs font-semibold transition-colors shadow-sm"
              >
                <span>Visit Resource</span>
                <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
                  />
                </svg>
              </a>
            </div>
          </div>
        )}

        {/* Note / Description Content */}
        <div>
          <div className="flex justify-between items-center mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider text-text-secondary">
              {isLink ? 'Resource Notes & Highlights' : 'Full Content'}
            </span>
            {!isLink && item.content && (
              <button
                type="button"
                onClick={() => handleCopy(item.content)}
                className="text-xs text-brand-teal hover:underline font-medium cursor-pointer"
              >
                {copied ? 'Copied to Clipboard!' : 'Copy Text'}
              </button>
            )}
          </div>

          {item.content ? (
            <div className="p-4 rounded-xl bg-bg-surface-elevated/50 border border-border-subtle text-text-primary text-sm whitespace-pre-wrap leading-relaxed font-sans max-h-96 overflow-y-auto">
              {item.content}
            </div>
          ) : (
            <p className="text-xs text-text-muted italic p-3">No additional notes provided.</p>
          )}
        </div>

        {/* Timestamps */}
        <div className="pt-4 border-t border-border-subtle flex flex-wrap gap-y-2 gap-x-6 text-xs text-text-muted">
          <div>
            <span>Created: </span>
            <span className="font-mono text-text-secondary">{formatDate(item.createdAt)}</span>
          </div>
          <div>
            <span>Last Modified: </span>
            <span className="font-mono text-text-secondary">{formatDate(item.updatedAt)}</span>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="pt-4 border-t border-border-subtle flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Button
              variant="secondary"
              size="sm"
              onClick={() => {
                onClose();
                onEdit(item);
              }}
              icon={
                <svg className="w-3.5 h-3.5 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                  />
                </svg>
              }
            >
              Edit
            </Button>

            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                onClose();
                onDelete(item);
              }}
              className="text-status-error hover:text-red-700 hover:bg-status-error-bg/30"
              icon={
                <svg className="w-3.5 h-3.5 text-status-error" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                  />
                </svg>
              }
            >
              Delete
            </Button>
          </div>

          <Button variant="ghost" size="sm" onClick={onClose}>
            Close
          </Button>
        </div>
      </div>
    </Modal>
  );
}
