import React, { useState } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Modal from '../ui/Modal';

export default function LearningRoadmapsTab({
  roadmaps,
  selectedRoadmap,
  onSelectRoadmap,
  onCreateRoadmap,
  onUpdateTaskStatus,
  onAddTaskToPhase,
  onDeleteRoadmap,
  onDeleteTask,
  onOpenResourceModal,
}) {
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [addTaskModalOpen, setAddTaskModalOpen] = useState(false);
  const [targetPhaseId, setTargetPhaseId] = useState(null);

  // New Roadmap Form
  const [roadmapForm, setRoadmapForm] = useState({
    title: '',
    description: '',
    targetRole: 'Software Development Engineer',
    goalType: 'ROLE',
    targetSkillOrTopic: '',
    targetCompletionDate: '',
    pace: 'MODERATE',
  });

  // Add Task Form
  const [taskForm, setTaskForm] = useState({
    title: '',
    topic: '',
    subtopics: '',
    estimatedMinutes: 60,
    learningObjectives: '',
    practicalExercises: '',
    completionCriteria: '',
    miniProject: '',
  });

  const handleOpenAddTask = (phaseId) => {
    setTargetPhaseId(phaseId);
    setTaskForm({
      title: '',
      topic: '',
      subtopics: '',
      estimatedMinutes: 60,
      learningObjectives: '',
      practicalExercises: '',
      completionCriteria: '',
      miniProject: '',
    });
    setAddTaskModalOpen(true);
  };

  const handleCreateSubmit = (e) => {
    e.preventDefault();
    onCreateRoadmap(roadmapForm);
    setCreateModalOpen(false);
  };

  const handleAddTaskSubmit = (e) => {
    e.preventDefault();
    if (targetPhaseId) {
      onAddTaskToPhase(targetPhaseId, taskForm);
      setAddTaskModalOpen(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header & Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div>
          <h2 className="text-lg font-bold text-text-primary tracking-tight">Curriculum & Learning Roadmaps</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Structured multi-phase curricula generated for high-tier engineering benchmarks and personal skill mastery.
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => setCreateModalOpen(true)}
          className="bg-brand-teal text-white shadow-sm text-xs self-start sm:self-auto"
        >
          <svg className="w-4 h-4 mr-1.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Generate New Roadmap
        </Button>
      </div>

      {/* Main Grid: Left List / Right Active Roadmap View */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Column: Roadmap Cards */}
        <div className="lg:col-span-4 space-y-3">
          <h3 className="text-xs font-bold uppercase tracking-wider text-text-muted px-1">Your Curricula</h3>
          {roadmaps && roadmaps.length > 0 ? (
            roadmaps.map((r) => {
              const isSelected = selectedRoadmap?.id === r.id;
              return (
                <div
                  key={r.id}
                  onClick={() => onSelectRoadmap(r.id)}
                  className={`p-4 rounded-2xl border cursor-pointer transition-all ${
                    isSelected
                      ? 'bg-brand-teal/5 border-brand-teal ring-1 ring-brand-teal shadow-md'
                      : 'bg-bg-surface border-border-default hover:border-brand-teal/40 hover:bg-bg-surface-elevated/40'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded bg-brand-navy/10 dark:bg-white/10 text-text-primary">
                      {r.goalType}
                    </span>
                    <Badge variant={r.status === 'COMPLETED' ? 'success' : 'primary'} size="sm">
                      {r.status}
                    </Badge>
                  </div>

                  <h4 className="text-sm font-bold text-text-primary mt-2">{r.title}</h4>
                  <p className="text-xs text-text-muted line-clamp-2 mt-1">{r.description}</p>

                  <div className="space-y-1 mt-3">
                    <div className="flex justify-between text-[11px] text-text-muted">
                      <span>Curriculum Progress</span>
                      <span className="font-semibold text-text-primary">{r.completionPercentage}%</span>
                    </div>
                    <div className="w-full bg-bg-surface-elevated rounded-full h-1.5 overflow-hidden">
                      <div
                        className="bg-brand-teal h-1.5 rounded-full transition-all"
                        style={{ width: `${r.completionPercentage}%` }}
                      ></div>
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-[11px] text-text-muted mt-3 pt-2 border-t border-border-subtle">
                    <span>{r.phases?.length || 0} Phases</span>
                    <span>{r.completedHours || 0} / {r.estimatedHours || 0} hrs</span>
                  </div>
                </div>
              );
            })
          ) : (
            <div className="p-8 text-center text-xs text-text-muted bg-bg-surface border border-border-default rounded-2xl">
              No roadmaps created yet.
            </div>
          )}
        </div>

        {/* Right Column: Selected Roadmap Deep Dive */}
        <div className="lg:col-span-8">
          {selectedRoadmap ? (
            <div className="space-y-5">
              {/* Roadmap Header Card */}
              <Card>
                <CardContent className="p-5 sm:p-6 space-y-4">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="px-2 py-0.5 rounded text-[10px] font-bold tracking-wider uppercase bg-brand-teal/15 text-brand-teal">
                          {selectedRoadmap.goalType}
                        </span>
                        <span className="text-xs text-text-muted font-medium">Target: {selectedRoadmap.targetRole}</span>
                      </div>
                      <h2 className="text-xl sm:text-2xl font-extrabold text-text-primary tracking-tight">
                        {selectedRoadmap.title}
                      </h2>
                    </div>
                    <div className="flex items-center gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => onDeleteRoadmap(selectedRoadmap.id)}
                        className="text-xs text-rose-500 hover:bg-rose-500/10 border-rose-500/30"
                      >
                        Delete Roadmap
                      </Button>
                    </div>
                  </div>

                  <p className="text-xs sm:text-sm text-text-secondary leading-relaxed">
                    {selectedRoadmap.description}
                  </p>

                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-3 border-t border-border-subtle text-xs">
                    <div>
                      <span className="text-text-muted block text-[11px]">Completion %</span>
                      <span className="font-bold text-text-primary text-sm">{selectedRoadmap.completionPercentage}%</span>
                    </div>
                    <div>
                      <span className="text-text-muted block text-[11px]">Estimated Time</span>
                      <span className="font-bold text-text-primary text-sm">{selectedRoadmap.estimatedHours} hrs</span>
                    </div>
                    <div>
                      <span className="text-text-muted block text-[11px]">Completed Time</span>
                      <span className="font-bold text-text-primary text-sm">{selectedRoadmap.completedHours} hrs</span>
                    </div>
                    <div>
                      <span className="text-text-muted block text-[11px]">Target Date</span>
                      <span className="font-bold text-text-primary text-sm">{selectedRoadmap.targetCompletionDate || 'Open'}</span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Phases Accordion */}
              <div className="space-y-4">
                {selectedRoadmap.phases && selectedRoadmap.phases.map((phase) => (
                  <div key={phase.id} className="bg-bg-surface border border-border-default rounded-2xl overflow-hidden shadow-sm">
                    {/* Phase Header */}
                    <div className="p-4 sm:p-5 bg-bg-surface-elevated/40 border-b border-border-subtle flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="w-5 h-5 rounded-full bg-brand-teal text-white text-[11px] font-bold flex items-center justify-center">
                            {phase.phaseNumber}
                          </span>
                          <h4 className="text-sm sm:text-base font-bold text-text-primary">{phase.title}</h4>
                        </div>
                        <p className="text-xs text-text-muted mt-1">{phase.description}</p>
                      </div>
                      <div className="flex items-center gap-2 self-start sm:self-center">
                        <span className="text-[11px] font-medium text-text-muted">
                          {phase.tasks?.filter((t) => t.status === 'COMPLETED').length || 0} / {phase.tasks?.length || 0} tasks
                        </span>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleOpenAddTask(phase.id)}
                          className="text-xs text-brand-teal"
                        >
                          + Add Task
                        </Button>
                      </div>
                    </div>

                    {/* Phase Tasks List */}
                    <div className="p-4 sm:p-5 divide-y divide-border-subtle">
                      {phase.tasks && phase.tasks.length > 0 ? (
                        phase.tasks.map((task) => (
                          <div key={task.id} className="py-3 first:pt-0 last:pb-0 space-y-2">
                            <div className="flex items-start justify-between gap-3">
                              <div className="flex items-start gap-3 min-w-0">
                                <button
                                  type="button"
                                  onClick={() =>
                                    onUpdateTaskStatus(
                                      task.id,
                                      task.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED',
                                      task.estimatedMinutes
                                    )
                                  }
                                  className={`w-5 h-5 mt-0.5 rounded border flex items-center justify-center transition-colors shrink-0 ${
                                    task.status === 'COMPLETED'
                                      ? 'bg-emerald-500 border-emerald-500 text-white'
                                      : 'border-border-default hover:border-brand-teal'
                                  }`}
                                >
                                  {task.status === 'COMPLETED' && (
                                    <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                                    </svg>
                                  )}
                                </button>
                                <div className="min-w-0">
                                  <div className="flex items-center gap-2 flex-wrap">
                                    <span className={`text-xs font-bold ${task.status === 'COMPLETED' ? 'line-through text-text-muted' : 'text-text-primary'}`}>
                                      {task.title}
                                    </span>
                                    {task.subtopics && (
                                      <span className="text-[10px] px-1.5 py-0.5 rounded bg-bg-surface-elevated text-text-muted">
                                        {task.subtopics}
                                      </span>
                                    )}
                                  </div>
                                  {task.learningObjectives && (
                                    <p className="text-[11px] text-text-muted mt-1 leading-normal">
                                      🎯 {task.learningObjectives}
                                    </p>
                                  )}
                                  {task.practicalExercises && (
                                    <p className="text-[11px] text-brand-teal mt-0.5 font-medium">
                                      💻 Exercise: {task.practicalExercises}
                                    </p>
                                  )}
                                  {task.miniProject && (
                                    <p className="text-[11px] text-brand-gold mt-0.5 font-semibold">
                                      🚀 Project: {task.miniProject}
                                    </p>
                                  )}
                                </div>
                              </div>

                              <div className="flex items-center gap-2 shrink-0">
                                <span className="text-[11px] text-text-muted">
                                  {task.actualMinutes > 0 ? `${task.actualMinutes}m` : `${task.estimatedMinutes}m`}
                                </span>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => onOpenResourceModal(task.topic, task.id)}
                                  className="text-[11px] text-brand-teal p-1 h-auto"
                                  title="Attach Resources"
                                >
                                  📎
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="sm"
                                  onClick={() => onDeleteTask(task.id)}
                                  className="text-[11px] text-rose-500 p-1 h-auto"
                                  title="Delete Task"
                                >
                                  ✕
                                </Button>
                              </div>
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className="py-4 text-center text-xs text-text-muted">
                          No tasks in this phase yet. Click "+ Add Task" to populate.
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="p-12 text-center text-text-muted bg-bg-surface border border-border-default rounded-2xl">
              <p className="text-sm">Select a roadmap from the left panel to explore its phases and tasks.</p>
            </div>
          )}
        </div>
      </div>

      {/* Modal: Create Roadmap */}
      <Modal
        isOpen={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        title="Generate Personalized Roadmap"
        subtitle="Generates structured phases, subtopics, practical exercises, and mini-projects tailored to your target role."
      >
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          <Input
            label="Roadmap Title"
            value={roadmapForm.title}
            onChange={(e) => setRoadmapForm({ ...roadmapForm, title: e.target.value })}
            placeholder="e.g. Tier-1 SDE Core Preparation"
            required
          />

          <div>
            <label className="block text-xs font-semibold text-text-muted mb-1.5">Target Engineering Role</label>
            <select
              value={roadmapForm.targetRole}
              onChange={(e) => setRoadmapForm({ ...roadmapForm, targetRole: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              <option value="Software Development Engineer">Software Development Engineer (SDE)</option>
              <option value="Full Stack Engineer">Full Stack Engineer</option>
              <option value="Data Structures & Algorithms">Data Structures & Algorithms Mastery</option>
              <option value="AI / Machine Learning Engineer">AI / Machine Learning Engineer</option>
              <option value="Custom Technical Specialization">Custom Technical Specialization</option>
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-text-muted mb-1.5">Learning Pace</label>
              <select
                value={roadmapForm.pace}
                onChange={(e) => setRoadmapForm({ ...roadmapForm, pace: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
              >
                <option value="RELAXED">Relaxed (30 mins/day)</option>
                <option value="MODERATE">Moderate (60 mins/day)</option>
                <option value="INTENSIVE">Intensive (120+ mins/day)</option>
              </select>
            </div>
            <Input
              type="date"
              label="Target Completion Date"
              value={roadmapForm.targetCompletionDate}
              onChange={(e) => setRoadmapForm({ ...roadmapForm, targetCompletionDate: e.target.value })}
            />
          </div>

          <Input
            label="Roadmap Description & Objectives"
            value={roadmapForm.description}
            onChange={(e) => setRoadmapForm({ ...roadmapForm, description: e.target.value })}
            placeholder="Describe your primary targets, focus areas, and target companies..."
          />

          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="outline" type="button" onClick={() => setCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" className="bg-brand-teal text-white">
              Generate Curriculum
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal: Add Task to Phase */}
      <Modal
        isOpen={addTaskModalOpen}
        onClose={() => setAddTaskModalOpen(false)}
        title="Add Custom Learning Task"
        subtitle="Add a specialized topic, exercise, or mini-project to this curriculum phase."
      >
        <form onSubmit={handleAddTaskSubmit} className="space-y-4">
          <Input
            label="Task Title"
            value={taskForm.title}
            onChange={(e) => setTaskForm({ ...taskForm, title: e.target.value })}
            placeholder="e.g. Master In-Memory Caching with Redis"
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Topic"
              value={taskForm.topic}
              onChange={(e) => setTaskForm({ ...taskForm, topic: e.target.value })}
              placeholder="e.g. Distributed Systems"
            />
            <Input
              type="number"
              label="Estimated Minutes"
              value={taskForm.estimatedMinutes}
              onChange={(e) => setTaskForm({ ...taskForm, estimatedMinutes: parseInt(e.target.value) || 60 })}
            />
          </div>

          <Input
            label="Subtopics (comma-separated)"
            value={taskForm.subtopics}
            onChange={(e) => setTaskForm({ ...taskForm, subtopics: e.target.value })}
            placeholder="e.g. Cache Aside, TTL, Eviction Policies"
          />

          <Input
            label="Learning Objectives"
            value={taskForm.learningObjectives}
            onChange={(e) => setTaskForm({ ...taskForm, learningObjectives: e.target.value })}
            placeholder="e.g. Understand cache thundering herd and consistency guarantees"
          />

          <Input
            label="Practical Exercises"
            value={taskForm.practicalExercises}
            onChange={(e) => setTaskForm({ ...taskForm, practicalExercises: e.target.value })}
            placeholder="e.g. Integrate Redis cache layer into Spring Boot API"
          />

          <Input
            label="Mini-Project Details"
            value={taskForm.miniProject}
            onChange={(e) => setTaskForm({ ...taskForm, miniProject: e.target.value })}
            placeholder="e.g. Build an API rate limiter with sliding window logs"
          />

          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="outline" type="button" onClick={() => setAddTaskModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" className="bg-brand-teal text-white">
              Add Task
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
