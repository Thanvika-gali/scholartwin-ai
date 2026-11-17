/**
 * Validation utilities for Smart Knowledge Hub 2.0
 */

export function validateFolderName(name) {
  if (!name || !name.trim()) {
    return { valid: false, error: 'Folder name cannot be blank.' };
  }
  const trimmed = name.trim();
  if (trimmed.length > 150) {
    return { valid: false, error: 'Folder name must not exceed 150 characters.' };
  }
  return { valid: true, value: trimmed };
}

export function validateCollectionName(name) {
  if (!name || !name.trim()) {
    return { valid: false, error: 'Collection name cannot be blank.' };
  }
  const trimmed = name.trim();
  if (trimmed.length > 100) {
    return { valid: false, error: 'Collection name must not exceed 100 characters.' };
  }
  return { valid: true, value: trimmed };
}

export function validateRule({ ruleName, targetFolderId, fieldName, operator, conditionValue }) {
  if (!ruleName || !ruleName.trim()) {
    return { valid: false, error: 'Rule name is required.' };
  }
  if (!targetFolderId) {
    return { valid: false, error: 'Target folder must be selected.' };
  }
  const validFields = ['FILENAME', 'TITLE', 'CONTENT', 'FILE_TYPE'];
  if (!validFields.includes(fieldName)) {
    return { valid: false, error: `Invalid field: ${fieldName}` };
  }
  const validOperators = ['CONTAINS', 'STARTS_WITH', 'ENDS_WITH', 'EQUALS', 'REGEX'];
  if (!validOperators.includes(operator)) {
    return { valid: false, error: `Invalid operator: ${operator}` };
  }
  if (!conditionValue || !conditionValue.trim()) {
    return { valid: false, error: 'Condition value cannot be blank.' };
  }
  return { valid: true };
}

export function formatTag(tag) {
  if (!tag) return '';
  return tag.trim().toLowerCase().replace(/[^a-z0-9_-]/g, '');
}

