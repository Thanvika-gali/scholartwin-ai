import { describe, it, expect } from 'vitest';
import { validateDocumentFile, formatFileSize, MAX_DOCUMENT_FILE_SIZE_BYTES } from './documentValidation';

describe('documentValidation utility', () => {
  it('validates PDF files correctly', () => {
    const validPdf = { name: 'research_paper.pdf', size: 1024 * 500 };
    const result = validateDocumentFile(validPdf);
    expect(result.valid).toBe(true);
    expect(result.format).toBe('PDF');
    expect(result.error).toBeNull();
  });

  it('validates DOCX files correctly', () => {
    const validDocx = { name: 'thesis_draft.docx', size: 1024 * 1024 * 2 };
    const result = validateDocumentFile(validDocx);
    expect(result.valid).toBe(true);
    expect(result.format).toBe('DOCX');
    expect(result.error).toBeNull();
  });

  it('rejects unsupported extensions', () => {
    const exeFile = { name: 'malware.exe', size: 1024 };
    const result = validateDocumentFile(exeFile);
    expect(result.valid).toBe(false);
    expect(result.error).toContain('Unsupported file type');

    const txtFile = { name: 'notes.txt', size: 1024 };
    expect(validateDocumentFile(txtFile).valid).toBe(false);
  });

  it('rejects files exceeding 100 MB', () => {
    const oversizedFile = { name: 'large.pdf', size: MAX_DOCUMENT_FILE_SIZE_BYTES + 1 };
    const result = validateDocumentFile(oversizedFile);
    expect(result.valid).toBe(false);
    expect(result.error).toContain('exceeds the maximum allowed limit of 100 MB');
  });

  it('rejects empty or null files', () => {
    expect(validateDocumentFile(null).valid).toBe(false);
    expect(validateDocumentFile({ name: 'empty.pdf', size: 0 }).valid).toBe(false);
  });

  it('formats file sizes accurately', () => {
    expect(formatFileSize(0)).toBe('0 B');
    expect(formatFileSize(1024)).toBe('1 KB');
    expect(formatFileSize(1024 * 1024 * 5)).toBe('5 MB');
  });
});

