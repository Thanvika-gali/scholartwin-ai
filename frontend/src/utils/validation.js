/**
 * Client-side validation helpers for AI ScholarTwin authentication forms.
 */

export function validateRegisterInput(fullName, email, password, confirmPassword) {
  const trimmedName = (fullName || '').trim();
  const trimmedEmail = (email || '').trim();

  if (!trimmedName || trimmedName.length < 2) {
    return { isValid: false, error: 'Full name must be at least 2 characters long.' };
  }

  if (!trimmedEmail || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
    return { isValid: false, error: 'Please provide a valid academic or personal email address.' };
  }

  if (!password || password.length < 8) {
    return { isValid: false, error: 'Password must be at least 8 characters long.' };
  }

  if (password !== confirmPassword) {
    return { isValid: false, error: 'Passwords do not match. Please verify both fields.' };
  }

  return { isValid: true, error: null };
}

export function validateLoginInput(email, password) {
  const trimmedEmail = (email || '').trim();

  if (!trimmedEmail) {
    return { isValid: false, error: 'Please enter your email address.' };
  }

  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
    return { isValid: false, error: 'Please enter a valid email address.' };
  }

  if (!password) {
    return { isValid: false, error: 'Please enter your password.' };
  }

  return { isValid: true, error: null };
}

export function validateProfileInput(fullName) {
  const trimmed = (fullName || '').trim();
  if (!trimmed || trimmed.length < 2) {
    return { isValid: false, error: 'Full name must be at least 2 characters long.' };
  }
  return { isValid: true, error: null };
}

/**
 * Validates Knowledge Item (Note or Link) client inputs
 */
export function validateKnowledgeItemInput(data = {}) {
  const { type, title, url, content, category } = data;

  if (type !== 'NOTE' && type !== 'LINK') {
    return { isValid: false, error: 'Type must be either NOTE or LINK.' };
  }

  const trimmedTitle = (title || '').trim();
  if (!trimmedTitle) {
    return { isValid: false, error: 'Title is required (maximum 200 characters).' };
  }
  if (trimmedTitle.length > 200) {
    return { isValid: false, error: 'Title must be 200 characters or fewer.' };
  }

  if (category && category.trim().length > 50) {
    return { isValid: false, error: 'Category must be 50 characters or fewer.' };
  }

  if (type === 'LINK') {
    const trimmedUrl = (url || '').trim();
    if (!trimmedUrl) {
      return { isValid: false, error: 'URL is required for learning links.' };
    }
    if (trimmedUrl.length > 1000) {
      return { isValid: false, error: 'URL must be 1000 characters or fewer.' };
    }
    try {
      const parsed = new URL(trimmedUrl);
      if (parsed.protocol !== 'http:' && parsed.protocol !== 'https:') {
        return { isValid: false, error: 'URL must start with http:// or https://' };
      }
    } catch {
      return { isValid: false, error: 'Please enter a valid, complete web URL (e.g., https://example.com).' };
    }
  } else {
    // NOTE
    const trimmedContent = (content || '').trim();
    if (!trimmedContent) {
      return { isValid: false, error: 'Note content is required.' };
    }
    if (trimmedContent.length > 10000) {
      return { isValid: false, error: 'Note content must be 10,000 characters or fewer.' };
    }
  }

  return { isValid: true, error: null };
}


