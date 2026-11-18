import React, { useState, useEffect } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Button from '../ui/Button';
import Input from '../ui/Input';

export default function LearningProfileTab({ profile, onUpdateProfile, isSaving }) {
  const [form, setForm] = useState({
    targetRole: '',
    learningPace: 'MODERATE',
    dailyStudyMinutes: 60,
    preferredStudyTime: 'MORNING',
    targetCompletionDate: '',
    learningGoals: '',
    revisionIntervals: '1,3,7,14,30',
    activeTopics: [],
    weakAreas: [],
    learningInterests: [],
  });

  const [newTopic, setNewTopic] = useState('');
  const [newWeak, setNewWeak] = useState('');
  const [newInterest, setNewInterest] = useState('');

  useEffect(() => {
    if (profile) {
      setForm({
        targetRole: profile.targetRole || 'Software Development Engineer',
        learningPace: profile.learningPace || 'MODERATE',
        dailyStudyMinutes: profile.dailyStudyMinutes || 60,
        preferredStudyTime: profile.preferredStudyTime || 'MORNING',
        targetCompletionDate: profile.targetCompletionDate || '',
        learningGoals: profile.learningGoals || '',
        revisionIntervals: profile.revisionIntervals || '1,3,7,14,30',
        activeTopics: profile.activeTopics || ['Data Structures & Algorithms', 'Java & Spring Boot'],
        weakAreas: profile.weakAreas || ['Dynamic Programming', 'Distributed Systems'],
        learningInterests: profile.learningInterests || ['Backend Development', 'Cloud Architecture'],
      });
    }
  }, [profile]);

  const handleAddTopic = () => {
    if (newTopic.trim() && !form.activeTopics.includes(newTopic.trim())) {
      setForm({ ...form, activeTopics: [...form.activeTopics, newTopic.trim()] });
      setNewTopic('');
    }
  };

  const handleRemoveTopic = (t) => {
    setForm({ ...form, activeTopics: form.activeTopics.filter((x) => x !== t) });
  };

  const handleAddWeak = () => {
    if (newWeak.trim() && !form.weakAreas.includes(newWeak.trim())) {
      setForm({ ...form, weakAreas: [...form.weakAreas, newWeak.trim()] });
      setNewWeak('');
    }
  };

  const handleRemoveWeak = (w) => {
    setForm({ ...form, weakAreas: form.weakAreas.filter((x) => x !== w) });
  };

  const handleAddInterest = () => {
    if (newInterest.trim() && !form.learningInterests.includes(newInterest.trim())) {
      setForm({ ...form, learningInterests: [...form.learningInterests, newInterest.trim()] });
      setNewInterest('');
    }
  };

  const handleRemoveInterest = (i) => {
    setForm({ ...form, learningInterests: form.learningInterests.filter((x) => x !== i) });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onUpdateProfile(form);
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">Learning Profile & Engine Calibration</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Calibrate your target role benchmarks, daily time budgets, pace, and memory intervals.
          </p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Engineering Role & Study Pace</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <Input
              label="Target Role Benchmark"
              value={form.targetRole}
              onChange={(e) => setForm({ ...form, targetRole: e.target.value })}
              placeholder="e.g. Software Development Engineer, Full Stack Engineer"
              required
            />

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <div>
                <label className="block text-xs font-semibold text-text-muted mb-1.5">Learning Pace</label>
                <select
                  value={form.learningPace}
                  onChange={(e) => setForm({ ...form, learningPace: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
                >
                  <option value="RELAXED">Relaxed (30 min/day)</option>
                  <option value="MODERATE">Moderate (60 min/day)</option>
                  <option value="INTENSIVE">Intensive (120+ min/day)</option>
                </select>
              </div>

              <Input
                type="number"
                label="Daily Study Time (Minutes)"
                value={form.dailyStudyMinutes}
                onChange={(e) => setForm({ ...form, dailyStudyMinutes: parseInt(e.target.value) || 60 })}
                required
              />

              <div>
                <label className="block text-xs font-semibold text-text-muted mb-1.5">Preferred Study Window</label>
                <select
                  value={form.preferredStudyTime}
                  onChange={(e) => setForm({ ...form, preferredStudyTime: e.target.value })}
                  className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
                >
                  <option value="MORNING">Morning (Focus & Concepts)</option>
                  <option value="AFTERNOON">Afternoon (Hands-on Practice)</option>
                  <option value="EVENING">Evening (Problem Solving)</option>
                  <option value="NIGHT">Night (Quiet Study)</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <Input
                type="date"
                label="Target Completion Milestone"
                value={form.targetCompletionDate}
                onChange={(e) => setForm({ ...form, targetCompletionDate: e.target.value })}
              />
              <Input
                label="Spaced Intervals (Comma-separated days)"
                value={form.revisionIntervals}
                onChange={(e) => setForm({ ...form, revisionIntervals: e.target.value })}
                placeholder="1,3,7,14,30"
              />
            </div>

            <Input
              label="Primary Learning Goals & Milestones"
              value={form.learningGoals}
              onChange={(e) => setForm({ ...form, learningGoals: e.target.value })}
              placeholder="e.g. Master system design fundamentals and complete 100 medium LeetCode questions before fall recruitment."
            />
          </CardContent>
        </Card>

        {/* Focus Tags Section */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Focus Topics & Gap Areas</CardTitle>
          </CardHeader>
          <CardContent className="space-y-5">
            {/* Active Study Topics */}
            <div className="space-y-2">
              <label className="block text-xs font-semibold text-text-muted">Active Study Topics</label>
              <div className="flex gap-2">
                <Input
                  value={newTopic}
                  onChange={(e) => setNewTopic(e.target.value)}
                  placeholder="Add active topic (e.g. Dynamic Programming, Docker)..."
                  className="flex-1"
                />
                <Button type="button" variant="outline" size="sm" onClick={handleAddTopic} className="text-xs">
                  + Add
                </Button>
              </div>
              <div className="flex flex-wrap gap-2 pt-1">
                {form.activeTopics.map((t) => (
                  <span
                    key={t}
                    className="px-2.5 py-1 rounded-lg bg-brand-teal/10 text-brand-teal text-xs font-semibold flex items-center gap-1.5"
                  >
                    {t}
                    <button type="button" onClick={() => handleRemoveTopic(t)} className="hover:text-rose-500 font-bold">
                      ×
                    </button>
                  </span>
                ))}
              </div>
            </div>

            {/* Self-Reported Weak Areas */}
            <div className="space-y-2 pt-2 border-t border-border-subtle">
              <label className="block text-xs font-semibold text-text-muted">Self-Reported Weak Areas</label>
              <div className="flex gap-2">
                <Input
                  value={newWeak}
                  onChange={(e) => setNewWeak(e.target.value)}
                  placeholder="Add weak area (e.g. Graph Traversals, Database Indexing)..."
                  className="flex-1"
                />
                <Button type="button" variant="outline" size="sm" onClick={handleAddWeak} className="text-xs">
                  + Add
                </Button>
              </div>
              <div className="flex flex-wrap gap-2 pt-1">
                {form.weakAreas.map((w) => (
                  <span
                    key={w}
                    className="px-2.5 py-1 rounded-lg bg-rose-500/10 text-rose-500 text-xs font-semibold flex items-center gap-1.5"
                  >
                    {w}
                    <button type="button" onClick={() => handleRemoveWeak(w)} className="hover:text-rose-700 font-bold">
                      ×
                    </button>
                  </span>
                ))}
              </div>
            </div>

            {/* Technical Interests */}
            <div className="space-y-2 pt-2 border-t border-border-subtle">
              <label className="block text-xs font-semibold text-text-muted">Technical Interests & Aspirations</label>
              <div className="flex gap-2">
                <Input
                  value={newInterest}
                  onChange={(e) => setNewInterest(e.target.value)}
                  placeholder="Add interest (e.g. Distributed Consensus, LLM Fine-tuning)..."
                  className="flex-1"
                />
                <Button type="button" variant="outline" size="sm" onClick={handleAddInterest} className="text-xs">
                  + Add
                </Button>
              </div>
              <div className="flex flex-wrap gap-2 pt-1">
                {form.learningInterests.map((i) => (
                  <span
                    key={i}
                    className="px-2.5 py-1 rounded-lg bg-brand-gold/15 text-brand-gold text-xs font-semibold flex items-center gap-1.5"
                  >
                    {i}
                    <button type="button" onClick={() => handleRemoveInterest(i)} className="hover:text-rose-500 font-bold">
                      ×
                    </button>
                  </span>
                ))}
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="flex justify-end">
          <Button
            type="submit"
            variant="primary"
            disabled={isSaving}
            className="bg-brand-teal text-white text-xs px-6 py-2.5 shadow-md"
          >
            {isSaving ? 'Calibrating...' : 'Save Learning Profile'}
          </Button>
        </div>
      </form>
    </div>
  );
}
