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
  viewMode = 'grid', // 'grid' | 'list'
}) {
  const isDocument = resource.resourceType === 'DOCUMENT';
  const isLink = resource.resourceType === 'LINK';
  const isNote = resource.resourceType === 'NOTE';

  const typeConfig = {
    DOCUMENT: {
      badge: 'teal',
      label: resource.fileExtension ? resource.fileExtension.toUpperCase() : 'PDF/DOCX',
      icon: (
        <svg className="w-4 h-4 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
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
      badge: 'gold',
      label: 'NOTE',
      icon: (
        <svg className="w-4 h-4 text-brand-gold" fill="none" viewBox="0 0 24 24" stroke="currentColor">
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
      badge: 'teal',
      label: 'LINK',
      icon: (
        <svg className="w-4 h-4 text-brand-teal" fill="none" viewBox="0 0 24 24" stroke="currentColor">
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
    badge: 'default',
    label: resource.resourceType,
    icon: null,
  };

  // List View Rendering
  if (viewMode === 'list') {
    return (
      <div
        onClick={() => onSelectResource(resource)}
        className={`group flex items-center justify-between p-3 sm:p-3.5 rounded-xl bg-bg-surface border transition-all duration-150 cursor-pointer ${
          isSelected
            ? 'border-brand-teal ring-2 ring-brand-teal/20 bg-brand-teal/5'
            : 'border-border-default hover:border-brand-teal/50 hover:bg-bg-surface-hover/50'
        }`}
      >
        <div className="flex items-center gap-3 min-w-0 flex-1">
          <input
            type="checkbox"
            checked={isSelected}
            onChange={(e) => {
              e.stopPropagation();
              onToggleSelect(resource);
            }}
            className="w-4 h-4 rounded text-brand-teal focus:ring-brand-teal border-border-default bg-bg-surface cursor-pointer shrink-0"
          />

          <span className="p-1.5 rounded-lg bg-bg-surface-elevated shrink-0">
            {typeConfig.icon}
          </span>

          <div className="min-w-0 flex-1">
            <div className="flex items-center gap-2">
              <h4 className="text-xs sm:text-sm font-bold text-text-primary truncate group-hover:text-brand-teal transition-colors">
                {resource.title}
              </h4>
              <Badge variant={typeConfig.badge} size="xs">
                {typeConfig.label}
              </Badge>
              {resource.versionNumber > 1 && (
                <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-brand-teal/20 text-brand-teal font-bold shrink-0">
                  v{resource.versionNumber}
                </span>
              )}
            </div>
            <p className="text-[11px] text-text-muted truncate mt-0.5 max-w-xl">
              {resource.summary || resource.description || resource.content || 'Academic resource'}
            </p>
          </div>
        </div>

        {/* List View Metadata & Controls */}
        <div className="flex items-center gap-3 shrink-0 ml-3 text-xs">
          <span className="hidden md:inline-block text-[11px] text-text-muted font-medium max-w-[120px] truncate">
            {resource.folderName || 'Smart Inbox'}
          </span>

          {resource.fileSizeFormatted && (
            <span className="hidden sm:inline-block text-[11px] text-text-muted font-mono">
              {resource.fileSizeFormatted}
            </span>
          )}

          <div className="flex items-center gap-1">
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onTogglePin(resource);
              }}
              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                resource.isPinned
                  ? 'text-brand-teal bg-brand-teal/10'
                  : 'text-text-muted hover:text-text-primary'
              }`}
              title={resource.isPinned ? 'Unpin' : 'Pin to top'}
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
              </svg>
            </button>

            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onToggleFavorite(resource);
              }}
              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                resource.isFavorite ? 'text-amber-400' : 'text-text-muted hover:text-amber-400'
              }`}
              title={resource.isFavorite ? 'Favorited' : 'Add favorite'}
            >
              <svg className="w-4 h-4" fill={resource.isFavorite ? 'currentColor' : 'none'} viewBox="0 0 20 20" stroke="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Grid View Rendering
  return (
    <div
      onClick={() => onSelectResource(resource)}
      className={`group relative flex flex-col justify-between p-4 sm:p-5 rounded-2xl bg-bg-surface border transition-all duration-200 cursor-pointer shadow-card hover:shadow-card-hover ${
        isSelected
          ? 'border-brand-teal ring-2 ring-brand-teal/20 bg-brand-teal/5'
          : 'border-border-default hover:border-brand-teal/50'
      }`}
    >
      <div>
        {/* Top bar with Select Checkbox, Type Badge, and Actions */}
        <div className="flex items-center justify-between gap-2 mb-3">
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={isSelected}
              onChange={(e) => {
                e.stopPropagation();
                onToggleSelect(resource);
              }}
              className="w-4 h-4 rounded text-brand-teal focus:ring-brand-teal border-border-default bg-bg-surface cursor-pointer"
            />
            <div className="flex items-center gap-1.5">
              <span className="p-1 rounded-md bg-bg-surface-elevated text-brand-teal">
                {typeConfig.icon}
              </span>
              <Badge variant={typeConfig.badge} size="xs">
                {typeConfig.label}
              </Badge>
              {resource.versionNumber > 1 && (
                <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-brand-teal/20 text-brand-teal font-bold">
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
              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                resource.isPinned
                  ? 'text-brand-teal bg-brand-teal/10'
                  : 'text-text-muted hover:text-text-primary'
              }`}
              title={resource.isPinned ? 'Unpin' : 'Pin to top'}
            >
              <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 5a2 2 0 012-2h10a2 2 0 012 2v16l-7-3.5L5 21V5z" />
              </svg>
            </button>

            {/* Favorite Star */}
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onToggleFavorite(resource);
              }}
              className={`p-1.5 rounded-lg transition-colors cursor-pointer ${
                resource.isFavorite ? 'text-amber-400' : 'text-text-muted hover:text-amber-400'
              }`}
              title={resource.isFavorite ? 'Favorited' : 'Add to favorites'}
            >
              <svg className="w-4 h-4" fill={resource.isFavorite ? 'currentColor' : 'none'} viewBox="0 0 20 20" stroke="currentColor">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
              </svg>
            </button>
          </div>
        </div>

        {/* Title */}
        <h4 className="text-sm font-bold text-text-primary line-clamp-1 group-hover:text-brand-teal transition-colors">
          {resource.title}
        </h4>

        {/* Summary or Description preview */}
        <p className="text-xs text-text-muted line-clamp-2 mt-1.5 min-h-[2.2rem] leading-relaxed">
          {resource.summary || resource.description || resource.content || 'Academic vault item.'}
        </p>

        {/* Tags */}
        {resource.tags && resource.tags.length > 0 && (
          <div className="flex flex-wrap gap-1 mt-2.5">
            {resource.tags.slice(0, 3).map((tag) => (
              <span
                key={tag}
                className="text-[10px] px-1.5 py-0.5 rounded-md bg-bg-surface-elevated text-text-secondary border border-border-subtle"
              >
                #{tag}
              </span>
            ))}
            {resource.tags.length > 3 && (
              <span className="text-[10px] text-text-muted self-center">
                +{resource.tags.length - 3}
              </span>
            )}
          </div>
        )}
      </div>

      {/* Footer Info: Folder, Pages, Size */}
      <div className="pt-3 mt-3 border-t border-border-subtle flex items-center justify-between text-[11px] text-text-muted">
        <div className="flex items-center gap-1.5 truncate">
          <svg className="w-3.5 h-3.5 text-brand-teal shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path d="M2 6a2 2 0 012-2h5l2 2h5a2 2 0 012 2v6a2 2 0 01-2 2H4a2 2 0 01-2-2V6z" />
          </svg>
          <span className="truncate max-w-[130px] font-medium text-text-secondary">
            {resource.folderName || 'Smart Inbox'}
          </span>
        </div>

        <div className="flex items-center gap-1 shrink-0 font-medium font-mono text-[10px]">
          {resource.pageCount > 0 && (
            <span>{resource.pageCount}p • </span>
          )}
          {resource.fileSizeFormatted && (
            <span>{resource.fileSizeFormatted}</span>
          )}
        </div>
      </div>
    </div>
  );
}
