/**
 * Utility functions and validators for Milestone 8: Personalized Learning & Revision Engine
 */

export function validateIntervalSequence(intervalStr) {
  if (!intervalStr || typeof intervalStr !== 'string') return false;
  const parts = intervalStr.split(',').map((p) => p.trim());
  if (parts.length === 0) return false;
  return parts.every((p) => /^\d+$/.test(p) && parseInt(p, 10) > 0);
}

export function parseIntervalSequence(intervalStr) {
  if (!validateIntervalSequence(intervalStr)) {
    return [1, 3, 7, 14, 30];
  }
  return intervalStr.split(',').map((p) => parseInt(p.trim(), 10));
}

export function validateStudyDuration(minutes) {
  const num = Number(minutes);
  if (isNaN(num)) return { valid: false, error: 'Duration must be a valid number' };
  if (num <= 0) return { valid: false, error: 'Duration must be greater than 0 minutes' };
  if (num > 720) return { valid: false, error: 'Duration cannot exceed 12 hours (720 minutes)' };
  return { valid: true, error: null };
}

export function formatStudyTime(minutes) {
  if (!minutes || minutes <= 0) return '0m';
  const hrs = Math.floor(minutes / 60);
  const remMins = minutes % 60;
  if (hrs > 0 && remMins > 0) return `${hrs}h ${remMins}m`;
  if (hrs > 0) return `${hrs}h`;
  return `${remMins}m`;
}

export function calculateReadinessTier(percentage) {
  if (percentage >= 80) return { tier: 'Tier-1 Ready', variant: 'success', description: 'Exceeds top-tier technical benchmarks' };
  if (percentage >= 60) return { tier: 'Competitive', variant: 'primary', description: 'Strong foundational readiness with minor gaps' };
  if (percentage >= 40) return { tier: 'Developing', variant: 'warning', description: 'Core principles established; requires focus on key gaps' };
  return { tier: 'Foundation Phase', variant: 'neutral', description: 'Requires structured curriculum progression' };
}

export function isFatiguedAllocation(plannedMinutes, availableMinutes) {
  if (!availableMinutes || availableMinutes <= 0) return false;
  return plannedMinutes > availableMinutes;
}
