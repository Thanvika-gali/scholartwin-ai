import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import {
  MEMORY_CATEGORIES,
  MEMORY_IMPORTANCES,
  MEMORY_TYPES,
  validateMemoryForm,
} from '../../utils/intelligenceValidation';

export default function AddEditMemoryModal({ isOpen, onClose, memory, onSave, isSubmitting }) {
  const [formData, setFormData] = useState({
    category: 'LEARNING',
    summary: '',
    content: '',
    memoryType: 'FACT',
    importance: 'MEDIUM',
    provenance: 'USER_EXPLICIT',
    sourceReference: 'Manual User Entry',
  });
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (memory) {
      setFormData({
        category: memory.category || 'LEARNING',
        summary: memory.summary || '',
        content: memory.content || '',
        memoryType: memory.memoryType || 'FACT',
        importance: memory.importance || 'MEDIUM',
        provenance: memory.provenance || 'USER_EXPLICIT',
        sourceReference: memory.sourceReference || 'User Edit',
      });
    } else {
      setFormData({
        category: 'LEARNING',
        summary: '',
        content: '',
        memoryType: 'FACT',
        importance: 'MEDIUM',
        provenance: 'USER_EXPLICIT',
        sourceReference: 'Manual User Entry',
      });
    }
    setErrors({});
  }, [memory, isOpen]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const validation = validateMemoryForm(formData);
    if (!validation.isValid) {
      setErrors(validation.errors);
      return;
    }
    onSave(formData);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={memory ? 'Edit Academic Memory' : 'Record New Memory'}
      subtitle="Preserve key facts, learning goals, or project decisions to ground future AI interactions."
      size="md"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Category & Importance */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-text-muted mb-1.5">
              Memory Category <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              {MEMORY_CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-text-muted mb-1.5">Importance Level</label>
            <select
              value={formData.importance}
              onChange={(e) => setFormData({ ...formData, importance: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              {MEMORY_IMPORTANCES.map((imp) => (
                <option key={imp} value={imp}>
                  {imp}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Memory Type */}
        <div>
          <label className="block text-xs font-semibold text-text-muted mb-1.5">Memory Type</label>
          <select
            value={formData.memoryType}
            onChange={(e) => setFormData({ ...formData, memoryType: e.target.value })}
            className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
          >
            {MEMORY_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </div>

        {/* Short Summary */}
        <Input
          label="Short Summary / Tag"
          value={formData.summary}
          onChange={(e) => setFormData({ ...formData, summary: e.target.value })}
          placeholder="e.g. Mastered Java Concurrency Barriers, Preferred Study Time"
        />

        {/* Full Content */}
        <div>
          <label className="block text-xs font-semibold text-text-muted mb-1.5">
            Memory Content <span className="text-red-500">*</span>
          </label>
          <textarea
            value={formData.content}
            onChange={(e) => setFormData({ ...formData, content: e.target.value })}
            rows={4}
            className={`w-full px-3.5 py-2.5 rounded-xl border bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal resize-none ${
              errors.content ? 'border-red-500' : 'border-border-default'
            }`}
            placeholder="Write clear, factual context. e.g. 'I am studying distributed systems with an emphasis on Paxos and Raft leader election.' (No passwords or tokens)"
          />
          {errors.content && <p className="text-[11px] text-red-500 mt-1">{errors.content}</p>}
        </div>

        {/* Source Reference */}
        <Input
          label="Source Reference"
          value={formData.sourceReference}
          onChange={(e) => setFormData({ ...formData, sourceReference: e.target.value })}
          placeholder="e.g. User Explicit Fact, Chapter 4 Notes, Mock Interview #3"
        />

        {/* Modal Actions */}
        <div className="flex justify-end gap-2.5 pt-3 border-t border-border-default">
          <Button type="button" variant="ghost" size="sm" onClick={onClose} disabled={isSubmitting}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" size="sm" loading={isSubmitting}>
            {memory ? 'Update Memory' : 'Save Memory'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
