import React, { useState } from 'react';
import Button from '../ui/Button';

export default function FolderDeleteModal({
  isOpen,
  onClose,
  folder,
  allFolders = [],
  onConfirmDelete,
}) {
  const [reassignOption, setReassignOption] = useState('INBOX'); // 'INBOX' | 'PARENT' | 'TARGET_FOLDER'
  const [targetFolderId, setTargetFolderId] = useState('');
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState('');

  if (!isOpen || !folder) return null;

  const validTargetFolders = allFolders.filter((f) => f.id !== folder.id);

  const handleDelete = async () => {
    setIsDeleting(true);
    setError('');
    try {
      await onConfirmDelete(
        folder.id,
        reassignOption,
        reassignOption === 'TARGET_FOLDER' && targetFolderId ? Number(targetFolderId) : null
      );
      onClose();
    } catch (err) {
      setError(err.message || 'Failed to delete folder.');
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div
        className="bg-white dark:bg-slate-900 rounded-2xl max-w-md w-full shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex items-center gap-3">
          <div className="w-9 h-9 rounded-full bg-rose-100 dark:bg-rose-950/50 text-rose-600 dark:text-rose-400 flex items-center justify-center flex-shrink-0">
            <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
              />
            </svg>
          </div>
          <div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              Delete Folder "{folder.name}"
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Your resources inside will be safely preserved.
            </p>
          </div>
        </div>

        <div className="p-6 space-y-4 text-sm">
          {error && (
            <div className="p-3 text-xs bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-700 dark:text-rose-300 rounded-lg">
              {error}
            </div>
          )}

          <p className="text-xs text-slate-600 dark:text-slate-300">
            Where would you like to move the documents, notes, and links currently in this folder?
          </p>

          <div className="space-y-2">
            <label className="flex items-start gap-2.5 p-2.5 rounded-lg border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/50 cursor-pointer">
              <input
                type="radio"
                name="reassignOption"
                value="INBOX"
                checked={reassignOption === 'INBOX'}
                onChange={() => setReassignOption('INBOX')}
                className="mt-0.5 text-teal-600 focus:ring-teal-500"
              />
              <div>
                <span className="font-semibold text-xs text-slate-900 dark:text-white block">
                  Move to Smart Inbox (Recommended)
                </span>
                <span className="text-[11px] text-slate-500 dark:text-slate-400">
                  Unassign folder and place items in Smart Inbox for future organization.
                </span>
              </div>
            </label>

            {folder.parentId && (
              <label className="flex items-start gap-2.5 p-2.5 rounded-lg border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/50 cursor-pointer">
                <input
                  type="radio"
                  name="reassignOption"
                  value="PARENT"
                  checked={reassignOption === 'PARENT'}
                  onChange={() => setReassignOption('PARENT')}
                  className="mt-0.5 text-teal-600 focus:ring-teal-500"
                />
                <div>
                  <span className="font-semibold text-xs text-slate-900 dark:text-white block">
                    Move up to Parent Folder
                  </span>
                  <span className="text-[11px] text-slate-500 dark:text-slate-400">
                    Reassign all contents to the parent folder directory.
                  </span>
                </div>
              </label>
            )}

            {validTargetFolders.length > 0 && (
              <label className="flex items-start gap-2.5 p-2.5 rounded-lg border border-slate-200 dark:border-slate-800 hover:bg-slate-50 dark:hover:bg-slate-800/50 cursor-pointer">
                <input
                  type="radio"
                  name="reassignOption"
                  value="TARGET_FOLDER"
                  checked={reassignOption === 'TARGET_FOLDER'}
                  onChange={() => setReassignOption('TARGET_FOLDER')}
                  className="mt-0.5 text-teal-600 focus:ring-teal-500"
                />
                <div className="flex-1">
                  <span className="font-semibold text-xs text-slate-900 dark:text-white block mb-1">
                    Reassign to another folder
                  </span>
                  {reassignOption === 'TARGET_FOLDER' && (
                    <select
                      value={targetFolderId}
                      onChange={(e) => setTargetFolderId(e.target.value)}
                      className="w-full mt-1 px-2.5 py-1.5 text-xs rounded border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-teal-500"
                    >
                      <option value="">Select destination folder...</option>
                      {validTargetFolders.map((f) => (
                        <option key={f.id} value={f.id}>
                          {f.name}
                        </option>
                      ))}
                    </select>
                  )}
                </div>
              </label>
            )}
          </div>

          <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-100 dark:border-slate-800">
            <Button type="button" variant="ghost" onClick={onClose} disabled={isDeleting}>
              Cancel
            </Button>
            <Button
              type="button"
              variant="danger"
              onClick={handleDelete}
              disabled={isDeleting || (reassignOption === 'TARGET_FOLDER' && !targetFolderId)}
            >
              {isDeleting ? 'Deleting...' : 'Safely Delete Folder'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

