import { describe, it, expect } from 'vitest';
import {
  validateResumeFile,
  getScoreVariant,
  formatSkillCategory,
  formatProficiency,
  formatSkillSource,
  formatActionPriority,
  formatActionCategory,
  formatActionStatus,
  MAX_RESUME_FILE_SIZE_BYTES,
} from './careerValidation';

describe('Career Validation Utilities', () => {
  describe('validateResumeFile', () => {
    it('returns error when file is null or undefined', () => {
      expect(validateResumeFile(null).valid).toBe(false);
      expect(validateResumeFile(undefined).valid).toBe(false);
    });

    it('accepts valid PDF file', () => {
      const file = {
        name: 'my_resume.pdf',
        size: 1024 * 500, // 500 KB
        type: 'application/pdf',
      };
      const result = validateResumeFile(file);
      expect(result.valid).toBe(true);
      expect(result.error).toBeNull();
    });

    it('accepts valid DOCX file', () => {
      const file = {
        name: 'candidate_cv.docx',
        size: 1024 * 1200, // 1.2 MB
        type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      };
      const result = validateResumeFile(file);
      expect(result.valid).toBe(true);
      expect(result.error).toBeNull();
    });

    it('rejects unsupported file extension', () => {
      const file = {
        name: 'script.exe',
        size: 1024 * 100,
        type: 'application/x-msdownload',
      };
      const result = validateResumeFile(file);
      expect(result.valid).toBe(false);
      expect(result.error).toContain('Unsupported file type');
    });

    it('rejects files exceeding 20MB limit', () => {
      const file = {
        name: 'huge_portfolio.pdf',
        size: MAX_RESUME_FILE_SIZE_BYTES + 1024,
        type: 'application/pdf',
      };
      const result = validateResumeFile(file);
      expect(result.valid).toBe(false);
      expect(result.error).toContain('exceeds the maximum allowed limit of 20 MB');
    });
  });

  describe('getScoreVariant', () => {
    it('maps scores accurately to badge variants', () => {
      expect(getScoreVariant(90)).toBe('success');
      expect(getScoreVariant(80)).toBe('success');
      expect(getScoreVariant(75)).toBe('teal');
      expect(getScoreVariant(65)).toBe('teal');
      expect(getScoreVariant(50)).toBe('warning');
      expect(getScoreVariant(40)).toBe('error');
      expect(getScoreVariant(null)).toBe('default');
    });
  });

  describe('formatters', () => {
    it('formats skill categories properly', () => {
      expect(formatSkillCategory('PROGRAMMING_LANGUAGES')).toBe('Programming Languages');
      expect(formatSkillCategory('CORE_CS')).toBe('Core Computer Science');
      expect(formatSkillCategory('CLOUD_DEVOPS')).toBe('Cloud & DevOps');
      expect(formatSkillCategory('UNKNOWN_CAT')).toBe('UNKNOWN_CAT');
    });

    it('formats proficiency levels properly', () => {
      expect(formatProficiency('EXPERT')).toBe('Expert');
      expect(formatProficiency('BEGINNER')).toBe('Beginner');
    });

    it('formats skill sources properly', () => {
      expect(formatSkillSource('RESUME')).toBe('Resume Parsed');
      expect(formatSkillSource('PROJECT')).toBe('Project Evidence');
    });

    it('formats action priority', () => {
      expect(formatActionPriority('HIGH').variant).toBe('error');
      expect(formatActionPriority('MEDIUM').variant).toBe('warning');
      expect(formatActionPriority('LOW').variant).toBe('default');
    });

    it('formats action category', () => {
      expect(formatActionCategory('DSA_PRACTICE')).toBe('DSA Practice');
      expect(formatActionCategory('RESUME_IMPROVEMENT')).toBe('Resume Polish');
    });

    it('formats action status', () => {
      expect(formatActionStatus('COMPLETED').variant).toBe('success');
      expect(formatActionStatus('IN_PROGRESS').variant).toBe('teal');
      expect(formatActionStatus('NOT_STARTED').variant).toBe('default');
    });
  });
});
