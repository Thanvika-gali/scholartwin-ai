import React, { useState, useEffect } from 'react';
import Button from '../ui/Button';
import { fetchRulesApi, createRuleApi, deleteRuleApi } from '../../services/api';

export default function OrganizationRulesModal({
  isOpen,
  onClose,
  folders = [],
}) {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);

  // New rule form
  const [ruleName, setRuleName] = useState('');
  const [targetFolderId, setTargetFolderId] = useState('');
  const [fieldName, setFieldName] = useState('FILENAME');
  const [operator, setOperator] = useState('CONTAINS');
  const [conditionValue, setConditionValue] = useState('');
  const [autoMove, setAutoMove] = useState(false);
  const [suggestedTags, setSuggestedTags] = useState('');
  const [priority, setPriority] = useState(1);
  const [isSaving, setIsSaving] = useState(false);

  const loadRules = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchRulesApi();
      setRules(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Failed to load organization rules.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      loadRules();
      setShowCreateForm(false);
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleCreateRule = async (e) => {
    e.preventDefault();
    if (!ruleName.trim() || !conditionValue.trim() || !targetFolderId) {
      setError('Please fill in Rule Name, Target Folder, and Condition.');
      return;
    }

    setIsSaving(true);
    setError('');
    try {
      await createRuleApi({
        ruleName: ruleName.trim(),
        targetFolderId: Number(targetFolderId),
        fieldName,
        operator,
        conditionValue: conditionValue.trim(),
        autoMove,
        suggestedTags: suggestedTags.trim(),
        priority: Number(priority),
        isEnabled: true,
      });

      // Reset form
      setRuleName('');
      setConditionValue('');
      setSuggestedTags('');
      setAutoMove(false);
      setShowCreateForm(false);
      loadRules();
    } catch (err) {
      setError(err.message || 'Failed to create rule.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteRule = async (id) => {
    try {
      await deleteRuleApi(id);
      setRules((prev) => prev.filter((r) => r.id !== id));
    } catch (err) {
      setError(err.message || 'Failed to delete rule.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div
        className="bg-white dark:bg-slate-900 rounded-2xl max-w-2xl w-full shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[85vh]"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-teal-600 text-white flex items-center justify-center">
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900 dark:text-white">
                Academic Organization Rules
              </h3>
              <p className="text-xs text-slate-500">
                Configure automated folder classification based on filename, title, or academic keywords.
              </p>
            </div>
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

          {!showCreateForm ? (
            <div>
              <div className="flex items-center justify-between mb-3">
                <span className="font-semibold text-slate-700 dark:text-slate-300">
                  Active Rules ({rules.length})
                </span>
                <Button
                  type="button"
                  variant="primary"
                  size="sm"
                  onClick={() => setShowCreateForm(true)}
                  className="text-xs bg-teal-600 hover:bg-teal-700 text-white"
                >
                  + Add New Rule
                </Button>
              </div>

              {loading ? (
                <div className="py-8 text-center text-slate-400">Loading rules...</div>
              ) : rules.length === 0 ? (
                <div className="py-8 text-center text-slate-400 border border-dashed border-slate-200 dark:border-slate-800 rounded-xl p-4">
                  No automated rules configured yet.
                  <button
                    onClick={() => setShowCreateForm(true)}
                    className="block mx-auto mt-2 text-teal-600 font-semibold hover:underline"
                  >
                    Create your first rule
                  </button>
                </div>
              ) : (
                <div className="space-y-2">
                  {rules.map((rule) => (
                    <div
                      key={rule.id}
                      className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/80 flex items-center justify-between gap-3"
                    >
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-slate-900 dark:text-white">
                            {rule.ruleName}
                          </span>
                          {rule.autoMove ? (
                            <span className="px-1.5 py-0.2 rounded text-[10px] bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300 font-semibold">
                              Auto-Move
                            </span>
                          ) : (
                            <span className="px-1.5 py-0.2 rounded text-[10px] bg-teal-100 text-teal-800 dark:bg-teal-950/60 dark:text-teal-300 font-semibold">
                              Suggest Only
                            </span>
                          )}
                          <span className="text-[10px] font-mono text-slate-400">
                            Priority: {rule.priority}
                          </span>
                        </div>

                        <p className="text-slate-500 mt-1">
                          If <strong className="text-slate-700 dark:text-slate-300">{rule.fieldName}</strong> {rule.operator.toLowerCase().replace('_', ' ')} "<strong className="text-teal-600">{rule.conditionValue}</strong>" → Move to <strong className="text-slate-700 dark:text-slate-300">{rule.targetFolderName}</strong>
                        </p>
                      </div>

                      <button
                        onClick={() => handleDeleteRule(rule.id)}
                        className="text-slate-400 hover:text-rose-500 p-1"
                        title="Delete rule"
                      >
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <form onSubmit={handleCreateRule} className="space-y-4">
              <div className="flex items-center justify-between border-b border-slate-100 dark:border-slate-800 pb-2">
                <span className="font-bold text-slate-800 dark:text-white">New Organization Rule</span>
                <button
                  type="button"
                  onClick={() => setShowCreateForm(false)}
                  className="text-xs text-slate-400 hover:text-slate-600"
                >
                  Back to list
                </button>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Rule Name *
                </label>
                <input
                  type="text"
                  value={ruleName}
                  onChange={(e) => setRuleName(e.target.value)}
                  placeholder="e.g. DBMS Homework & Lectures"
                  className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Field to Inspect *
                  </label>
                  <select
                    value={fieldName}
                    onChange={(e) => setFieldName(e.target.value)}
                    className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
                  >
                    <option value="FILENAME">Filename</option>
                    <option value="TITLE">Title / Subject</option>
                    <option value="CONTENT">Extracted Content Text</option>
                    <option value="FILE_TYPE">File Extension (pdf, docx, etc.)</option>
                  </select>
                </div>

                <div>
                  <label className="block font-semibold text-slate-700 dark:text-slate-300 mb-1">
                    Matching Operator *
                  </label>
                  <select
                    value={operator}
                    onChange={(e) => setOperator(e.target.value)}
                    className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
                  >
                    <option value="CONTAINS">Contains (Case-insensitive)</option>
                    <option value="STARTS_WITH">Starts with</option>
                    <option value="ENDS_WITH">Ends with</option>
                    <option value="EQUALS">Exact Match</option>
                    <option value="REGEX">Regular Expression</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Condition Value *
                </label>
                <input
                  type="text"
                  value={conditionValue}
                  onChange={(e) => setConditionValue(e.target.value)}
                  placeholder="e.g. dbms, sql, cs201, lecture"
                  className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
                  required
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Target Destination Folder *
                </label>
                <select
                  value={targetFolderId}
                  onChange={(e) => setTargetFolderId(e.target.value)}
                  className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
                  required
                >
                  <option value="">Select Target Folder...</option>
                  {folders.map((f) => (
                    <option key={f.id} value={f.id}>
                      {f.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 dark:text-slate-300 mb-1">
                  Suggested Tags (Optional, comma-separated)
                </label>
                <input
                  type="text"
                  value={suggestedTags}
                  onChange={(e) => setSuggestedTags(e.target.value)}
                  placeholder="e.g. database, sql, homework"
                  className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-teal-500"
                />
              </div>

              <div className="p-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/40">
                <label className="flex items-start gap-2.5 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={autoMove}
                    onChange={(e) => setAutoMove(e.target.checked)}
                    className="mt-0.5 rounded text-teal-600 focus:ring-teal-500"
                  />
                  <div>
                    <span className="font-semibold text-slate-900 dark:text-white block">
                      Auto-Move automatically on upload
                    </span>
                    <span className="text-[11px] text-slate-500 block">
                      When checked, matching materials will move directly to the target folder without waiting for manual confirmation in Smart Inbox.
                    </span>
                  </div>
                </label>
              </div>

              <div className="flex items-center justify-end gap-2 pt-2">
                <Button type="button" variant="ghost" onClick={() => setShowCreateForm(false)}>
                  Cancel
                </Button>
                <Button type="submit" variant="primary" disabled={isSaving}>
                  {isSaving ? 'Creating...' : 'Create Rule'}
                </Button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

