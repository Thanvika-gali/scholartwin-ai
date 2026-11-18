import React, { useState, useEffect } from 'react';
import {
  fetchLearningProfileApi,
  updateLearningProfileApi,
  fetchLearningGapsApi,
  fetchLearningRoadmapsApi,
  createLearningRoadmapApi,
  fetchLearningRoadmapDetailApi,
  updateTaskStatusApi,
  addRoadmapTaskApi,
  deleteLearningRoadmapApi,
  deleteRoadmapTaskApi,
  fetchDailyPlanApi,
  createPlannerTaskApi,
  updatePlannerTaskApi,
  reschedulePlannerTaskApi,
  deletePlannerTaskApi,
  autoPopulateDailyPlanApi,
  fetchStudySessionsApi,
  logStudySessionApi,
  fetchRevisionQueueApi,
  createRevisionItemApi,
  completeRevisionApi,
  deleteRevisionItemApi,
  fetchAssessmentsApi,
  generateAssessmentApi,
  fetchAssessmentForDeliveryApi,
  submitAssessmentApi,
  fetchAssessmentHistoryApi,
  fetchLearningResourcesApi,
  associateLearningResourceApi,
  markResourceStudiedApi,
  deleteLearningResourceApi,
  fetchLearningAnalyticsApi,
  consultAiLearningAssistantApi,
} from '../services/api';

import Alert from '../components/ui/Alert';
import Button from '../components/ui/Button';
import Modal from '../components/ui/Modal';
import Input from '../components/ui/Input';

import LearningOverviewTab from '../components/learning/LearningOverviewTab';
import LearningRoadmapsTab from '../components/learning/LearningRoadmapsTab';
import DailyPlannerTab from '../components/learning/DailyPlannerTab';
import SpacedRevisionTab from '../components/learning/SpacedRevisionTab';
import AssessmentsTab from '../components/learning/AssessmentsTab';
import LearningGapRadarTab from '../components/learning/LearningGapRadarTab';
import LearningAnalyticsTab from '../components/learning/LearningAnalyticsTab';
import LearningResourcesTab from '../components/learning/LearningResourcesTab';
import LearningProfileTab from '../components/learning/LearningProfileTab';
import AiLearningAssistantTab from '../components/learning/AiLearningAssistantTab';

export default function LearningHubPage() {
  const [activeTab, setActiveTab] = useState('overview');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Core Data States
  const [profile, setProfile] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [dailyPlan, setDailyPlan] = useState(null);
  const [plannerDate, setPlannerDate] = useState(new Date().toISOString().split('T')[0]);
  const [revisionQueue, setRevisionQueue] = useState(null);
  const [activeReviewItem, setActiveReviewItem] = useState(null);
  const [gaps, setGaps] = useState(null);
  const [roadmaps, setRoadmaps] = useState([]);
  const [selectedRoadmap, setSelectedRoadmap] = useState(null);
  const [assessments, setAssessments] = useState([]);
  const [attemptHistory, setAttemptHistory] = useState([]);
  const [activeDelivery, setActiveDelivery] = useState(null);
  const [lastResult, setLastResult] = useState(null);
  const [resources, setResources] = useState([]);
  const [assistantResponse, setAssistantResponse] = useState(null);
  const [assistantLoading, setAssistantLoading] = useState(false);
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  // Study Session Logger Modal State
  const [sessionModalOpen, setSessionModalOpen] = useState(false);
  const [sessionForm, setSessionForm] = useState({
    durationMinutes: 45,
    category: 'ROADMAP_STUDY',
    title: '',
    notes: '',
  });

  // Resource Association Modal from Roadmap Task
  const [resourceTaskAssociation, setResourceTaskAssociation] = useState(null);

  const tabs = [
    { id: 'overview', label: 'Command Center', icon: '📊' },
    { id: 'roadmaps', label: 'Curricula & Roadmaps', icon: '🗺️' },
    { id: 'planner', label: 'Daily Study Planner', icon: '📅' },
    { id: 'revision', label: 'Spaced Revision', icon: '🔁' },
    { id: 'assessments', label: 'Assessment Studio', icon: '📝' },
    { id: 'radar', label: 'Learning Gap Radar', icon: '🎯' },
    { id: 'analytics', label: 'Performance Analytics', icon: '📈' },
    { id: 'resources', label: 'Resource Vault', icon: '📚' },
    { id: 'ai-assistant', label: 'AI Learning Assistant', icon: '✨' },
    { id: 'profile', label: 'Profile & Calibration', icon: '⚙️' },
  ];

  // Load all initial learning data
  useEffect(() => {
    loadAllData();
  }, []);

  const loadAllData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [
        profileRes,
        analyticsRes,
        planRes,
        revisionRes,
        roadmapsRes,
        gapsRes,
        assessmentsRes,
        historyRes,
        resourcesRes,
      ] = await Promise.allSettled([
        fetchLearningProfileApi(),
        fetchLearningAnalyticsApi(),
        fetchDailyPlanApi(plannerDate),
        fetchRevisionQueueApi(),
        fetchLearningRoadmapsApi(),
        fetchLearningGapsApi(),
        fetchAssessmentsApi(),
        fetchAssessmentHistoryApi(),
        fetchLearningResourcesApi(),
      ]);

      if (profileRes.status === 'fulfilled') setProfile(profileRes.value);
      if (analyticsRes.status === 'fulfilled') setAnalytics(analyticsRes.value);
      if (planRes.status === 'fulfilled') setDailyPlan(planRes.value);
      if (revisionRes.status === 'fulfilled') setRevisionQueue(revisionRes.value);
      if (roadmapsRes.status === 'fulfilled') {
        setRoadmaps(roadmapsRes.value);
        if (roadmapsRes.value && roadmapsRes.value.length > 0 && !selectedRoadmap) {
          loadRoadmapDetail(roadmapsRes.value[0].id);
        }
      }
      if (gapsRes.status === 'fulfilled') setGaps(gapsRes.value);
      if (assessmentsRes.status === 'fulfilled') setAssessments(assessmentsRes.value);
      if (historyRes.status === 'fulfilled') setAttemptHistory(historyRes.value);
      if (resourcesRes.status === 'fulfilled') setResources(resourcesRes.value);
    } catch (err) {
      setError(err.message || 'Failed to load personalized learning workspace.');
    } finally {
      setLoading(false);
    }
  };

  const loadRoadmapDetail = async (id) => {
    try {
      const detail = await fetchLearningRoadmapDetailApi(id);
      setSelectedRoadmap(detail);
    } catch (err) {
      setError('Failed to load roadmap details: ' + err.message);
    }
  };

  const handlePlannerDateChange = async (newDate) => {
    setPlannerDate(newDate);
    try {
      const plan = await fetchDailyPlanApi(newDate);
      setDailyPlan(plan);
    } catch (err) {
      setError('Failed to fetch plan for date: ' + err.message);
    }
  };

  // --- Handlers: Roadmaps ---
  const handleCreateRoadmap = async (data) => {
    try {
      const created = await createLearningRoadmapApi(data);
      setSuccessMessage(`Roadmap "${created.title}" successfully generated with structured phases.`);
      const updatedList = await fetchLearningRoadmapsApi();
      setRoadmaps(updatedList);
      loadRoadmapDetail(created.id);
      refreshAnalytics();
    } catch (err) {
      setError('Failed to create roadmap: ' + err.message);
    }
  };

  const handleUpdateTaskStatus = async (taskId, status, actualMinutes) => {
    try {
      await updateTaskStatusApi(taskId, { status, actualMinutes });
      if (selectedRoadmap) {
        loadRoadmapDetail(selectedRoadmap.id);
      }
      refreshAnalytics();
    } catch (err) {
      setError('Failed to update task status: ' + err.message);
    }
  };

  const handleAddTaskToPhase = async (phaseId, taskData) => {
    try {
      await addRoadmapTaskApi(phaseId, taskData);
      setSuccessMessage('Custom learning task successfully added to curriculum.');
      if (selectedRoadmap) {
        loadRoadmapDetail(selectedRoadmap.id);
      }
      refreshAnalytics();
    } catch (err) {
      setError('Failed to add task to phase: ' + err.message);
    }
  };

  const handleDeleteRoadmap = async (id) => {
    if (!window.confirm('Are you sure you want to delete this entire learning roadmap?')) return;
    try {
      await deleteLearningRoadmapApi(id);
      setSuccessMessage('Roadmap successfully removed.');
      const updatedList = await fetchLearningRoadmapsApi();
      setRoadmaps(updatedList);
      setSelectedRoadmap(updatedList.length > 0 ? updatedList[0] : null);
      refreshAnalytics();
    } catch (err) {
      setError('Failed to delete roadmap: ' + err.message);
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (!window.confirm('Delete this task from the curriculum?')) return;
    try {
      await deleteRoadmapTaskApi(taskId);
      if (selectedRoadmap) {
        loadRoadmapDetail(selectedRoadmap.id);
      }
      refreshAnalytics();
    } catch (err) {
      setError('Failed to delete task: ' + err.message);
    }
  };

  // --- Handlers: Daily Planner ---
  const handleCreatePlannerTask = async (data) => {
    try {
      await createPlannerTaskApi(data);
      setSuccessMessage('Task added to daily study plan.');
      handlePlannerDateChange(plannerDate);
      refreshAnalytics();
    } catch (err) {
      setError('Failed to create planner task: ' + err.message);
    }
  };

  const handleUpdatePlannerTask = async (taskId, data) => {
    try {
      await updatePlannerTaskApi(taskId, data);
      handlePlannerDateChange(plannerDate);
      refreshAnalytics();
    } catch (err) {
      setError('Failed to update task: ' + err.message);
    }
  };

  const handleReschedulePlannerTask = async (taskId, data) => {
    try {
      await reschedulePlannerTaskApi(taskId, data);
      setSuccessMessage('Task rescheduled successfully.');
      handlePlannerDateChange(plannerDate);
      refreshAnalytics();
    } catch (err) {
      setError('Failed to reschedule task: ' + err.message);
    }
  };

  const handleDeletePlannerTask = async (taskId) => {
    try {
      await deletePlannerTaskApi(taskId);
      handlePlannerDateChange(plannerDate);
      refreshAnalytics();
    } catch (err) {
      setError('Failed to delete planner task: ' + err.message);
    }
  };

  const handleAutoPopulateDailyPlan = async () => {
    try {
      const plan = await autoPopulateDailyPlanApi(plannerDate);
      setDailyPlan(plan);
      setSuccessMessage("Today's schedule auto-populated from active roadmaps and revisions.");
    } catch (err) {
      setError('Failed to auto-populate daily plan: ' + err.message);
    }
  };

  // --- Handlers: Study Session ---
  const handleLogStudySession = async (e) => {
    e.preventDefault();
    try {
      await logStudySessionApi(sessionForm);
      setSessionModalOpen(false);
      setSuccessMessage(`Logged ${sessionForm.durationMinutes} minutes of focused study.`);
      setSessionForm({
        durationMinutes: 45,
        category: 'ROADMAP_STUDY',
        title: '',
        notes: '',
      });
      refreshAnalytics();
      handlePlannerDateChange(plannerDate);
    } catch (err) {
      setError('Failed to log study session: ' + err.message);
    }
  };

  // --- Handlers: Spaced Revision ---
  const handleCreateRevisionItem = async (data) => {
    try {
      await createRevisionItemApi(data);
      setSuccessMessage('Revision item added with spaced repetition schedule.');
      const queue = await fetchRevisionQueueApi();
      setRevisionQueue(queue);
      refreshAnalytics();
    } catch (err) {
      setError('Failed to create revision item: ' + err.message);
    }
  };

  const handleCompleteRevision = async (id, data) => {
    try {
      const updated = await completeRevisionApi(id, data);
      setSuccessMessage(`Revision completed. Next scheduled check-in: ${updated.scheduledDate} (Stage ${updated.stage}).`);
      const queue = await fetchRevisionQueueApi();
      setRevisionQueue(queue);
      refreshAnalytics();
    } catch (err) {
      setError('Failed to complete revision: ' + err.message);
    }
  };

  const handleDeleteRevisionItem = async (id) => {
    if (!window.confirm('Delete this spaced revision item?')) return;
    try {
      await deleteRevisionItemApi(id);
      const queue = await fetchRevisionQueueApi();
      setRevisionQueue(queue);
      refreshAnalytics();
    } catch (err) {
      setError('Failed to delete revision item: ' + err.message);
    }
  };

  // --- Handlers: Assessments ---
  const handleGenerateAssessment = async (data) => {
    try {
      const generated = await generateAssessmentApi(data);
      setSuccessMessage(`Assessment "${generated.title}" generated successfully.`);
      const list = await fetchAssessmentsApi();
      setAssessments(list);
    } catch (err) {
      setError('Failed to generate assessment: ' + err.message);
    }
  };

  const handleStartAssessment = async (id) => {
    try {
      const delivery = await fetchAssessmentForDeliveryApi(id);
      setActiveDelivery(delivery);
      setLastResult(null);
    } catch (err) {
      setError('Failed to launch assessment delivery: ' + err.message);
    }
  };

  const handleSubmitAssessment = async (id, submission) => {
    try {
      const result = await submitAssessmentApi(id, submission);
      setLastResult(result);
      setActiveDelivery(null);
      const history = await fetchAssessmentHistoryApi();
      setAttemptHistory(history);
      refreshAnalytics();
      const updatedGaps = await fetchLearningGapsApi();
      setGaps(updatedGaps);
    } catch (err) {
      setError('Failed to submit assessment: ' + err.message);
    }
  };

  // --- Handlers: Resources ---
  const handleAssociateResource = async (data) => {
    try {
      await associateLearningResourceApi(data);
      setSuccessMessage('Resource attached to learning vault.');
      const list = await fetchLearningResourcesApi();
      setResources(list);
    } catch (err) {
      setError('Failed to attach resource: ' + err.message);
    }
  };

  const handleMarkResourceStudied = async (id, studied) => {
    try {
      await markResourceStudiedApi(id, studied);
      const list = await fetchLearningResourcesApi();
      setResources(list);
    } catch (err) {
      setError('Failed to update studied status: ' + err.message);
    }
  };

  const handleDeleteResource = async (id) => {
    try {
      await deleteLearningResourceApi(id);
      const list = await fetchLearningResourcesApi();
      setResources(list);
    } catch (err) {
      setError('Failed to remove resource: ' + err.message);
    }
  };

  // --- Handlers: Profile Calibration ---
  const handleUpdateProfile = async (formData) => {
    setIsSavingProfile(true);
    try {
      const updated = await updateLearningProfileApi(formData);
      setProfile(updated);
      setSuccessMessage('Learning Profile and Engine calibration saved successfully.');
      const updatedGaps = await fetchLearningGapsApi();
      setGaps(updatedGaps);
    } catch (err) {
      setError('Failed to update profile: ' + err.message);
    } finally {
      setIsSavingProfile(false);
    }
  };

  // --- Handlers: AI Learning Assistant ---
  const handleConsultAssistant = async (queryPayload) => {
    setAssistantLoading(true);
    try {
      const response = await consultAiLearningAssistantApi(queryPayload);
      setAssistantResponse(response);
    } catch (err) {
      setError('Failed to consult AI Assistant: ' + err.message);
    } finally {
      setAssistantLoading(false);
    }
  };

  const refreshAnalytics = async () => {
    try {
      const a = await fetchLearningAnalyticsApi();
      setAnalytics(a);
    } catch (ignored) {}
  };

  if (loading) {
    return (
      <div className="p-8 max-w-7xl mx-auto space-y-6">
        <div className="h-8 bg-bg-surface-elevated rounded-xl w-64 animate-pulse"></div>
        <div className="grid grid-cols-2 md:grid-cols-6 gap-4">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="h-24 bg-bg-surface border border-border-default rounded-2xl animate-pulse"></div>
          ))}
        </div>
        <div className="h-64 bg-bg-surface border border-border-default rounded-2xl animate-pulse"></div>
      </div>
    );
  }

  return (
    <div className="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto space-y-6">
      {/* Top Banner Alerts */}
      {error && (
        <Alert variant="error" dismissible onDismiss={() => setError(null)}>
          {error}
        </Alert>
      )}
      {successMessage && (
        <Alert variant="success" dismissible onDismiss={() => setSuccessMessage(null)}>
          {successMessage}
        </Alert>
      )}

      {/* Workspace Navigation Tabs Header */}
      <div className="bg-bg-surface border border-border-default rounded-2xl p-2 shadow-sm flex items-center gap-1.5 overflow-x-auto scrollbar-thin">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            type="button"
            onClick={() => setActiveTab(tab.id)}
            className={`px-3.5 py-2 rounded-xl text-xs font-bold transition-all flex items-center gap-2 whitespace-nowrap shrink-0 ${
              activeTab === tab.id
                ? 'bg-brand-navy text-white dark:bg-brand-teal shadow-sm'
                : 'text-text-muted hover:text-text-primary hover:bg-bg-surface-elevated'
            }`}
          >
            <span>{tab.icon}</span>
            <span>{tab.label}</span>
          </button>
        ))}
      </div>

      {/* Tab Panels */}
      {activeTab === 'overview' && (
        <LearningOverviewTab
          analytics={analytics}
          dailyPlan={dailyPlan}
          revisionQueue={revisionQueue}
          gaps={gaps}
          roadmaps={roadmaps}
          onNavigateTab={setActiveTab}
          onOpenReviewModal={(item) => setActiveReviewItem(item)}
          onOpenStudySessionModal={() => setSessionModalOpen(true)}
          onToggleTaskComplete={(taskId, status) =>
            handleUpdatePlannerTask(taskId, {
              status: status === 'COMPLETED' ? 'PENDING' : 'COMPLETED',
            })
          }
        />
      )}

      {activeTab === 'roadmaps' && (
        <LearningRoadmapsTab
          roadmaps={roadmaps}
          selectedRoadmap={selectedRoadmap}
          onSelectRoadmap={loadRoadmapDetail}
          onCreateRoadmap={handleCreateRoadmap}
          onUpdateTaskStatus={handleUpdateTaskStatus}
          onAddTaskToPhase={handleAddTaskToPhase}
          onDeleteRoadmap={handleDeleteRoadmap}
          onDeleteTask={handleDeleteTask}
          onOpenResourceModal={(topic, taskId) => {
            setResourceTaskAssociation({ topic, taskId });
            setActiveTab('resources');
          }}
        />
      )}

      {activeTab === 'planner' && (
        <DailyPlannerTab
          dailyPlan={dailyPlan}
          currentDate={plannerDate}
          onDateChange={handlePlannerDateChange}
          onCreateTask={handleCreatePlannerTask}
          onUpdateTask={handleUpdatePlannerTask}
          onRescheduleTask={handleReschedulePlannerTask}
          onDeleteTask={handleDeletePlannerTask}
          onAutoPopulate={handleAutoPopulateDailyPlan}
          onOpenStudySessionModal={() => setSessionModalOpen(true)}
        />
      )}

      {activeTab === 'revision' && (
        <SpacedRevisionTab
          revisionQueue={revisionQueue}
          onCreateRevisionItem={handleCreateRevisionItem}
          onCompleteRevision={handleCompleteRevision}
          onDeleteRevisionItem={handleDeleteRevisionItem}
          activeReviewItem={activeReviewItem}
          setActiveReviewItem={setActiveReviewItem}
        />
      )}

      {activeTab === 'assessments' && (
        <AssessmentsTab
          assessments={assessments}
          attemptHistory={attemptHistory}
          onGenerateAssessment={handleGenerateAssessment}
          onStartAssessment={handleStartAssessment}
          activeDelivery={activeDelivery}
          onSubmitAssessment={handleSubmitAssessment}
          lastResult={lastResult}
          onClearDeliveryAndResult={() => {
            setActiveDelivery(null);
            setLastResult(null);
          }}
        />
      )}

      {activeTab === 'radar' && (
        <LearningGapRadarTab gaps={gaps} onNavigateTab={setActiveTab} />
      )}

      {activeTab === 'analytics' && (
        <LearningAnalyticsTab analytics={analytics} />
      )}

      {activeTab === 'resources' && (
        <LearningResourcesTab
          resources={resources}
          onAssociateResource={handleAssociateResource}
          onMarkStudied={handleMarkResourceStudied}
          onDeleteResource={handleDeleteResource}
        />
      )}

      {activeTab === 'ai-assistant' && (
        <AiLearningAssistantTab
          profile={profile}
          onConsultAssistant={handleConsultAssistant}
          isLoading={assistantLoading}
          assistantResponse={assistantResponse}
          onNavigateTab={setActiveTab}
        />
      )}

      {activeTab === 'profile' && (
        <LearningProfileTab
          profile={profile}
          onUpdateProfile={handleUpdateProfile}
          isSaving={isSavingProfile}
        />
      )}

      {/* Modal: Log Study Session */}
      <Modal
        isOpen={sessionModalOpen}
        onClose={() => setSessionModalOpen(false)}
        title="Log Focused Study Session"
        subtitle="Track actual learning minutes to update streaks, daily completion, and verified study hours."
      >
        <form onSubmit={handleLogStudySession} className="space-y-4">
          <Input
            type="number"
            label="Duration (Minutes)"
            value={sessionForm.durationMinutes}
            onChange={(e) =>
              setSessionForm({ ...sessionForm, durationMinutes: parseInt(e.target.value) || 30 })
            }
            required
          />

          <div>
            <label className="block text-xs font-semibold text-text-muted mb-1.5">Session Category</label>
            <select
              value={sessionForm.category}
              onChange={(e) => setSessionForm({ ...sessionForm, category: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl border border-border-default bg-bg-surface text-text-primary text-xs focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              <option value="ROADMAP_STUDY">Curriculum Roadmap Study</option>
              <option value="DSA_PRACTICE">DSA Algorithmic Practice</option>
              <option value="SPACED_REVISION">Active Spaced Revision</option>
              <option value="ACADEMIC_RESEARCH">Knowledge Hub Research</option>
              <option value="MOCK_INTERVIEW">Mock Technical Assessment</option>
            </select>
          </div>

          <Input
            label="Session Title / Topic Focus"
            value={sessionForm.title}
            onChange={(e) => setSessionForm({ ...sessionForm, title: e.target.value })}
            placeholder="e.g. Tree Traversal Recursion Patterns"
          />

          <Input
            label="Session Notes / Reflection"
            value={sessionForm.notes}
            onChange={(e) => setSessionForm({ ...sessionForm, notes: e.target.value })}
            placeholder="What concepts clicked? What still needs practice?"
          />

          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="outline" type="button" onClick={() => setSessionModalOpen(false)}>
              Cancel
            </Button>
            <Button variant="primary" type="submit" className="bg-brand-teal text-white">
              Save Study Session
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
