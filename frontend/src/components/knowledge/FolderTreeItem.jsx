import React, { useState } from 'react';

export default function FolderTreeItem({
  folder,
  selectedFolderId,
  onSelectFolder,
  onCreateSubfolder,
  onEditFolder,
  onDeleteFolder,
  level = 0,
}) {
  const [isExpanded, setIsExpanded] = useState(true);
  const [menuOpen, setMenuOpen] = useState(false);

  const hasChildren = folder.children && folder.children.length > 0;
  const isSelected = selectedFolderId === folder.id;

  const toggleExpand = (e) => {
    e.stopPropagation();
    setIsExpanded((prev) => !prev);
  };

  const handleSelect = () => {
    onSelectFolder(folder);
  };

  const handleMenuClick = (e) => {
    e.stopPropagation();
    setMenuOpen((prev) => !prev);
  };

  return (
    <div className="select-none text-xs">
      <div
        onClick={handleSelect}
        className={`group relative flex items-center justify-between px-2.5 py-1.5 rounded-xl cursor-pointer transition-colors ${
          isSelected
            ? 'bg-brand-teal text-white font-semibold shadow-xs'
            : 'text-text-secondary hover:text-text-primary hover:bg-bg-surface-hover'
        }`}
        style={{ paddingLeft: `${Math.max(10, level * 14 + 10)}px` }}
      >
        <div className="flex items-center gap-1.5 min-w-0">
          {hasChildren ? (
            <button
              onClick={toggleExpand}
              className={`p-0.5 rounded transition-transform duration-200 cursor-pointer ${
                isSelected ? 'text-white' : 'text-text-muted hover:text-text-primary'
              }`}
              title={isExpanded ? 'Collapse' : 'Expand'}
            >
              <svg
                className={`w-3.5 h-3.5 transition-transform duration-200 ${
                  isExpanded ? 'transform rotate-90' : ''
                }`}
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
              </svg>
            </button>
          ) : (
            <span className="w-3.5 inline-block shrink-0" />
          )}

          {/* Folder Icon with Custom Color */}
          <span
            className="w-4 h-4 flex-shrink-0 flex items-center justify-center"
            style={{ color: isSelected ? '#FFFFFF' : folder.color || '#178582' }}
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
              <path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z" />
            </svg>
          </span>

          <span className="truncate text-xs font-medium" title={folder.name}>
            {folder.name}
          </span>
        </div>

        <div className="flex items-center gap-1 flex-shrink-0 ml-1">
          {folder.totalCount > 0 && (
            <span
              className={`text-[10px] px-1.5 py-0.2 rounded-full font-mono ${
                isSelected
                  ? 'bg-white/20 text-white font-bold'
                  : 'bg-bg-surface-elevated text-text-muted'
              }`}
            >
              {folder.totalCount}
            </span>
          )}

          {/* Context menu toggle button */}
          <div className="relative">
            <button
              onClick={handleMenuClick}
              className={`opacity-0 group-hover:opacity-100 p-1 rounded-lg transition-opacity cursor-pointer ${
                isSelected
                  ? 'text-white hover:bg-white/20'
                  : 'text-text-muted hover:text-text-primary hover:bg-bg-surface-hover'
              }`}
              title="Folder options"
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 5v.01M12 12v.01M12 19v.01M12 6a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2zm0 7a1 1 0 110-2 1 1 0 010 2z"
                />
              </svg>
            </button>

            {menuOpen && (
              <>
                <div
                  className="fixed inset-0 z-30"
                  onClick={(e) => {
                    e.stopPropagation();
                    setMenuOpen(false);
                  }}
                />
                <div className="absolute right-0 top-full mt-1 w-36 bg-bg-surface border border-border-default rounded-xl shadow-modal py-1 z-40 text-xs text-text-primary animate-in fade-in zoom-in-95 duration-100">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setMenuOpen(false);
                      onCreateSubfolder(folder);
                    }}
                    className="w-full text-left px-3 py-1.5 hover:bg-bg-surface-hover flex items-center gap-2"
                  >
                    + New Subfolder
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setMenuOpen(false);
                      onEditFolder(folder);
                    }}
                    className="w-full text-left px-3 py-1.5 hover:bg-bg-surface-hover flex items-center gap-2"
                  >
                    Edit Folder
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setMenuOpen(false);
                      onDeleteFolder(folder);
                    }}
                    className="w-full text-left px-3 py-1.5 hover:bg-status-error-bg text-status-error flex items-center gap-2"
                  >
                    Delete Folder
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Render children subfolders */}
      {hasChildren && isExpanded && (
        <div className="space-y-0.5">
          {folder.children.map((child) => (
            <FolderTreeItem
              key={child.id}
              folder={child}
              selectedFolderId={selectedFolderId}
              onSelectFolder={onSelectFolder}
              onCreateSubfolder={onCreateSubfolder}
              onEditFolder={onEditFolder}
              onDeleteFolder={onDeleteFolder}
              level={level + 1}
            />
          ))}
        </div>
      )}
    </div>
  );
}
