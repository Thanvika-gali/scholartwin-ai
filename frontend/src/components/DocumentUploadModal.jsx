import React, { useState, useRef } from 'react';
import Modal from './ui/Modal';
import Button from './ui/Button';
import Alert from './ui/Alert';
import Badge from './ui/Badge';
import { uploadDocumentApi } from '../services/api';
import { validateDocumentFile, formatFileSize } from '../utils/documentValidation';

export default function DocumentUploadModal({
  isOpen,
  onClose,
  onUploaded,
  folderId = null,
  allFolders = [],
}) {
  const [selectedFile, setSelectedFile] = useState(null);
  const [format, setFormat] = useState(null);
  const [destinationFolderId, setDestinationFolderId] = useState(folderId ? String(folderId) : '');
  const [isDragging, setIsDragging] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [isProcessing, setIsProcessing] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');
  const [error, setError] = useState('');

  const fileInputRef = useRef(null);

  // Sync destinationFolderId when folderId prop changes
  React.useEffect(() => {
    if (folderId != null) {
      setDestinationFolderId(String(folderId));
    } else {
      setDestinationFolderId('');
    }
  }, [folderId, isOpen]);

  const resetState = () => {
    setSelectedFile(null);
    setFormat(null);
    setIsDragging(false);
    setUploadProgress(0);
    setIsProcessing(false);
    setStatusMessage('');
    setError('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleClose = () => {
    if (isProcessing) return; // Prevent closing mid-upload
    resetState();
    onClose();
  };

  const handleFileSelection = (file) => {
    setError('');
    const validation = validateDocumentFile(file);
    if (!validation.valid) {
      setError(validation.error);
      setSelectedFile(null);
      setFormat(null);
      return;
    }

    setSelectedFile(file);
    setFormat(validation.format);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isDragging) setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFileSelection(e.dataTransfer.files[0]);
    }
  };

  const handleFileInputChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      handleFileSelection(e.target.files[0]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedFile || isProcessing) return;

    setError('');
    setIsProcessing(true);
    setUploadProgress(10);
    setStatusMessage('Uploading document securely...');

    try {
      const uploadOptions = {
        folderId: destinationFolderId ? Number(destinationFolderId) : null,
      };

      const response = await uploadDocumentApi(
        selectedFile,
        (percent) => {
          setUploadProgress(percent);
          if (percent === 100) {
            setStatusMessage('Processing document pages and extracting text...');
          }
        },
        uploadOptions
      );

      setStatusMessage('Document extracted and chunked successfully!');
      setTimeout(() => {
        resetState();
        onUploaded(response);
      }, 500);
    } catch (err) {
      setIsProcessing(false);
      setUploadProgress(0);
      setStatusMessage('');
      setError(err.message || 'Failed to upload and process document.');
    }
  };

  if (!isOpen) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title="Upload Document"
      subtitle="Upload research papers, lecture notes, or thesis drafts in PDF or DOCX format."
      maxWidth="max-w-xl"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
        </svg>
      }
    >
      <form onSubmit={handleSubmit} className="p-6 space-y-5">
        {error && (
          <Alert variant="danger" onClose={() => setError('')}>
            {error}
          </Alert>
        )}

        {/* Drag and Drop Upload Zone */}
        {!selectedFile ? (
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-2xl p-8 text-center cursor-pointer transition-all duration-150 flex flex-col items-center justify-center gap-3 ${
              isDragging
                ? 'border-brand-teal bg-brand-teal/10 scale-[1.01]'
                : 'border-border-default hover:border-brand-teal/50 hover:bg-bg-subtle/40'
            }`}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
              onChange={handleFileInputChange}
              className="hidden"
            />
            <div className="w-14 h-14 rounded-2xl bg-brand-teal/15 text-brand-teal flex items-center justify-center border border-brand-teal/20 shadow-sm">
              <svg className="w-7 h-7" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 13h6m-3-3v6m5 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>
            <div>
              <p className="text-sm font-semibold text-text-primary">
                Drag & drop your document here, or <span className="text-brand-teal hover:underline">browse file</span>
              </p>
              <p className="text-xs text-text-muted mt-1">
                Supports PDF (.pdf) and Word (.docx) up to 100 MB (research papers, textbooks, notes)
              </p>
            </div>
          </div>
        ) : (
          /* File Preview Card */
          <div className="rounded-xl border border-border-subtle bg-bg-subtle/50 p-4 flex items-center justify-between gap-4">
            <div className="flex items-center gap-3 min-w-0">
              <div className={`w-11 h-11 rounded-lg flex items-center justify-center shrink-0 font-bold text-xs ${
                format === 'PDF'
                  ? 'bg-red-500/15 text-red-600 dark:text-red-400 border border-red-500/20'
                  : 'bg-blue-500/15 text-blue-600 dark:text-blue-400 border border-blue-500/20'
              }`}>
                {format}
              </div>
              <div className="min-w-0">
                <p className="text-sm font-medium text-text-primary truncate" title={selectedFile.name}>
                  {selectedFile.name}
                </p>
                <div className="flex items-center gap-2 mt-0.5">
                  <span className="text-xs text-text-muted">
                    {formatFileSize(selectedFile.size)}
                  </span>
                  <Badge variant={format === 'PDF' ? 'primary' : 'info'} size="sm">
                    {format}
                  </Badge>
                </div>
              </div>
            </div>

            {!isProcessing && (
              <button
                type="button"
                onClick={() => {
                  setSelectedFile(null);
                  setFormat(null);
                }}
                className="p-1.5 text-text-muted hover:text-red-500 rounded-lg transition-colors"
                title="Remove file"
              >
                <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            )}
          </div>
        )}

        {/* Upload Progress and Processing State */}
        {isProcessing && (
          <div className="space-y-2">
            <div className="flex items-center justify-between text-xs font-medium text-text-secondary">
              <span>{statusMessage}</span>
              <span>{uploadProgress}%</span>
            </div>
            <div className="w-full h-2 rounded-full bg-bg-subtle overflow-hidden">
              <div
                className="h-full bg-brand-teal transition-all duration-200 rounded-full"
                style={{ width: `${uploadProgress}%` }}
              />
            </div>
          </div>
        )}

        {/* Destination Folder Selector */}
        {allFolders.length > 0 && !isProcessing && (
          <div className="pt-2">
            <label className="block text-xs font-semibold text-text-secondary mb-1">
              Destination Folder (or leave unassigned for Smart Inbox)
            </label>
            <select
              value={destinationFolderId}
              onChange={(e) => setDestinationFolderId(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-xl border border-border-default bg-bg-surface text-text-primary focus:outline-none focus:ring-2 focus:ring-brand-teal"
            >
              <option value="">(Smart Inbox / Unassigned)</option>
              {allFolders.map((f) => (
                <option key={f.id} value={f.id}>
                  📁 {f.name}
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Modal Actions */}
        <div className="flex items-center justify-end gap-3 pt-3 border-t border-border-subtle">
          <Button
            type="button"
            variant="ghost"
            onClick={handleClose}
            disabled={isProcessing}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            isLoading={isProcessing}
            disabled={!selectedFile || isProcessing}
          >
            {isProcessing ? 'Processing...' : 'Upload & Extract'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}

