import React, { useState, useEffect } from 'react';
import Modal from '../ui/Modal';
import Button from '../ui/Button';
import Input from '../ui/Input';
import Alert from '../ui/Alert';
import { addCareerSkill, updateCareerSkill } from '../../services/api';

const SKILL_CATEGORIES = [
  { value: 'PROGRAMMING_LANGUAGES', label: 'Programming Languages' },
  { value: 'FRONTEND', label: 'Frontend Development' },
  { value: 'BACKEND', label: 'Backend Development' },
  { value: 'DATABASES', label: 'Databases & Storage' },
  { value: 'CLOUD_DEVOPS', label: 'Cloud & DevOps' },
  { value: 'AI_ML', label: 'AI & Machine Learning' },
  { value: 'TESTING', label: 'Testing & QA' },
  { value: 'TOOLS', label: 'Developer Tools' },
  { value: 'CORE_CS', label: 'Core Computer Science' },
  { value: 'OTHER', label: 'Other Technical Skills' },
];

const PROFICIENCY_LEVELS = [
  { value: 'BEGINNER', label: 'Beginner' },
  { value: 'INTERMEDIATE', label: 'Intermediate' },
  { value: 'ADVANCED', label: 'Advanced' },
  { value: 'EXPERT', label: 'Expert' },
];

const SKILL_SOURCES = [
  { value: 'USER_ENTERED', label: 'Manual Entry' },
  { value: 'RESUME', label: 'Resume Parsed' },
  { value: 'PROJECT', label: 'Project Evidence' },
  { value: 'CERTIFICATION', label: 'Certification' },
];

export default function AddEditSkillModal({ isOpen, onClose, onSuccess, initialData = null }) {
  const [skillName, setSkillName] = useState('');
  const [category, setCategory] = useState('PROGRAMMING_LANGUAGES');
  const [proficiency, setProficiency] = useState('INTERMEDIATE');
  const [source, setSource] = useState('USER_ENTERED');
  const [verificationEvidence, setVerificationEvidence] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const isEdit = Boolean(initialData?.id);

  useEffect(() => {
    if (initialData) {
      setSkillName(initialData.skillName || '');
      setCategory(initialData.category || 'PROGRAMMING_LANGUAGES');
      setProficiency(initialData.proficiency || 'INTERMEDIATE');
      setSource(initialData.source || 'USER_ENTERED');
      setVerificationEvidence(initialData.verificationEvidence || '');
    } else {
      setSkillName('');
      setCategory('PROGRAMMING_LANGUAGES');
      setProficiency('INTERMEDIATE');
      setSource('USER_ENTERED');
      setVerificationEvidence('');
    }
    setError(null);
  }, [initialData, isOpen]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!skillName.trim()) {
      setError('Please provide a skill name.');
      return;
    }

    setIsLoading(true);
    setError(null);

    const payload = {
      skillName: skillName.trim(),
      category,
      proficiency,
      source,
      verificationEvidence: verificationEvidence.trim() || null,
    };

    try {
      let savedSkill;
      if (isEdit) {
        savedSkill = await updateCareerSkill(initialData.id, payload);
      } else {
        savedSkill = await addCareerSkill(payload);
      }
      setIsLoading(false);
      if (onSuccess) {
        onSuccess(savedSkill);
      }
      onClose();
    } catch (err) {
      setIsLoading(false);
      setError(err.message || 'Failed to save skill. Please try again.');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEdit ? 'Edit Skill' : 'Add Skill to Inventory'}
      subtitle={isEdit ? 'Update skill proficiency or evidence' : 'Track your technical and soft skill competencies'}
      maxWidth="max-w-lg"
      icon={
        <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
          />
        </svg>
      }
      footer={
        <>
          <Button variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" form="add-edit-skill-form" variant="primary" isLoading={isLoading}>
            {isEdit ? 'Save Changes' : 'Add Skill'}
          </Button>
        </>
      }
    >
      <form id="add-edit-skill-form" onSubmit={handleSubmit} className="space-y-4">
        {error && (
          <Alert variant="error" dismissible onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}

        <Input
          label="Skill Name"
          placeholder="e.g. Docker, TypeScript, Spring Boot, MySQL"
          value={skillName}
          onChange={(e) => setSkillName(e.target.value)}
          required
          autoFocus
          disabled={isLoading}
        />

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
              Category
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
              disabled={isLoading}
            >
              {SKILL_CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
              Proficiency Level
            </label>
            <select
              value={proficiency}
              onChange={(e) => setProficiency(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
              disabled={isLoading}
            >
              {PROFICIENCY_LEVELS.map((p) => (
                <option key={p.value} value={p.value}>
                  {p.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
              Source
            </label>
            <select
              value={source}
              onChange={(e) => setSource(e.target.value)}
              className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
              disabled={isLoading}
            >
              {SKILL_SOURCES.map((s) => (
                <option key={s.value} value={s.value}>
                  {s.label}
                </option>
              ))}
            </select>
          </div>

          <div>
            <Input
              label="Verification Evidence (Optional)"
              placeholder="e.g. AWS Certified, 2 GitHub projects"
              value={verificationEvidence}
              onChange={(e) => setVerificationEvidence(e.target.value)}
              disabled={isLoading}
            />
          </div>
        </div>
      </form>
    </Modal>
  );
}
