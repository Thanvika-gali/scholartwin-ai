import { describe, it, expect } from 'vitest';
import {
  validateMemoryForm,
  sanitizeChatInput,
  getCategoryBadgeStyle,
  getImportanceBadgeStyle,
  MEMORY_CATEGORIES,
} from './intelligenceValidation';

describe('intelligenceValidation', () => {
  it('validates required memory category and content', () => {
    const invalid = validateMemoryForm({ category: '', content: '' });
    expect(invalid.isValid).toBe(false);
    expect(invalid.errors.category).toBeDefined();
    expect(invalid.errors.content).toBeDefined();
  });

  it('rejects passwords and private keys', () => {
    const leaked = validateMemoryForm({
      category: 'TECHNICAL',
      content: 'My secret password is pass123456',
    });
    expect(leaked.isValid).toBe(false);
    expect(leaked.errors.content).toContain('Security warning');
  });

  it('accepts clean valid memory', () => {
    const valid = validateMemoryForm({
      category: 'LEARNING',
      content: 'Mastered Red-Black Tree invariant rotations and balancing rules.',
    });
    expect(valid.isValid).toBe(true);
    expect(Object.keys(valid.errors).length).toBe(0);
  });

  it('sanitizes oversized chat input', () => {
    const longInput = 'A'.repeat(2500);
    const sanitized = sanitizeChatInput(longInput);
    expect(sanitized.length).toBe(2000);
  });

  it('returns valid badge styles for all categories and importances', () => {
    MEMORY_CATEGORIES.forEach((cat) => {
      const style = getCategoryBadgeStyle(cat.value);
      expect(typeof style).toBe('string');
      expect(style.length).toBeGreaterThan(0);
    });

    ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].forEach((imp) => {
      const style = getImportanceBadgeStyle(imp);
      expect(typeof style).toBe('string');
      expect(style.length).toBeGreaterThan(0);
    });
  });
});
