import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Card } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Alert from '../components/ui/Alert';

export default function RegisterPage() {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    const trimmedName = fullName.trim();
    const trimmedEmail = email.trim();

    if (!trimmedName || trimmedName.length < 2) {
      setErrorMessage('Full name must be at least 2 characters long.');
      return;
    }
    if (!trimmedEmail || !/\S+@\S+\.\S+/.test(trimmedEmail)) {
      setErrorMessage('Please provide a valid academic or personal email address.');
      return;
    }
    if (!password || password.length < 8) {
      setErrorMessage('Password must be at least 8 characters long.');
      return;
    }
    if (password !== confirmPassword) {
      setErrorMessage('Passwords do not match. Please verify both fields.');
      return;
    }

    setIsSubmitting(true);

    try {
      await register(trimmedName, trimmedEmail, password);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      if (err.validationErrors) {
        const firstError = Object.values(err.validationErrors)[0];
        setErrorMessage(firstError || err.message);
      } else {
        setErrorMessage(err.message || 'Registration failed. Please check your details.');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-12rem)] flex items-center justify-center px-4 py-12">
      <Card className="w-full max-w-md p-8 sm:p-10 shadow-card">
        {/* Brand Header */}
        <div className="text-center mb-8">
          <div className="w-12 h-12 rounded-2xl bg-brand-navy border border-brand-teal/40 flex items-center justify-center shadow-sm text-white font-extrabold text-lg tracking-wider mx-auto mb-4">
            <span className="text-brand-gold">S</span>
            <span className="text-brand-teal">T</span>
          </div>
          <h1 className="text-2xl font-extrabold text-text-primary tracking-tight">
            Create your Account
          </h1>
          <p className="text-xs sm:text-sm text-text-muted mt-1.5 leading-relaxed">
            Begin building your grounded computer science academic companion
          </p>
        </div>

        {/* Error Alert */}
        {errorMessage && (
          <div className="mb-6">
            <Alert variant="error" onClose={() => setErrorMessage('')}>
              {errorMessage}
            </Alert>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="reg-name"
              className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5"
            >
              Full Name
            </label>
            <input
              id="reg-name"
              type="text"
              autoComplete="name"
              required
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="e.g. Katherine Johnson"
              className="w-full px-4 py-2.5 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary placeholder:text-text-muted text-sm focus:outline-none focus:border-brand-teal focus:ring-1 focus:ring-brand-teal transition-colors"
              disabled={isSubmitting}
            />
          </div>

          <div>
            <label
              htmlFor="reg-email"
              className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5"
            >
              Academic or Personal Email
            </label>
            <input
              id="reg-email"
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="scholar@university.edu"
              className="w-full px-4 py-2.5 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary placeholder:text-text-muted text-sm focus:outline-none focus:border-brand-teal focus:ring-1 focus:ring-brand-teal transition-colors"
              disabled={isSubmitting}
            />
          </div>

          <div>
            <label
              htmlFor="reg-password"
              className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5"
            >
              Password (min. 8 characters)
            </label>
            <div className="relative">
              <input
                id="reg-password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Choose a strong password"
                className="w-full px-4 py-2.5 pr-11 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary placeholder:text-text-muted text-sm focus:outline-none focus:border-brand-teal focus:ring-1 focus:ring-brand-teal transition-colors"
                disabled={isSubmitting}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-primary transition-colors p-1 cursor-pointer"
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? (
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
                  </svg>
                ) : (
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                )}
              </button>
            </div>
          </div>

          <div>
            <label
              htmlFor="reg-confirm-password"
              className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5"
            >
              Confirm Password
            </label>
            <input
              id="reg-confirm-password"
              type={showPassword ? 'text' : 'password'}
              autoComplete="new-password"
              required
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Re-enter your password"
              className="w-full px-4 py-2.5 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary placeholder:text-text-muted text-sm focus:outline-none focus:border-brand-teal focus:ring-1 focus:ring-brand-teal transition-colors"
              disabled={isSubmitting}
            />
          </div>

          <div className="pt-2">
            <Button
              type="submit"
              variant="primary"
              className="w-full py-2.5"
              isLoading={isSubmitting}
            >
              Create Account
            </Button>
          </div>
        </form>

        {/* Footer Link */}
        <div className="mt-6 pt-6 border-t border-border-subtle text-center text-xs text-text-muted">
          Already have an account?{' '}
          <Link to="/login" className="text-brand-teal hover:underline font-semibold">
            Sign in
          </Link>
        </div>
      </Card>
    </div>
  );
}
