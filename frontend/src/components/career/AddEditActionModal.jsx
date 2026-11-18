import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Alert from '../ui/Alert';
import { createCareerAction, updateCareerAction } from '../../services/api';

const ACTION_CATEGORIES = [
  { value: 'SKILL_BUILDING', label: 'Skill Building' },
  { value: 'RESUME_IMPROVEMENT', label: 'Resume Improvement' },
  { value: 'PROJECT_CREATION', label: 'Project Creation' },
  { value: 'CERTIFICATION', label: 'Certification' },
  { value: 'DSA_PRACTICE', label: 'DSA Practice' },
  { value: 'GENERAL', label: 'General Milestone' },
];

const ACTION_PRIORITIES = [
  { value: 'HIGH', label: 'High Priority' },
  { value: 'MEDIUM', label: 'Medium Priority' },
  { value: 'LOW', label: 'Low Priority' },
];

const ACTION_STATUSES = [
  { value: 'NOT_STARTED', label: 'Not Started' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'COMPLETED', label: 'Completed' },
];

export default function AddEditActionModal({ isOpen, onClose, onSuccess, initialData = null }) {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('SKILL_BUILDING');
  const [priority, setPriority] = useState('HIGH');
  const [targetRoleOrSkill, setTargetRoleOrSkill] = useState('');
  const [dueDate, setDueDate] = useState('');
  const [status, setStatus] = useState('NOT_STARTED');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const isEdit = Boolean(initialData?.id);

  useEffect(() => {
    if (initialData) {
      setTitle(initialData.title || '');
      setDescription(initialData.description || '');
      setCategory(initialData.category || 'SKILL_BUILDING');
      setPriority(initialData.priority || 'HIGH');
      setTargetRoleOrSkill(initialData.targetRoleOrSkill || '');
      setDueDate(initialData.dueDate ? initialData.dueDate.substring(0, 10) : '');
      setStatus(initialData.status || 'NOT_STARTED');
    } else {
      setTitle('');
      setDescription('');
      setCategory('SKILL_BUILDING');
      setPriority('HIGH');
      setTargetRoleOrSkill('');
      setDueDate('');
      setStatus('NOT_STARTED');
    }
    setError(null);
  }, [initialData, isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title.trim()) {
      setError('Please provide an action title.');
      return;
    }

    setIsLoading(true);
    setError(null);

    const payload = {
      title: title.trim(),
      description: description.trim() || title.trim(),
      category,
      priority,
      targetRoleOrSkill: targetRoleOrSkill.trim() || null,
      dueDate: dueDate || null,
      ...(isEdit ? { status } : {}),
    };

    try {
      let savedAction;
      if (isEdit) {
        savedAction = await updateCareerAction(initialData.id, payload);
      } else {
        savedAction = await createCareerAction(payload);
      }
      setIsLoading(false);
      if (onSuccess) {
        onSuccess(savedAction);
      }
      onClose();
    } catch (err) {
      setIsLoading(false);
      setError(err.message || 'Failed to save career action. Please try again.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEdit ? 'Edit Action Item' : 'New Career Action Item'}
      subtitle={isEdit ? 'Update your career target item' : 'Set an actionable goal to improve readiness'}
      maxWidth="max-w-lg"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"
          />
        </svg>
      }
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" form="add-edit-action-form" variant="primary" isLoading={isLoading}>
            {isEdit ? 'Save Action' : 'Create Action'}
          </Button>
        </>
      }
    >
      <form id="add-edit-action-form" onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <Alert variant="error" dismissible onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}

        <Input
          label="Action Title"
          placeholder="e.g. Solve 10 Dynamic Programming problems on LeetCode"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
          autoFocus
          disabled={isLoading}
        />

        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
            Description (Optional)
          </label>
          <textarea
            rows={2}
            placeholder="Details, learning resources, or specific targets..."
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            disabled={isLoading}
            className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal resize-none"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
              Category
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
              disabled={isLoading}
            >
              {ACTION_CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
              Priority
            </label>
            <select
              value={priority}
              onChange={(e) => setPriority(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
              disabled={isLoading}
            >
              {ACTION_PRIORITIES.map((p) => (
                <option key={p.value} value={p.value}>
                  {p.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <Input
              label="Target Role or Skill"
              placeholder="e.g. SDE, Docker, Redis"
              value={targetRoleOrSkill}
              onChange={(e) => setTargetRoleOrSkill(e.target.value)}
              disabled={isLoading}
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
              Target Deadline
            </label>
            <input
              type="date"
              value={dueDate}
              onChange={(e) => setDueDate(e.target.value)}
              disabled={isLoading}
              className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
            />
          </div>
        </div>

        {isEdit && (
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
              Status
            </label>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
              disabled={isLoading}
            >
              {ACTION_STATUSES.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>
        )}
      </form>
    </Modal>
  );
}
