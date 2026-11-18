import React from 'react';
import FolderTreeItem from './FolderTreeItem';

export default function KnowledgeSidebar({
  activeView, // 'ALL' | 'INBOX' | 'FAVORITES' | 'PINNED' | 'RECENT' | 'ARCHIVE' | 'FOLDER' | 'COLLECTION'
  onSelectView,
  folderTree = [],
  selectedFolderId,
  onSelectFolder,
  onCreateFolder,
  onCreateSubfolder,
  onEditFolder,
  onDeleteFolder,
  collections = [],
  selectedCollectionId,
  onSelectCollection,
  onCreateCollection,
  inboxCount = 0,
  onOpenRules,
  onOpenAnalytics,
  isMobileOpen = false,
  onCloseMobile,
}) {
  const content = (
    <div className="flex flex-col h-full bg-bg-sidebar text-text-primary select-none overflow-hidden">
      {/* Mobile Header if in drawer */}
      {onCloseMobile && (
        <div className="lg:hidden p-4 border-b border-border-subtle flex items-center justify-between">
          <span className="text-sm font-bold text-text-primary">Library Navigation</span>
          <button
            type="button"
            onClick={onCloseMobile}
            className="p-1.5 rounded-lg text-text-muted hover:text-text-primary hover:bg-bg-surface-hover"
            aria-label="Close library sidebar"
          >
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      )}

      {/* Scrollable Navigation */}
      <div className="flex-1 overflow-y-auto px-3 py-4 space-y-6">
        {/* Core Views */}
        <div>
          <div className="text-[10px] font-bold text-text-muted uppercase tracking-wider px-2.5 mb-2">
            Library
          </div>
          <nav className="space-y-0.5">
            <button
              onClick={() => {
                onSelectView('ALL');
                if (onCloseMobile) onCloseMobile();
              }}
              className={`w-full flex items-center justify-between px-2.5 py-1.5 rounded-xl text-xs font-medium transition-colors cursor-pointer ${
                activeView === 'ALL'
                  ? 'bg-brand-teal text-white font-semibold shadow-xs'
                  : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
              }`}
            >
              <div className="flex items-center gap-2">
                <svg className="w-4 h-4 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
                  />
                </svg>
                <span>All Resources</span>
              </div>
            </button>

            <button
              onClick={() => {
                onSelectView('INBOX');
                if (onCloseMobile) onCloseMobile();
              }}
              className={`w-full flex items-center justify-between px-2.5 py-1.5 rounded-xl text-xs font-medium transition-colors cursor-pointer ${
                activeView === 'INBOX'
                  ? 'bg-brand-teal text-white font-semibold shadow-xs'
                  : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
              }`}
            >
              <div className="flex items-center gap-2">
                <svg className="w-4 h-4 text-amber-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
                  />
                </svg>
                <span>Smart Inbox</span>
              </div>
              {inboxCount > 0 && (
                <span className="px-1.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-500/20 text-amber-600 dark:text-amber-400">
                  {inboxCount}
                </span>
              )}
            </button>

            <button
              onClick={() => {
                onSelectView('FAVORITES');
                if (onCloseMobile) onCloseMobile();
              }}
              className={`w-full flex items-center gap-2 px-2.5 py-1.5 rounded-xl text-xs font-medium transition-colors cursor-pointer ${
                activeView === 'FAVORITES'
                  ? 'bg-brand-teal text-white font-semibold shadow-xs'
                  : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
              }`}
            >
              <svg className="w-4 h-4 text-amber-400" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
              <span>Favorites</span>
            </button>

            <button
              onClick={() => {
                onSelectView('PINNED');
                if (onCloseMobile) onCloseMobile();
              }}
              className={`w-full flex items-center gap-2 px-2.5 py-1.5 rounded-xl text-xs font-medium transition-colors cursor-pointer ${
                activeView === 'PINNED'
                  ? 'bg-brand-teal text-white font-semibold shadow-xs'
                  : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
              }`}
            >
              <svg className="w-4 h-4 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
              </svg>
              <span>Pinned Resources</span>
            </button>

            <button
              onClick={() => {
                onSelectView('RECENT');
                if (onCloseMobile) onCloseMobile();
              }}
              className={`w-full flex items-center gap-2 px-2.5 py-1.5 rounded-xl text-xs font-medium transition-colors cursor-pointer ${
                activeView === 'RECENT'
                  ? 'bg-brand-teal text-white font-semibold shadow-xs'
                  : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
              }`}
            >
              <svg className="w-4 h-4 text-text-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>Recently Viewed</span>
            </button>

            <button
              onClick={() => {
                onSelectView('ARCHIVE');
                if (onCloseMobile) onCloseMobile();
              }}
              className={`w-full flex items-center gap-2 px-2.5 py-1.5 rounded-xl text-xs font-medium transition-colors cursor-pointer ${
                activeView === 'ARCHIVE'
                  ? 'bg-brand-teal text-white font-semibold shadow-xs'
                  : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
              }`}
            >
              <svg className="w-4 h-4 text-text-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
              </svg>
              <span>Archive</span>
            </button>
          </nav>
        </div>

        {/* Folders Section */}
        <div>
          <div className="flex items-center justify-between px-2.5 mb-2">
            <span className="text-[10px] font-bold text-text-muted uppercase tracking-wider">
              Folders
            </span>
            <button
              onClick={onCreateFolder}
              className="p-1 rounded-lg hover:bg-bg-surface-hover text-brand-teal transition-colors"
              title="Create Root Folder"
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 4v16m8-8H4" />
              </svg>
            </button>
          </div>

          {folderTree.length === 0 ? (
            <div className="px-2.5 py-3 text-[11px] text-text-muted text-center rounded-xl bg-bg-surface-elevated/40 border border-border-subtle">
              No folders yet. Click + to create one.
            </div>
          ) : (
            <div className="space-y-0.5">
              {folderTree.map((node) => (
                <FolderTreeItem
                  key={node.id}
                  folder={node}
                  selectedFolderId={selectedFolderId}
                  onSelectFolder={(f) => {
                    onSelectFolder(f);
                    if (onCloseMobile) onCloseMobile();
                  }}
                  onCreateSubfolder={onCreateSubfolder}
                  onEditFolder={onEditFolder}
                  onDeleteFolder={onDeleteFolder}
                />
              ))}
            </div>
          )}
        </div>

        {/* Collections Section */}
        <div>
          <div className="flex items-center justify-between px-2.5 mb-2">
            <span className="text-[10px] font-bold text-text-muted uppercase tracking-wider">
              Collections
            </span>
            <button
              onClick={onCreateCollection}
              className="p-1 rounded-lg hover:bg-bg-surface-hover text-brand-teal transition-colors"
              title="Create Collection"
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 4v16m8-8H4" />
              </svg>
            </button>
          </div>

          {collections.length === 0 ? (
            <div className="px-2.5 py-2 text-[11px] text-text-muted text-center">
              No custom collections.
            </div>
          ) : (
            <div className="space-y-0.5">
              {collections.map((coll) => {
                const isSelected = activeView === 'COLLECTION' && selectedCollectionId === coll.id;
                return (
                  <button
                    key={coll.id}
                    onClick={() => {
                      onSelectCollection(coll);
                      if (onCloseMobile) onCloseMobile();
                    }}
                    className={`w-full flex items-center justify-between px-2.5 py-1.5 rounded-xl text-xs transition-colors cursor-pointer ${
                      isSelected
                        ? 'bg-brand-teal text-white font-semibold shadow-xs'
                        : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
                    }`}
                  >
                    <div className="flex items-center gap-2 truncate">
                      <svg className="w-4 h-4 text-brand-teal flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
                      </svg>
                      <span className="truncate">{coll.name}</span>
                    </div>
                    <span className="text-[10px] px-1.5 py-0.2 rounded-full font-mono bg-bg-surface-elevated text-text-muted">
                      {coll.itemCount || 0}
                    </span>
                  </button>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Bottom Action Footer */}
      <div className="p-3 border-t border-border-subtle space-y-1 bg-bg-surface-elevated/40 shrink-0">
        <button
          onClick={onOpenRules}
          className="w-full flex items-center gap-2 px-2.5 py-1.5 text-xs text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover rounded-xl transition-colors cursor-pointer"
        >
          <svg className="w-4 h-4 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <span>Organization Rules</span>
        </button>

        <button
          onClick={onOpenAnalytics}
          className="w-full flex items-center gap-2 px-2.5 py-1.5 text-xs text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover rounded-xl transition-colors cursor-pointer"
        >
          <svg className="w-4 h-4 text-brand-gold" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          <span>Hub Analytics</span>
        </button>
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop Persistent Sidebar */}
      <aside className="hidden lg:flex w-64 flex-shrink-0 border-r border-border-default flex-col h-[calc(100vh-3.5rem)] sticky top-14 select-none overflow-hidden">
        {content}
      </aside>

      {/* Mobile Drawer */}
      {isMobileOpen && (
        <div className="lg:hidden fixed inset-0 z-50 flex">
          <div
            className="fixed inset-0 bg-brand-navy/70 dark:bg-black/80 backdrop-blur-sm transition-opacity"
            onClick={onCloseMobile}
            aria-hidden="true"
          />
          <div className="relative flex-1 flex flex-col max-w-xs w-full bg-bg-sidebar z-10 shadow-modal">
            {content}
          </div>
        </div>
      )}
    </>
  );
}
