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
  const [topicInput, setTopicInput] = useState('');

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
          ? 'Update algorithmic problem specifications and personal insights'
          : 'Log a new coding problem from LeetCode, GFG, or other competitive platforms'
      }
      maxWidth="max-w-2xl"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"
          />
        </svg>
      }
    >
      <form onSubmit={handleSubmit} className="p-6 space-y-5 overflow-y-auto max-h-[75vh]">
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

        {/* Problem URL & Platform */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="sm:col-span-2">
            <Input
              label="Problem URL *"
              name="problemUrl"
              placeholder="https://leetcode.com/problems/..."
              value={form.problemUrl}
              onChange={handleUrlChange}
              error={errors.problemUrl}
              required
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
              Platform
            </label>
            <select
              value={form.platform}
              onChange={(e) => setForm({ ...form, platform: e.target.value })}
              className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3.5 py-2.5 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              {DSA_PLATFORMS.map((p) => (
                <option key={p} value={p}>
                  {p}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Difficulty & Status */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-2">
              Difficulty *
            </label>
            <div className="flex gap-2">
              {DSA_DIFFICULTIES.map((d) => {
                const isSelected = form.difficulty === d.value;
                const colors = {
                  EASY: isSelected
                    ? 'bg-emerald-500 text-white border-emerald-500'
                    : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20 hover:bg-emerald-500/20',
                  MEDIUM: isSelected
                    ? 'bg-amber-500 text-white border-amber-500'
                    : 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20 hover:bg-amber-500/20',
                  HARD: isSelected
                    ? 'bg-rose-500 text-white border-rose-500'
                    : 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20 hover:bg-rose-500/20',
                };
                return (
                  <button
                    key={d.value}
                    type="button"
                    onClick={() => setForm({ ...form, difficulty: d.value })}
                    className={`flex-1 py-2 text-xs font-semibold rounded-xl border transition-all cursor-pointer ${
                      colors[d.value]
                    }`}
                  >
                    {d.label}
                  </button>
                );
              })}
            </div>
            {errors.difficulty && (
              <p className="text-xs text-status-error-text mt-1">{errors.difficulty}</p>
            )}
          </div>

          <div>
            <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
              Current Status
            </label>
            <select
              value={form.status}
              onChange={(e) => setForm({ ...form, status: e.target.value })}
              className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3.5 py-2.5 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              {DSA_STATUSES.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>
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
            className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3.5 py-2.5 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
          >
            {DSA_TOPICS.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
          {errors.primaryTopic && (
            <p className="text-xs text-status-error-text mt-1">{errors.primaryTopic}</p>
          )}
        </div>

        {/* Additional Topics Tags */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Additional Topics / Patterns (Optional)
          </label>
          <div className="flex flex-wrap gap-1.5 p-2 bg-bg-surface-elevated/50 border border-border-default/60 rounded-xl max-h-28 overflow-y-auto">
            {DSA_TOPICS.filter((t) => t !== form.primaryTopic).map((topic) => {
              const selected = form.additionalTopics.includes(topic);
              return (
                <button
                  key={topic}
                  type="button"
                  onClick={() => toggleAdditionalTopic(topic)}
                  className={`text-xs px-2.5 py-1 rounded-lg border transition-all cursor-pointer ${
                    selected
                      ? 'bg-brand-teal text-white border-brand-teal'
                      : 'bg-bg-surface text-text-muted border-border-default hover:text-text-primary hover:border-brand-teal/40'
                  }`}
                >
                  {topic} {selected ? '✓' : '+'}
                </button>
              );
            })}
          </div>
        </div>

        {/* Complexities */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
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
            rows={3}
            value={form.solutionApproach}
            onChange={(e) => setForm({ ...form, solutionApproach: e.target.value })}
            placeholder="Explain the optimal algorithm, key invariants, and edge cases handled..."
            className="w-full bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal resize-y"
          />
        </div>

        {/* Personal Notes */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Personal Notes & Takeaways
          </label>
          <textarea
            rows={2}
            value={form.personalNotes}
            onChange={(e) => setForm({ ...form, personalNotes: e.target.value })}
            placeholder="Common mistakes to watch out for in interviews..."
            className="w-full bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal resize-y"
          />
        </div>

        {/* Revision Required Checkbox */}
        <div className="flex items-center gap-3 p-3 bg-brand-gold/10 border border-brand-gold/20 rounded-xl">
          <input
            type="checkbox"
            id="revisionRequired"
            checked={form.revisionRequired}
            onChange={(e) => setForm({ ...form, revisionRequired: e.target.checked })}
            className="w-4 h-4 rounded text-brand-teal focus:ring-brand-teal border-border-default cursor-pointer"
          />
          <label htmlFor="revisionRequired" className="text-xs text-text-primary font-medium cursor-pointer">
            Mark this problem for future revision (spaced repetition)
          </label>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-3 pt-3 border-t border-border-subtle">
          <Button variant="ghost" onClick={onClose} disabled={isSaving}>
            Cancel
          </Button>
          <Button variant="primary" type="submit" isLoading={isSaving}>
            {isEditing ? 'Save Changes' : 'Add Problem'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}

