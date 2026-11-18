import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  fetchMemoriesApi,
  fetchMemoryStatsApi,
  createMemoryApi,
  updateMemoryApi,
  deleteMemoryApi,
  archiveMemoryApi,
  togglePinMemoryApi,
  extractMemoriesApi,
  consolidateMemoriesApi,
  bulkDeleteMemoriesApi,
  fetchMemoryPreferencesApi,
  updateMemoryPreferencesApi,
  fetchInsightsApi,
  refreshInsightsApi,
  dismissInsightApi,
  fetchRecommendationsApi,
  generateRecommendationsApi,
  acceptRecommendationApi,
  completeRecommendationApi,
  dismissRecommendationApi,
  fetchKnowledgeGraphApi,
  syncKnowledgeGraphApi,
  fetchIntelligenceOverviewApi,
  sendChatMessageApi,
  fetchChatHistoryApi,
  clearChatHistoryApi,
} from '../services/api';

import Alert from '../components/ui/Alert';
import Button from '../components/ui/Button';

// Tab Components
import IntelligenceDashboardTab from '../components/intelligence/IntelligenceDashboardTab';
import MemoryCenterTab from '../components/intelligence/MemoryCenterTab';
import AddEditMemoryModal from '../components/intelligence/AddEditMemoryModal';
import PersonalizedInsightsTab from '../components/intelligence/PersonalizedInsightsTab';
import RecommendationsTab from '../components/intelligence/RecommendationsTab';
import KnowledgeGraphTab from '../components/intelligence/KnowledgeGraphTab';
import IntelligenceAnalyticsTab from '../components/intelligence/IntelligenceAnalyticsTab';
import MemoryPreferencesTab from '../components/intelligence/MemoryPreferencesTab';
import AiTwinChatTab from '../components/intelligence/AiTwinChatTab';

export default function IntelligenceHubPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialTab = searchParams.get('tab') || 'overview';
  const [activeTab, setActiveTab] = useState(initialTab);

  // Loading & notification states
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Data states
  const [memories, setMemories] = useState([]);
  const [memoryStats, setMemoryStats] = useState(null);
  const [insights, setInsights] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [graphData, setGraphData] = useState({ nodes: [], relationships: [] });
  const [analytics, setAnalytics] = useState(null);
  const [preferences, setPreferences] = useState(null);
  const [chatHistory, setChatHistory] = useState([]);

  // Modal State
  const [isMemoryModalOpen, setIsMemoryModalOpen] = useState(false);
  const [editingMemory, setEditingMemory] = useState(null);

  // Sync tab with URL
  const handleTabChange = (tabKey) => {
    setActiveTab(tabKey);
    setSearchParams({ tab: tabKey });
  };

  const showNotification = (msg, isErr = false) => {
    if (isErr) {
      setError(msg);
      setSuccessMessage(null);
    } else {
      setSuccessMessage(msg);
      setError(null);
    }
    setTimeout(() => {
      setError(null);
      setSuccessMessage(null);
    }, 4000);
  };

  // Initial load
  const loadAllData = useCallback(async () => {
    setLoading(true);
    try {
      const [
        memoriesRes,
        statsRes,
        insightsRes,
        recsRes,
        graphRes,
        overviewRes,
        prefsRes,
        historyRes,
      ] = await Promise.allSettled([
        fetchMemoriesApi(),
        fetchMemoryStatsApi(),
        fetchInsightsApi(),
        fetchRecommendationsApi(),
        fetchKnowledgeGraphApi(),
        fetchIntelligenceOverviewApi(),
        fetchMemoryPreferencesApi(),
        fetchChatHistoryApi(),
      ]);

      if (memoriesRes.status === 'fulfilled' && memoriesRes.value) {
        setMemories(memoriesRes.value.content || memoriesRes.value || []);
      }
      if (statsRes.status === 'fulfilled' && statsRes.value) {
        setMemoryStats(statsRes.value);
      }
      if (insightsRes.status === 'fulfilled' && insightsRes.value) {
        setInsights(insightsRes.value || []);
      }
      if (recsRes.status === 'fulfilled' && recsRes.value) {
        setRecommendations(recsRes.value || []);
      }
      if (graphRes.status === 'fulfilled' && graphRes.value) {
        setGraphData(graphRes.value || { nodes: [], relationships: [] });
      }
      if (overviewRes.status === 'fulfilled' && overviewRes.value) {
        setAnalytics(overviewRes.value);
      }
      if (prefsRes.status === 'fulfilled' && prefsRes.value) {
        setPreferences(prefsRes.value);
      }
      if (historyRes.status === 'fulfilled' && historyRes.value) {
        setChatHistory(historyRes.value || []);
      }
    } catch (err) {
      console.error('Failed to load intelligence data:', err);
      showNotification('Failed to load some intelligence services. Please try refreshing.', true);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAllData();
  }, [loadAllData]);

  // Handle direct tab deep-linking
  useEffect(() => {
    const tabParam = searchParams.get('tab');
    if (tabParam && tabParam !== activeTab) {
      setActiveTab(tabParam);
    }
  }, [searchParams]);

  // ==========================================
  // MEMORY ACTIONS
  // ==========================================
  const handleOpenAddMemory = () => {
    setEditingMemory(null);
    setIsMemoryModalOpen(true);
  };

  const handleOpenEditMemory = (memory) => {
    setEditingMemory(memory);
    setIsMemoryModalOpen(true);
  };

  const handleSaveMemory = async (formData) => {
    setActionLoading(true);
    try {
      if (editingMemory) {
        const updated = await updateMemoryApi(editingMemory.id, formData);
        setMemories((prev) => prev.map((m) => (m.id === updated.id ? updated : m)));
        showNotification('Memory updated successfully');
      } else {
        const created = await createMemoryApi(formData);
        setMemories((prev) => [created, ...prev]);
        showNotification('New memory added to profile');
      }
      setIsMemoryModalOpen(false);
      setEditingMemory(null);
      // Refresh stats
      fetchMemoryStatsApi().then(setMemoryStats).catch(console.error);
    } catch (err) {
      showNotification(err.message || 'Failed to save memory', true);
    } finally {
      setActionLoading(false);
    }
  };

  const handleTogglePinMemory = async (id) => {
    try {
      const updated = await togglePinMemoryApi(id);
      setMemories((prev) => prev.map((m) => (m.id === id ? updated : m)));
      showNotification(updated.pinned ? 'Memory pinned' : 'Memory unpinned');
    } catch (err) {
      showNotification(err.message || 'Failed to update pin state', true);
    }
  };

  const handleArchiveMemory = async (id) => {
    try {
      const updated = await archiveMemoryApi(id);
      setMemories((prev) => prev.map((m) => (m.id === id ? updated : m)));
      showNotification('Memory archived');
    } catch (err) {
      showNotification(err.message || 'Failed to archive memory', true);
    }
  };

  const handleDeleteMemory = async (id) => {
    try {
      await deleteMemoryApi(id);
      setMemories((prev) => prev.filter((m) => m.id !== id));
      showNotification('Memory deleted');
      fetchMemoryStatsApi().then(setMemoryStats).catch(console.error);
    } catch (err) {
      showNotification(err.message || 'Failed to delete memory', true);
    }
  };

  const handleBulkDeleteMemories = async (ids) => {
    if (!ids || ids.length === 0) return;
    try {
      await bulkDeleteMemoriesApi({ ids });
      setMemories((prev) => prev.filter((m) => !ids.includes(m.id)));
      showNotification(`Deleted ${ids.length} memories`);
      fetchMemoryStatsApi().then(setMemoryStats).catch(console.error);
    } catch (err) {
      showNotification(err.message || 'Bulk deletion failed', true);
    }
  };

  const handleExtractFromActivity = async () => {
    setActionLoading(true);
    try {
      const result = await extractMemoriesApi();
      const count = result?.extractedCount || 0;
      showNotification(`Extraction complete: ${count} new memories identified`);
      const updated = await fetchMemoriesApi();
      setMemories(updated.content || updated || []);
      fetchMemoryStatsApi().then(setMemoryStats).catch(console.error);
    } catch (err) {
      showNotification(err.message || 'Failed to extract memories from activity', true);
    } finally {
      setActionLoading(false);
    }
  };

  const handleConsolidateMemories = async () => {
    setActionLoading(true);
    try {
      const result = await consolidateMemoriesApi();
      showNotification(result?.message || 'Memories consolidated and contradictions resolved');
      const updated = await fetchMemoriesApi();
      setMemories(updated.content || updated || []);
    } catch (err) {
      showNotification(err.message || 'Consolidation failed', true);
    } finally {
      setActionLoading(false);
    }
  };

  // ==========================================
  // INSIGHT ACTIONS
  // ==========================================
  const handleRefreshInsights = async () => {
    setActionLoading(true);
    try {
      const refreshed = await refreshInsightsApi();
      setInsights(refreshed || []);
      showNotification('Insights refreshed from current academic records');
    } catch (err) {
      showNotification(err.message || 'Failed to refresh insights', true);
    } finally {
      setActionLoading(false);
    }
  };

  const handleDismissInsight = async (id) => {
    try {
      await dismissInsightApi(id);
      setInsights((prev) => prev.filter((i) => i.id !== id));
      showNotification('Insight dismissed');
    } catch (err) {
      showNotification(err.message || 'Failed to dismiss insight', true);
    }
  };

  // ==========================================
  // RECOMMENDATION ACTIONS
  // ==========================================
  const handleRefreshRecommendations = async () => {
    setActionLoading(true);
    try {
      const result = await generateRecommendationsApi();
      setRecommendations(result || []);
      showNotification('Recommendation queue regenerated');
    } catch (err) {
      showNotification(err.message || 'Failed to regenerate recommendations', true);
    } finally {
      setActionLoading(false);
    }
  };

  const handleUpdateRecommendationStatus = async (id, status) => {
    try {
      let updated;
      if (status === 'ACCEPTED') {
        updated = await acceptRecommendationApi(id);
        showNotification('Recommendation accepted into active plan');
      } else if (status === 'COMPLETED') {
        updated = await completeRecommendationApi(id);
        showNotification('Recommendation marked as completed');
      } else if (status === 'DISMISSED') {
        updated = await dismissRecommendationApi(id);
        showNotification('Recommendation dismissed');
      }

      if (updated) {
        setRecommendations((prev) => prev.map((r) => (r.id === id ? updated : r)));
      }
    } catch (err) {
      showNotification(err.message || 'Failed to update recommendation', true);
    }
  };

  // ==========================================
  // KNOWLEDGE GRAPH ACTIONS
  // ==========================================
  const handleSyncGraph = async () => {
    setActionLoading(true);
    try {
      const synced = await syncKnowledgeGraphApi();
      setGraphData(synced || { nodes: [], relationships: [] });
      showNotification('Knowledge Graph synced with all courses, skills, and DSA entities');
    } catch (err) {
      showNotification(err.message || 'Failed to sync knowledge graph', true);
    } finally {
      setActionLoading(false);
    }
  };

  // ==========================================
  // ANALYTICS ACTIONS
  // ==========================================
  const handleRefreshAnalytics = async () => {
    setActionLoading(true);
    try {
      const data = await fetchIntelligenceOverviewApi();
      setAnalytics(data);
      showNotification('Analytics telemetry refreshed');
    } catch (err) {
      showNotification(err.message || 'Failed to refresh analytics', true);
    } finally {
      setActionLoading(false);
    }
  };

  // ==========================================
  // CHAT ACTIONS
  // ==========================================
  const handleSendMessage = async (msgText, includeGrounding) => {
    setActionLoading(true);
    try {
      // Optimistic user message
      const userMsg = {
        id: Date.now(),
        sender: 'USER',
        message: msgText,
        createdAt: new Date().toISOString(),
      };
      setChatHistory((prev) => [...prev, userMsg]);

      const response = await sendChatMessageApi({
        prompt: msgText,
        includeGrounding,
        model: 'gemini-1.5-flash',
      });

      const assistantMsg = {
        id: Date.now() + 1,
        sender: 'ASSISTANT',
        message: response.reply,
        groundedMemories: response.groundedMemories || [],
        sources: response.sources || [],
        createdAt: new Date().toISOString(),
      };
      setChatHistory((prev) => [...prev, assistantMsg]);

      // If new memories were extracted in conversation, refresh memories list in background
      if (response.newMemoriesExtracted && response.newMemoriesExtracted.length > 0) {
        fetchMemoriesApi().then((res) => setMemories(res.content || res || [])).catch(console.error);
        fetchMemoryStatsApi().then(setMemoryStats).catch(console.error);
      }
    } catch (err) {
      showNotification(err.message || 'Failed to reach AI Twin service', true);
    } finally {
      setActionLoading(false);
    }
  };

  const handleClearHistory = async () => {
    try {
      await clearChatHistoryApi();
      setChatHistory([]);
      showNotification('Chat conversation history cleared');
    } catch (err) {
      showNotification(err.message || 'Failed to clear chat history', true);
    }
  };

  // ==========================================
  // PREFERENCES ACTIONS
  // ==========================================
  const handleSavePreferences = async (formData) => {
    setActionLoading(true);
    try {
      const updated = await updateMemoryPreferencesApi(formData);
      setPreferences(updated);
      showNotification('Memory preferences saved successfully');
    } catch (err) {
      showNotification(err.message || 'Failed to save preferences', true);
    } finally {
      setActionLoading(false);
    }
  };

  const handleClearAllMemories = async () => {
    setActionLoading(true);
    try {
      const allIds = memories.map((m) => m.id);
      if (allIds.length > 0) {
        await bulkDeleteMemoriesApi({ ids: allIds });
      }
      setMemories([]);
      fetchMemoryStatsApi().then(setMemoryStats).catch(console.error);
      showNotification('All active memories have been purged permanently');
    } catch (err) {
      showNotification(err.message || 'Failed to clear memories', true);
    } finally {
      setActionLoading(false);
    }
  };

  const tabs = [
    { key: 'overview', label: 'Intelligence Overview', icon: '🧠' },
    { key: 'memories', label: 'Memory Center', icon: '💾', badge: memories.length },
    { key: 'insights', label: 'Personalized Insights', icon: '💡', badge: insights.filter((i) => i.status === 'ACTIVE').length },
    { key: 'recommendations', label: 'Recommendations', icon: '📋', badge: recommendations.filter((r) => r.status === 'PENDING').length },
    { key: 'graph', label: 'Knowledge Graph', icon: '🕸️' },
    { key: 'analytics', label: 'Analytics Telemetry', icon: '📊' },
    { key: 'chat', label: 'AI Twin Chat', icon: '🤖' },
    { key: 'preferences', label: 'Preferences & Privacy', icon: '⚙️' },
  ];

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      {/* Toast / Notification Banner */}
      {error && <Alert type="danger" message={error} />}
      {successMessage && <Alert type="success" message={successMessage} />}

      {/* Main Page Header */}
      <div className="bg-gradient-to-r from-[#0a1828] via-[#0d2238] to-[#0a1828] border border-border-default/80 rounded-2xl p-6 shadow-md relative overflow-hidden">
        <div className="absolute top-0 right-0 w-96 h-96 bg-primary-500/5 rounded-full blur-3xl pointer-events-none" />
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 relative z-10">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="text-[10px] font-bold px-2.5 py-0.5 rounded-full bg-primary-500/10 text-primary-400 border border-primary-500/30 uppercase tracking-wider font-mono">
                Milestone 9 Intelligence Engine
              </span>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                Gemini 2.5 Grounded
              </span>
            </div>
            <h1 className="text-2xl font-black text-white tracking-tight">
              AI Insights, Memory & Academic Analytics
            </h1>
            <p className="text-xs text-slate-300 max-w-2xl leading-relaxed">
              Autonomous cognitive layer connecting your study schedule, DSA submissions, resume gaps, and research vault with contextual long-term memory.
            </p>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <Button
              variant="secondary"
              size="sm"
              onClick={handleExtractFromActivity}
              loading={actionLoading}
              title="Scan all recent student activity and extract new memories"
            >
              ⚡ Extract Activity Facts
            </Button>
            <Button
              variant="primary"
              size="sm"
              onClick={() => handleTabChange('chat')}
            >
              💬 Open AI Twin Chat
            </Button>
          </div>
        </div>
      </div>

      {/* Tabs Navigation Bar */}
      <div className="border-b border-border-default overflow-x-auto scrollbar-none">
        <div className="flex items-center gap-2 min-w-max pb-2">
          {tabs.map((tab) => {
            const isActive = activeTab === tab.key;
            return (
              <button
                key={tab.key}
                onClick={() => handleTabChange(tab.key)}
                className={`flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold transition-all select-none ${
                  isActive
                    ? 'bg-primary-500/15 text-primary-400 border border-primary-500/40 shadow-xs'
                    : 'text-text-muted hover:text-text-primary hover:bg-bg-surface border border-transparent'
                }`}
              >
                <span>{tab.icon}</span>
                <span>{tab.label}</span>
                {tab.badge !== undefined && tab.badge > 0 && (
                  <span
                    className={`text-[10px] px-1.5 py-0.2 rounded-full font-mono ${
                      isActive
                        ? 'bg-primary-500/30 text-white'
                        : 'bg-border-default/60 text-text-muted'
                    }`}
                  >
                    {tab.badge}
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Tab Panels */}
      {loading && activeTab !== 'chat' ? (
        <div className="p-12 text-center text-text-muted space-y-3">
          <div className="w-8 h-8 rounded-full border-2 border-primary-500 border-t-transparent animate-spin mx-auto" />
          <p className="text-xs font-mono">Synchronizing ScholarTwin Intelligence layers...</p>
        </div>
      ) : (
        <div>
          {activeTab === 'overview' && (
            <IntelligenceDashboardTab
              memories={memories}
              insights={insights}
              recommendations={recommendations}
              graphData={graphData}
              overview={analytics}
              analytics={analytics}
              onNavigateTab={handleTabChange}
            />
          )}

          {activeTab === 'memories' && (
            <MemoryCenterTab
              memories={memories}
              stats={memoryStats}
              isLoading={actionLoading}
              onAddMemory={handleOpenAddMemory}
              onEditMemory={handleOpenEditMemory}
              onTogglePin={handleTogglePinMemory}
              onArchiveMemory={handleArchiveMemory}
              onDeleteMemory={handleDeleteMemory}
              onBulkDelete={handleBulkDeleteMemories}
              onExtractFromActivity={handleExtractFromActivity}
              onConsolidateMemories={handleConsolidateMemories}
            />
          )}

          {activeTab === 'insights' && (
            <PersonalizedInsightsTab
              insights={insights}
              isLoading={actionLoading}
              onRefreshInsights={handleRefreshInsights}
              onDismissInsight={handleDismissInsight}
              onNavigateTab={handleTabChange}
            />
          )}

          {activeTab === 'recommendations' && (
            <RecommendationsTab
              recommendations={recommendations}
              isLoading={actionLoading}
              onRefreshRecommendations={handleRefreshRecommendations}
              onUpdateStatus={handleUpdateRecommendationStatus}
              onNavigateTab={handleTabChange}
            />
          )}

          {activeTab === 'graph' && (
            <KnowledgeGraphTab
              graphData={graphData}
              isLoading={actionLoading}
              onSyncGraph={handleSyncGraph}
            />
          )}

          {activeTab === 'analytics' && (
            <IntelligenceAnalyticsTab
              analytics={analytics}
              isLoading={actionLoading}
              onRefreshAnalytics={handleRefreshAnalytics}
            />
          )}

          {activeTab === 'chat' && (
            <AiTwinChatTab
              chatHistory={chatHistory}
              isLoading={actionLoading}
              onSendMessage={handleSendMessage}
              onClearHistory={handleClearHistory}
            />
          )}

          {activeTab === 'preferences' && (
            <MemoryPreferencesTab
              preferences={preferences}
              isLoading={actionLoading}
              onSavePreferences={handleSavePreferences}
              onClearAllMemories={handleClearAllMemories}
            />
          )}
        </div>
      )}

      {/* Add / Edit Memory Modal */}
      <AddEditMemoryModal
        isOpen={isMemoryModalOpen}
        onClose={() => {
          setIsMemoryModalOpen(false);
          setEditingMemory(null);
        }}
        onSave={handleSaveMemory}
        initialData={editingMemory}
        isSaving={actionLoading}
      />
    </div>
  );
}
