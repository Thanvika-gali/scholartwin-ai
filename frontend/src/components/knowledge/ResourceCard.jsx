import React from 'react';
import Badge from '../ui/Badge';

export default function ResourceCard({
  resource,
  isSelected,
  onToggleSelect,
  onSelectResource,
  onToggleFavorite,
  onTogglePin,
  onToggleArchive,
  onDeleteResource,
  onMoveResource,
}) {
  const isDocument = resource.resourceType === 'DOCUMENT';
  const isLink = resource.resourceType === 'LINK';
  const isNote = resource.resourceType === 'NOTE';

  const typeConfig = {
    DOCUMENT: {
      badge: 'teal',
      label: resource.fileExtension ? resource.fileExtension.toUpperCase() : 'DOC',
      icon: (
        <svg className="w-4 h-4 text-teal-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
      ),
    },
    NOTE: {
      badge: 'amber',
      label: 'NOTE',
      icon: (
        <svg className="w-4 h-4 text-amber-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
          />
        </svg>
      ),
    },
    LINK: {
      badge: 'sky',
      label: 'LINK',
      icon: (
        <svg className="w-4 h-4 text-sky-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
          />
        </svg>
      ),
    },
  }[resource.resourceType] || {
    badge: 'slate',
    label: resource.resourceType,
    icon: null,
  };

  return (
    <div
      onClick={() => onSelectResource(resource)}
      className={`group relative flex flex-col justify-between p-4 rounded-2xl bg-white dark:bg-slate-900 border transition-all duration-200 cursor-pointer hover:shadow-md ${
        isSelected
          ? 'border-teal-500 ring-2 ring-teal-500/20 bg-teal-50/20 dark:bg-teal-950/20'
          : 'border-slate-200/80 dark:border-slate-800 hover:border-teal-500/40 dark:hover:border-teal-500/40'
      }`}
    >
      <div>
        {/* Top bar with Select Checkbox, Type Badge, and Actions */}
        <div className="flex items-center justify-between gap-2 mb-2.5">
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={isSelected}
              onChange={(e) => {
                e.stopPropagation();
                onToggleSelect(resource);
              }}
              className="w-4 h-4 rounded text-teal-600 focus:ring-teal-500 border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
            <div className="flex items-center gap-1.5">
              <span className="p-1 rounded-md bg-slate-100 dark:bg-slate-800">
                {typeConfig.icon}
              </span>
              <Badge variant={typeConfig.badge} size="sm">
                {typeConfig.label}
              </Badge>
              {resource.versionNumber > 1 && (
                <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-indigo-50 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300 font-bold">
                  v{resource.versionNumber}
                </span>
              )}
            </div>
          </div>

          <div className="flex items-center gap-1">
            {/* Pinned Indicator / Toggle */}
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onTogglePin(resource);
              }}
              className={`p-1 rounded-lg transition-colors ${
                resource.isPinned
                  ? 'text-teal-600 dark:text-teal-400 bg-teal-50 dark:bg-teal-950/50'
                  : 'text-slate-300 dark:text-slate-600 hover:text-slate-500'
              }`}
              title={resource.isPinned ? 'Unpin' : 'Pin to top'}
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z"
                />
              </svg>
            </button>

            {/* Favorite Star */}
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onToggleFavorite(resource);
              }}
              className={`p-1 rounded-lg transition-colors ${
                resource.isFavorite
                  ? 'text-amber-400 hover:text-amber-500'
                  : 'text-slate-300 dark:text-slate-600 hover:text-amber-400'
              }`}
              title={resource.isFavorite ? 'Remove from favorites' : 'Add to favorites'}
            >
              <svg className="w-4 h-4" fill={resource.isFavorite ? 'currentColor' : 'none'} viewBox="0 0 20 20" stroke="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
            </button>
          </div>
        </div>

        {/* Title */}
        <h4 className="text-sm font-bold text-slate-900 dark:text-white line-clamp-1 group-hover:text-teal-600 dark:group-hover:text-teal-400 transition-colors">
          {resource.title}
        </h4>

        {/* Summary or Description preview */}
        <p className="text-xs text-slate-500 dark:text-slate-400 line-clamp-2 mt-1 min-h-[2rem]">
          {resource.summary || resource.description || resource.content || 'No preview available.'}
        </p>

        {/* Tags */}
        {resource.tags && resource.tags.length > 0 && (
          <div className="flex flex-wrap gap-1 mt-2.5">
            {resource.tags.slice(0, 3).map((tag) => (
              <span
                key={tag}
                className="text-[10px] px-1.5 py-0.2 rounded bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300"
              >
                #{tag}
              </span>
            ))}
            {resource.tags.length > 3 && (
              <span className="text-[10px] text-slate-400">
                +{resource.tags.length - 3}
              </span>
            )}
          </div>
        )}
      </div>

      {/* Footer Info: Folder & Details */}
      <div className="pt-3 mt-3 border-t border-slate-100 dark:border-slate-800/80 flex items-center justify-between text-[11px] text-slate-400 dark:text-slate-500">
        <div className="flex items-center gap-1.5 truncate">
          <svg className="w-3.5 h-3.5 text-teal-600 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z" />
          </svg>
          <span className="truncate max-w-[130px] font-medium text-slate-600 dark:text-slate-300">
            {resource.folderName || 'Smart Inbox'}
          </span>
        </div>

        <div className="flex items-center gap-1 flex-shrink-0">
          {resource.pageCount > 0 && (
            <span>{resource.pageCount} {resource.pageCount === 1 ? 'page' : 'pages'} • </span>
          )}
          {resource.fileSizeFormatted && (
            <span>{resource.fileSizeFormatted}</span>
          )}
        </div>
      </div>
    </div>
  );
}

