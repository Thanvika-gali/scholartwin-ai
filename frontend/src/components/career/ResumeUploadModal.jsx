import React, { useState, useRef } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Alert from '../ui/Alert';
import { uploadResumeApi } from '../../services/api';
import { validateResumeFile } from '../../utils/careerValidation';

export default function ResumeUploadModal({ isOpen, onClose, onSuccess }) {
  const [file, setFile] = useState(null);
  const [title, setTitle] = useState('');
  const [isDragging, setIsDragging] = useState(false);
  const [progress, setProgress] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const resetForm = () => {
    setFile(null);
    setTitle('');
    setProgress(0);
    setIsLoading(false);
    setError(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleClose = () => {
    if (!isLoading) {
      resetForm();
      onClose();
    }
  };

  const handleSelectFile = (selectedFile) => {
    setError(null);
    if (!selectedFile) return;

    const validation = validateResumeFile(selectedFile);
    if (!validation.valid) {
      setError(validation.error);
      setFile(null);
      return;
    }

    setFile(selectedFile);
    if (!title.trim()) {
      // derive clean title from filename
      const baseName = selectedFile.name.replace(/\.[^/.]+$/, '').replace(/[_-]/g, ' ');
      setTitle(baseName.charAt(0).toUpperCase() + baseName.slice(1));
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleSelectFile(e.dataTransfer.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      setError('Please select a resume file (PDF or DOCX).');
      return;
    }

    setIsLoading(true);
    setError(null);
    setProgress(10);

    try {
      const response = await uploadResumeApi(file, title, (pct) => {
        setProgress(pct);
      });
      setIsLoading(false);
      resetForm();
      if (onSuccess) {
        onSuccess(response);
      }
      onClose();
    } catch (err) {
      setIsLoading(false);
      setError(err.message || 'Failed to upload and parse resume. Please try again.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title="Upload Resume"
      subtitle="Upload your latest PDF or DOCX resume for ATS diagnostics & skills extraction"
      maxWidth="max-w-xl"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <Alert variant="error" dismissible onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* Drag & Drop Area */}
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={() => !isLoading && fileInputRef.current?.click()}
          className={`border-2 border-dashed rounded-xl p-6 text-center cursor-pointer transition-all ${
            isDragging
              ? 'border-brand-teal bg-brand-teal/10'
              : file
              ? 'border-brand-teal/60 bg-bg-surface-elevated'
              : 'border-border-default hover:border-brand-teal/50 bg-bg-surface-elevated/40'
          }`}
        >
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            className="hidden"
            onChange={(e) => handleSelectFile(e.target.files?.[0])}
            disabled={isLoading}
          />

          <div className="flex flex-col items-center justify-center space-y-2">
            <div className="w-12 h-12 rounded-full bg-brand-teal/15 text-brand-teal flex items-center justify-center">
              {file ? (
                <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              ) : (
                <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.75}
                    d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
                  />
                </svg>
              )}
            </div>

            {file ? (
              <div className="text-center">
                <p className="text-sm font-semibold text-text-primary break-all">{file.name}</p>
                <p className="text-xs text-text-muted mt-0.5">
                  {(file.size / (1024 * 1024)).toFixed(2)} MB • Click to change file
                </p>
              </div>
            ) : (
              <div>
                <p className="text-sm font-medium text-text-primary">
                  Drag & drop your resume file, or <span className="text-brand-teal font-semibold">browse</span>
                </p>
                <p className="text-xs text-text-muted mt-1">Supported formats: PDF or DOCX (Max 20 MB)</p>
              </div>
            )}
          </div>
        </div>

        {/* Resume Title Input */}
        <Input
          label="Resume Title / Version Label"
          placeholder="e.g. Full Stack Developer - v2.0"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          disabled={isLoading}
          helperText="Give this version a name to easily compare iterations"
        />

        {/* Upload Progress Bar */}
        {isLoading && (
          <div className="space-y-1.5 pt-1">
            <div className="flex justify-between text-xs text-text-muted">
              <span>Extracting text & analyzing dimensions...</span>
              <span>{progress}%</span>
            </div>
            <div className="w-full bg-bg-surface-elevated rounded-full h-2 overflow-hidden border border-border-default">
              <div
                className="bg-brand-teal h-2 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }}
              />
            </div>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
          <Button variant="secondary" onClick={handleClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            isLoading={isLoading}
            disabled={!file || isLoading}
            icon={
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
              </svg>
            }
          >
            Upload & Analyze
          </Button>
        </div>
      </form>
    </Modal>
  );
}

