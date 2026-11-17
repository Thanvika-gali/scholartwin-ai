/**
 * AI ScholarTwin - Career Validation & Helper Utilities
 * Milestone 7: Resume & Career Readiness Intelligence
 */

export const MAX_RESUME_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB

export const ALLOWED_RESUME_EXTENSIONS = ['.pdf', '.docx'];

export const ALLOWED_RESUME_MIME_TYPES = [
  'application/pdf',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/msword', // legacy doc occasionally uploaded
];

/**
 * Validate resume file before upload
 * @param {File} file
 * @returns {{ valid: boolean, error: string | null }}
 */
export function validateResumeFile(file) {
  if (!file) {
    return { valid: false, error: 'No file selected' };
  }

  const name = file.name || '';
  const lowerName = name.toLowerCase();
  const hasValidExt = ALLOWED_RESUME_EXTENSIONS.some((ext) => lowerName.endsWith(ext));

  if (!hasValidExt) {
    return {
      valid: false,
      error: 'Unsupported file type. Please upload a PDF (.pdf) or Word document (.docx).',
    };
  }

  if (file.size > MAX_RESUME_FILE_SIZE_BYTES) {
    const sizeMb = (file.size / (1024 * 1024)).toFixed(1);
    return {
      valid: false,
      error: `Resume file size (${sizeMb} MB) exceeds the maximum allowed limit of 20 MB. Please choose a smaller file.`,
    };
  }

  if (file.type && !ALLOWED_RESUME_MIME_TYPES.includes(file.type)) {
    // If browser supplies a MIME type that doesn't match
    return {
      valid: false,
      error: 'Invalid file MIME type. Only PDF and DOCX documents are accepted.',
    };
  }

  return { valid: true, error: null };
}

/**
 * Get visual score variant for 100-point score
 * @param {number} score
 * @returns {'success' | 'teal' | 'warning' | 'error' | 'default'}
 */
export function getScoreVariant(score) {
  if (score == null || isNaN(score)) return 'default';
  if (score >= 80) return 'success';
  if (score >= 65) return 'teal';
  if (score >= 45) return 'warning';
  return 'error';
}

/**
 * Get category display label
 */
export function formatSkillCategory(category) {
  const map = {
    PROGRAMMING_LANGUAGES: 'Programming Languages',
    FRONTEND: 'Frontend Development',
    BACKEND: 'Backend Development',
    DATABASES: 'Databases & Storage',
    CLOUD_DEVOPS: 'Cloud & DevOps',
    AI_ML: 'AI & Machine Learning',
    TESTING: 'Testing & QA',
    TOOLS: 'Developer Tools',
    CORE_CS: 'Core Computer Science',
    OTHER: 'Other Skills',
  };
  return map[category] || category || 'Other';
}

/**
 * Format proficiency level
 */
export function formatProficiency(proficiency) {
  const map = {
    BEGINNER: 'Beginner',
    INTERMEDIATE: 'Intermediate',
    ADVANCED: 'Advanced',
    EXPERT: 'Expert',
  };
  return map[proficiency] || proficiency || 'Beginner';
}

/**
 * Format skill source
 */
export function formatSkillSource(source) {
  const map = {
    RESUME: 'Resume Parsed',
    PROJECT: 'Project Evidence',
    CERTIFICATION: 'Certification',
    USER_ENTERED: 'Manual Entry',
  };
  return map[source] || source || 'Manual Entry';
}

/**
 * Format action priority
 */
export function formatActionPriority(priority) {
  const map = {
    HIGH: { label: 'High Priority', variant: 'error' },
    MEDIUM: { label: 'Medium Priority', variant: 'warning' },
    LOW: { label: 'Low Priority', variant: 'default' },
  };
  return map[priority] || { label: priority || 'Medium', variant: 'default' };
}

/**
 * Format action category
 */
export function formatActionCategory(category) {
  const map = {
    SKILL_BUILDING: 'Skill Building',
    RESUME_IMPROVEMENT: 'Resume Polish',
    PROJECT_CREATION: 'Project Creation',
    CERTIFICATION: 'Certification',
    DSA_PRACTICE: 'DSA Practice',
    GENERAL: 'General Goal',
  };
  return map[category] || category || 'General';
}

/**
 * Format action status
 */
export function formatActionStatus(status) {
  const map = {
    NOT_STARTED: { label: 'Not Started', variant: 'default' },
    IN_PROGRESS: { label: 'In Progress', variant: 'teal' },
    COMPLETED: { label: 'Completed', variant: 'success' },
  };
  return map[status] || { label: status || 'Pending', variant: 'default' };
}
