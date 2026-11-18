import React, { useState, useEffect } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Button from '../ui/Button';

export default function MemoryPreferencesTab({
  preferences,
  isLoading,
  onSavePreferences,
  onClearAllMemories,
}) {
  const [formData, setFormData] = useState({
    enableLearning: true,
    enableTechnical: true,
    enableCareer: true,
    enableKnowledge: true,
    enableInteraction: true,
    autoExtractFromChat: true,
    autoExtractFromActivity: true,
    retentionDays: 365,
    maxMemories: 500,
  });

  const [hasChanges, setHasChanges] = useState(false);
  const [showClearConfirm, setShowClearConfirm] = useState(false);

  useEffect(() => {
    if (preferences) {
      setFormData({
        enableLearning: preferences.enableLearning !== false,
        enableTechnical: preferences.enableTechnical !== false,
        enableCareer: preferences.enableCareer !== false,
        enableKnowledge: preferences.enableKnowledge !== false,
        enableInteraction: preferences.enableInteraction !== false,
        autoExtractFromChat: preferences.autoExtractFromChat !== false,
        autoExtractFromActivity: preferences.autoExtractFromActivity !== false,
        retentionDays: preferences.retentionDays || 365,
        maxMemories: preferences.maxMemories || 500,
      });
      setHasChanges(false);
    }
  }, [preferences]);

  const handleChange = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setHasChanges(true);
  };

  const handleSave = (e) => {
    e.preventDefault();
    onSavePreferences(formData);
    setHasChanges(false);
  };

  const categoryToggles = [
    {
      field: 'enableLearning',
      title: 'Learning & Study Memory',
      desc: 'Stores learning patterns, weak topics, study goals, and revision habits.',
      tag: 'LEARNING',
      color: 'text-blue-400 bg-blue-500/10 border-blue-500/20',
    },
    {
      field: 'enableTechnical',
      title: 'Technical & Coding Memory',
      desc: 'Tracks programming languages, algorithms, preferred syntax, and debugging experience.',
      tag: 'TECHNICAL',
      color: 'text-amber-400 bg-amber-500/10 border-amber-500/20',
    },
    {
      field: 'enableCareer',
      title: 'Career & Industry Memory',
      desc: 'Remembers target roles, target companies, verified skills, and job readiness gaps.',
      tag: 'CAREER',
      color: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20',
    },
    {
      field: 'enableKnowledge',
      title: 'Knowledge Vault Memory',
      desc: 'Indexes key takeaways from your uploaded research papers, course notes, and summaries.',
      tag: 'KNOWLEDGE',
      color: 'text-purple-400 bg-purple-500/10 border-purple-500/20',
    },
    {
      field: 'enableInteraction',
      title: 'Interaction & Communication Style',
      desc: 'Adapts AI responses to your preferred depth, tone, and feedback preferences.',
      tag: 'INTERACTION',
      color: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/20',
    },
  ];

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">Memory & Privacy Controls</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Configure exactly what the AI Twin is permitted to remember about your academic profile. Disabled categories are permanently omitted from context prompts.
          </p>
        </div>

        <Button
          variant="primary"
          size="sm"
          onClick={handleSave}
          disabled={!hasChanges}
          loading={isLoading}
        >
          {hasChanges ? 'Save Changes' : 'Saved'}
        </Button>
      </div>

      {/* Category Permissions */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Category Privacy Policies</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {categoryToggles.map((item) => {
            const isChecked = !!formData[item.field];

            return (
              <div
                key={item.field}
                className="flex items-center justify-between p-3.5 rounded-xl bg-bg-surface border border-border-default transition-colors hover:border-border-default/80"
              >
                <div className="space-y-1 pr-4">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-semibold text-text-primary">{item.title}</span>
                    <span className={`text-[10px] font-bold px-2 py-0.5 rounded border uppercase tracking-wider ${item.color}`}>
                      {item.tag}
                    </span>
                  </div>
                  <p className="text-xs text-text-secondary">{item.desc}</p>
                </div>

                <label className="relative inline-flex items-center cursor-pointer shrink-0">
                  <input
                    type="checkbox"
                    checked={isChecked}
                    onChange={(e) => handleChange(item.field, e.target.checked)}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-border-default/80 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-500" />
                </label>
              </div>
            );
          })}
        </CardContent>
      </Card>

      {/* Extraction Automations */}
      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Automated Memory Consolidation</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex items-center justify-between p-3.5 rounded-xl bg-bg-surface border border-border-default">
            <div className="space-y-0.5 pr-4">
              <span className="text-sm font-semibold text-text-primary">Chat Memory Extraction</span>
              <p className="text-xs text-text-secondary">
                Automatically extract durable preferences, weak areas, and goals when you explicitly tell the AI Twin in chat.
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer shrink-0">
              <input
                type="checkbox"
                checked={formData.autoExtractFromChat}
                onChange={(e) => handleChange('autoExtractFromChat', e.target.checked)}
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-border-default/80 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-500" />
            </label>
          </div>

          <div className="flex items-center justify-between p-3.5 rounded-xl bg-bg-surface border border-border-default">
            <div className="space-y-0.5 pr-4">
              <span className="text-sm font-semibold text-text-primary">Activity & Milestone Extraction</span>
              <p className="text-xs text-text-secondary">
                Automatically synchronize new memories when you solve DSA problems, complete revision tasks, or upload new resumes.
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer shrink-0">
              <input
                type="checkbox"
                checked={formData.autoExtractFromActivity}
                onChange={(e) => handleChange('autoExtractFromActivity', e.target.checked)}
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-border-default/80 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-500" />
            </label>
          </div>
        </CardContent>
      </Card>

      {/* Security & Strict Privacy Guarantee */}
      <div className="bg-bg-surface/60 border border-primary-500/30 rounded-2xl p-4 flex items-start gap-3">
        <span className="text-primary-400 text-lg">🛡️</span>
        <div className="space-y-1 text-xs">
          <span className="font-bold text-text-primary block">Built-in Sensitive Credential Filter</span>
          <p className="text-text-secondary leading-relaxed">
            ScholarTwin enforces strict regex patterns on both client and server before storing any memory. Passwords, auth tokens, bearer credentials, and API keys are automatically blocked from persistence and prompt assembly.
          </p>
        </div>
      </div>

      {/* Danger Zone */}
      <Card className="border-rose-500/20 bg-rose-500/5">
        <CardHeader>
          <CardTitle className="text-sm text-rose-400">Danger Zone</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <span className="text-sm font-semibold text-text-primary block">Purge All Active Memories</span>
              <p className="text-xs text-text-muted mt-0.5">
                Permanently deletes every stored memory, custom preference, and extracted fact. This action is irreversible.
              </p>
            </div>

            {showClearConfirm ? (
              <div className="flex items-center gap-2">
                <Button
                  variant="danger"
                  size="sm"
                  onClick={() => {
                    onClearAllMemories();
                    setShowClearConfirm(false);
                  }}
                >
                  Yes, Purge Everything
                </Button>
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => setShowClearConfirm(false)}
                >
                  Cancel
                </Button>
              </div>
            ) : (
              <Button
                variant="danger"
                size="sm"
                onClick={() => setShowClearConfirm(true)}
              >
                Clear All Memories
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
