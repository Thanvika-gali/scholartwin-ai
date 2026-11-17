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
    <div className="select-none text-sm">
      <div
        onClick={handleSelect}
        className={`group relative flex items-center justify-between px-2.5 py-1.5 rounded-lg cursor-pointer transition-colors ${
          isSelected
            ? 'bg-teal-500/15 text-teal-700 dark:text-teal-300 font-semibold'
            : 'text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800/60'
        }`}
        style={{ paddingLeft: `${Math.max(10, level * 16 + 10)}px` }}
      >
        <div className="flex items-center gap-1.5 min-w-0">
          {hasChildren ? (
            <button
              onClick={toggleExpand}
              className="p-0.5 rounded hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200"
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
            <span className="w-3.5 inline-block" />
          )}

          {/* Folder Icon with Custom Color */}
          <span
            className="w-4 h-4 flex-shrink-0 flex items-center justify-center"
            style={{ color: folder.color || '#178582' }}
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
                  ? 'bg-teal-500/20 text-teal-800 dark:text-teal-200 font-bold'
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400'
              }`}
            >
              {folder.totalCount}
            </span>
          )}

          {/* Context menu toggle button */}
          <div className="relative">
            <button
              onClick={handleMenuClick}
              className="opacity-0 group-hover:opacity-100 p-1 rounded hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-opacity"
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
                  className="fixed inset-0 z-40"
                  onClick={(e) => {
                    e.stopPropagation();
                    setMenuOpen(false);
                  }}
                />
                <div
                  className="absolute right-0 top-full mt-1 w-40 py-1 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-lg shadow-xl z-50 text-xs"
                  onClick={(e) => e.stopPropagation()}
                >
                  <button
                    onClick={() => {
                      setMenuOpen(false);
                      onCreateSubfolder(folder);
                    }}
                    className="w-full text-left px-3 py-1.5 text-slate-700 dark:text-slate-200 hover:bg-teal-50 dark:hover:bg-teal-900/30 flex items-center gap-2"
                  >
                    <svg className="w-3.5 h-3.5 text-teal-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                    </svg>
                    New Subfolder
                  </button>
                  <button
                    onClick={() => {
                      setMenuOpen(false);
                      onEditFolder(folder);
                    }}
                    className="w-full text-left px-3 py-1.5 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 flex items-center gap-2"
                  >
                    <svg className="w-3.5 h-3.5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"
                      />
                    </svg>
                    Edit Folder
                  </button>
                  <div className="border-t border-slate-100 dark:border-slate-800 my-1" />
                  <button
                    onClick={() => {
                      setMenuOpen(false);
                      onDeleteFolder(folder);
                    }}
                    className="w-full text-left px-3 py-1.5 text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/30 flex items-center gap-2"
                  >
                    <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                      />
                    </svg>
                    Delete Folder...
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Recursive children */}
      {hasChildren && isExpanded && (
        <div className="space-y-0.5 mt-0.5">
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

