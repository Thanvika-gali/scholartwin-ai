import React, { useState } from 'react';
import Card, { CardHeader, CardTitle, CardContent } from '../ui/Card';
import Badge from '../ui/Badge';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Modal from '../ui/Modal';

export default function DailyPlannerTab({
  dailyPlan,
  currentDate,
  onDateChange,
  onCreateTask,
  onUpdateTask,
  onRescheduleTask,
  onDeleteTask,
  onAutoPopulate,
  onOpenStudySessionModal,
}) {
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [rescheduleModalOpen, setRescheduleModalOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState(null);

  // New Planner Task State
  const [taskForm, setTaskForm] = useState({
    title: '',
    description: '',
    taskType: 'CUSTOM',
    priority: 'MEDIUM',
    estimatedMinutes: 45,
    notes: '',
  });

  // Reschedule Task State
  const [rescheduleForm, setRescheduleForm] = useState({
    newDate: '',
    reason: 'Time constraints / Priority shift',
  });

  const handleOpenReschedule = (task) => {
    setSelectedTask(task);
    setRescheduleForm({
      newDate: new Date(Date.now() + 86400000).toISOString().split('T')[0],
      reason: 'Time constraints / Priority shift',
    });
    setRescheduleModalOpen(true);
  };

  const handleCreateSubmit = (e) => {
    e.preventDefault();
    onCreateTask({
      ...taskForm,
      scheduledDate: currentDate,
    });
    setCreateModalOpen(false);
    setTaskForm({
      title: '',
      description: '',
      taskType: 'CUSTOM',
      priority: 'MEDIUM',
      estimatedMinutes: 45,
      notes: '',
    });
  };

  const handleRescheduleSubmit = (e) => {
    e.preventDefault();
    if (selectedTask) {
      onRescheduleTask(selectedTask.id, rescheduleForm);
      setRescheduleModalOpen(false);
    }
  };

  const isOverAllocated =
    dailyPlan &&
    dailyPlan.dailyAvailableMinutes > 0 &&
    dailyPlan.plannedMinutes > dailyPlan.dailyAvailableMinutes;

  return (
    <div className="space-y-6">
      {/* Date Header & Action Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-bg-surface border border-border-default rounded-2xl p-5 shadow-sm">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => {
              const d = new Date(currentDate);
              d.setDate(d.getDate() - 1);
              onDateChange(d.toISOString().split('T')[0]);
            }}
            className="p-2 rounded-xl border border-border-default hover:bg-bg-surface-elevated text-text-muted hover:text-text-primary transition-colors"
          >
            ←
          </button>
          <div>
            <h2 className="text-base sm:text-lg font-bold text-text-primary tracking-tight">
              Schedule for {currentDate}
            </h2>
            <p className="text-xs text-text-muted">
              {dailyPlan?.dailyAvailableMinutes || 60} mins target study time configured
            </p>
          </div>
          <button
            type="button"
            onClick={() => {
              const d = new Date(currentDate);
              d.setDate(d.getDate() + 1);
              onDateChange(d.toISOString().split('T')[0]);
            }}
            className="p-2 rounded-xl border border-border-default hover:bg-bg-surface-elevated text-text-muted hover:text-text-primary transition-colors"
          >
            →
          </button>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => onDateChange(new Date().toISOString().split('T')[0])}
            className="text-xs"
          >
            Today
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={onAutoPopulate}
            className="text-xs text-brand-teal"
          >
            ⚡ Auto-populate from Roadmaps
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={onOpenStudySessionModal}
            className="text-xs"
          >
            Log Session
          </Button>
          <Button
            variant="primary"
            size="sm"
            onClick={() => setCreateModalOpen(true)}
            className="bg-brand-teal text-white text-xs shadow-sm"
          >
            + Add Task
          </Button>
        </div>
      </div>

      {/* Over-allocation warning guard */}
      {isOverAllocated && (
        <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-600 dark:text-amber-400 text-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="text-base">⚠️</span>
            <span>
              <strong>Study Fatigue Guard:</strong> Planned duration ({dailyPlan.plannedMinutes}m) exceeds your daily available time ({dailyPlan.dailyAvailableMinutes}m). Consider rescheduling lower-priority tasks.
            </span>
          </div>
        </div>
      )}

      {/* Daily Metrics Summary Bar */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="bg-bg-surface border border-border-default rounded-xl p-3.5 shadow-sm">
          <span className="text-[11px] text-text-muted block">Planned Time</span>
          <span className="text-xl font-bold text-text-primary">{dailyPlan?.plannedMinutes || 0}m</span>
        </div>
        <div className="bg-bg-surface border border-border-default rounded-xl p-3.5 shadow-sm">
          <span className="text-[11px] text-text-muted block">Completed Time</span>
          <span className="text-xl font-bold text-brand-teal">{dailyPlan?.completedMinutes || 0}m</span>
        </div>
        <div className="bg-bg-surface border border-border-default rounded-xl p-3.5 shadow-sm">
          <span className="text-[11px] text-text-muted block">Completion Rate</span>
          <span className="text-xl font-bold text-text-primary">{dailyPlan?.completionPercentage || 0}%</span>
        </div>
        <div className="bg-bg-surface border border-border-default rounded-xl p-3.5 shadow-sm">
          <span className="text-[11px] text-text-muted block">Tasks Progress</span>
          <span className="text-xl font-bold text-text-primary">
            {dailyPlan?.completedTasksCount || 0} / {dailyPlan?.totalTasksCount || 0}
          </span>
        </div>
      </div>

      {/* Task List Table / Cards */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Planned Tasks & Activities</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          {dailyPlan?.tasks && dailyPlan.tasks.length > 0 ? (
            <div className="space-y-2">
              {dailyPlan.tasks.map((task) => (
                <div
                  key={task.id}
                  className={`p-4 rounded-xl border flex flex-col sm:flex-row sm:items-center justify-between gap-3 transition-colors ${
                    task.status === 'COMPLETED'
                      ? 'bg-emerald-500/5 border-emerald-500/20 text-text-muted'
                      : task.status === 'RESCHEDULED'
                      ? 'bg-amber-500/5 border-amber-500/20 text-text-muted'
                      : 'bg-bg-surface-elevated/40 border-border-default hover:border-brand-teal/40'
                  }`}
                >
                  <div className="flex items-start gap-3 min-w-0">
                    <button
                      type="button"
                      onClick={() =>
                        onUpdateTask(task.id, {
                          status: task.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED',
                          actualMinutes: task.actualMinutes > 0 ? task.actualMinutes : task.estimatedMinutes,
                        })
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
                        <span className={`text-xs sm:text-sm font-bold ${task.status === 'COMPLETED' ? 'line-through text-text-muted' : 'text-text-primary'}`}>
                          {task.title}
                        </span>
                        <span className="text-[10px] px-1.5 py-0.5 rounded uppercase font-semibold bg-bg-surface text-text-muted border border-border-subtle">
                          {task.taskType}
                        </span>
                      </div>
                      {task.description && (
                        <p className="text-xs text-text-muted mt-0.5">{task.description}</p>
                      )}
                      {task.notes && (
                        <p className="text-[11px] text-brand-gold mt-1 italic">{task.notes}</p>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-3 shrink-0 self-end sm:self-auto">
                    <div className="text-right">
                      <span className="text-xs font-semibold text-text-primary block">
                        {task.actualMinutes > 0 ? `${task.actualMinutes}m` : `${task.estimatedMinutes}m`}
                      </span>
                      <span className="text-[10px] text-text-muted block">estimated</span>
                    </div>

                    <Badge
                      variant={
                        task.priority === 'HIGH' ? 'error' : task.priority === 'MEDIUM' ? 'warning' : 'neutral'
                      }
                      size="sm"
                    >
                      {task.priority}
                    </Badge>

                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleOpenReschedule(task)}
                      className="text-xs text-brand-teal p-1.5 h-auto"
                      title="Reschedule Task"
                    >
                      📅
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => onDeleteTask(task.id)}
                      className="text-xs text-rose-500 p-1.5 h-auto"
                      title="Delete Task"
                    >
                      ✕
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="p-8 text-center text-text-muted text-xs space-y-3">
              <p>No study tasks scheduled for {currentDate}.</p>
              <div className="flex items-center justify-center gap-3">
                <Button variant="outline" size="sm" onClick={onAutoPopulate} className="text-xs text-brand-teal">
                  Auto-populate from Roadmap & Revisions
                </Button>
                <Button variant="primary" size="sm" onClick={() => setCreateModalOpen(true)} className="bg-brand-teal text-white text-xs">
                  Create Custom Task
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Modal: Create Task */}
      <Modal
        isOpen={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        title="Add Study Task"
        subtitle={`Schedule an actionable learning task for ${currentDate}.`}
      >
        <form onSubmit={handleCreateSubmit} className="space-y-4">
          <Input
            label="Task Title"
            value={taskForm.title}
            onChange={(e) => setTaskForm({ ...taskForm, title: e.target.value })}
            placeholder="e.g. Master Sliding Window Two Pointers"
            required
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-text-muted mb-1.5">Task Category</label>
              <select
                value={taskForm.taskType}
                onChange={(e) => setTaskForm({ ...taskForm, taskType: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
              >
                <option value="ROADMAP_TOPIC">Roadmap Topic</option>
                <option value="REVISION">Spaced Revision</option>
                <option value="DSA_PRACTICE">DSA Coding Practice</option>
                <option value="KNOWLEDGE_HUB">Knowledge Hub Research</option>
                <option value="CAREER_PREP">Career & Interview Prep</option>
                <option value="CUSTOM">Custom Objective</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-text-muted mb-1.5">Priority</label>
              <select
                value={taskForm.priority}
                onChange={(e) => setTaskForm({ ...taskForm, priority: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
              >
                <option value="HIGH">High Priority</option>
                <option value="MEDIUM">Medium Priority</option>
                <option value="LOW">Low Priority</option>
              </select>
            </div>
          </div>

          <Input
            type="number"
            label="Estimated Duration (Minutes)"
            value={taskForm.estimatedMinutes}
            onChange={(e) => setTaskForm({ ...taskForm, estimatedMinutes: parseInt(e.target.value) || 45 })}
            required
          />

          <Input
            label="Task Notes / Focus Area"
            value={taskForm.notes}
            onChange={(e) => setTaskForm({ ...taskForm, notes: e.target.value })}
            placeholder="Specific constraints, LeetCode numbers, or document pages..."
          />

          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="outline" type="button" onClick={() => setCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" className="bg-brand-teal text-white">
              Schedule Task
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal: Reschedule Task */}
      <Modal
        isOpen={rescheduleModalOpen}
        onClose={() => setRescheduleModalOpen(false)}
        title="Reschedule Study Task"
        subtitle={`Move "${selectedTask?.title}" to an alternate study day.`}
      >
        <form onSubmit={handleRescheduleSubmit} className="space-y-4">
          <Input
            type="date"
            label="New Scheduled Date"
            value={rescheduleForm.newDate}
            onChange={(e) => setRescheduleForm({ ...rescheduleForm, newDate: e.target.value })}
            required
          />

          <div>
            <label className="block text-xs font-semibold text-text-muted mb-1.5">Reason for Rescheduling</label>
            <select
              value={rescheduleForm.reason}
              onChange={(e) => setRescheduleForm({ ...rescheduleForm, reason: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              <option value="Time constraints / Priority shift">Time constraints / Priority shift</option>
              <option value="Concept requires prerequisite study">Concept requires prerequisite study</option>
              <option value="Academic exam / Assignment conflict">Academic exam / Assignment conflict</option>
              <option value="Fatigue guard: Over-allocated daily study time">Fatigue guard: Over-allocated daily study time</option>
              <option value="Other">Other</option>
            </select>
          </div>

          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="outline" type="button" onClick={() => setRescheduleModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" className="bg-brand-teal text-white">
              Confirm Reschedule
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
