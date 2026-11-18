import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import { PROGRAMMING_LANGUAGES } from '../../constants/dsaConstants';
import { validateAttemptForm } from '../../utils/dsaValidation';

export default function LogAttemptModal({
  isOpen,
  onClose,
  onSave,
  problem = null,
  isSaving = false,
}) {
  const getTodayDate = () => new Date().toISOString().split('T')[0];

  const [form, setForm] = useState({
    attemptDate: getTodayDate(),
    outcome: 'SUCCESS',
    timeSpentMinutes: 30,
    programmingLanguage: 'Java',
    learningNotes: '',
    difficultyEncountered: '',
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isOpen) {
      setForm({
        attemptDate: getTodayDate(),
        outcome: 'SUCCESS',
        timeSpentMinutes: 30,
        programmingLanguage: 'Java',
        learningNotes: '',
        difficultyEncountered: '',
      });
      setErrors({});
    }
  }, [isOpen]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const validation = validateAttemptForm(form);
    if (!validation.isValid) {
      setErrors(validation.errors);
      return;
    }
    onSave({
      attemptDate: form.attemptDate,
      outcome: form.outcome,
      timeSpentMinutes: form.timeSpentMinutes ? Number(form.timeSpentMinutes) : null,
      programmingLanguage: form.programmingLanguage,
      learningNotes: form.learningNotes || null,
      difficultyEncountered: form.difficultyEncountered || null,
    });
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Log Practice Attempt"
      subtitle={problem ? `Recording effort for: ${problem.title}` : 'Record a new problem-solving attempt'}
      maxWidth="max-w-lg"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
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
            form="log-dsa-attempt-form"
            isLoading={isSaving}
            size="sm"
          >
            Save Attempt
          </Button>
        </div>
      }
    >
      <form id="log-dsa-attempt-form" onSubmit={handleSubmit} className="space-y-4">
        {/* Outcome Selection Buttons */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-2">
            Attempt Outcome *
          </label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setForm({ ...form, outcome: 'SUCCESS' })}
              className={`flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl border text-xs sm:text-sm font-semibold transition-all cursor-pointer ${
                form.outcome === 'SUCCESS'
                  ? 'bg-status-success text-white border-status-success shadow-xs'
                  : 'bg-status-success-bg text-status-success-text border-status-success-border hover:opacity-90'
              }`}
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
              </svg>
              Solved / Passed
            </button>

            <button
              type="button"
              onClick={() => setForm({ ...form, outcome: 'FAILED' })}
              className={`flex items-center justify-center gap-2 py-2.5 px-4 rounded-xl border text-xs sm:text-sm font-semibold transition-all cursor-pointer ${
                form.outcome === 'FAILED'
                  ? 'bg-status-error text-white border-status-error shadow-xs'
                  : 'bg-status-error-bg text-status-error-text border-status-error-border hover:opacity-90'
              }`}
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
              </svg>
              Unsolved / Time Limit
            </button>
          </div>
        </div>

        {/* Date and Time Spent */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <Input
            label="Attempt Date *"
            type="date"
            name="attemptDate"
            value={form.attemptDate}
            onChange={(e) => setForm({ ...form, attemptDate: e.target.value })}
            error={errors.attemptDate}
            required
          />
          <Input
            label="Time Spent (minutes)"
            type="number"
            min="1"
            max="600"
            name="timeSpentMinutes"
            placeholder="e.g. 25"
            value={form.timeSpentMinutes}
            onChange={(e) => setForm({ ...form, timeSpentMinutes: e.target.value })}
            error={errors.timeSpentMinutes}
          />
        </div>

        {/* Programming Language */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Implementation Language
          </label>
          <select
            value={form.programmingLanguage}
            onChange={(e) => setForm({ ...form, programmingLanguage: e.target.value })}
            className="w-full bg-bg-surface border border-border-default rounded-xl px-3 py-2.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
          >
            {PROGRAMMING_LANGUAGES.map((lang) => (
              <option key={lang} value={lang}>
                {lang}
              </option>
            ))}
          </select>
        </div>

        {/* Difficulty Encountered / Notes */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Key Learning Takeaway & Observations
          </label>
          <textarea
            rows={2}
            value={form.learningNotes}
            onChange={(e) => setForm({ ...form, learningNotes: e.target.value })}
            placeholder="What invariant did you discover? Any edge cases that triggered wrong answers?"
            className="w-full bg-bg-surface border border-border-default rounded-xl p-2.5 text-xs text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal resize-y"
          />
        </div>
      </form>
    </Modal>
  );
}
