import { describe, it, expect } from 'vitest';
import {
  validateFolderName,
  validateCollectionName,
  validateRule,
  formatTag,
} from './knowledgeValidation';

describe('Knowledge Hub 2.0 Validation Utilities', () => {
  describe('validateFolderName', () => {
    it('rejects blank folder names', () => {
      expect(validateFolderName('')).toEqual({ valid: false, error: 'Folder name cannot be blank.' });
      expect(validateFolderName('   ')).toEqual({ valid: false, error: 'Folder name cannot be blank.' });
    });

    it('rejects overly long folder names', () => {
      const longName = 'A'.repeat(151);
      expect(validateFolderName(longName).valid).toBe(false);
    });

    it('accepts valid folder names', () => {
      const res = validateFolderName('  Computer Science 101  ');
      expect(res.valid).toBe(true);
      expect(res.value).toBe('Computer Science 101');
    });
  });

  describe('validateCollectionName', () => {
    it('validates collection name limits', () => {
      expect(validateCollectionName('').valid).toBe(false);
      expect(validateCollectionName('DBMS Exam Prep').valid).toBe(true);
    });
  });

  describe('validateRule', () => {
    it('validates full rule structure', () => {
      const invalid = validateRule({
        ruleName: '',
        targetFolderId: 1,
        fieldName: 'FILENAME',
        operator: 'CONTAINS',
        conditionValue: 'dbms',
      });
      expect(invalid.valid).toBe(false);

      const valid = validateRule({
        ruleName: 'Auto DBMS',
        targetFolderId: 5,
        fieldName: 'FILENAME',
        operator: 'CONTAINS',
        conditionValue: 'dbms',
      });
      expect(valid.valid).toBe(true);
    });

    it('rejects invalid field or operator', () => {
      const badField = validateRule({
        ruleName: 'Test',
        targetFolderId: 1,
        fieldName: 'UNKNOWN',
        operator: 'CONTAINS',
        conditionValue: 'val',
      });
      expect(badField.valid).toBe(false);
    });
  });

  describe('formatTag', () => {
    it('cleans and formats tags', () => {
      expect(formatTag(' #Computer-Science! ')).toBe('computer-science');
      expect(formatTag('SQL_2024')).toBe('sql_2024');
      expect(formatTag('')).toBe('');
    });
  });
});

