import React, { useState } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Button from '../ui/Button';
import Badge from '../ui/Badge';
import Input from '../ui/Input';
import Modal from '../ui/Modal';
import AddEditMemoryModal from './AddEditMemoryModal';
import {
  MEMORY_CATEGORIES,
  getCategoryBadgeStyle,
  getImportanceBadgeStyle,
} from '../../utils/intelligenceValidation';

export default function MemoryCenterTab({
  memories,
  isLoading,
  onAddMemory,
  onUpdateMemory,
  onDeleteMemory,
  onArchiveMemory,
  onTogglePin,
  onExtractFromActivity,
  onConsolidate,
  onBulkDelete,
}) {
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [editingMemory, setEditingMemory] = useState(null);
  const [deleteConfirmId, setDeleteConfirmId] = useState(null);
  const [clearAllConfirmOpen, setClearAllConfirmOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Filter memories
  const filteredMemories = memories.filter((m) => {
    if (selectedCategory !== 'ALL' && m.category !== selectedCategory) {
      return false;
    }
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const contentMatch = m.content && m.content.toLowerCase().includes(q);
      const summaryMatch = m.summary && m.summary.toLowerCase().includes(q);
      const sourceMatch = m.sourceReference && m.sourceReference.toLowerCase().includes(q);
      if (!contentMatch && !summaryMatch && !sourceMatch) return false;
    }
    return true;
  });

  const handleSaveMemory = async (formData) => {
    try {
      setIsSubmitting(true);
      if (editingMemory) {
        await onUpdateMemory(editingMemory.id, formData);
      } else {
        await onAddMemory(formData);
      }
      setModalOpen(false);
      setEditingMemory(null);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deleteConfirmId) return;
    try {
      setIsSubmitting(true);
      await onDeleteMemory(deleteConfirmId);
      setDeleteConfirmId(null);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirmClearAll = async () => {
    try {
      setIsSubmitting(true);
      await onBulkDelete({ clearAll: true });
      setClearAllConfirmOpen(false);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header & Action Controls */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">AI Memory Center</h2>
          <p className="text-xs text-text-muted mt-0.5">
            View, edit, or prune the persistent contextual facts AI ScholarTwin recalls about your academic progress.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <Button
            variant="ghost"
            size="sm"
            onClick={onExtractFromActivity}
            title="Scan your latest roadmaps, tasks, and skills to refresh memory facts"
          >
            Extract From Activity
          </Button>
          <Button
            variant="ghost"
            size="sm"
            onClick={onConsolidate}
            title="Detect and remove duplicate memory records"
          >
            Consolidate
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => {
              setEditingMemory(null);
              setModalOpen(true);
            }}
          >
            + Add Memory
          </Button>
          {memories.length > 0 && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => setClearAllConfirmOpen(true)}
              className="text-red-500 hover:text-red-600 hover:border-red-500/50"
            >
              Clear All
            </Button>
          )}
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row gap-3 items-stretch sm:items-center justify-between">
        <div className="flex flex-wrap gap-1.5">
          <button
            onClick={() => setSelectedCategory('ALL')}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors ${
              selectedCategory === 'ALL'
                ? 'bg-brand-teal text-white shadow-sm'
                : 'bg-bg-surface border border-border-default text-text-secondary hover:text-text-primary'
            }`}
          >
            All Categories ({memories.length})
          </button>
          {MEMORY_CATEGORIES.map((c) => {
            const count = memories.filter((m) => m.category === c.value).length;
            return (
              <button
                key={c.value}
                onClick={() => setSelectedCategory(c.value)}
                className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition-colors flex items-center gap-1.5 ${
                  selectedCategory === c.value
                    ? 'bg-brand-teal text-white shadow-sm'
                    : 'bg-bg-surface border border-border-default text-text-secondary hover:text-text-primary'
                }`}
              >
                <span>{c.label.split(' ')[0]}</span>
                <span className="text-[10px] opacity-75 font-mono">({count})</span>
              </button>
            );
          })}
        </div>

        <div className="w-full sm:w-64">
          <Input
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search memories..."
            className="text-xs"
          />
        </div>
      </div>

      {/* Memory Cards Grid */}
      {isLoading ? (
        <div className="py-16 text-center text-xs text-text-muted">Loading persistent memory records...</div>
      ) : filteredMemories.length === 0 ? (
        <Card className="py-12 text-center space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-brand-teal/15 text-brand-teal flex items-center justify-center mx-auto text-xl font-bold">
            🧠
          </div>
          <h3 className="text-sm font-bold text-text-primary">No memories found in this view</h3>
          <p className="text-xs text-text-muted max-w-md mx-auto">
            {searchQuery
              ? `No memories matched query "${searchQuery}".`
              : 'Click "Extract From Activity" to automatically ingest your study goals, or add a custom memory statement.'}
          </p>
          <Button
            variant="primary"
            size="sm"
            onClick={() => {
              setEditingMemory(null);
              setModalOpen(true);
            }}
          >
            + Add First Memory
          </Button>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredMemories.map((m) => {
            const categoryBadgeClass = getCategoryBadgeStyle(m.category);
            const importanceBadgeClass = getImportanceBadgeStyle(m.importance);

            return (
              <Card
                key={m.id}
                className={`hover:border-brand-teal/40 transition-all ${
                  m.isPinned ? 'ring-1 ring-brand-gold/40 border-brand-gold/40' : ''
                }`}
              >
                <CardContent className="p-5 space-y-3">
                  {/* Card Header: Category + Importance + Pin */}
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded-md border ${categoryBadgeClass}`}>
                        {m.category}
                      </span>
                      <span className={`text-[10px] font-semibold px-2 py-0.5 rounded-md border ${importanceBadgeClass}`}>
                        {m.importance}
                      </span>
                      {m.provenance && (
                        <span className="text-[10px] px-1.5 py-0.5 rounded text-text-muted bg-border-default/40">
                          {m.provenance === 'USER_EXPLICIT' ? 'User Fact' : 'Inferred'}
                        </span>
                      )}
                    </div>

                    <button
                      onClick={() => onTogglePin(m.id)}
                      title={m.isPinned ? 'Unpin memory' : 'Pin as important memory'}
                      className={`text-sm p-1 rounded-lg hover:bg-border-default/40 transition-colors ${
                        m.isPinned ? 'text-brand-gold' : 'text-text-muted hover:text-brand-gold'
                      }`}
                    >
                      ★
                    </button>
                  </div>

                  {/* Summary / Title */}
                  {m.summary && (
                    <h4 className="font-bold text-sm text-text-primary leading-tight">
                      {m.summary}
                    </h4>
                  )}

                  {/* Memory Content */}
                  <p className="text-xs text-text-secondary leading-relaxed bg-bg-surface-hover/50 p-3 rounded-xl border border-border-default/60">
                    "{m.content}"
                  </p>

                  {/* Footer metadata & buttons */}
                  <div className="flex items-center justify-between text-[11px] text-text-muted pt-1">
                    <span className="truncate max-w-[180px]" title={m.sourceReference}>
                      Src: {m.sourceReference || 'Manual'}
                    </span>

                    <div className="flex items-center gap-1.5">
                      <button
                        onClick={() => onArchiveMemory(m.id)}
                        className="text-xs px-2 py-0.5 rounded text-text-muted hover:text-text-primary hover:bg-border-default/50 transition-colors"
                        title={m.status === 'ARCHIVED' ? 'Restore' : 'Archive'}
                      >
                        {m.status === 'ARCHIVED' ? 'Restore' : 'Archive'}
                      </button>
                      <button
                        onClick={() => {
                          setEditingMemory(m);
                          setModalOpen(true);
                        }}
                        className="text-xs px-2 py-0.5 rounded text-brand-teal hover:bg-brand-teal/15 transition-colors"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => setDeleteConfirmId(m.id)}
                        className="text-xs px-2 py-0.5 rounded text-red-500 hover:bg-red-500/15 transition-colors"
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {/* Add / Edit Modal */}
      <AddEditMemoryModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        memory={editingMemory}
        onSave={handleSaveMemory}
        isSubmitting={isSubmitting}
      />

      {/* Single Delete Confirmation Modal */}
      <Modal
        isOpen={Boolean(deleteConfirmId)}
        onClose={() => setDeleteConfirmId(null)}
        title="Delete Memory"
        subtitle="This action will permanently delete this memory from AI ScholarTwin and exclude it from all future AI retrieval."
        size="sm"
      >
        <div className="space-y-4 pt-2">
          <p className="text-xs text-text-muted">
            The original document or source note will NOT be deleted. Only the extracted memory fact is removed.
          </p>
          <div className="flex justify-end gap-2.5">
            <Button variant="ghost" size="sm" onClick={() => setDeleteConfirmId(null)} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button variant="danger" size="sm" onClick={handleConfirmDelete} loading={isSubmitting}>
              Confirm Delete
            </Button>
          </div>
        </div>
      </Modal>

      {/* Clear All Confirmation Modal */}
      <Modal
        isOpen={clearAllConfirmOpen}
        onClose={() => setClearAllConfirmOpen(false)}
        title="Clear All User Memories"
        subtitle="Are you sure you want to delete all stored memories across all categories?"
        size="sm"
      >
        <div className="space-y-4 pt-2">
          <p className="text-xs text-red-500 font-semibold">
            Warning: This action is permanent and cannot be undone. All persistent context used by AI Twin will be cleared.
          </p>
          <div className="flex justify-end gap-2.5">
            <Button variant="ghost" size="sm" onClick={() => setClearAllConfirmOpen(false)} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button variant="danger" size="sm" onClick={handleConfirmClearAll} loading={isSubmitting}>
              Yes, Clear All Memories
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
