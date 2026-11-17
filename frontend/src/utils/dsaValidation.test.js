import { describe, it, expect } from 'vitest';
import {
  isValidHttpUrl,
  validateProblemForm,
  validateAttemptForm,
  validateRevisionForm,
} from './dsaValidation';
import { detectPlatformFromUrl } from '../constants/dsaConstants';

describe('DSA Validation Utilities', () => {
  describe('isValidHttpUrl', () => {
    it('accepts valid http and https urls', () => {
      expect(isValidHttpUrl('https://leetcode.com/problems/two-sum/')).toBe(true);
      expect(isValidHttpUrl('http://example.com/test')).toBe(true);
      expect(isValidHttpUrl('https://practice.geeksforgeeks.org/problems/sample')).toBe(true);
    });

    it('rejects invalid or malformed urls', () => {
      expect(isValidHttpUrl('')).toBe(false);
      expect(isValidHttpUrl(null)).toBe(false);
      expect(isValidHttpUrl(undefined)).toBe(false);
      expect(isValidHttpUrl('not-a-url')).toBe(false);
      expect(isValidHttpUrl('ftp://example.com')).toBe(false);
      expect(isValidHttpUrl('javascript:alert(1)')).toBe(false);
    });
  });

  describe('detectPlatformFromUrl', () => {
    it('detects LeetCode URLs accurately', () => {
      expect(detectPlatformFromUrl('https://leetcode.com/problems/reverse-linked-list/')).toBe('LeetCode');
    });

    it('detects GeeksforGeeks URLs accurately', () => {
      expect(detectPlatformFromUrl('https://practice.geeksforgeeks.org/problems/subarray-with-given-sum')).toBe('GeeksforGeeks');
    });

    it('detects HackerRank URLs accurately', () => {
      expect(detectPlatformFromUrl('https://www.hackerrank.com/challenges/simple-array-sum')).toBe('HackerRank');
    });

    it('detects Codeforces URLs accurately', () => {
      expect(detectPlatformFromUrl('https://codeforces.com/problemset/problem/4/A')).toBe('Codeforces');
    });

    it('defaults to Other for unrecognized hosts', () => {
      expect(detectPlatformFromUrl('https://myblog.com/dsa-notes')).toBe('Other');
      expect(detectPlatformFromUrl('')).toBe('Other');
    });
  });

  describe('validateProblemForm', () => {
    it('passes for valid problem data', () => {
      const validForm = {
        title: 'Valid Problem',
        problemUrl: 'https://leetcode.com/problems/valid/',
        primaryTopic: 'Arrays',
        difficulty: 'MEDIUM',
      };
      const result = validateProblemForm(validForm);
      expect(result.isValid).toBe(true);
      expect(Object.keys(result.errors).length).toBe(0);
    });

    it('fails when title is missing or blank', () => {
      const form = {
        title: '   ',
        problemUrl: 'https://leetcode.com/problems/test/',
        primaryTopic: 'Arrays',
        difficulty: 'EASY',
      };
      const result = validateProblemForm(form);
      expect(result.isValid).toBe(false);
      expect(result.errors.title).toBeDefined();
    });

    it('fails when problemUrl is invalid', () => {
      const form = {
        title: 'Invalid URL Problem',
        problemUrl: 'not_a_valid_url',
        primaryTopic: 'Binary Trees',
        difficulty: 'HARD',
      };
      const result = validateProblemForm(form);
      expect(result.isValid).toBe(false);
      expect(result.errors.problemUrl).toBeDefined();
    });

    it('fails when primaryTopic is missing', () => {
      const form = {
        title: 'Missing Topic',
        problemUrl: 'https://leetcode.com/problems/test/',
        primaryTopic: '',
        difficulty: 'MEDIUM',
      };
      const result = validateProblemForm(form);
      expect(result.isValid).toBe(false);
      expect(result.errors.primaryTopic).toBeDefined();
    });
  });

  describe('validateAttemptForm', () => {
    it('passes for valid attempt data', () => {
      const valid = {
        attemptDate: '2026-11-04',
        outcome: 'SUCCESS',
        timeSpentMinutes: 30,
      };
      const result = validateAttemptForm(valid);
      expect(result.isValid).toBe(true);
    });

    it('rejects missing date', () => {
      const form = {
        attemptDate: '',
        outcome: 'SUCCESS',
      };
      const result = validateAttemptForm(form);
      expect(result.isValid).toBe(false);
      expect(result.errors.attemptDate).toBeDefined();
    });

    it('rejects negative or impossible timeSpentMinutes', () => {
      const form = {
        attemptDate: '2026-11-04',
        outcome: 'SUCCESS',
        timeSpentMinutes: -5,
      };
      const result = validateAttemptForm(form);
      expect(result.isValid).toBe(false);
      expect(result.errors.timeSpentMinutes).toBeDefined();
    });
  });

  describe('validateRevisionForm', () => {
    it('passes when scheduledDate is provided', () => {
      const result = validateRevisionForm({ scheduledDate: '2026-11-10' });
      expect(result.isValid).toBe(true);
    });

    it('fails when scheduledDate is missing', () => {
      const result = validateRevisionForm({ scheduledDate: '' });
      expect(result.isValid).toBe(false);
      expect(result.errors.scheduledDate).toBeDefined();
    });
  });
});

