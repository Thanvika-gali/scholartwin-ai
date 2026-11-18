import { describe, it, expect } from 'vitest';
import {
  validateIntervalSequence,
  parseIntervalSequence,
  validateStudyDuration,
  formatStudyTime,
  calculateReadinessTier,
  isFatiguedAllocation,
} from './learningValidation';

describe('Milestone 8 Learning Engine Utilities', () => {
  describe('validateIntervalSequence & parseIntervalSequence', () => {
    it('validates proper comma-separated interval sequences', () => {
      expect(validateIntervalSequence('1,3,7,14,30')).toBe(true);
      expect(validateIntervalSequence(' 1, 2, 5, 10 ')).toBe(true);
      expect(validateIntervalSequence('1')).toBe(true);
    });

    it('rejects invalid sequences with zero, negatives, or non-numeric tokens', () => {
      expect(validateIntervalSequence('')).toBe(false);
      expect(validateIntervalSequence('1,0,5')).toBe(false);
      expect(validateIntervalSequence('1,-3,7')).toBe(false);
      expect(validateIntervalSequence('1,a,7')).toBe(false);
      expect(validateIntervalSequence(null)).toBe(false);
    });

    it('parses valid interval sequences into integers or falls back gracefully', () => {
      expect(parseIntervalSequence('1,3,7,14,30')).toEqual([1, 3, 7, 14, 30]);
      expect(parseIntervalSequence('invalid')).toEqual([1, 3, 7, 14, 30]);
    });
  });

  describe('validateStudyDuration', () => {
    it('validates reasonable study durations', () => {
      expect(validateStudyDuration(45).valid).toBe(true);
      expect(validateStudyDuration(120).valid).toBe(true);
    });

    it('flags non-numeric or non-positive durations', () => {
      expect(validateStudyDuration(0).valid).toBe(false);
      expect(validateStudyDuration(-10).valid).toBe(false);
      expect(validateStudyDuration('abc').valid).toBe(false);
    });

    it('guards against excessive study fatigue beyond 12 hours', () => {
      expect(validateStudyDuration(721).valid).toBe(false);
      expect(validateStudyDuration(720).valid).toBe(true);
    });
  });

  describe('formatStudyTime', () => {
    it('formats minutes into human readable hours and minutes', () => {
      expect(formatStudyTime(0)).toBe('0m');
      expect(formatStudyTime(45)).toBe('45m');
      expect(formatStudyTime(60)).toBe('1h');
      expect(formatStudyTime(125)).toBe('2h 5m');
    });
  });

  describe('calculateReadinessTier', () => {
    it('assigns correct benchmarks and badge variants based on percentage', () => {
      expect(calculateReadinessTier(85).tier).toBe('Tier-1 Ready');
      expect(calculateReadinessTier(65).tier).toBe('Competitive');
      expect(calculateReadinessTier(45).tier).toBe('Developing');
      expect(calculateReadinessTier(20).tier).toBe('Foundation Phase');
    });
  });

  describe('isFatiguedAllocation', () => {
    it('detects when planned minutes exceed daily available allowance', () => {
      expect(isFatiguedAllocation(90, 60)).toBe(true);
      expect(isFatiguedAllocation(60, 60)).toBe(false);
      expect(isFatiguedAllocation(45, 60)).toBe(false);
      expect(isFatiguedAllocation(60, 0)).toBe(false);
    });
  });
});
