import { describe, it, expect } from 'vitest';
import {
  validateRegisterInput,
  validateLoginInput,
  validateProfileInput,
  validateKnowledgeItemInput,
} from './validation';

describe('Authentication Form Validation', () => {
  describe('validateRegisterInput', () => {
    it('accepts valid registration input', () => {
      const result = validateRegisterInput('Katherine Johnson', 'katherine@nasa.gov', 'OrbitalMath1962!', 'OrbitalMath1962!');
      expect(result.isValid).toBe(true);
      expect(result.error).toBeNull();
    });

    it('rejects short full name', () => {
      const result = validateRegisterInput('A', 'valid@email.com', 'ValidPassword123!', 'ValidPassword123!');
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('at least 2 characters');
    });

    it('rejects invalid email address', () => {
      const result = validateRegisterInput('Valid Name', 'invalid-email', 'ValidPassword123!', 'ValidPassword123!');
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('valid academic or personal email');
    });

    it('rejects short password (< 8 chars)', () => {
      const result = validateRegisterInput('Valid Name', 'valid@email.com', 'short', 'short');
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('at least 8 characters');
    });

    it('rejects mismatched passwords', () => {
      const result = validateRegisterInput('Valid Name', 'valid@email.com', 'PasswordOne123!', 'PasswordTwo123!');
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('Passwords do not match');
    });
  });

  describe('validateLoginInput', () => {
    it('accepts valid credentials', () => {
      const result = validateLoginInput('scholar@mit.edu', 'SecretPassword!');
      expect(result.isValid).toBe(true);
      expect(result.error).toBeNull();
    });

    it('rejects empty email', () => {
      const result = validateLoginInput('', 'SomePassword');
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('enter your email address');
    });

    it('rejects empty password', () => {
      const result = validateLoginInput('scholar@mit.edu', '');
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('enter your password');
    });
  });

  describe('validateProfileInput', () => {
    it('accepts valid full name', () => {
      const result = validateProfileInput('Margaret Hamilton');
      expect(result.isValid).toBe(true);
    });

    it('rejects whitespace-only or short name', () => {
      const result = validateProfileInput('   ');
      expect(result.isValid).toBe(false);
    });
  });

  describe('validateKnowledgeItemInput', () => {
    it('accepts valid Note input', () => {
      const result = validateKnowledgeItemInput({
        type: 'NOTE',
        title: 'Binary Search Invariant',
        content: 'Maintain search space [low, high] where target is guaranteed to lie.',
        category: 'Algorithms',
      });
      expect(result.isValid).toBe(true);
      expect(result.error).toBeNull();
    });

    it('accepts valid Link input', () => {
      const result = validateKnowledgeItemInput({
        type: 'LINK',
        title: 'MDN Web Docs',
        url: 'https://developer.mozilla.org',
        category: 'Web Dev',
      });
      expect(result.isValid).toBe(true);
      expect(result.error).toBeNull();
    });

    it('rejects missing or invalid type', () => {
      const result = validateKnowledgeItemInput({
        type: 'INVALID',
        title: 'Some title',
        content: 'Some content',
      });
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('Type must be either NOTE or LINK');
    });

    it('rejects empty title', () => {
      const result = validateKnowledgeItemInput({
        type: 'NOTE',
        title: '   ',
        content: 'Some content',
      });
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('Title is required');
    });

    it('rejects title longer than 200 characters', () => {
      const result = validateKnowledgeItemInput({
        type: 'NOTE',
        title: 'A'.repeat(201),
        content: 'Some content',
      });
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('200 characters');
    });

    it('rejects Note with empty content', () => {
      const result = validateKnowledgeItemInput({
        type: 'NOTE',
        title: 'Valid Title',
        content: '   ',
      });
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('Note content is required');
    });

    it('rejects Link with empty URL', () => {
      const result = validateKnowledgeItemInput({
        type: 'LINK',
        title: 'Valid Title',
        url: '   ',
      });
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('URL is required');
    });

    it('rejects Link with unsafe or non-http protocol', () => {
      const result = validateKnowledgeItemInput({
        type: 'LINK',
        title: 'Malicious Link',
        url: 'javascript:alert(1)',
      });
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('http:// or https://');
    });

    it('rejects category exceeding 50 characters', () => {
      const result = validateKnowledgeItemInput({
        type: 'NOTE',
        title: 'Valid Title',
        content: 'Valid content',
        category: 'C'.repeat(51),
      });
      expect(result.isValid).toBe(false);
      expect(result.error).toContain('50 characters');
    });
  });
});


