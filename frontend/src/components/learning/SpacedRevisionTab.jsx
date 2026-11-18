import React, { useState } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Modal from '../ui/Modal';

export default function SpacedRevisionTab({
  revisionQueue,
  onCreateRevisionItem,
  onCompleteRevision,
  onDeleteRevisionItem,
  activeReviewItem,
  setActiveReviewItem,
}) {
  const [activeBucket, setActiveBucket] = useState('dueToday'); // 'dueToday', 'overdue', 'upcoming', 'completed'
  const [createModalOpen, setCreateModalOpen] = useState(false);

  // New Revision Item Form
  const [itemForm, setItemForm] = useState({
    title: '',
    itemType: 'CUSTOM_TOPIC',
    customIntervalSequence: '1,3,7,14,30',
    notes: '',
  });

  // Review Modal Form
  const [reviewForm, setReviewForm] = useState({
    rating: 'MODERATE',
    reflectionNotes: '',
  });

  const handleOpenReview = (item) => {
    setActiveReviewItem(item);
    setReviewForm({
      rating: 'MODERATE',
      reflectionNotes: '',
    });
  };

  const handleCreateSubmit = (e) => {
    e.preventDefault();
    onCreateRevisionItem(itemForm);
    setCreateModalOpen(false);
    setItemForm({
      title: '',
      itemType: 'CUSTOM_TOPIC',
      customIntervalSequence: '1,3,7,14,30',
      notes: '',
    });
  };

  const handleCompleteSubmit = (rating) => {
    if (activeReviewItem) {
      onCompleteRevision(activeReviewItem.id, {
        rating: rating || reviewForm.rating,
        reflectionNotes: reviewForm.reflectionNotes,
      });
      setActiveReviewItem(null);
    }
  };

  const currentItems =
    activeBucket === 'dueToday'
      ? revisionQueue?.dueToday || []
      : activeBucket === 'overdue'
      ? revisionQueue?.overdue || []
      : activeBucket === 'upcoming'
      ? revisionQueue?.upcoming || []
      : revisionQueue?.completed || [];

  return (
    <div className="space-y-6">
      {/* Top Header & Overview */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">Spaced Revision Studio</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Ebbinghaus forgetting curve optimizer with dynamic stage expansion and self-assessment grading.
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => setCreateModalOpen(true)}
          className="bg-brand-teal text-white shadow-sm text-xs self-start sm:self-auto"
        >
          + Add Revision Target
        </Button>
      </div>

      {/* Queue Filter Tabs */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <button
          type="button"
          onClick={() => setActiveBucket('dueToday')}
          className={`p-4 rounded-2xl border text-left transition-all ${
            activeBucket === 'dueToday'
              ? 'bg-brand-gold/10 border-brand-gold ring-1 ring-brand-gold shadow-sm'
              : 'bg-bg-surface border-border-default hover:border-brand-gold/40'
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-text-muted">Due Today</span>
            <span className="w-2.5 h-2.5 rounded-full bg-brand-gold"></span>
          </div>
          <span className="text-2xl font-extrabold text-text-primary mt-1 block">
            {revisionQueue?.dueTodayCount || 0}
          </span>
          <span className="text-[11px] text-text-muted">High retention priority</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveBucket('overdue')}
          className={`p-4 rounded-2xl border text-left transition-all ${
            activeBucket === 'overdue'
              ? 'bg-rose-500/10 border-rose-500 ring-1 ring-rose-500 shadow-sm'
              : 'bg-bg-surface border-border-default hover:border-rose-500/40'
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-text-muted">Overdue</span>
            <span className="w-2.5 h-2.5 rounded-full bg-rose-500"></span>
          </div>
          <span className="text-2xl font-extrabold text-rose-500 mt-1 block">
            {revisionQueue?.overdueCount || 0}
          </span>
          <span className="text-[11px] text-text-muted">Risk of forgetting</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveBucket('upcoming')}
          className={`p-4 rounded-2xl border text-left transition-all ${
            activeBucket === 'upcoming'
              ? 'bg-brand-teal/10 border-brand-teal ring-1 ring-brand-teal shadow-sm'
              : 'bg-bg-surface border-border-default hover:border-brand-teal/40'
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-text-muted">Upcoming</span>
            <span className="w-2.5 h-2.5 rounded-full bg-brand-teal"></span>
          </div>
          <span className="text-2xl font-extrabold text-text-primary mt-1 block">
            {revisionQueue?.upcomingCount || 0}
          </span>
          <span className="text-[11px] text-text-muted">Scheduled check-ins</span>
        </button>

        <button
          type="button"
          onClick={() => setActiveBucket('completed')}
          className={`p-4 rounded-2xl border text-left transition-all ${
            activeBucket === 'completed'
              ? 'bg-emerald-500/10 border-emerald-500 ring-1 ring-emerald-500 shadow-sm'
              : 'bg-bg-surface border-border-default hover:border-emerald-500/40'
          }`}
        >
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-text-muted">Mastered / Completed</span>
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500"></span>
          </div>
          <span className="text-2xl font-extrabold text-emerald-500 mt-1 block">
            {revisionQueue?.completedCount || 0}
          </span>
          <span className="text-[11px] text-text-muted">Long-term stored</span>
        </button>
      </div>

      {/* Bucket Items Table / Cards */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-base capitalize">
            {activeBucket.replace(/([A-Z])/g, ' $1')} ({currentItems.length})
          </CardTitle>
          <span className="text-xs text-text-muted">Interval Progression: 1 → 3 → 7 → 14 → 30 days</span>
        </CardHeader>
        <CardContent>
          {currentItems.length > 0 ? (
            <div className="space-y-3">
              {currentItems.map((item) => (
                <div
                  key={item.id}
                  className="p-4 rounded-xl border border-border-default bg-bg-surface-elevated/40 flex flex-col sm:flex-row sm:items-center justify-between gap-3 hover:border-brand-gold/40 transition-all"
                >
                  <div className="min-w-0 space-y-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded uppercase bg-brand-gold/15 text-brand-gold border border-brand-gold/20">
                        Stage {item.stage}
                      </span>
                      <span className="text-xs font-bold text-text-primary">{item.title}</span>
                      <span className="text-[10px] text-text-muted uppercase px-1.5 py-0.5 rounded bg-bg-surface border border-border-subtle">
                        {item.itemType}
                      </span>
                    </div>

                    <div className="flex items-center gap-4 text-xs text-text-muted flex-wrap">
                      <span>Scheduled: <strong className="text-text-primary">{item.scheduledDate}</strong></span>
                      <span>•</span>
                      <span>Interval: <strong className="text-text-primary">{item.intervalDays} days</strong></span>
                      <span>•</span>
                      <span>Consecutive Passes: <strong className="text-brand-teal">{item.consecutiveSuccessCount}</strong></span>
                      {item.lastRating && (
                        <>
                          <span>•</span>
                          <span className="text-[11px]">Last Rating: <strong className="uppercase">{item.lastRating}</strong></span>
                        </>
                      )}
                    </div>

                    {item.notes && (
                      <p className="text-xs text-text-secondary mt-1 italic">"{item.notes}"</p>
                    )}
                  </div>

                  <div className="flex items-center gap-2 shrink-0 self-end sm:self-auto">
                    {activeBucket !== 'completed' && (
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => handleOpenReview(item)}
                        className="bg-brand-gold/15 text-brand-gold hover:bg-brand-gold hover:text-brand-navy text-xs"
                      >
                        Review & Rate
                      </Button>
                    )}
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onDeleteRevisionItem(item.id)}
                      className="text-xs text-rose-500 hover:bg-rose-500/10 p-2 h-auto"
                    >
                      ✕
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-8 text-center text-xs text-text-muted">
              No items in the {activeBucket.replace(/([A-Z])/g, ' $1')} queue.
            </div>
          )}
        </CardContent>
      </Card>

      {/* Modal: Interactive Review & Self-Assessment Rating */}
      <Modal
        isOpen={!!activeReviewItem}
        onClose={() => setActiveReviewItem(null)}
        title="Spaced Revision Self-Assessment"
        subtitle={`Evaluate your recall depth for "${activeReviewItem?.title}".`}
      >
        <div className="space-y-4">
          <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-subtle space-y-2 text-xs">
            <div className="flex items-center justify-between">
              <span className="text-text-muted">Current Stage:</span>
              <span className="font-bold text-brand-gold">Stage {activeReviewItem?.stage}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-text-muted">Current Interval:</span>
              <span className="font-bold text-text-primary">{activeReviewItem?.intervalDays} days</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-text-muted">Consecutive Recalls:</span>
              <span className="font-bold text-brand-teal">{activeReviewItem?.consecutiveSuccessCount} times</span>
            </div>
          </div>

          <Input
            label="Recall Reflection Notes"
            value={reviewForm.reflectionNotes}
            onChange={(e) => setReviewForm({ ...reviewForm, reflectionNotes: e.target.value })}
            placeholder="What key insights, tricky edge cases, or gotchas did you observe during recall?"
          />

          <div>
            <label className="block text-xs font-semibold text-text-muted mb-2">
              Select Your Honest Retention Rating:
            </label>
            <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
              <button
                type="button"
                onClick={() => handleCompleteSubmit('EASY')}
                className="p-3 rounded-xl border border-emerald-500/40 bg-emerald-500/10 hover:bg-emerald-500 hover:text-white transition-all text-center space-y-1"
              >
                <div className="text-base">🟢</div>
                <div className="text-xs font-bold">Easy</div>
                <div className="text-[10px] text-text-muted">+2.5x leap</div>
              </button>

              <button
                type="button"
                onClick={() => handleCompleteSubmit('MODERATE')}
                className="p-3 rounded-xl border border-sky-500/40 bg-sky-500/10 hover:bg-sky-500 hover:text-white transition-all text-center space-y-1"
              >
                <div className="text-base">🔵</div>
                <div className="text-xs font-bold">Moderate</div>
                <div className="text-[10px] text-text-muted">Next stage</div>
              </button>

              <button
                type="button"
                onClick={() => handleCompleteSubmit('DIFFICULT')}
                className="p-3 rounded-xl border border-amber-500/40 bg-amber-500/10 hover:bg-amber-500 hover:text-white transition-all text-center space-y-1"
              >
                <div className="text-base">🟠</div>
                <div className="text-xs font-bold">Difficult</div>
                <div className="text-[10px] text-text-muted">Shortened interval</div>
              </button>

              <button
                type="button"
                onClick={() => handleCompleteSubmit('NEED_TO_RELEARN')}
                className="p-3 rounded-xl border border-rose-500/40 bg-rose-500/10 hover:bg-rose-500 hover:text-white transition-all text-center space-y-1"
              >
                <div className="text-base">🔴</div>
                <div className="text-xs font-bold">Relearn</div>
                <div className="text-[10px] text-text-muted">Reset to 1 day</div>
              </button>
            </div>
          </div>

          <div className="flex justify-end pt-3 border-t border-border-subtle">
            <Button variant="outline" type="button" onClick={() => setActiveReviewItem(null)}>
              Cancel
            </Button>
          </div>
        </div>
      </Modal>

      {/* Modal: Create Spaced Revision Item */}
      <Modal
        isOpen={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        title="Add Spaced Revision Item"
        subtitle="Schedule automated repetition intervals for a concept, DSA problem, or knowledge topic."
      >
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          <Input
            label="Item Title"
            value={itemForm.title}
            onChange={(e) => setItemForm({ ...itemForm, title: e.target.value })}
            placeholder="e.g. Inverting Binary Trees / SQL B-Tree Indices"
            required
          />

          <div>
            <label className="block text-xs font-semibold text-text-muted mb-1.5">Revision Item Type</label>
            <select
              value={itemForm.itemType}
              onChange={(e) => setItemForm({ ...itemForm, itemType: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              <option value="CUSTOM_TOPIC">Custom Concept</option>
              <option value="DSA_PROBLEM">DSA Coding Problem</option>
              <option value="ROADMAP_TOPIC">Curriculum Topic</option>
              <option value="KNOWLEDGE_ITEM">Knowledge Hub Document / Note</option>
            </select>
          </div>

          <Input
            label="Spaced Intervals (Comma-separated days)"
            value={itemForm.customIntervalSequence}
            onChange={(e) => setItemForm({ ...itemForm, customIntervalSequence: e.target.value })}
            placeholder="1,3,7,14,30"
          />

          <Input
            label="Initial Study Notes"
            value={itemForm.notes}
            onChange={(e) => setItemForm({ ...itemForm, notes: e.target.value })}
            placeholder="Core invariants, gotchas, or formulas to remember..."
          />

          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="outline" type="button" onClick={() => setCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" className="bg-brand-teal text-white">
              Save Revision Target
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
