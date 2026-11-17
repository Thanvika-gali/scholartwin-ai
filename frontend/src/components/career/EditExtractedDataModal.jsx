import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Alert from '../ui/Alert';
import { updateResumeApi } from '../../services/api';

export default function EditExtractedDataModal({ isOpen, onClose, onSuccess, resume }) {
  const [title, setTitle] = useState('');
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [location, setLocation] = useState('');
  const [githubUrl, setGithubUrl] = useState('');
  const [linkedinUrl, setLinkedinUrl] = useState('');
  const [portfolioUrl, setPortfolioUrl] = useState('');
  const [summary, setSummary] = useState('');
  const [skillsStr, setSkillsStr] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (resume) {
      setTitle(resume.title || '');
      const data = resume.extractedData || {};
      setFullName(data.fullName || '');
      setEmail(data.email || '');
      setPhone(data.phone || '');
      setLocation(data.location || '');
      setGithubUrl(data.githubUrl || '');
      setLinkedinUrl(data.linkedinUrl || '');
      setPortfolioUrl(data.portfolioUrl || '');
      setSummary(data.summary || '');

      const skills = Array.isArray(data.skills) ? data.skills : [];
      setSkillsStr(skills.join(', '));
    }
    setError(null);
  }, [resume, isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!resume) return;

    setIsLoading(true);
    setError(null);

    const parsedSkills = skillsStr
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);

    const updatedData = {
      ...(resume.extractedData || {}),
      fullName: fullName.trim() || null,
      email: email.trim() || null,
      phone: phone.trim() || null,
      location: location.trim() || null,
      githubUrl: githubUrl.trim() || null,
      linkedinUrl: linkedinUrl.trim() || null,
      portfolioUrl: portfolioUrl.trim() || null,
      summary: summary.trim() || null,
      skills: parsedSkills,
    };

    const payload = {
      title: title.trim() || resume.title,
      extractedData: updatedData,
    };

    try {
      const updated = await updateResumeApi(resume.id, payload);
      setIsLoading(false);
      if (onSuccess) {
        onSuccess(updated);
      }
      onClose();
    } catch (err) {
      setIsLoading(false);
      setError(err.message || 'Failed to update resume information. Please try again.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Edit Extracted Resume Info"
      subtitle="Correct or enrich parsed contact info, summary, and skills"
      maxWidth="max-w-2xl"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
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

        <Input
          label="Resume Version Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          disabled={isLoading}
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Full Name"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            disabled={isLoading}
          />
          <Input
            label="Email Address"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isLoading}
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Phone"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            disabled={isLoading}
          />
          <Input
            label="Location"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
            disabled={isLoading}
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Input
            label="GitHub URL"
            value={githubUrl}
            onChange={(e) => setGithubUrl(e.target.value)}
            disabled={isLoading}
          />
          <Input
            label="LinkedIn URL"
            value={linkedinUrl}
            onChange={(e) => setLinkedinUrl(e.target.value)}
            disabled={isLoading}
          />
          <Input
            label="Portfolio URL"
            value={portfolioUrl}
            onChange={(e) => setPortfolioUrl(e.target.value)}
            disabled={isLoading}
          />
        </div>

        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
            Professional Summary / Objective
          </label>
          <textarea
            rows={3}
            value={summary}
            onChange={(e) => setSummary(e.target.value)}
            disabled={isLoading}
            className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal resize-none"
          />
        </div>

        <div>
          <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
            Extracted Skills (Comma separated)
          </label>
          <textarea
            rows={2}
            value={skillsStr}
            onChange={(e) => setSkillsStr(e.target.value)}
            disabled={isLoading}
            placeholder="Java, Spring Boot, MySQL, React, Docker..."
            className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal resize-none"
          />
        </div>

        <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
          <Button variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Save Changes
          </Button>
        </div>
      </form>
    </Modal>
  );
}

