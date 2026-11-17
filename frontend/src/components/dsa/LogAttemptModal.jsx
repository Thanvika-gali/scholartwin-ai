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
    >
      <form onSubmit={handleSubmit} className="p-6 space-y-4">
        {/* Outcome Selection Buttons */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-2">
            Attempt Outcome *
          </label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setForm({ ...form, outcome: 'SUCCESS' })}
              className={`flex items-center justify-center gap-2 py-3 px-4 rounded-xl border text-sm font-semibold transition-all cursor-pointer ${
                form.outcome === 'SUCCESS'
                  ? 'bg-emerald-500 text-white border-emerald-500 shadow-md shadow-emerald-500/20'
                  : 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20 hover:bg-emerald-500/20'
              }`}
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 13l4 4L19 7" />
              </svg>
              Solved / Passed
            </button>

            <button
              type="button"
              onClick={() => setForm({ ...form, outcome: 'FAILED' })}
              className={`flex items-center justify-center gap-2 py-3 px-4 rounded-xl border text-sm font-semibold transition-all cursor-pointer ${
                form.outcome === 'FAILED'
                  ? 'bg-amber-500 text-white border-amber-500 shadow-md shadow-amber-500/20'
                  : 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20 hover:bg-amber-500/20'
              }`}
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
              </svg>
              Attempted / Struggled
            </button>
          </div>
          <p className="text-[11px] text-text-muted mt-1.5">
            {form.outcome === 'SUCCESS'
              ? 'Promotes problem status to SOLVED and increments your practice streak.'
              : 'Marks problem status as ATTEMPTED for targeted revision.'}
          </p>
        </div>

        {/* Date & Time Spent */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Date *"
            type="date"
            name="attemptDate"
            value={form.attemptDate}
            onChange={(e) => setForm({ ...form, attemptDate: e.target.value })}
            error={errors.attemptDate}
            required
          />
          <Input
            label="Time Spent (Minutes)"
            type="number"
            min="1"
            max="1440"
            name="timeSpentMinutes"
            placeholder="e.g. 30"
            value={form.timeSpentMinutes}
            onChange={(e) => setForm({ ...form, timeSpentMinutes: e.target.value })}
            error={errors.timeSpentMinutes}
          />
        </div>

        {/* Programming Language */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Programming Language
          </label>
          <select
            value={form.programmingLanguage}
            onChange={(e) => setForm({ ...form, programmingLanguage: e.target.value })}
            className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3.5 py-2.5 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
          >
            {PROGRAMMING_LANGUAGES.map((lang) => (
              <option key={lang} value={lang}>
                {lang}
              </option>
            ))}
          </select>
        </div>

        {/* Learning Notes */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Key Learning Notes / What Went Well
          </label>
          <textarea
            rows={2}
            value={form.learningNotes}
            onChange={(e) => setForm({ ...form, learningNotes: e.target.value })}
            placeholder="e.g. Discovered the sliding window condition; avoided O(N^2) brute force..."
            className="w-full bg-bg-surface-elevated border border-border-default rounded-xl p-3 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal resize-y"
          />
        </div>

        {/* Obstacles / Errors Encountered */}
        <div>
          <label className="block text-xs font-semibold text-text-secondary uppercase tracking-wider mb-1.5">
            Obstacles / Bugs Encountered (Optional)
          </label>
          <input
            type="text"
            value={form.difficultyEncountered}
            onChange={(e) => setForm({ ...form, difficultyEncountered: e.target.value })}
            placeholder="e.g. Off-by-one on loop termination, integer overflow..."
            className="w-full bg-bg-surface-elevated border border-border-default rounded-xl px-3.5 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
          />
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-3 pt-3 border-t border-border-subtle">
          <Button variant="ghost" onClick={onClose} disabled={isSaving}>
            Cancel
          </Button>
          <Button variant="primary" type="submit" isLoading={isSaving}>
            Log Attempt
          </Button>
        </div>
      </form>
    </Modal>
  );
}

