import React, { useState } from 'react';
import Button from '../ui/Button';

export default function BulkActionToolbar({
  selectedCount,
  folders = [],
  onClearSelection,
  onMoveToFolder,
  onFavorite,
  onArchive,
  onDelete,
}) {
  const [targetFolderId, setTargetFolderId] = useState('');
  const [showFolderSelect, setShowFolderSelect] = useState(false);

  if (selectedCount === 0) return null;

  const handleApplyMove = () => {
    onMoveToFolder(targetFolderId ? Number(targetFolderId) : null);
    setShowFolderSelect(false);
  };

  return (
    <div className="fixed bottom-6 left-1/2 transform -translate-x-1/2 z-40 bg-slate-900/95 dark:bg-slate-800/95 text-white backdrop-blur-md px-5 py-3 rounded-2xl shadow-2xl border border-slate-700/80 flex items-center gap-4 animate-slide-up">
      <div className="flex items-center gap-2 pr-3 border-r border-slate-700">
        <span className="w-5 h-5 rounded-full bg-teal-500 text-slate-950 text-xs font-bold flex items-center justify-center">
          {selectedCount}
        </span>
        <span className="text-xs font-semibold whitespace-nowrap">
          {selectedCount} {selectedCount === 1 ? 'item' : 'items'} selected
        </span>
      </div>

      <div className="flex items-center gap-2">
        {/* Move to folder */}
        {showFolderSelect ? (
          <div className="flex items-center gap-1.5 bg-slate-800 dark:bg-slate-700 p-1 rounded-lg">
            <select
              value={targetFolderId}
              onChange={(e) => setTargetFolderId(e.target.value)}
              className="px-2 py-1 text-xs rounded bg-slate-900 text-white border border-slate-600 focus:outline-none focus:ring-1 focus:ring-teal-500"
            >
              <option value="">(Smart Inbox / No Folder)</option>
              {folders.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
            <button
              onClick={handleApplyMove}
              className="px-2 py-1 text-xs bg-teal-600 hover:bg-teal-500 rounded font-semibold text-white"
            >
              Move
            </button>
            <button
              onClick={() => setShowFolderSelect(false)}
              className="px-1.5 py-1 text-xs text-slate-400 hover:text-white"
            >
              ✕
            </button>
          </div>
        ) : (
          <button
            onClick={() => setShowFolderSelect(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-slate-800 dark:hover:bg-slate-700 transition-colors"
          >
            <svg className="w-3.5 h-3.5 text-teal-400" fill="currentColor" viewBox="0 0 20 20">
              <path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z" />
            </svg>
            Move to Folder
          </button>
        )}

        {/* Favorite */}
        <button
          onClick={onFavorite}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-slate-800 dark:hover:bg-slate-700 transition-colors"
        >
          <svg className="w-3.5 h-3.5 text-amber-400" fill="currentColor" viewBox="0 0 20 20">
            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
          </svg>
          Favorite
        </button>

        {/* Archive */}
        <button
          onClick={onArchive}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium hover:bg-slate-800 dark:hover:bg-slate-700 transition-colors"
        >
          <svg className="w-3.5 h-3.5 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4"
            />
          </svg>
          Archive
        </button>

        {/* Delete */}
        <button
          onClick={onDelete}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium text-rose-400 hover:bg-rose-950/40 transition-colors"
        >
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
            />
          </svg>
          Delete
        </button>
      </div>

      <div className="pl-3 border-l border-slate-700">
        <button
          onClick={onClearSelection}
          className="text-xs text-slate-400 hover:text-white underline"
        >
          Deselect
        </button>
      </div>
    </div>
  );
}

