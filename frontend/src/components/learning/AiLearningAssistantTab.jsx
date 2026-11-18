import React, { useState } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import Input from '../ui/Input';

export default function AiLearningAssistantTab({
  profile,
  onConsultAssistant,
  isLoading,
  assistantResponse,
  onNavigateTab,
}) {
  const [query, setQuery] = useState('');
  const [topic, setTopic] = useState('Data Structures & Algorithms');
  const [contextType, setContextType] = useState('CONCEPT_EXPLANATION');

  const presets = [
    { label: 'Explain Core Principle', type: 'CONCEPT_EXPLANATION', prompt: 'Explain the core principles, invariant guarantees, and time/space complexity of' },
    { label: 'Code Implementation', type: 'EXAMPLE_GENERATION', prompt: 'Provide a production-quality, clean implementation demonstrating best practices for' },
    { label: 'Mock Interview Questions', type: 'PRACTICE_QUESTIONS', prompt: 'Give me 3 diagnostic interview questions testing edge cases and tradeoffs in' },
    { label: 'Revision Strategy', type: 'REVISION_ADVICE', prompt: 'What is the optimal active-recall strategy to retain and master' },
  ];

  const handlePresetClick = (p) => {
    setContextType(p.type);
    setQuery(`${p.prompt} ${topic}`);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!query.trim()) return;
    onConsultAssistant({
      query: query.trim(),
      topic: topic.trim(),
      contextType,
    });
  };

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">AI Academic Learning Assistant</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Personalized tutor grounded in your target role, study history, Knowledge Hub notes, and assessment diagnostics.
          </p>
        </div>
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <span className="w-2 h-2 rounded-full bg-brand-teal animate-pulse"></span>
          <span className="text-xs font-semibold text-brand-teal">
            Grounded in: {profile?.targetRole || 'Software Engineer'}
          </span>
        </div>
      </div>

      {/* Query Form */}
      <Card>
        <CardContent className="p-5 sm:p-6 space-y-4">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                label="Topic of Interest"
                value={topic}
                onChange={(e) => setTopic(e.target.value)}
                placeholder="e.g. Dynamic Programming, Java Concurrency, Redis"
                required
              />

              <div>
                <label className="block text-xs font-semibold text-text-muted mb-1.5">Context Goal</label>
                <select
                  value={contextType}
                  onChange={(e) => setContextType(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
                >
                  <option value="CONCEPT_EXPLANATION">Conceptual Explanation & Tradeoffs</option>
                  <option value="EXAMPLE_GENERATION">Code Example & Architectural Pattern</option>
                  <option value="PRACTICE_QUESTIONS">Diagnostic Practice Questions</option>
                  <option value="REVISION_ADVICE">Active Recall & Spaced Repetition Advice</option>
                  <option value="ASSESSMENT_REVIEW">Assessment Error Remediation</option>
                  <option value="GENERAL_QUERY">General Technical Inquiry</option>
                </select>
              </div>
            </div>

            {/* Quick Presets */}
            <div className="flex gap-2 flex-wrap items-center">
              <span className="text-[11px] font-semibold text-text-muted">Prompt Starters:</span>
              {presets.map((p, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => handlePresetClick(p)}
                  className="text-[11px] px-2.5 py-1 rounded-lg border border-border-default bg-bg-surface hover:border-brand-teal text-text-secondary hover:text-text-primary transition-colors"
                >
                  {p.label}
                </button>
              ))}
            </div>

            <div>
              <label className="block text-xs font-semibold text-text-muted mb-1.5">
                Your Academic Question or Prompt
              </label>
              <textarea
                rows={4}
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Ask ScholarTwin to explain a concept, critique your understanding, clarify edge cases, or diagnose why a particular approach fails..."
                className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
                required
              />
            </div>

            <div className="flex justify-end">
              <Button
                type="submit"
                variant="primary"
                disabled={isLoading}
                className="bg-brand-teal text-white text-xs px-6 py-2.5 shadow-md"
              >
                {isLoading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
                    Synthesizing Guidance...
                  </span>
                ) : (
                  'Consult AI Mentor'
                )}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {/* Assistant Response Display */}
      {assistantResponse && (
        <Card className="border-brand-teal/30 shadow-md">
          <CardHeader className="flex flex-row items-center justify-between pb-3">
            <div className="flex items-center gap-2">
              <span className="p-1.5 rounded-lg bg-brand-teal/15 text-brand-teal text-xs">✨</span>
              <div>
                <CardTitle className="text-base">ScholarTwin Guidance: {assistantResponse.topic}</CardTitle>
                <p className="text-[11px] text-text-muted mt-0.5">
                  Mode: <strong className="text-brand-teal font-semibold">{assistantResponse.sourceGroundingMode}</strong>
                </p>
              </div>
            </div>
            <Badge variant="primary" size="sm">
              {assistantResponse.sourceGroundingMode === 'GEMINI_AI' ? 'Live LLM' : 'Academic Engine'}
            </Badge>
          </CardHeader>
          <CardContent className="space-y-5">
            {/* Main Response Text */}
            <div className="prose prose-sm dark:prose-invert max-w-none text-xs sm:text-sm text-text-primary leading-relaxed whitespace-pre-wrap bg-bg-surface-elevated/40 p-4 rounded-xl border border-border-subtle">
              {assistantResponse.responseText}
            </div>

            {/* Key Takeaways */}
            {assistantResponse.keyTakeaways && assistantResponse.keyTakeaways.length > 0 && (
              <div className="space-y-2">
                <h4 className="text-xs font-bold uppercase tracking-wider text-text-primary">Key Takeaways</h4>
                <div className="space-y-1.5">
                  {assistantResponse.keyTakeaways.map((k, idx) => (
                    <div key={idx} className="flex items-start gap-2 text-xs text-text-secondary">
                      <span className="text-brand-teal font-bold">•</span>
                      <span>{k}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Practical Exercises */}
            {assistantResponse.practicalExercises && assistantResponse.practicalExercises.length > 0 && (
              <div className="space-y-2 pt-2 border-t border-border-subtle">
                <h4 className="text-xs font-bold uppercase tracking-wider text-text-primary">Recommended Exercises</h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                  {assistantResponse.practicalExercises.map((ex, idx) => (
                    <div
                      key={idx}
                      className="p-3 rounded-xl border border-brand-teal/20 bg-brand-teal/5 text-xs text-text-secondary"
                    >
                      <strong className="text-brand-teal font-semibold block mb-0.5">Exercise {idx + 1}:</strong>
                      {ex}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Suggested Next Topic */}
            {assistantResponse.suggestedNextTopic && (
              <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-subtle flex items-center justify-between gap-3 text-xs">
                <div>
                  <span className="text-[11px] text-text-muted block">Suggested Next Topic:</span>
                  <span className="font-bold text-text-primary">{assistantResponse.suggestedNextTopic}</span>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => onNavigateTab('assessments')}
                  className="text-xs text-brand-teal"
                >
                  Generate Quiz on Topic →
                </Button>
              </div>
            )}

            {/* Transparency Note */}
            {assistantResponse.limitationsNote && (
              <p className="text-[11px] text-text-muted italic border-t border-border-subtle pt-2">
                ℹ️ {assistantResponse.limitationsNote}
              </p>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
