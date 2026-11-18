import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import {
  DSA_TOPICS,
  DSA_PLATFORMS,
  DSA_DIFFICULTIES,
  DSA_STATUSES,
  detectPlatformFromUrl,
} from '../../constants/dsaConstants';
import { validateProblemForm } from '../../utils/dsaValidation';

export default function AddEditProblemModal({
  isOpen,
  onClose,
  onSave,
  initialData = null,
  isSaving = false,
}) {
  const isEditing = Boolean(initialData && initialData.id);

  const [form, setForm] = useState({
    title: '',
    problemUrl: '',
    platform: 'LeetCode',
    difficulty: 'MEDIUM',
    primaryTopic: 'Arrays',
    additionalTopics: [],
    status: 'NOT_STARTED',
    personalNotes: '',
    solutionApproach: '',
    timeComplexity: '',
    spaceComplexity: '',
    revisionRequired: false,
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (initialData) {
      setForm({
        title: initialData.title || '',
        problemUrl: initialData.problemUrl || '',
        platform: initialData.platform || 'LeetCode',
        difficulty: initialData.difficulty || 'MEDIUM',
        primaryTopic: initialData.primaryTopic || 'Arrays',
        additionalTopics: Array.isArray(initialData.additionalTopics)
          ? [...initialData.additionalTopics]
          : [],
        status: initialData.status || 'NOT_STARTED',
        personalNotes: initialData.personalNotes || '',
        solutionApproach: initialData.solutionApproach || '',
        timeComplexity: initialData.timeComplexity || '',
        spaceComplexity: initialData.spaceComplexity || '',
        revisionRequired: Boolean(initialData.revisionRequired),
      });
    } else {
      setForm({
        title: '',
        problemUrl: '',
        platform: 'LeetCode',
        difficulty: 'MEDIUM',
        primaryTopic: 'Arrays',
        additionalTopics: [],
        status: 'NOT_STARTED',
        personalNotes: '',
        solutionApproach: '',
        timeComplexity: '',
        spaceComplexity: '',
        revisionRequired: false,
      });
    }
    setErrors({});
  }, [initialData, isOpen]);

  const handleUrlChange = (e) => {
    const val = e.target.value;
    const detected = detectPlatformFromUrl(val);
    setForm((prev) => ({
      ...prev,
      problemUrl: val,
      platform: detected !== 'Other' || prev.platform === 'Other' ? detected : prev.platform,
    }));
  };

  const toggleAdditionalTopic = (topic) => {
    if (topic === form.primaryTopic) return;
    setForm((prev) => {
      const exists = prev.additionalTopics.includes(topic);
      return {
        ...prev,
        additionalTopics: exists
          ? prev.additionalTopics.filter((t) => t !== topic)
          : [...prev.additionalTopics, topic],
      };
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const validation = validateProblemForm(form);
    if (!validation.isValid) {
      setErrors(validation.errors);
      return;
    }
    onSave(form);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? 'Edit DSA Problem' : 'Track New DSA Problem'}
      subtitle={
        isEditing
          ? 'Update algorithmic specifications, complexity invariants, and personal notes'
          : 'Log a new coding problem with topic tags and spaced repetition tracking'
      }
      maxWidth="max-w-2xl"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
        </svg>
      }
      footer={
        <div className="flex items-center justify-end gap-3 w-full">
          <Button variant="ghost" onClick={onClose} disabled={isSaving} size="sm">
            Cancel
          </Button>
          <Button
            variant="primary"
            type="submit"
            form="add-edit-dsa-form"
            isLoading={isSaving}
            size="sm"
          >
            {isEditing ? 'Save Changes' : 'Track Problem'}
          </Button>
        </div>
      }
    >
      <form id="add-edit-dsa-form" onSubmit={handleSubmit} className="space-y-4">
        {/* Title */}
        <Input
          label="Problem Title *"
          name="title"
          placeholder="e.g. Merge Intervals, 3Sum, LRU Cache"
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
          error={errors.title}
          required
        />

        {/* URL & Platform */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <div className="sm:col-span-2">
            <Input
              label="Problem URL"
              name="problemUrl"
              placeholder="https://leetcode.com/problems/..."
              value={form.problemUrl}
              onChange={handleUrlChange}
              error={errors.problemUrl}
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
              Platform
            </label>
            <select
              value={form.platform}
              onChange={(e) => setForm({ ...form, platform: e.target.value })}
              className="w-full bg-bg-surface border border-border-default rounded-xl px-3 py-2.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
            >
              {DSA_PLATFORMS.map((p) => (
                <option key={p} value={p}>
                  {p}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Difficulty, Primary Topic, and Initial Status */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {/* Difficulty */}
          <div>
            <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
              Difficulty *
            </label>
            <select
              value={form.difficulty}
              onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
              className="w-full bg-bg-surface border border-border-default rounded-xl px-3 py-2.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal font-semibold"
            >
              {DSA_DIFFICULTIES.map((d) => (
                <option key={d.value} value={d.value}>
                  {d.label}
                </option>
              ))}
            </select>
          </div>

          {/* Primary Topic */}
          <div>
            <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
              Primary Topic *
            </label>
            <select
              value={form.primaryTopic}
              onChange={(e) => {
                const newTopic = e.target.value;
                setForm({
                  ...form,
                  primaryTopic: newTopic,
                  additionalTopics: form.additionalTopics.filter((t) => t !== newTopic),
                });
              }}
              className="w-full bg-bg-surface border border-border-default rounded-xl px-3 py-2.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
            >
              {DSA_TOPICS.map((topic) => (
                <option key={topic} value={topic}>
                  {topic}
                </option>
              ))}
            </select>
          </div>

          {/* Status */}
          <div>
            <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
              Current Status *
            </label>
            <select
              value={form.status}
              onChange={(e) => setForm({ ...form, status: e.target.value })}
              className="w-full bg-bg-surface border border-border-default rounded-xl px-3 py-2.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
            >
              {DSA_STATUSES.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Additional Topics Picker */}
        <div>
          <div className="flex items-center justify-between mb-1.5">
            <label className="text-xs font-semibold text-text-secondary uppercase tracking-wider">
              Secondary Topics / Algorithmic Patterns
            </label>
            <span className="text-[10px] text-text-muted">
              {form.additionalTopics.length} selected
            </span>
          </div>
          <div className="flex flex-wrap gap-1.5 p-2.5 rounded-xl bg-bg-surface-elevated border border-border-default max-h-28 overflow-y-auto">
            {DSA_TOPICS.map((topic) => {
              const isPrimary = topic === form.primaryTopic;
              const selected = form.additionalTopics.includes(topic);
              return (
                <button
                  key={topic}
                  type="button"
                  disabled={isPrimary}
                  onClick={() => toggleAdditionalTopic(topic)}
                  className={`text-[11px] px-2 py-0.5 rounded-lg border transition-colors cursor-pointer ${
                    isPrimary
                      ? 'bg-brand-teal/20 text-brand-teal border-brand-teal/40 font-bold opacity-75'
                      : selected
                      ? 'bg-brand-teal text-white border-brand-teal font-semibold'
                      : 'bg-bg-surface text-text-secondary border-border-default hover:border-brand-teal/40 hover:text-text-primary'
                  }`}
                >
                  {topic} {isPrimary ? '(Primary)' : selected ? '✓' : '+'}
                </button>
              );
            })}
          </div>
        </div>

        {/* Complexities */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <Input
            label="Time Complexity"
            name="timeComplexity"
            placeholder="e.g. O(N log N), O(V + E)"
            value={form.timeComplexity}
            onChange={(e) => setForm({ ...form, timeComplexity: e.target.value })}
          />
          <Input
            label="Space Complexity"
            name="spaceComplexity"
            placeholder="e.g. O(N), O(1)"
            value={form.spaceComplexity}
            onChange={(e) => setForm({ ...form, spaceComplexity: e.target.value })}
          />
        </div>

        {/* Solution Approach */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Solution Approach / Key Intuition
          </label>
          <textarea
            rows={2}
            value={form.solutionApproach}
            onChange={(e) => setForm({ ...form, solutionApproach: e.target.value })}
            placeholder="Optimal algorithm invariant, edge cases, two-pointer or sliding window condition..."
            className="w-full bg-bg-surface border border-border-default rounded-xl p-2.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal resize-y"
          />
        </div>

        {/* Personal Notes */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Personal Notes & Common Pitfalls
          </label>
          <textarea
            rows={2}
            value={form.personalNotes}
            onChange={(e) => setForm({ ...form, personalNotes: e.target.value })}
            placeholder="Tricky off-by-one errors, recursion base conditions to remember..."
            className="w-full bg-bg-surface border border-border-default rounded-xl p-2.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal resize-y"
          />
        </div>

        {/* Revision Required Checkbox */}
        <div className="flex items-center gap-3 p-3 bg-brand-gold/10 border border-brand-gold/25 rounded-xl">
          <input
            type="checkbox"
            id="revisionRequired"
            checked={form.revisionRequired}
            onChange={(e) => setForm({ ...form, revisionRequired: e.target.checked })}
            className="w-4 h-4 rounded text-brand-teal focus:ring-brand-teal border-border-default cursor-pointer"
          />
          <label htmlFor="revisionRequired" className="text-xs text-text-primary font-medium cursor-pointer">
            Queue this problem in Spaced Repetition for scheduled revision
          </label>
        </div>
      </form>
    </Modal>
  );
}
