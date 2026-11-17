/**
 * Document upload validation utilities
 */

export const MAX_DOCUMENT_FILE_SIZE_BYTES = 100 * 1024 * 1024; // 100 MB

export const SUPPORTED_DOCUMENT_EXTENSIONS = ['pdf', 'docx'];

export function formatFileSize(bytes) {
  if (bytes === 0 || !bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}

export function validateDocumentFile(file) {
  if (!file) {
    return {
      valid: false,
      error: 'Please select a document file to upload.',
      format: null,
    };
  }

  if (file.size === 0) {
    return {
      valid: false,
      error: 'The selected file is empty (0 bytes).',
      format: null,
    };
  }

  if (file.size > MAX_DOCUMENT_FILE_SIZE_BYTES) {
    return {
      valid: false,
      error: `File size (${formatFileSize(file.size)}) exceeds the maximum allowed limit of 100 MB.`,
      format: null,
    };
  }

  const name = file.name || '';
  const lastDot = name.lastIndexOf('.');
  if (lastDot === -1 || lastDot === name.length - 1) {
    return {
      valid: false,
      error: 'Unsupported file format. Please upload a PDF (.pdf) or Word document (.docx).',
      format: null,
    };
  }

  const ext = name.substring(lastDot + 1).toLowerCase();
  if (ext === 'pdf') {
    return {
      valid: true,
      error: null,
      format: 'PDF',
    };
  }

  if (ext === 'docx') {
    return {
      valid: true,
      error: null,
      format: 'DOCX',
    };
  }

  return {
    valid: false,
    error: `Unsupported file type (.${ext}). Only PDF (.pdf) and Word (.docx) files are supported.`,
    format: null,
  };
}

