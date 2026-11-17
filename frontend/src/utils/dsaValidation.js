/**
 * DSA Tracker validation utilities
 */

export function isValidHttpUrl(string) {
  if (!string || typeof string !== 'string') return false;
  try {
    const url = new URL(string.trim());
    return url.protocol === 'http:' || url.protocol === 'https:';
  } catch (_) {
    return false;
  }
}

export function validateProblemForm(formData) {
  const errors = {};

  if (!formData.title || !formData.title.trim()) {
    errors.title = 'Problem title is required';
  } else if (formData.title.trim().length > 200) {
    errors.title = 'Title must be 200 characters or fewer';
  }

  if (!formData.problemUrl || !formData.problemUrl.trim()) {
    errors.problemUrl = 'Problem URL is required';
  } else if (!isValidHttpUrl(formData.problemUrl)) {
    errors.problemUrl = 'Must be a valid URL starting with http:// or https://';
  } else if (formData.problemUrl.trim().length > 500) {
    errors.problemUrl = 'Problem URL must be 500 characters or fewer';
  }

  if (!formData.primaryTopic || !formData.primaryTopic.trim()) {
    errors.primaryTopic = 'Primary topic is required';
  }

  if (!formData.difficulty) {
    errors.difficulty = 'Difficulty level is required';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}

export function validateAttemptForm(formData) {
  const errors = {};

  if (!formData.attemptDate) {
    errors.attemptDate = 'Attempt date is required';
  }

  if (!formData.outcome) {
    errors.outcome = 'Outcome (SUCCESS or FAILED) is required';
  }

  if (formData.timeSpentMinutes !== null && formData.timeSpentMinutes !== undefined && formData.timeSpentMinutes !== '') {
    const minutes = Number(formData.timeSpentMinutes);
    if (isNaN(minutes) || minutes < 0 || minutes > 1440) {
      errors.timeSpentMinutes = 'Time spent must be between 0 and 1440 minutes';
    }
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}

export function validateRevisionForm(formData) {
  const errors = {};

  if (!formData.scheduledDate) {
    errors.scheduledDate = 'Scheduled date is required';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}

