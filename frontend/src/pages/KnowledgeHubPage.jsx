import React, { useState, useEffect, useCallback, useMemo } from 'react';
import {
  fetchKnowledgeResourcesApi,
  fetchFolderTreeApi,
  fetchFoldersApi,
  createFolderApi,
  updateFolderApi,
  deleteFolderApi,
  fetchCollectionsApi,
  createCollectionApi,
  fetchSuggestionsApi,
  acceptSuggestionApi,
  rejectSuggestionApi,
  bulkOrganizeApi,
  moveResourceApi,
  toggleFavoriteResourceApi,
  togglePinResourceApi,
  toggleArchiveResourceApi,
  executeBulkActionApi,
  searchKnowledgeApi,
  deleteDocumentApi,
  deleteKnowledgeItem,
  checkDuplicateDocumentApi,
} from '../services/api';

import KnowledgeSidebar from '../components/knowledge/KnowledgeSidebar';
import SmartInboxBanner from '../components/knowledge/SmartInboxBanner';
import ResourceCard from '../components/knowledge/ResourceCard';
import BulkActionToolbar from '../components/knowledge/BulkActionToolbar';
import ResourceDetailsPanel from '../components/knowledge/ResourceDetailsPanel';
import FolderModal from '../components/knowledge/FolderModal';
import FolderDeleteModal from '../components/knowledge/FolderDeleteModal';
import OrganizationRulesModal from '../components/knowledge/OrganizationRulesModal';
import CollectionModal from '../components/knowledge/CollectionModal';
import KnowledgeAnalyticsModal from '../components/knowledge/KnowledgeAnalyticsModal';
import DuplicatePromptModal from '../components/knowledge/DuplicatePromptModal';
import RelatedResourcesModal from '../components/knowledge/RelatedResourcesModal';

import KnowledgeItemModal from '../components/KnowledgeItemModal';
import DocumentUploadModal from '../components/DocumentUploadModal';
import DocumentViewModal from '../components/DocumentViewModal';
import DeleteConfirmModal from '../components/DeleteConfirmModal';

import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import EmptyState from '../components/ui/EmptyState';
import { CardSkeleton } from '../components/ui/LoadingSkeleton';
import Alert from '../components/ui/Alert';

export default function KnowledgeHubPage() {
  // Navigation & View state
  const [activeView, setActiveView] = useState('ALL'); // 'ALL' | 'INBOX' | 'FAVORITES' | 'PINNED' | 'RECENT' | 'ARCHIVE' | 'FOLDER' | 'COLLECTION'
  const [selectedFolder, setSelectedFolder] = useState(null);
  const [selectedCollection, setSelectedCollection] = useState(null);
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  // Data states
  const [resources, setResources] = useState([]);
  const [folderTree, setFolderTree] = useState([]);
  const [flatFolders, setFlatFolders] = useState([]);
  const [collections, setCollections] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [selectedResource, setSelectedResource] = useState(null);

  // Filters & Search
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedType, setSelectedType] = useState('ALL'); // 'ALL' | 'DOCUMENT' | 'NOTE' | 'LINK'
  const [sortBy, setSortBy] = useState('RECENT_MODIFIED');
  const [viewMode, setViewMode] = useState('grid'); // 'grid' | 'list'

  // Loading & Error states
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Multi-selection for bulk operations
  const [selectedIds, setSelectedIds] = useState(new Set());

  // Modal open states
  const [folderModalOpen, setFolderModalOpen] = useState(false);
  const [editingFolder, setEditingFolder] = useState(null);
  const [parentFolderForCreate, setParentFolderForCreate] = useState(null);
  const [folderDeleteModalOpen, setFolderDeleteModalOpen] = useState(false);
  const [folderToDelete, setFolderToDelete] = useState(null);

  const [rulesModalOpen, setRulesModalOpen] = useState(false);
  const [collectionModalOpen, setCollectionModalOpen] = useState(false);
  const [analyticsModalOpen, setAnalyticsModalOpen] = useState(false);
  const [connectModalOpen, setConnectModalOpen] = useState(false);

  const [uploadModalOpen, setUploadModalOpen] = useState(false);
  const [noteModalOpen, setNoteModalOpen] = useState(false);
  const [editingNoteItem, setEditingNoteItem] = useState(null);
  const [noteDefaultType, setNoteDefaultType] = useState('NOTE');

  const [documentViewModalOpen, setDocumentViewModalOpen] = useState(false);
  const [viewingDocumentId, setViewingDocumentId] = useState(null);

  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);
  const [resourceToDelete, setResourceToDelete] = useState(null);

  // 1. Load Folders, Collections & Suggestions
  const loadSidebarData = useCallback(async () => {
    try {
      const [treeData, flatData, collData, suggData] = await Promise.all([
        fetchFolderTreeApi().catch(() => []),
        fetchFoldersApi().catch(() => []),
        fetchCollectionsApi().catch(() => []),
        fetchSuggestionsApi().catch(() => []),
      ]);
      setFolderTree(Array.isArray(treeData) ? treeData : []);
      setFlatFolders(Array.isArray(flatData) ? flatData : []);
      setCollections(Array.isArray(collData) ? collData : []);
      setSuggestions(Array.isArray(suggData) ? suggData : []);
    } catch (err) {
      console.error('Failed to load navigation data:', err);
    }
  }, []);

  // 2. Load Resources
  const loadResources = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      let data = [];
      if (searchQuery.trim()) {
        data = await searchKnowledgeApi({
          q: searchQuery.trim(),
          type: selectedType,
          folderId: activeView === 'FOLDER' && selectedFolder ? selectedFolder.id : null,
          favoriteOnly: activeView === 'FAVORITES',
          archivedOnly: activeView === 'ARCHIVE',
          sortBy,
        });
      } else {
        data = await fetchKnowledgeResourcesApi({
          type: selectedType,
          folderId: activeView === 'FOLDER' && selectedFolder ? selectedFolder.id : null,
          inboxOnly: activeView === 'INBOX',
          favoriteOnly: activeView === 'FAVORITES',
          pinnedOnly: activeView === 'PINNED',
          archivedOnly: activeView === 'ARCHIVE',
          sortBy,
        });
      }
      setResources(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Failed to load knowledge resources.');
    } finally {
      setIsLoading(false);
    }
  }, [activeView, selectedFolder, selectedCollection, searchQuery, selectedType, sortBy]);

  useEffect(() => {
    loadSidebarData();
  }, [loadSidebarData]);

  useEffect(() => {
    loadResources();
  }, [loadResources]);

  // View & Folder Switch Handlers
  const handleSelectView = (view) => {
    setActiveView(view);
    setSelectedFolder(null);
    setSelectedCollection(null);
  };

  const handleSelectFolder = (folder) => {
    setActiveView('FOLDER');
    setSelectedFolder(folder);
    setSelectedCollection(null);
  };

  const handleSelectCollection = (collection) => {
    setActiveView('COLLECTION');
    setSelectedCollection(collection);
    setSelectedFolder(null);
  };

  // Folder Operations
  const handleCreateRootFolder = () => {
    setEditingFolder(null);
    setParentFolderForCreate(null);
    setFolderModalOpen(true);
  };

  const handleCreateSubfolder = (parentFolder) => {
    setEditingFolder(null);
    setParentFolderForCreate(parentFolder);
    setFolderModalOpen(true);
  };

  const handleEditFolder = (folder) => {
    setEditingFolder(folder);
    setParentFolderForCreate(null);
    setFolderModalOpen(true);
  };

  const handleFolderSubmit = async (folderData) => {
    try {
      if (editingFolder) {
        await updateFolderApi(editingFolder.id, folderData);
      } else {
        await createFolderApi(folderData);
      }
      setFolderModalOpen(false);
      loadSidebarData();
    } catch (err) {
      alert(err.message || 'Failed to save folder');
    }
  };

  const handleDeleteFolderPrompt = (folder) => {
    setFolderToDelete(folder);
    setFolderDeleteModalOpen(true);
  };

  const handleFolderDeleteConfirm = async (reassignOption, targetFolderId) => {
    if (!folderToDelete) return;
    try {
      await deleteFolderApi(folderToDelete.id, reassignOption, targetFolderId);
      setFolderDeleteModalOpen(false);
      setFolderToDelete(null);
      if (selectedFolder && selectedFolder.id === folderToDelete.id) {
        setActiveView('ALL');
        setSelectedFolder(null);
      }
      loadSidebarData();
      loadResources();
    } catch (err) {
      alert(err.message || 'Failed to delete folder');
    }
  };

  // Collection Handlers
  const handleCreateCollection = () => {
    setCollectionModalOpen(true);
  };

  const handleCollectionSubmit = async (collData) => {
    try {
      await createCollectionApi(collData);
      setCollectionModalOpen(false);
      loadSidebarData();
    } catch (err) {
      alert(err.message || 'Failed to create collection');
    }
  };

  // Resource Selection & Details
  const handleSelectResource = (res) => {
    setSelectedResource(res);
  };

  const handleToggleSelect = (res) => {
    const key = `${res.resourceType}-${res.id}`;
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(key)) {
        next.delete(key);
      } else {
        next.add(key);
      }
      return next;
    });
  };

  // Resource Actions
  const handleToggleFavorite = async (res) => {
    try {
      await toggleFavoriteResourceApi(res.resourceType, res.id);
      setResources((prev) =>
        prev.map((r) => (r.id === res.id && r.resourceType === res.resourceType ? { ...r, isFavorite: !r.isFavorite } : r))
      );
      if (selectedResource && selectedResource.id === res.id && selectedResource.resourceType === res.resourceType) {
        setSelectedResource((prev) => ({ ...prev, isFavorite: !prev.isFavorite }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleTogglePin = async (res) => {
    try {
      await togglePinResourceApi(res.resourceType, res.id);
      setResources((prev) =>
        prev.map((r) => (r.id === res.id && r.resourceType === res.resourceType ? { ...r, isPinned: !r.isPinned } : r))
      );
      if (selectedResource && selectedResource.id === res.id && selectedResource.resourceType === res.resourceType) {
        setSelectedResource((prev) => ({ ...prev, isPinned: !prev.isPinned }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleToggleArchive = async (res) => {
    try {
      await toggleArchiveResourceApi(res.resourceType, res.id);
      loadResources();
      loadSidebarData();
      if (selectedResource && selectedResource.id === res.id && selectedResource.resourceType === res.resourceType) {
        setSelectedResource((prev) => ({ ...prev, isArchived: !prev.isArchived }));
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleMoveResource = async (res, targetFolderId) => {
    try {
      await moveResourceApi(res.resourceType, res.id, targetFolderId);
      loadResources();
      loadSidebarData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleDeleteResourcePrompt = (res) => {
    setResourceToDelete(res);
    setDeleteConfirmOpen(true);
  };

  const handleDeleteResourceConfirm = async () => {
    if (!resourceToDelete) return;
    try {
      if (resourceToDelete.resourceType === 'DOCUMENT') {
        await deleteDocumentApi(resourceToDelete.id);
      } else {
        await deleteKnowledgeItem(resourceToDelete.id);
      }
      if (selectedResource && selectedResource.id === resourceToDelete.id && selectedResource.resourceType === resourceToDelete.resourceType) {
        setSelectedResource(null);
      }
      setDeleteConfirmOpen(false);
      setResourceToDelete(null);
      loadResources();
      loadSidebarData();
    } catch (err) {
      console.error(err);
    }
  };

  // Suggestion actions from Smart Inbox
  const handleAcceptSuggestion = async (suggestion) => {
    try {
      await acceptSuggestionApi(suggestion.id, suggestion.suggestedFolderId, suggestion.suggestedTags);
      setSuggestions((prev) => prev.filter((s) => s.id !== suggestion.id));
      loadResources();
      loadSidebarData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleRejectSuggestion = async (id) => {
    try {
      await rejectSuggestionApi(id);
      setSuggestions((prev) => prev.filter((s) => s.id !== id));
      loadSidebarData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleBulkAcceptHighConfidence = async () => {
    try {
      await bulkOrganizeApi([], true);
      await loadSidebarData();
      loadResources();
    } catch (err) {
      console.error(err);
    }
  };

  // Bulk Actions on Selected Items
  const handleBulkMove = async (targetFolderId) => {
    const docIds = [];
    const itemIds = [];
    selectedIds.forEach((key) => {
      const [type, id] = key.split('-');
      if (type === 'DOCUMENT') docIds.push(Number(id));
      else itemIds.push(Number(id));
    });

    try {
      if (docIds.length > 0) {
        await executeBulkActionApi('MOVE', 'DOCUMENT', docIds, targetFolderId);
      }
      if (itemIds.length > 0) {
        await executeBulkActionApi('MOVE', 'NOTE', itemIds, targetFolderId);
      }
      setSelectedIds(new Set());
      loadResources();
      loadSidebarData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleBulkFavorite = async () => {
    const docIds = [];
    const itemIds = [];
    selectedIds.forEach((key) => {
      const [type, id] = key.split('-');
      if (type === 'DOCUMENT') docIds.push(Number(id));
      else itemIds.push(Number(id));
    });

    try {
      if (docIds.length > 0) await executeBulkActionApi('FAVORITE', 'DOCUMENT', docIds);
      if (itemIds.length > 0) await executeBulkActionApi('FAVORITE', 'NOTE', itemIds);
      loadResources();
      loadSidebarData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleBulkArchive = async () => {
    const docIds = [];
    const itemIds = [];
    selectedIds.forEach((key) => {
      const [type, id] = key.split('-');
      if (type === 'DOCUMENT') docIds.push(Number(id));
      else itemIds.push(Number(id));
    });

    try {
      if (docIds.length > 0) await executeBulkActionApi('ARCHIVE', 'DOCUMENT', docIds);
      if (itemIds.length > 0) await executeBulkActionApi('ARCHIVE', 'NOTE', itemIds);
      setSelectedIds(new Set());
      loadResources();
      loadSidebarData();
    } catch (err) {
      console.error(err);
    }
  };

  const handleBulkDelete = async () => {
    if (!window.confirm(`Delete ${selectedIds.size} selected resources?`)) return;

    const docIds = [];
    const itemIds = [];
    selectedIds.forEach((key) => {
      const [type, id] = key.split('-');
      if (type === 'DOCUMENT') docIds.push(Number(id));
      else itemIds.push(Number(id));
    });

    try {
      if (docIds.length > 0) await executeBulkActionApi('DELETE', 'DOCUMENT', docIds);
      if (itemIds.length > 0) await executeBulkActionApi('DELETE', 'NOTE', itemIds);
      setSelectedIds(new Set());
      loadResources();
      loadSidebarData();
    } catch (err) {
      console.error(err);
    }
  };

  // Breadcrumbs title computation
  const viewTitle = useMemo(() => {
    if (activeView === 'INBOX') return 'Smart Inbox';
    if (activeView === 'FAVORITES') return 'Favorite Materials';
    if (activeView === 'PINNED') return 'Pinned Resources';
    if (activeView === 'RECENT') return 'Recently Viewed';
    if (activeView === 'ARCHIVE') return 'Archived Resources';
    if (activeView === 'FOLDER' && selectedFolder) return selectedFolder.name;
    if (activeView === 'COLLECTION' && selectedCollection) return selectedCollection.name;
    return 'All Academic Resources';
  }, [activeView, selectedFolder, selectedCollection]);

  return (
    <div className="flex bg-bg-page min-h-[calc(100vh-3.5rem)] text-text-primary transition-colors">
      {/* 1. Left Workspace Sidebar */}
      <KnowledgeSidebar
        activeView={activeView}
        onSelectView={handleSelectView}
        folderTree={folderTree}
        selectedFolderId={selectedFolder?.id}
        onSelectFolder={handleSelectFolder}
        onCreateFolder={handleCreateRootFolder}
        onCreateSubfolder={handleCreateSubfolder}
        onEditFolder={handleEditFolder}
        onDeleteFolder={handleDeleteFolderPrompt}
        collections={collections}
        selectedCollectionId={selectedCollection?.id}
        onSelectCollection={handleSelectCollection}
        onCreateCollection={handleCreateCollection}
        inboxCount={suggestions.length}
        onOpenRules={() => setRulesModalOpen(true)}
        onOpenAnalytics={() => setAnalyticsModalOpen(true)}
        isMobileOpen={mobileSidebarOpen}
        onCloseMobile={() => setMobileSidebarOpen(false)}
      />

      {/* 2. Main Workspace Content Area */}
      <main className="flex-1 min-w-0 p-4 sm:p-6 lg:p-8 overflow-y-auto">
        {/* Breadcrumb & Action Header */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
          <div className="min-w-0">
            <div className="flex items-center gap-2 text-xs text-text-muted mb-1">
              {/* Mobile Library Sidebar Drawer Toggle Button */}
              <button
                type="button"
                onClick={() => setMobileSidebarOpen(true)}
                className="lg:hidden inline-flex items-center gap-1.5 px-2 py-0.5 rounded-lg bg-bg-surface-elevated text-brand-teal font-semibold border border-border-default hover:bg-bg-surface-hover"
              >
                <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-6l-2-2H5a2 2 0 00-2 2z" />
                </svg>
                Folders
              </button>
              <span>Library</span>
              <span>/</span>
              <span className="font-semibold text-text-secondary truncate">{viewTitle}</span>
            </div>
            <h1 className="text-xl sm:text-2xl font-black text-text-primary tracking-tight truncate">
              {viewTitle}
            </h1>
          </div>

          <div className="flex items-center gap-2 flex-wrap shrink-0">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => {
                setEditingNoteItem(null);
                setNoteDefaultType('LINK');
                setNoteModalOpen(true);
              }}
            >
              + Save Link
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => {
                setEditingNoteItem(null);
                setNoteDefaultType('NOTE');
                setNoteModalOpen(true);
              }}
            >
              + New Note
            </Button>
            <Button
              type="button"
              variant="primary"
              size="sm"
              onClick={() => setUploadModalOpen(true)}
              icon={
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                </svg>
              }
            >
              Upload Document (100MB)
            </Button>
          </div>
        </div>

        {/* Smart Inbox Suggestions Banner */}
        <SmartInboxBanner
          suggestions={suggestions}
          onAccept={handleAcceptSuggestion}
          onReject={handleRejectSuggestion}
          onBulkAcceptHighConfidence={handleBulkAcceptHighConfidence}
          onNavigateInbox={() => setActiveView('INBOX')}
        />

        {/* Search, Filter & View Controls */}
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-6 bg-bg-surface p-3 rounded-2xl border border-border-default shadow-card">
          {/* Search Input */}
          <div className="relative flex-1 max-w-md">
            <svg
              className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-text-muted"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search documents, notes, links, tags, or OCR text..."
              className="w-full pl-9 pr-3 py-1.5 text-xs rounded-xl border border-border-default bg-bg-surface-elevated text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
            />
          </div>

          {/* Type Filters, Sorters & View Switch */}
          <div className="flex items-center gap-2 flex-wrap">
            {/* Type selector */}
            <div className="flex rounded-xl bg-bg-surface-elevated p-0.5 text-xs font-semibold border border-border-subtle">
              {['ALL', 'DOCUMENT', 'NOTE', 'LINK'].map((t) => (
                <button
                  key={t}
                  type="button"
                  onClick={() => setSelectedType(t)}
                  className={`px-2.5 py-1 rounded-lg transition-colors cursor-pointer ${
                    selectedType === t
                      ? 'bg-brand-teal text-white shadow-xs font-bold'
                      : 'text-text-muted hover:text-text-primary'
                  }`}
                >
                  {t === 'ALL' ? 'All Types' : t === 'DOCUMENT' ? 'Docs' : t === 'NOTE' ? 'Notes' : 'Links'}
                </button>
              ))}
            </div>

            {/* Sort selector */}
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="px-2.5 py-1 text-xs rounded-xl border border-border-default bg-bg-surface-elevated text-text-primary focus:outline-none focus:ring-1 focus:ring-brand-teal"
            >
              <option value="RECENT_MODIFIED">Recently Modified</option>
              <option value="TITLE">Title (A-Z)</option>
              <option value="DATE_CREATED">Date Uploaded</option>
            </select>

            {/* Grid / List View Switcher */}
            <div className="flex items-center rounded-xl bg-bg-surface-elevated p-0.5 border border-border-subtle">
              <button
                type="button"
                onClick={() => setViewMode('grid')}
                className={`p-1 rounded-lg transition-colors cursor-pointer ${
                  viewMode === 'grid'
                    ? 'bg-brand-teal text-white shadow-xs'
                    : 'text-text-muted hover:text-text-primary'
                }`}
                title="Grid View"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
                </svg>
              </button>
              <button
                type="button"
                onClick={() => setViewMode('list')}
                className={`p-1 rounded-lg transition-colors cursor-pointer ${
                  viewMode === 'list'
                    ? 'bg-brand-teal text-white shadow-xs'
                    : 'text-text-muted hover:text-text-primary'
                }`}
                title="List View"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        {/* Resources Grid / List */}
        {error && (
          <div className="mb-6">
            <Alert variant="error" onClose={() => setError('')}>
              {error}
            </Alert>
          </div>
        )}

        {isLoading ? (
          <div className={viewMode === 'grid' ? 'grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4' : 'space-y-2.5'}>
            <CardSkeleton count={6} />
          </div>
        ) : resources.length === 0 ? (
          <EmptyState
            title="No academic resources found"
            description={
              searchQuery
                ? `No materials matched "${searchQuery}". Try a different keyword.`
                : activeView === 'INBOX'
                ? 'Your Smart Inbox is clear! All items have been organized.'
                : 'Upload PDFs or DOCX papers (up to 100MB), create study notes, or save web links to build your knowledge base.'
            }
            actionLabel={searchQuery ? 'Clear Search' : '+ Upload First Document'}
            onAction={
              searchQuery
                ? () => setSearchQuery('')
                : () => setUploadModalOpen(true)
            }
          />
        ) : viewMode === 'grid' ? (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {resources.map((res) => (
              <ResourceCard
                key={`${res.resourceType}-${res.id}`}
                resource={res}
                viewMode="grid"
                isSelected={selectedIds.has(`${res.resourceType}-${res.id}`)}
                onToggleSelect={handleToggleSelect}
                onSelectResource={handleSelectResource}
                onToggleFavorite={handleToggleFavorite}
                onTogglePin={handleTogglePin}
                onToggleArchive={handleToggleArchive}
                onDeleteResource={handleDeleteResourcePrompt}
                onMoveResource={handleMoveResource}
              />
            ))}
          </div>
        ) : (
          <div className="space-y-2.5">
            {resources.map((res) => (
              <ResourceCard
                key={`${res.resourceType}-${res.id}`}
                resource={res}
                viewMode="list"
                isSelected={selectedIds.has(`${res.resourceType}-${res.id}`)}
                onToggleSelect={handleToggleSelect}
                onSelectResource={handleSelectResource}
                onToggleFavorite={handleToggleFavorite}
                onTogglePin={handleTogglePin}
                onToggleArchive={handleToggleArchive}
                onDeleteResource={handleDeleteResourcePrompt}
                onMoveResource={handleMoveResource}
              />
            ))}
          </div>
        )}
      </main>

      {/* 3. Right Contextual Details & Intelligence Drawer */}
      <ResourceDetailsPanel
        resource={selectedResource}
        isOpen={Boolean(selectedResource)}
        onClose={() => setSelectedResource(null)}
        allFolders={flatFolders}
        onMoveToFolder={handleMoveResource}
        onToggleFavorite={handleToggleFavorite}
        onToggleArchive={handleToggleArchive}
        onDeleteResource={handleDeleteResourcePrompt}
        onOpenConnectModal={() => setConnectModalOpen(true)}
      />

      {/* 4. Multi-Select Bulk Actions Toolbar */}
      <BulkActionToolbar
        selectedCount={selectedIds.size}
        folders={flatFolders}
        onClearSelection={() => setSelectedIds(new Set())}
        onMoveToFolder={handleBulkMove}
        onFavorite={handleBulkFavorite}
        onArchive={handleBulkArchive}
        onDelete={handleBulkDelete}
      />

      {/* 5. Modals */}
      <FolderModal
        isOpen={folderModalOpen}
        onClose={() => setFolderModalOpen(false)}
        onSubmit={handleFolderSubmit}
        folder={editingFolder}
        parentFolder={parentFolderForCreate}
        allFolders={flatFolders}
      />

      <FolderDeleteModal
        isOpen={folderDeleteModalOpen}
        onClose={() => setFolderDeleteModalOpen(false)}
        folder={folderToDelete}
        allFolders={flatFolders}
        onConfirmDelete={handleFolderDeleteConfirm}
      />

      <OrganizationRulesModal
        isOpen={rulesModalOpen}
        onClose={() => setRulesModalOpen(false)}
        folders={flatFolders}
      />

      <CollectionModal
        isOpen={collectionModalOpen}
        onClose={() => setCollectionModalOpen(false)}
        onSubmit={handleCollectionSubmit}
      />

      <KnowledgeAnalyticsModal
        isOpen={analyticsModalOpen}
        onClose={() => setAnalyticsModalOpen(false)}
      />

      <RelatedResourcesModal
        isOpen={connectModalOpen}
        onClose={() => setConnectModalOpen(false)}
        sourceResource={selectedResource}
        allResources={resources}
        onConnected={() => loadResources()}
      />

      <DocumentUploadModal
        isOpen={uploadModalOpen}
        onClose={() => setUploadModalOpen(false)}
        onUploadSuccess={() => {
          loadResources();
          loadSidebarData();
        }}
        allFolders={flatFolders}
      />

      <KnowledgeItemModal
        isOpen={noteModalOpen}
        onClose={() => setNoteModalOpen(false)}
        initialType={noteDefaultType}
        editingItem={editingNoteItem}
        onSaved={() => {
          loadResources();
          loadSidebarData();
        }}
      />

      <DocumentViewModal
        isOpen={documentViewModalOpen}
        onClose={() => setDocumentViewModalOpen(false)}
        documentId={viewingDocumentId}
      />

      <DeleteConfirmModal
        isOpen={deleteConfirmOpen}
        onClose={() => {
          setDeleteConfirmOpen(false);
          setResourceToDelete(null);
        }}
        onConfirm={handleDeleteResourceConfirm}
        itemName={resourceToDelete?.title || 'Resource'}
      />
    </div>
  );
}
