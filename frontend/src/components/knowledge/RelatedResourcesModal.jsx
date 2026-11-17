import React, { useState } from 'react';
import Button from '../ui/Button';
import Badge from '../ui/Badge';
import { connectResourceApi } from '../../services/api';

export default function RelatedResourcesModal({
  isOpen,
  onClose,
  sourceResource,
  allResources = [],
  onConnected,
}) {
  const [search, setSearch] = useState('');
  const [relationType, setRelationType] = useState('RELATED');
  const [selectedTarget, setSelectedTarget] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen || !sourceResource) return null;

  const filtered = allResources.filter(
    (r) =>
      (r.id !== sourceResource.id || r.resourceType !== sourceResource.resourceType) &&
      (!search.trim() || r.title.toLowerCase().includes(search.toLowerCase()) || (r.folderName && r.folderName.toLowerCase().includes(search.toLowerCase())))
  );

  const handleConnect = async () => {
    if (!selectedTarget) {
      setError('Please select a resource to link.');
      return;
    }

    setIsSubmitting(true);
    setError('');
    try {
      await connectResourceApi(
        sourceResource.resourceType,
        sourceResource.id,
        selectedTarget.resourceType,
        selectedTarget.id,
        relationType
      );
      if (onConnected) onConnected();
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to connect resources.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div
        className="bg-white dark:bg-slate-900 rounded-2xl max-w-lg w-full shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[80vh]"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              Connect Related Material
            </h3>
            <p className="text-xs text-slate-500">
              Link related notes, textbooks, or learning links to "{sourceResource.title}".
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className="p-6 overflow-y-auto flex-1 space-y-4 text-xs scrollbar-thin">
          {error && (
            <div className="p-3 text-xs bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 rounded-lg">
              {error}
            </div>
          )}

          <div>
            <label className="block font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Relationship Type
            </label>
            <select
              value={relationType}
              onChange={(e) => setRelationType(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
            >
              <option value="RELATED">Related Concept</option>
              <option value="PREREQUISITE">Prerequisite Knowledge</option>
              <option value="SUMMARY">Revision / Summary Note</option>
              <option value="REFERENCE">Citation / Reference</option>
            </select>
          </div>

          <div>
            <label className="block font-semibold text-slate-700 dark:text-slate-300 mb-1">
              Search Material to Connect
            </label>
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by title, subject, or folder..."
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
            />
          </div>

          <div className="space-y-1.5 max-h-48 overflow-y-auto pr-1">
            {filtered.slice(0, 15).map((item) => {
              const isSelected =
                selectedTarget &&
                selectedTarget.id === item.id &&
                selectedTarget.resourceType === item.resourceType;
              return (
                <div
                  key={`${item.resourceType}-${item.id}`}
                  onClick={() => setSelectedTarget(item)}
                  className={`p-2.5 rounded-xl border cursor-pointer flex items-center justify-between transition-colors ${
                    isSelected
                      ? 'border-teal-500 bg-teal-50/40 dark:bg-teal-950/40 ring-1 ring-teal-500'
                      : 'border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/50'
                  }`}
                >
                  <div className="flex items-center gap-2 truncate">
                    <Badge variant="teal" size="sm">
                      {item.resourceType}
                    </Badge>
                    <span className="font-semibold text-slate-800 dark:text-slate-200 truncate">
                      {item.title}
                    </span>
                  </div>
                  <span className="text-[10px] text-slate-400 flex-shrink-0">
                    {item.folderName || 'Smart Inbox'}
                  </span>
                </div>
              );
            })}
          </div>

          <div className="flex justify-end gap-2 pt-2 border-t border-slate-100 dark:border-slate-800">
            <Button type="button" variant="ghost" onClick={onClose} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button
              type="button"
              variant="primary"
              disabled={isSubmitting || !selectedTarget}
              onClick={handleConnect}
            >
              {isSubmitting ? 'Connecting...' : 'Connect Material'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

