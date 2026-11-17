import { describe, it, expect, beforeEach } from 'vitest';
import {
  getStoredToken,
  setStoredToken,
  removeStoredToken,
} from './api';

describe('Frontend API Token Storage', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('stores and retrieves token from localStorage', () => {
    expect(getStoredToken()).toBeNull();
    setStoredToken('test_jwt_token_123');
    expect(getStoredToken()).toBe('test_jwt_token_123');
  });

  it('removes token properly', () => {
    setStoredToken('token_to_remove');
    removeStoredToken();
    expect(getStoredToken()).toBeNull();
  });

  it('calling setStoredToken with null removes token', () => {
    setStoredToken('token_active');
    setStoredToken(null);
    expect(getStoredToken()).toBeNull();
  });
});

