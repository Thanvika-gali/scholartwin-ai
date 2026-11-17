import React from 'react';
import Button from '../ui/Button';

export default function DuplicatePromptModal({
  isOpen,
  onClose,
  duplicateInfo,
  onUploadAsVersion,
  onUploadSeparate,
  onViewExisting,
}) {
  if (!isOpen || !duplicateInfo) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div
        className="bg-white dark:bg-slate-900 rounded-2xl max-w-md w-full shadow-2xl border border-amber-300 dark:border-amber-700/60 overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-6 py-4 bg-amber-50/70 dark:bg-amber-950/40 border-b border-amber-200 dark:border-amber-800/80 flex items-center gap-3">
          <div className="w-9 h-9 rounded-full bg-amber-100 dark:bg-amber-900/60 text-amber-600 dark:text-amber-300 flex items-center justify-center flex-shrink-0">
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
              Identical Document Detected
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              SHA-256 fingerprint matches an existing library document.
            </p>
          </div>
        </div>

        <div className="p-6 space-y-4 text-xs">
          <div className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700">
            <div className="font-semibold text-slate-800 dark:text-slate-200 mb-1">
              Existing Document:
            </div>
            <div className="text-teal-600 dark:text-teal-400 font-bold truncate">
              {duplicateInfo.existingDocumentTitle || duplicateInfo.filename}
            </div>
            <div className="text-[11px] text-slate-500 mt-1 flex justify-between">
              <span>Folder: {duplicateInfo.existingDocumentFolder || 'Smart Inbox'}</span>
              <span>v{duplicateInfo.existingVersionNumber || 1}</span>
            </div>
          </div>

          <p className="text-slate-600 dark:text-slate-300 leading-relaxed">
            How would you like to handle this upload? You can view the existing file, link this as a new version, or keep a separate duplicate copy.
          </p>

          <div className="space-y-2 pt-2">
            <button
              type="button"
              onClick={onUploadAsVersion}
              className="w-full text-left p-3 rounded-xl border border-teal-200 dark:border-teal-900 bg-teal-50/50 dark:bg-teal-950/30 hover:bg-teal-100/60 dark:hover:bg-teal-900/50 transition-colors"
            >
              <div className="font-bold text-teal-800 dark:text-teal-200">
                Upload as Next Version (v{(duplicateInfo.existingVersionNumber || 1) + 1})
              </div>
              <div className="text-[11px] text-slate-500 dark:text-slate-400 mt-0.5">
                Maintains document history and groups this file under the original version chain.
              </div>
            </button>

            <button
              type="button"
              onClick={onUploadSeparate}
              className="w-full text-left p-3 rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800/60 transition-colors"
            >
              <div className="font-bold text-slate-800 dark:text-slate-200">
                Upload as Separate Duplicate
              </div>
              <div className="text-[11px] text-slate-500 mt-0.5">
                Upload as a distinct standalone resource in your current library folder.
              </div>
            </button>

            <button
              type="button"
              onClick={onViewExisting}
              className="w-full text-left p-3 rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800/60 transition-colors"
            >
              <div className="font-bold text-slate-800 dark:text-slate-200">
                Cancel Upload & View Existing Document
              </div>
              <div className="text-[11px] text-slate-500 mt-0.5">
                Open the existing copy and do not upload a new file.
              </div>
            </button>
          </div>

          <div className="flex justify-end pt-2 border-t border-slate-100 dark:border-slate-800">
            <Button type="button" variant="ghost" onClick={onClose}>
              Cancel
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

