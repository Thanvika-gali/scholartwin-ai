import React, { useState } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Modal from '../ui/Modal';

export default function LearningResourcesTab({
  resources,
  onAssociateResource,
  onMarkStudied,
  onDeleteResource,
}) {
  const [modalOpen, setModalOpen] = useState(false);
  const [topicFilter, setTopicFilter] = useState('');

  const [form, setForm] = useState({
    title: '',
    topic: '',
    resourceType: 'EXTERNAL_URL',
    url: '',
    notes: '',
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    onAssociateResource(form);
    setModalOpen(false);
    setForm({
      title: '',
      topic: '',
      resourceType: 'EXTERNAL_URL',
      url: '',
      notes: '',
    });
  };

  const filteredResources =
    resources && topicFilter
      ? resources.filter((r) => r.topic?.toLowerCase().includes(topicFilter.toLowerCase()))
      : resources || [];

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">Academic Resource Vault</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Attach Knowledge Hub research documents, notes, documentation links, and articles to your study topics.
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => setModalOpen(true)}
          className="bg-brand-teal text-white shadow-sm text-xs self-start sm:self-auto"
        >
          + Attach Resource
        </Button>
      </div>

      {/* Filter Row */}
      <div className="flex items-center gap-3">
        <Input
          placeholder="Filter resources by topic (e.g. Concurrency, SQL, Graphs)..."
          value={topicFilter}
          onChange={(e) => setTopicFilter(e.target.value)}
          className="max-w-md"
        />
        {topicFilter && (
          <Button variant="ghost" size="sm" onClick={() => setTopicFilter('')} className="text-xs">
            Clear
          </Button>
        )}
      </div>

      {/* Resource Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredResources.length > 0 ? (
          filteredResources.map((res) => (
            <Card
              key={res.id}
              className={`transition-all ${res.studied ? 'opacity-80 bg-bg-surface-elevated/40' : 'hover:border-brand-teal/40'}`}
            >
              <CardContent className="p-5 space-y-3">
                <div className="flex items-start justify-between gap-2">
                  <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-brand-teal/10 text-brand-teal">
                    {res.resourceType}
                  </span>
                  <button
                    type="button"
                    onClick={() => onMarkStudied(res.id, !res.studied)}
                    className={`text-[11px] font-bold px-2 py-0.5 rounded transition-colors ${
                      res.studied
                        ? 'bg-emerald-500/15 text-emerald-500 hover:bg-emerald-500/25'
                        : 'bg-bg-surface border border-border-default text-text-muted hover:border-brand-teal'
                    }`}
                  >
                    {res.studied ? '✓ Studied' : 'Mark Studied'}
                  </button>
                </div>

                <div>
                  <h4 className="text-sm font-bold text-text-primary line-clamp-2">{res.title}</h4>
                  <p className="text-xs text-brand-gold font-semibold mt-0.5">Topic: {res.topic}</p>
                </div>

                {res.notes && <p className="text-xs text-text-muted italic line-clamp-2">"{res.notes}"</p>}

                <div className="flex items-center justify-between text-xs pt-2 border-t border-border-subtle">
                  {res.url ? (
                    <a
                      href={res.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-brand-teal hover:underline text-xs flex items-center gap-1 font-semibold"
                    >
                      Open Link ↗
                    </a>
                  ) : (
                    <span className="text-text-muted text-[11px]">Local Knowledge Item</span>
                  )}
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => onDeleteResource(res.id)}
                    className="text-xs text-rose-500 hover:bg-rose-500/10 p-1.5 h-auto"
                  >
                    Remove
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))
        ) : (
          <div className="col-span-full p-8 text-center text-xs text-text-muted bg-bg-surface border border-border-default rounded-2xl">
            No learning resources associated yet.
          </div>
        )}
      </div>

      {/* Modal: Associate Resource */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Associate Learning Resource"
        subtitle="Link documents, articles, or notes directly to your active learning goals."
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Resource Title"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            placeholder="e.g. Java Concurrency in Practice Chapter 3"
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Topic"
              value={form.topic}
              onChange={(e) => setForm({ ...form, topic: e.target.value })}
              placeholder="e.g. Multi-threading"
              required
            />
            <div>
              <label className="block text-xs font-semibold text-text-muted mb-1.5">Resource Type</label>
              <select
                value={form.resourceType}
                onChange={(e) => setForm({ ...form, resourceType: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
              >
                <option value="EXTERNAL_URL">External Article / Docs URL</option>
                <option value="KNOWLEDGE_NOTE">Knowledge Hub Note</option>
                <option value="ACADEMIC_DOCUMENT">Knowledge Hub PDF / Doc</option>
                <option value="CUSTOM_NOTES">Personal Markdown Notes</option>
              </select>
            </div>
          </div>

          <Input
            label="Resource URL (Optional for online links)"
            value={form.url}
            onChange={(e) => setForm({ ...form, url: e.target.value })}
            placeholder="https://docs.oracle.com/..."
          />

          <Input
            label="Key Takeaways / Focus Notes"
            value={form.notes}
            onChange={(e) => setForm({ ...form, notes: e.target.value })}
            placeholder="Pages to read, diagrams to replicate, or key theorems..."
          />

          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="outline" type="button" onClick={() => setModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" className="bg-brand-teal text-white">
              Attach Resource
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
