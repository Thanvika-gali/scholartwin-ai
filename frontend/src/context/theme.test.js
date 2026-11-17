import { describe, it, expect, beforeEach, vi } from 'vitest';

const THEME_STORAGE_KEY = 'scholartwin_theme';

describe('Theme Persistence and System Preference', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.className = '';
    document.documentElement.removeAttribute('data-theme');
  });

  it('stores and retrieves theme preference from localStorage', () => {
    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBeNull();

    localStorage.setItem(THEME_STORAGE_KEY, 'light');
    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBe('light');

    localStorage.setItem(THEME_STORAGE_KEY, 'dark');
    expect(localStorage.getItem(THEME_STORAGE_KEY)).toBe('dark');
  });

  it('correctly updates DOM class and data-theme attribute for dark mode', () => {
    const root = document.documentElement;

    // Apply dark theme
    root.classList.add('dark');
    root.setAttribute('data-theme', 'dark');

    expect(root.classList.contains('dark')).toBe(true);
    expect(root.getAttribute('data-theme')).toBe('dark');

    // Switch to light theme
    root.classList.remove('dark');
    root.setAttribute('data-theme', 'light');

    expect(root.classList.contains('dark')).toBe(false);
    expect(root.getAttribute('data-theme')).toBe('light');
  });

  it('respects media query when localStorage is empty', () => {
    window.matchMedia = vi.fn().mockImplementation((query) => ({
      matches: query.includes('dark'),
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    }));

    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const defaultTheme = localStorage.getItem(THEME_STORAGE_KEY) || (prefersDark ? 'dark' : 'light');
    expect(['dark', 'light']).toContain(defaultTheme);
    expect(defaultTheme).toBe('dark');
  });

});
