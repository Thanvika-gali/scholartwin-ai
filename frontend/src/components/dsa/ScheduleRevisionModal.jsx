import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import { validateRevisionForm } from '../../utils/dsaValidation';

export default function ScheduleRevisionModal({
  isOpen,
  onClose,
  onSave,
  problem = null,
  isSaving = false,
}) {
  const formatDate = (daysAhead = 0) => {
    const d = new Date();
    d.setDate(d.getDate() + daysAhead);
    return d.toISOString().split('T')[0];
  };

  const [form, setForm] = useState({
    scheduledDate: formatDate(3),
    revisionNotes: '',
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isOpen) {
      setForm({
        scheduledDate: formatDate(3),
        revisionNotes: '',
      });
      setErrors({});
    }
  }, [isOpen]);

  const quickPresets = [
    { label: 'Today', days: 0 },
    { label: 'Tomorrow', days: 1 },
    { label: '+3 Days', days: 3 },
    { label: '+1 Week', days: 7 },
    { label: '+2 Weeks', days: 14 },
  ];

  const handleSubmit = (e) => {
    e.preventDefault();
    const validation = validateRevisionForm(form);
    if (!validation.isValid) {
      setErrors(validation.errors);
      return;
    }
    onSave({
      scheduledDate: form.scheduledDate,
      revisionNotes: form.revisionNotes || null,
    });
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Schedule Spaced Revision"
      subtitle={problem ? `Queueing revision for: ${problem.title}` : 'Schedule a review session'}
      maxWidth="max-w-md"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
          />
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
            form="schedule-dsa-revision-form"
            isLoading={isSaving}
            size="sm"
          >
            Queue Revision
          </Button>
        </div>
      }
    >
      <form id="schedule-dsa-revision-form" onSubmit={handleSubmit} className="space-y-4">
        {/* Quick Date Presets */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-2">
            Quick Date Presets
          </label>
          <div className="flex flex-wrap gap-2">
            {quickPresets.map((preset) => {
              const targetDate = formatDate(preset.days);
              const isSelected = form.scheduledDate === targetDate;
              return (
                <button
                  key={preset.label}
                  type="button"
                  onClick={() => setForm({ ...form, scheduledDate: targetDate })}
                  className={`text-xs px-2.5 py-1.5 rounded-lg border font-medium transition-all cursor-pointer ${
                    isSelected
                      ? 'bg-brand-teal text-white border-brand-teal shadow-xs'
                      : 'bg-bg-surface-elevated text-text-secondary border-border-default hover:text-text-primary hover:border-brand-teal/40'
                  }`}
                >
                  {preset.label}
                </button>
              );
            })}
          </div>
        </div>

        {/* Date Input */}
        <Input
          label="Target Revision Date *"
          type="date"
          name="scheduledDate"
          value={form.scheduledDate}
          onChange={(e) => setForm({ ...form, scheduledDate: e.target.value })}
          error={errors.scheduledDate}
          required
        />

        {/* Notes */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Focus Areas / Pattern Prompts
          </label>
          <textarea
            rows={2}
            value={form.revisionNotes}
            onChange={(e) => setForm({ ...form, revisionNotes: e.target.value })}
            placeholder="Focus on two-pointer optimization, recurrence relation, or space reduction..."
            className="w-full bg-bg-surface border border-border-default rounded-xl p-2.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal resize-y"
          />
        </div>
      </form>
    </Modal>
  );
}
