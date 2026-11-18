/**
 * Validation and formatting utilities for AI Intelligence & Memory System (Milestone 9)
 */

export const MEMORY_CATEGORIES = [
  { value: 'LEARNING', label: 'Learning Memory', color: 'teal', desc: 'Topics studied, weak concepts, and revision history' },
  { value: 'TECHNICAL', label: 'Technical Memory', color: 'navy', desc: 'Languages, frameworks, tools, and technical skills' },
  { value: 'CAREER', label: 'Career Memory', color: 'gold', desc: 'Target roles, interview goals, and career readiness' },
  { value: 'KNOWLEDGE', label: 'Knowledge Memory', color: 'purple', desc: 'Notes, key concepts from documents, and vault citations' },
  { value: 'INTERACTION', label: 'Interaction Memory', color: 'slate', desc: 'User preferences, previous decisions, and project context' },
];

export const MEMORY_IMPORTANCES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

export const MEMORY_TYPES = ['FACT', 'PREFERENCE', 'GOAL', 'WEAKNESS', 'STRENGTH', 'PROJECT_CONTEXT', 'DECISION'];

export function validateMemoryForm(formData) {
  const errors = {};

  if (!formData.category) {
    errors.category = 'Memory category is required.';
  }

  if (!formData.content || !formData.content.trim()) {
    errors.content = 'Memory content cannot be blank.';
  } else if (formData.content.trim().length < 5) {
    errors.content = 'Memory content must be at least 5 characters.';
  } else if (formData.content.trim().length > 2000) {
    errors.content = 'Memory content cannot exceed 2000 characters.';
  }

  // Security check: reject passwords or api keys
  const sensitiveRegex = /(password|secret[_-]?key|bearer\s+[a-zA-Z0-9_.-]{20,}|private[_-]?key|api[_-]?key)/i;
  if (sensitiveRegex.test(formData.content)) {
    errors.content = 'Security warning: Memory cannot contain passwords, secret keys, or private credentials.';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}

export function sanitizeChatInput(input) {
  if (!input) return '';
  let cleaned = input.trim();
  if (cleaned.length > 2000) {
    cleaned = cleaned.substring(0, 2000);
  }
  return cleaned;
}

export function getCategoryBadgeStyle(category) {
  switch (category) {
    case 'LEARNING':
      return 'bg-brand-teal/15 text-brand-teal dark:text-brand-teal-light border-brand-teal/30';
    case 'TECHNICAL':
      return 'bg-blue-500/15 text-blue-600 dark:text-blue-400 border-blue-500/30';
    case 'CAREER':
      return 'bg-brand-gold/15 text-amber-700 dark:text-brand-gold border-brand-gold/30';
    case 'KNOWLEDGE':
      return 'bg-purple-500/15 text-purple-600 dark:text-purple-400 border-purple-500/30';
    case 'INTERACTION':
    default:
      return 'bg-slate-500/15 text-slate-700 dark:text-slate-300 border-slate-500/30';
  }
}

export function getImportanceBadgeStyle(importance) {
  switch (importance) {
    case 'CRITICAL':
      return 'bg-red-500/15 text-red-600 dark:text-red-400 border-red-500/30';
    case 'HIGH':
      return 'bg-amber-500/15 text-amber-600 dark:text-amber-400 border-amber-500/30';
    case 'LOW':
      return 'bg-slate-500/15 text-slate-500 border-slate-500/30';
    case 'MEDIUM':
    default:
      return 'bg-brand-teal/15 text-brand-teal dark:text-brand-teal-light border-brand-teal/30';
  }
}

export function getPriorityBadgeStyle(priority) {
  switch (priority) {
    case 'URGENT':
      return {
        label: 'Urgent',
        color: 'bg-rose-500/15 text-rose-500 border-rose-500/30',
      };
    case 'HIGH':
      return {
        label: 'High',
        color: 'bg-amber-500/15 text-amber-500 border-amber-500/30',
      };
    case 'LOW':
      return {
        label: 'Low',
        color: 'bg-slate-500/15 text-slate-400 border-slate-500/30',
      };
    case 'MEDIUM':
    default:
      return {
        label: 'Medium',
        color: 'bg-blue-500/15 text-blue-400 border-blue-500/30',
      };
  }
}
