import React, { useState, useEffect } from 'react';
import { createKnowledgeItem, updateKnowledgeItem } from '../services/api';
import Modal from './ui/Modal';
import Button from './ui/Button';
import { Input, Textarea } from './ui/Input';
import Alert from './ui/Alert';

const COMMON_CATEGORIES = [
  'Data Structures',
  'Algorithms',
  'Database & SQL',
  'System Design',
  'Web Development',
  'Machine Learning',
  'Operating Systems',
  'Computer Networks',
];

export default function KnowledgeItemModal({
  isOpen,
  onClose,
  onSaved,
  initialItem = null,
  defaultType = 'NOTE',
  folderId = null,
  allFolders = [],
}) {
  const isEditing = Boolean(initialItem && initialItem.id);

  const [type, setType] = useState(defaultType);
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState('');
  const [content, setContent] = useState('');
  const [url, setUrl] = useState('');
  const [targetFolderId, setTargetFolderId] = useState(folderId ? String(folderId) : '');

  const [validationError, setValidationError] = useState('');
  const [serverError, setServerError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (isOpen) {
      if (initialItem) {
        setType(initialItem.type || 'NOTE');
        setTitle(initialItem.title || '');
        setCategory(initialItem.category || '');
        setContent(initialItem.content || '');
        setUrl(initialItem.url || '');
        setTargetFolderId(initialItem.folderId ? String(initialItem.folderId) : '');
      } else {
        setType(defaultType || 'NOTE');
        setTitle('');
        setCategory('');
        setContent('');
        setUrl('');
        setTargetFolderId(folderId ? String(folderId) : '');
      }
      setValidationError('');
      setServerError('');
    }
  }, [isOpen, initialItem, defaultType, folderId]);

  if (!isOpen) return null;

  const validateForm = () => {
    setValidationError('');
    setServerError('');

    const trimmedTitle = title.trim();
    if (!trimmedTitle) {
      setValidationError('Title is required (maximum 200 characters).');
      return false;
    }
    if (trimmedTitle.length > 200) {
      setValidationError('Title must be 200 characters or fewer.');
      return false;
    }

    if (type === 'LINK') {
      const trimmedUrl = url.trim();
      if (!trimmedUrl) {
        setValidationError('URL is required for learning resource links.');
        return false;
      }
      if (trimmedUrl.length > 1000) {
        setValidationError('URL must be 1000 characters or fewer.');
        return false;
      }
      try {
        const parsed = new URL(trimmedUrl);
        if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
          setValidationError('URL must start with http:// or https://');
          return false;
        }
      } catch {
        setValidationError('Please enter a valid, complete web URL (e.g., https://example.com).');
        return false;
      }
    } else {
      // NOTE
      const trimmedContent = content.trim();
      if (!trimmedContent) {
        setValidationError('Note content is required.');
        return false;
      }
      if (trimmedContent.length > 10000) {
        setValidationError('Note content must be 10,000 characters or fewer.');
        return false;
      }
    }

    if (category && category.trim().length > 50) {
      setValidationError('Category must be 50 characters or fewer.');
      return false;
    }

    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    setServerError('');

    try {
      const payload = {
        type,
        title: title.trim(),
        category: category.trim() || null,
        content: content.trim() || null,
        url: type === 'LINK' ? url.trim() : null,
        folderId: targetFolderId ? Number(targetFolderId) : null,
      };

      let result;
      if (isEditing) {
        result = await updateKnowledgeItem(initialItem.id, payload);
      } else {
        result = await createKnowledgeItem(payload);
      }

      if (onSaved) {
        onSaved(result);
      }
      onClose();
    } catch (err) {
      setServerError(err.message || 'Failed to save knowledge item. Please check your input.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const icon =
    type === 'LINK' ? (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
        />
      </svg>
    ) : (
      <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
        />
      </svg>
    );

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => !isSubmitting && onClose()}
      title={
        isEditing
          ? 'Edit Knowledge Item'
          : type === 'LINK'
          ? 'Save Learning Resource'
          : 'Create Academic Note'
      }
      subtitle={
        type === 'LINK'
          ? 'Bookmark official documentation, papers, tutorials, or repositories.'
          : 'Capture concepts, revision formulas, code notes, and key takeaways.'
      }
      icon={icon}
      footer={
        <>
          <Button
            variant="secondary"
            onClick={onClose}
            disabled={isSubmitting}
          >
            Cancel
          </Button>

          <Button
            type="submit"
            form="knowledge-item-form"
            variant="primary"
            isLoading={isSubmitting}
          >
            {isEditing ? 'Save Changes' : type === 'LINK' ? 'Add Resource' : 'Create Note'}
          </Button>
        </>
      }
    >
      <form id="knowledge-item-form" onSubmit={handleSubmit} className="space-y-5">
        {/* Error Alerts */}
        {(validationError || serverError) && (
          <Alert variant="error" onClose={() => { setValidationError(''); setServerError(''); }}>
            {validationError || serverError}
          </Alert>
        )}

        {/* Type Selector Tabs */}
        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-2">
            Item Classification
          </label>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={() => setType('NOTE')}
              className={`flex items-center justify-center gap-2 p-3 rounded-xl border text-xs sm:text-sm font-medium transition-all cursor-pointer ${
                type === 'NOTE'
                  ? 'bg-brand-gold/15 border-brand-gold text-brand-gold font-semibold shadow-sm'
                  : 'bg-bg-surface-elevated/40 border-border-default text-text-muted hover:text-text-primary hover:bg-bg-surface-hover'
              }`}
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                />
              </svg>
              <span>Academic Note</span>
            </button>

            <button
              type="button"
              onClick={() => setType('LINK')}
              className={`flex items-center justify-center gap-2 p-3 rounded-xl border text-xs sm:text-sm font-medium transition-all cursor-pointer ${
                type === 'LINK'
                  ? 'bg-brand-teal/15 border-brand-teal text-brand-teal font-semibold shadow-sm'
                  : 'bg-bg-surface-elevated/40 border-border-default text-text-muted hover:text-text-primary hover:bg-bg-surface-hover'
              }`}
            >
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
                />
              </svg>
              <span>Learning Link</span>
            </button>
          </div>
        </div>

        {/* Title */}
        <Input
          label="Title"
          required
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          maxLength={200}
          placeholder={
            type === 'LINK'
              ? 'e.g. Stanford CS144 Computer Networking Notes'
              : 'e.g. Dynamic Programming Memoization Principles'
          }
          helperText={`${title.length}/200 characters`}
        />

        {/* URL (If Link) */}
        {type === 'LINK' && (
          <Input
            label="Resource Web URL"
            type="url"
            required
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            maxLength={1000}
            placeholder="https://ocw.mit.edu/courses/6-006-algorithms/"
            helperText={`${url.length}/1000 characters (must start with https:// or http://)`}
          />
        )}

        {/* Category with quick picks */}
        <div>
          <Input
            label="Category / Subject"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            maxLength={50}
            placeholder="e.g. Algorithms, System Design, Operating Systems"
            helperText={`${category.length}/50 characters`}
          />
          <div className="flex flex-wrap gap-1.5 mt-2">
            {COMMON_CATEGORIES.map((cat) => (
              <button
                key={cat}
                type="button"
                onClick={() => setCategory(cat)}
                className={`text-[11px] px-2.5 py-1 rounded-lg border transition-colors cursor-pointer ${
                  category === cat
                    ? 'bg-brand-teal/15 border-brand-teal text-brand-teal font-medium'
                    : 'bg-bg-surface-elevated/60 border-border-default text-text-muted hover:text-text-primary hover:bg-bg-surface-hover'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        {/* Content (Note body or link description) */}
        <Textarea
          label={type === 'LINK' ? 'Notes & Key Takeaways (Optional)' : 'Note Content'}
          required={type === 'NOTE'}
          rows={type === 'LINK' ? 3 : 7}
          maxLength={10000}
          characterCount={content.length}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder={
            type === 'LINK'
              ? 'Key highlights or notes regarding this resource...'
              : 'Write academic summaries, algorithm complexities, formulas, or code snippets...'
          }
        />

        {/* Folder Selection */}
        {allFolders.length > 0 && (
          <div>
            <label className="block text-xs font-semibold text-text-secondary mb-1">
              Destination Folder (or leave unassigned for Smart Inbox)
            </label>
            <select
              value={targetFolderId}
              onChange={(e) => setTargetFolderId(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-xl border border-border-default bg-bg-surface text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              <option value="">(Smart Inbox / Unassigned)</option>
              {allFolders.map((f) => (
                <option key={f.id} value={f.id}>
                  📁 {f.name}
                </option>
              ))}
            </select>
          </div>
        )}
      </form>
    </Modal>
  );
}
