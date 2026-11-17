import React, { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useTheme } from '../hooks/useTheme';
import { Card, CardHeader, CardTitle, CardContent } from '../components/ui/Card';
import Button from '../components/ui/Button';
import Badge from '../components/ui/Badge';
import Alert from '../components/ui/Alert';
import { Input } from '../components/ui/Input';
import Modal from '../components/ui/Modal';
import {
  updateFullProfileApi,
  updateAcademicPreferencesApi,
  updateAiPreferencesApi,
  updateNotificationPreferencesApi,
  updateAppearanceApi,
  changePasswordApi,
  exportUserDataApi,
  deleteAccountApi,
  fetchDashboardSummary,
} from '../services/api';

export default function ProfilePage() {
  const { user, updateCurrentUser, logout } = useAuth();
  const { theme, setTheme } = useTheme();

  const [activeTab, setActiveTab] = useState('profile'); // 'profile' | 'academic' | 'ai' | 'appearance' | 'notifications' | 'security' | 'privacy'
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  // Section A: Profile State
  const [fullName, setFullName] = useState(user?.fullName || '');
  const [institution, setInstitution] = useState(user?.institution || '');
  const [degree, setDegree] = useState(user?.degree || '');
  const [graduationYear, setGraduationYear] = useState(user?.graduationYear || '');
  const [academicYear, setAcademicYear] = useState(user?.academicYear || '');
  const [currentSemester, setCurrentSemester] = useState(user?.currentSemester || '');
  const [bio, setBio] = useState(user?.bio || '');
  const [avatarUrl, setAvatarUrl] = useState(user?.avatarUrl || '');
  const [githubUrl, setGithubUrl] = useState(user?.githubUrl || '');
  const [linkedinUrl, setLinkedinUrl] = useState(user?.linkedinUrl || '');
  const [portfolioUrl, setPortfolioUrl] = useState(user?.portfolioUrl || '');
  const [isSavingProfile, setIsSavingProfile] = useState(false);

  // Section B: Academic Preferences State
  const [targetSemester, setTargetSemester] = useState('');
  const [targetGraduationYear, setTargetGraduationYear] = useState('');
  const [primaryCareerTarget, setPrimaryCareerTarget] = useState('Software Development Engineer');
  const [secondaryRoles, setSecondaryRoles] = useState('');
  const [technicalInterests, setTechnicalInterests] = useState('');
  const [weeklyDsaTarget, setWeeklyDsaTarget] = useState('10');
  const [dailyReadingMinutes, setDailyReadingMinutes] = useState('45');
  const [preferredStudyTime, setPreferredStudyTime] = useState('MORNING');
  const [isSavingAcademic, setIsSavingAcademic] = useState(false);

  // Section C: AI Preferences State
  const [interactionMode, setInteractionMode] = useState('BALANCED'); // 'CONCISE' | 'BALANCED' | 'DETAILED'
  const [groundingMode, setGroundingMode] = useState('HYBRID');       // 'STRICT' | 'HYBRID'
  const [proactiveSuggestions, setProactiveSuggestions] = useState(true);
  const [isSavingAi, setIsSavingAi] = useState(false);

  // Section E: Notification Preferences State
  const [revisionReminders, setRevisionReminders] = useState(true);
  const [dailyStudyReminders, setDailyStudyReminders] = useState(true);
  const [careerActionAlerts, setCareerActionAlerts] = useState(true);
  const [processingFailureAlerts, setProcessingFailureAlerts] = useState(true);
  const [isSavingNotifications, setIsSavingNotifications] = useState(false);

  // Section F: Account Security State
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);

  // Section G: Data & Privacy State
  const [dashboardStats, setDashboardStats] = useState(null);
  const [isExporting, setIsExporting] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [deleteConfirmationText, setDeleteConfirmationText] = useState('');
  const [isDeletingAccount, setIsDeletingAccount] = useState(false);

  // Populate from user state
  useEffect(() => {
    if (user) {
      setFullName(user.fullName || '');
      setInstitution(user.institution || '');
      setDegree(user.degree || '');
      setGraduationYear(user.graduationYear || '');
      setAcademicYear(user.academicYear || '');
      setCurrentSemester(user.currentSemester || '');
      setBio(user.bio || '');
      setAvatarUrl(user.avatarUrl || '');
      setGithubUrl(user.githubUrl || '');
      setLinkedinUrl(user.linkedinUrl || '');
      setPortfolioUrl(user.portfolioUrl || '');

      // Parse JSON Preferences
      if (user.academicPreferencesJson) {
        try {
          const ac = JSON.parse(user.academicPreferencesJson);
          setTargetSemester(ac.targetSemester || '');
          setTargetGraduationYear(ac.targetGraduationYear || '');
          setPrimaryCareerTarget(ac.primaryCareerTarget || 'Software Development Engineer');
          setSecondaryRoles(Array.isArray(ac.secondaryRoles) ? ac.secondaryRoles.join(', ') : ac.secondaryRoles || '');
          setTechnicalInterests(Array.isArray(ac.technicalInterests) ? ac.technicalInterests.join(', ') : ac.technicalInterests || '');
          setWeeklyDsaTarget(ac.weeklyDsaTarget != null ? String(ac.weeklyDsaTarget) : '10');
          setDailyReadingMinutes(ac.dailyReadingMinutes != null ? String(ac.dailyReadingMinutes) : '45');
          setPreferredStudyTime(ac.preferredStudyTime || 'MORNING');
        } catch (_) {}
      }

      if (user.aiPreferencesJson) {
        try {
          const ai = JSON.parse(user.aiPreferencesJson);
          setInteractionMode(ai.interactionMode || 'BALANCED');
          setGroundingMode(ai.groundingMode || 'HYBRID');
          setProactiveSuggestions(ai.proactiveSuggestions !== false);
        } catch (_) {}
      }

      if (user.notificationPreferencesJson) {
        try {
          const np = JSON.parse(user.notificationPreferencesJson);
          setRevisionReminders(np.revisionReminders !== false);
          setDailyStudyReminders(np.dailyStudyReminders !== false);
          setCareerActionAlerts(np.careerActionAlerts !== false);
          setProcessingFailureAlerts(np.processingFailureAlerts !== false);
        } catch (_) {}
      }
    }
  }, [user]);

  // Load summary counts for Data & Privacy tab
  useEffect(() => {
    fetchDashboardSummary()
      .then((data) => setDashboardStats(data))
      .catch(() => {});
  }, []);

  if (!user) {
    return null;
  }

  const initials = user.fullName
    ? user.fullName
        .split(' ')
        .map((n) => n[0])
        .join('')
        .toUpperCase()
        .slice(0, 2)
    : 'ST';

  // 1. Save Profile Information
  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setSuccessMessage('');
    setErrorMessage('');

    if (!fullName.trim() || fullName.trim().length < 2) {
      setErrorMessage('Full name must be at least 2 characters long.');
      return;
    }

    setIsSavingProfile(true);
    try {
      const payload = {
        fullName: fullName.trim(),
        institution: institution.trim() || null,
        degree: degree.trim() || null,
        graduationYear: graduationYear ? Number(graduationYear) : null,
        academicYear: academicYear.trim() || null,
        currentSemester: currentSemester ? Number(currentSemester) : null,
        bio: bio.trim() || null,
        avatarUrl: avatarUrl.trim() || null,
        githubUrl: githubUrl.trim() || null,
        linkedinUrl: linkedinUrl.trim() || null,
        portfolioUrl: portfolioUrl.trim() || null,
      };
      const updated = await updateFullProfileApi(payload);
      updateCurrentUser(updated);
      setSuccessMessage('Profile information saved successfully.');
    } catch (err) {
      setErrorMessage(err.message || 'Failed to save profile information.');
    } finally {
      setIsSavingProfile(false);
    }
  };

  // 2. Save Academic Preferences
  const handleSaveAcademicPreferences = async (e) => {
    e.preventDefault();
    setSuccessMessage('');
    setErrorMessage('');
    setIsSavingAcademic(true);

    try {
      const secRolesList = secondaryRoles
        .split(',')
        .map((r) => r.trim())
        .filter(Boolean);
      const techInterestsList = technicalInterests
        .split(',')
        .map((t) => t.trim())
        .filter(Boolean);

      const payload = {
        targetSemester: targetSemester ? Number(targetSemester) : null,
        targetGraduationYear: targetGraduationYear ? Number(targetGraduationYear) : null,
        primaryCareerTarget: primaryCareerTarget.trim() || null,
        secondaryRoles: secRolesList,
        technicalInterests: techInterestsList,
        weeklyDsaTarget: weeklyDsaTarget ? Number(weeklyDsaTarget) : 10,
        dailyReadingMinutes: dailyReadingMinutes ? Number(dailyReadingMinutes) : 45,
        preferredStudyTime,
      };

      const updated = await updateAcademicPreferencesApi(payload);
      updateCurrentUser(updated);
      setSuccessMessage('Academic preferences and study targets updated successfully.');
    } catch (err) {
      setErrorMessage(err.message || 'Failed to update academic preferences.');
    } finally {
      setIsSavingAcademic(false);
    }
  };

  // 3. Save AI Preferences
  const handleSaveAiPreferences = async (e) => {
    e.preventDefault();
    setSuccessMessage('');
    setErrorMessage('');
    setIsSavingAi(true);

    try {
      const payload = {
        interactionMode,
        groundingMode,
        proactiveSuggestions,
      };
      const updated = await updateAiPreferencesApi(payload);
      updateCurrentUser(updated);
      setSuccessMessage('AI ScholarTwin intelligence preferences saved.');
    } catch (err) {
      setErrorMessage(err.message || 'Failed to update AI preferences.');
    } finally {
      setIsSavingAi(false);
    }
  };

  // 4. Save Notification Preferences
  const handleSaveNotifications = async (e) => {
    e.preventDefault();
    setSuccessMessage('');
    setErrorMessage('');
    setIsSavingNotifications(true);

    try {
      const payload = {
        revisionReminders,
        dailyStudyReminders,
        careerActionAlerts,
        processingFailureAlerts,
      };
      const updated = await updateNotificationPreferencesApi(payload);
      updateCurrentUser(updated);
      setSuccessMessage('Notification preferences updated.');
    } catch (err) {
      setErrorMessage(err.message || 'Failed to update notification preferences.');
    } finally {
      setIsSavingNotifications(false);
    }
  };

  // 5. Change Theme & Appearance
  const handleThemeChange = async (newTheme) => {
    setTheme(newTheme);
    try {
      const updated = await updateAppearanceApi(newTheme.toUpperCase());
      updateCurrentUser(updated);
      setSuccessMessage(`Theme set to ${newTheme} mode.`);
    } catch (_) {}
  };

  // 6. Change Password
  const handleChangePassword = async (e) => {
    e.preventDefault();
    setSuccessMessage('');
    setErrorMessage('');

    if (!currentPassword) {
      setErrorMessage('Please enter your current password.');
      return;
    }
    if (newPassword.length < 8) {
      setErrorMessage('New password must be at least 8 characters long.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setErrorMessage('New password and confirmation do not match.');
      return;
    }

    setIsChangingPassword(true);
    try {
      await changePasswordApi(currentPassword, newPassword);
      setSuccessMessage('Password changed successfully.');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setErrorMessage(err.message || 'Failed to change password. Verify your current password.');
    } finally {
      setIsChangingPassword(false);
    }
  };

  // 7. Export Personal Data in JSON
  const handleExportData = async () => {
    setIsExporting(true);
    setErrorMessage('');
    try {
      const data = await exportUserDataApi();
      const jsonString = `data:text/json;charset=utf-8,${encodeURIComponent(
        JSON.stringify(data, null, 2)
      )}`;
      const downloadAnchor = document.createElement('a');
      downloadAnchor.setAttribute('href', jsonString);
      downloadAnchor.setAttribute(
        'download',
        `scholartwin-export-${user.email.replace(/[^a-zA-Z0-9]/g, '_')}-${new Date().toISOString().slice(0, 10)}.json`
      );
      document.body.appendChild(downloadAnchor);
      downloadAnchor.click();
      downloadAnchor.remove();
      setSuccessMessage('Personal data archive downloaded successfully.');
    } catch (err) {
      setErrorMessage(err.message || 'Failed to export personal data.');
    } finally {
      setIsExporting(false);
    }
  };

  // 8. Delete Account Cascade
  const handleDeleteAccount = async () => {
    if (deleteConfirmationText !== 'DELETE MY ACCOUNT') {
      setErrorMessage('Please type DELETE MY ACCOUNT exactly to confirm account deletion.');
      return;
    }

    setIsDeletingAccount(true);
    try {
      await deleteAccountApi();
      logout();
      window.location.href = '/login';
    } catch (err) {
      setErrorMessage(err.message || 'Failed to delete account. Please try again.');
      setIsDeletingAccount(false);
    }
  };

  const navItems = [
    {
      id: 'profile',
      label: 'Profile Information',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
        </svg>
      ),
    },
    {
      id: 'academic',
      label: 'Academic Preferences',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 14l9-5-9-5-9 5 9 5z" />
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z" />
        </svg>
      ),
    },
    {
      id: 'ai',
      label: 'AI Twin Intelligence',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
      ),
    },
    {
      id: 'appearance',
      label: 'Appearance & Theme',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
        </svg>
      ),
    },
    {
      id: 'notifications',
      label: 'Notifications',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
      ),
    },
    {
      id: 'security',
      label: 'Security & Password',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
        </svg>
      ),
    },
    {
      id: 'privacy',
      label: 'Data, Privacy & Account',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
        </svg>
      ),
    },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Top Banner Identity */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pb-6 border-b border-border-subtle">
        <div className="flex items-center gap-4">
          {avatarUrl ? (
            <img
              src={avatarUrl}
              alt={user.fullName}
              className="w-16 h-16 rounded-2xl object-cover border-2 border-brand-teal/50 shadow-md shrink-0"
            />
          ) : (
            <div className="w-16 h-16 rounded-2xl bg-brand-navy border-2 border-brand-teal/50 flex items-center justify-center text-brand-gold font-extrabold text-2xl shadow-md shrink-0">
              {initials}
            </div>
          )}
          <div>
            <div className="flex items-center gap-2.5 flex-wrap">
              <h1 className="text-2xl sm:text-3xl font-extrabold text-text-primary tracking-tight">
                {user.fullName}
              </h1>
              <Badge variant="teal">Role: {user.role}</Badge>
              <Badge variant={user.isActive ? 'success' : 'error'}>
                {user.isActive ? 'Active' : 'Disabled'}
              </Badge>
            </div>
            <p className="text-xs sm:text-sm text-text-muted mt-0.5">
              {user.email} {institution ? `• ${institution}` : ''} {degree ? `(${degree})` : ''}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button
            variant="secondary"
            size="sm"
            onClick={() => setIsLogoutModalOpen(true)}
            icon={
              <svg className="w-4 h-4 text-text-muted" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            }
          >
            Sign Out
          </Button>
        </div>
      </div>

      {/* Global Alerts */}
      {successMessage && (
        <Alert variant="success" dismissible onDismiss={() => setSuccessMessage('')}>
          {successMessage}
        </Alert>
      )}
      {errorMessage && (
        <Alert variant="error" dismissible onDismiss={() => setErrorMessage('')}>
          {errorMessage}
        </Alert>
      )}

      {/* Layout Grid: Sidebar Navigation + Settings Main Panel */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8 items-start">
        {/* Desktop Sidebar Navigation */}
        <aside className="lg:col-span-1 space-y-1 bg-bg-surface p-3 rounded-2xl border border-border-default shadow-sm">
          <span className="text-[10px] font-bold uppercase tracking-wider text-text-muted px-3 py-1.5 block">
            Account & Settings
          </span>
          {navItems.map((item) => (
            <button
              key={item.id}
              onClick={() => {
                setActiveTab(item.id);
                setSuccessMessage('');
                setErrorMessage('');
              }}
              className={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all text-left cursor-pointer ${
                activeTab === item.id
                  ? 'bg-brand-navy text-brand-gold shadow-sm border border-brand-teal/40'
                  : 'text-text-secondary hover:bg-bg-surface-elevated hover:text-text-primary'
              }`}
            >
              <span className={activeTab === item.id ? 'text-brand-gold' : 'text-brand-teal'}>
                {item.icon}
              </span>
              <span>{item.label}</span>
            </button>
          ))}
        </aside>

        {/* Main Settings Panel */}
        <main className="lg:col-span-3 space-y-6">
          {/* TAB 1: PROFILE INFORMATION */}
          {activeTab === 'profile' && (
            <Card>
              <CardHeader>
                <CardTitle>Profile Information</CardTitle>
                <p className="text-xs text-text-muted">
                  Update your academic background, degree, and public social presence.
                </p>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSaveProfile} className="space-y-4">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <Input
                      label="Full Name"
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      placeholder="e.g. Jane Doe"
                      required
                    />
                    <Input
                      label="Account Email"
                      value={user.email}
                      disabled
                      placeholder="name@university.edu"
                      helpText="Email address is fixed to your identity."
                    />
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <Input
                      label="College / University"
                      value={institution}
                      onChange={(e) => setInstitution(e.target.value)}
                      placeholder="e.g. Stanford University / IIT"
                    />
                    <Input
                      label="Degree / Major"
                      value={degree}
                      onChange={(e) => setDegree(e.target.value)}
                      placeholder="e.g. B.Tech Computer Science"
                    />
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <Input
                      label="Graduation Year"
                      type="number"
                      value={graduationYear}
                      onChange={(e) => setGraduationYear(e.target.value)}
                      placeholder="2026"
                    />
                    <Input
                      label="Academic Year"
                      value={academicYear}
                      onChange={(e) => setAcademicYear(e.target.value)}
                      placeholder="e.g. 3rd Year"
                    />
                    <Input
                      label="Current Semester"
                      type="number"
                      value={currentSemester}
                      onChange={(e) => setCurrentSemester(e.target.value)}
                      placeholder="e.g. 6"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
                      Bio / Academic Headline
                    </label>
                    <textarea
                      rows={3}
                      value={bio}
                      onChange={(e) => setBio(e.target.value)}
                      placeholder="A short summary of your academic interests, engineering passion, and career aspirations..."
                      className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal resize-none"
                    />
                  </div>

                  <div className="pt-2 border-t border-border-subtle space-y-4">
                    <h4 className="text-xs font-bold uppercase tracking-wider text-text-secondary">
                      Online Presence & Portfolio Links
                    </h4>
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                      <Input
                        label="GitHub Profile URL"
                        value={githubUrl}
                        onChange={(e) => setGithubUrl(e.target.value)}
                        placeholder="https://github.com/username"
                      />
                      <Input
                        label="LinkedIn Profile URL"
                        value={linkedinUrl}
                        onChange={(e) => setLinkedinUrl(e.target.value)}
                        placeholder="https://linkedin.com/in/username"
                      />
                      <Input
                        label="Personal Portfolio / Website"
                        value={portfolioUrl}
                        onChange={(e) => setPortfolioUrl(e.target.value)}
                        placeholder="https://myportfolio.dev"
                      />
                    </div>
                  </div>

                  <div className="flex justify-end pt-3 border-t border-border-subtle">
                    <Button type="submit" variant="primary" isLoading={isSavingProfile}>
                      Save Profile Changes
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {/* TAB 2: ACADEMIC PREFERENCES */}
          {activeTab === 'academic' && (
            <Card>
              <CardHeader>
                <CardTitle>Academic Preferences & Study Targets</CardTitle>
                <p className="text-xs text-text-muted">
                  Configure your career targets, study rhythms, and weekly DSA practice goals.
                </p>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSaveAcademicPreferences} className="space-y-5">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
                        Primary Career Target
                      </label>
                      <select
                        value={primaryCareerTarget}
                        onChange={(e) => setPrimaryCareerTarget(e.target.value)}
                        className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
                      >
                        <option value="Software Development Engineer">Software Development Engineer (SDE)</option>
                        <option value="Java Developer">Java Developer</option>
                        <option value="Full Stack Developer">Full Stack Developer</option>
                        <option value="Frontend Developer">Frontend Developer</option>
                        <option value="Backend Developer">Backend Developer</option>
                        <option value="AI/ML Engineer">AI / Machine Learning Engineer</option>
                        <option value="Data Analyst">Data Analyst</option>
                        <option value="DevOps Engineer">DevOps & Cloud Engineer</option>
                      </select>
                    </div>

                    <Input
                      label="Secondary Roles of Interest"
                      value={secondaryRoles}
                      onChange={(e) => setSecondaryRoles(e.target.value)}
                      placeholder="e.g. Backend Developer, Cloud Architect (comma separated)"
                    />
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <Input
                      label="Target Placement / Graduation Year"
                      type="number"
                      value={targetGraduationYear}
                      onChange={(e) => setTargetGraduationYear(e.target.value)}
                      placeholder="2026"
                    />
                    <Input
                      label="Target Semester Goal"
                      type="number"
                      value={targetSemester}
                      onChange={(e) => setTargetSemester(e.target.value)}
                      placeholder="e.g. 6"
                    />
                  </div>

                  <div>
                    <Input
                      label="Technical Focus Topics & Interests"
                      value={technicalInterests}
                      onChange={(e) => setTechnicalInterests(e.target.value)}
                      placeholder="e.g. Distributed Systems, Microservices, RAG, Spring Boot (comma separated)"
                    />
                    <p className="text-[11px] text-text-muted mt-1">
                      These topics prioritize revision cues, recommendations, and Knowledge Hub tagging.
                    </p>
                  </div>

                  <div className="pt-2 border-t border-border-subtle grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <Input
                      label="Weekly DSA Target (Problems)"
                      type="number"
                      value={weeklyDsaTarget}
                      onChange={(e) => setWeeklyDsaTarget(e.target.value)}
                      placeholder="10"
                    />
                    <Input
                      label="Daily Study Target (Minutes)"
                      type="number"
                      value={dailyReadingMinutes}
                      onChange={(e) => setDailyReadingMinutes(e.target.value)}
                      placeholder="45"
                    />
                    <div>
                      <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary mb-1.5">
                        Preferred Study Routine
                      </label>
                      <select
                        value={preferredStudyTime}
                        onChange={(e) => setPreferredStudyTime(e.target.value)}
                        className="w-full px-3.5 py-2 rounded-xl bg-bg-surface-elevated border border-border-default text-text-primary text-sm focus:outline-none focus:ring-2 focus:ring-brand-teal"
                      >
                        <option value="MORNING">Early Morning (6 AM - 10 AM)</option>
                        <option value="AFTERNOON">Afternoon Focus (12 PM - 4 PM)</option>
                        <option value="EVENING">Evening (5 PM - 9 PM)</option>
                        <option value="NIGHT">Night Owl (10 PM - 2 AM)</option>
                        <option value="WEEKENDS">Weekend Intensive Blocks</option>
                      </select>
                    </div>
                  </div>

                  <div className="flex justify-end pt-3 border-t border-border-subtle">
                    <Button type="submit" variant="primary" isLoading={isSavingAcademic}>
                      Save Academic Preferences
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {/* TAB 3: AI TWIN INTELLIGENCE */}
          {activeTab === 'ai' && (
            <Card>
              <CardHeader>
                <CardTitle>AI Twin Intelligence Preferences</CardTitle>
                <p className="text-xs text-text-muted">
                  Control how AI ScholarTwin responds, grounds explanations, and recommends learning actions.
                </p>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSaveAiPreferences} className="space-y-6">
                  {/* Interaction Mode */}
                  <div className="space-y-3">
                    <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary">
                      Response & Explanation Detail
                    </label>
                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                      {[
                        {
                          id: 'CONCISE',
                          title: 'Concise & Direct',
                          desc: 'Code snippets first, direct answers, and minimal conversational prose.',
                        },
                        {
                          id: 'BALANCED',
                          title: 'Balanced (Recommended)',
                          desc: 'Clear explanations with relevant academic examples and practical context.',
                        },
                        {
                          id: 'DETAILED',
                          title: 'Detailed Walkthroughs',
                          desc: 'Deep-dive conceptual breakdowns, edge case analyses, and architectural mechanics.',
                        },
                      ].map((mode) => (
                        <div
                          key={mode.id}
                          onClick={() => setInteractionMode(mode.id)}
                          className={`p-4 rounded-xl border cursor-pointer transition-all ${
                            interactionMode === mode.id
                              ? 'bg-brand-navy/30 border-brand-teal text-text-primary shadow-sm'
                              : 'bg-bg-surface-elevated border-border-default text-text-muted hover:border-brand-teal/40'
                          }`}
                        >
                          <div className="flex items-center justify-between mb-1.5">
                            <span className="text-xs font-bold text-text-primary">{mode.title}</span>
                            <div className={`w-3.5 h-3.5 rounded-full border-2 flex items-center justify-center ${
                              interactionMode === mode.id ? 'border-brand-teal bg-brand-teal' : 'border-border-default'
                            }`}>
                              {interactionMode === mode.id && <div className="w-1.5 h-1.5 rounded-full bg-white" />}
                            </div>
                          </div>
                          <p className="text-[11px] text-text-secondary leading-relaxed">{mode.desc}</p>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Grounding Mode */}
                  <div className="space-y-3 pt-3 border-t border-border-subtle">
                    <label className="block text-xs font-semibold uppercase tracking-wider text-text-secondary">
                      Knowledge Grounding Strategy
                    </label>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      {[
                        {
                          id: 'HYBRID',
                          title: 'Hybrid Academic Grounding',
                          desc: 'Combines verified notes from your Knowledge Hub with general computer science foundations.',
                        },
                        {
                          id: 'STRICT',
                          title: 'Strict Knowledge Grounding',
                          desc: 'Restricts answers strictly to documents and notes explicitly stored in your ScholarTwin Knowledge Hub.',
                        },
                      ].map((mode) => (
                        <div
                          key={mode.id}
                          onClick={() => setGroundingMode(mode.id)}
                          className={`p-4 rounded-xl border cursor-pointer transition-all ${
                            groundingMode === mode.id
                              ? 'bg-brand-navy/30 border-brand-teal text-text-primary shadow-sm'
                              : 'bg-bg-surface-elevated border-border-default text-text-muted hover:border-brand-teal/40'
                          }`}
                        >
                          <div className="flex items-center justify-between mb-1.5">
                            <span className="text-xs font-bold text-text-primary">{mode.title}</span>
                            <div className={`w-3.5 h-3.5 rounded-full border-2 flex items-center justify-center ${
                              groundingMode === mode.id ? 'border-brand-teal bg-brand-teal' : 'border-border-default'
                            }`}>
                              {groundingMode === mode.id && <div className="w-1.5 h-1.5 rounded-full bg-white" />}
                            </div>
                          </div>
                          <p className="text-[11px] text-text-secondary leading-relaxed">{mode.desc}</p>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Proactive Suggestions Toggle */}
                  <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-default flex items-center justify-between">
                    <div>
                      <span className="text-xs font-bold text-text-primary block">Proactive Learning Suggestions</span>
                      <p className="text-[11px] text-text-muted mt-0.5">
                        Suggest related algorithms, missing resume skills, and daily practice action items automatically.
                      </p>
                    </div>
                    <label className="relative inline-flex items-center cursor-pointer">
                      <input
                        type="checkbox"
                        checked={proactiveSuggestions}
                        onChange={(e) => setProactiveSuggestions(e.target.checked)}
                        className="sr-only peer"
                      />
                      <div className="w-9 h-5 bg-border-default peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-brand-teal"></div>
                    </label>
                  </div>

                  {/* Explanatory Callout */}
                  <div className="p-3.5 rounded-xl bg-brand-navy/40 border border-brand-teal/30 text-xs text-text-secondary space-y-1">
                    <span className="text-brand-gold font-bold block">How these preferences work:</span>
                    <p className="text-[11px] leading-relaxed">
                      AI ScholarTwin personalizes its conversational tone, citation depth, and role readiness recommendations based on these settings. Changes apply immediately to all upcoming AI interactions.
                    </p>
                  </div>

                  <div className="flex justify-end pt-3 border-t border-border-subtle">
                    <Button type="submit" variant="primary" isLoading={isSavingAi}>
                      Save AI Preferences
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {/* TAB 4: APPEARANCE & THEME */}
          {activeTab === 'appearance' && (
            <Card>
              <CardHeader>
                <CardTitle>Appearance & Brand Theme</CardTitle>
                <p className="text-xs text-text-muted">
                  Choose your visual interface appearance. Settings persist across your devices.
                </p>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  {[
                    {
                      id: 'dark',
                      title: 'Dark Theme',
                      desc: 'Deep Navy #0A1828 with turquoise accents. Recommended for extended coding sessions.',
                      bg: 'bg-[#0A1828]',
                      border: 'border-[#178582]',
                    },
                    {
                      id: 'light',
                      title: 'Light Theme',
                      desc: 'Crisp white academic canvas with balanced slate typography and gold signals.',
                      bg: 'bg-white',
                      border: 'border-slate-300',
                    },
                    {
                      id: 'system',
                      title: 'System Default',
                      desc: 'Automatically matches your operating system dark/light mode preference.',
                      bg: 'bg-gradient-to-r from-slate-900 to-slate-100',
                      border: 'border-border-default',
                    },
                  ].map((t) => (
                    <div
                      key={t.id}
                      onClick={() => handleThemeChange(t.id)}
                      className={`p-4 rounded-xl border cursor-pointer transition-all space-y-3 ${
                        theme === t.id
                          ? 'border-brand-teal ring-2 ring-brand-teal/20 bg-bg-surface-elevated'
                          : 'border-border-default bg-bg-surface hover:border-brand-teal/40'
                      }`}
                    >
                      <div className={`w-full h-16 rounded-lg ${t.bg} border ${t.border} flex items-center justify-center p-2`}>
                        <div className="flex gap-1.5">
                          <div className="w-3 h-3 rounded-full bg-[#178582]" />
                          <div className="w-3 h-3 rounded-full bg-[#BFA181]" />
                        </div>
                      </div>
                      <div>
                        <div className="flex items-center justify-between">
                          <span className="text-xs font-bold text-text-primary">{t.title}</span>
                          {theme === t.id && (
                            <Badge variant="teal" size="xs">Active</Badge>
                          )}
                        </div>
                        <p className="text-[11px] text-text-muted mt-1 leading-relaxed">{t.desc}</p>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-default space-y-2 text-xs">
                  <h4 className="font-bold text-text-primary">ScholarTwin Permanent Brand Palette</h4>
                  <div className="flex flex-wrap gap-2 pt-1">
                    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-bg-surface border border-border-default text-[11px]">
                      <span className="w-3 h-3 rounded-full bg-[#0A1828] border border-white/20" /> Deep Navy (#0A1828)
                    </span>
                    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-bg-surface border border-border-default text-[11px]">
                      <span className="w-3 h-3 rounded-full bg-[#178582]" /> Turquoise Accent (#178582)
                    </span>
                    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-bg-surface border border-border-default text-[11px]">
                      <span className="w-3 h-3 rounded-full bg-[#BFA181]" /> Academic Gold (#BFA181)
                    </span>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* TAB 5: NOTIFICATIONS */}
          {activeTab === 'notifications' && (
            <Card>
              <CardHeader>
                <CardTitle>Notification Preferences</CardTitle>
                <p className="text-xs text-text-muted">
                  Choose which alerts and reminders you receive for study sessions and revisions.
                </p>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSaveNotifications} className="space-y-4">
                  {[
                    {
                      label: 'DSA Spaced Repetition Reminders',
                      desc: 'Alerts when scheduled coding problems become due for revision based on spaced repetition intervals.',
                      checked: revisionReminders,
                      onChange: setRevisionReminders,
                    },
                    {
                      label: 'Daily Study & Target Reminders',
                      desc: 'Gentle morning notifications highlighting today’s scheduled academic reading and practice targets.',
                      checked: dailyStudyReminders,
                      onChange: setDailyStudyReminders,
                    },
                    {
                      label: 'Career Readiness Action Alerts',
                      desc: 'Notifications when high-priority career readiness items approach their target deadlines.',
                      checked: careerActionAlerts,
                      onChange: setCareerActionAlerts,
                    },
                    {
                      label: 'Document Processing & OCR Status Alerts',
                      desc: 'Alerts when large document uploads complete extraction or if OCR is required.',
                      checked: processingFailureAlerts,
                      onChange: setProcessingFailureAlerts,
                    },
                  ].map((item, idx) => (
                    <div
                      key={idx}
                      className="p-4 rounded-xl bg-bg-surface-elevated border border-border-default flex items-center justify-between gap-4"
                    >
                      <div className="min-w-0">
                        <span className="text-xs font-bold text-text-primary block">{item.label}</span>
                        <p className="text-[11px] text-text-muted mt-0.5">{item.desc}</p>
                      </div>
                      <label className="relative inline-flex items-center cursor-pointer shrink-0">
                        <input
                          type="checkbox"
                          checked={item.checked}
                          onChange={(e) => item.onChange(e.target.checked)}
                          className="sr-only peer"
                        />
                        <div className="w-9 h-5 bg-border-default peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-brand-teal"></div>
                      </label>
                    </div>
                  ))}

                  <div className="flex justify-end pt-3 border-t border-border-subtle">
                    <Button type="submit" variant="primary" isLoading={isSavingNotifications}>
                      Save Notification Settings
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {/* TAB 6: SECURITY & PASSWORD */}
          {activeTab === 'security' && (
            <div className="space-y-6">
              <Card>
                <CardHeader>
                  <CardTitle>Change Password</CardTitle>
                  <p className="text-xs text-text-muted">
                    Update your account password. Requires verification of your existing password.
                  </p>
                </CardHeader>
                <CardContent>
                  <form onSubmit={handleChangePassword} className="space-y-4 max-w-md">
                    <Input
                      label="Current Password"
                      type="password"
                      value={currentPassword}
                      onChange={(e) => setCurrentPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                    />
                    <Input
                      label="New Password"
                      type="password"
                      value={newPassword}
                      onChange={(e) => setNewPassword(e.target.value)}
                      placeholder="At least 8 characters"
                      helpText="Must be at least 8 characters long."
                      required
                    />
                    <Input
                      label="Confirm New Password"
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="••••••••"
                      required
                    />
                    <div className="pt-2">
                      <Button type="submit" variant="primary" isLoading={isChangingPassword}>
                        Update Password
                      </Button>
                    </div>
                  </form>
                </CardContent>
              </Card>

              {/* Session Information */}
              <Card>
                <CardHeader>
                  <CardTitle>Active Session & Authentication Details</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 text-xs">
                    <div className="p-3 rounded-lg bg-bg-surface-elevated border border-border-subtle">
                      <span className="text-text-muted block text-[11px]">Primary Email</span>
                      <span className="font-bold text-text-primary truncate block mt-0.5">{user.email}</span>
                    </div>
                    <div className="p-3 rounded-lg bg-bg-surface-elevated border border-border-subtle">
                      <span className="text-text-muted block text-[11px]">Role Permission</span>
                      <span className="font-bold text-brand-teal block mt-0.5">{user.role}</span>
                    </div>
                    <div className="p-3 rounded-lg bg-bg-surface-elevated border border-border-subtle">
                      <span className="text-text-muted block text-[11px]">Member Since</span>
                      <span className="font-bold text-text-primary block mt-0.5">
                        {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}

          {/* TAB 7: DATA, PRIVACY & ACCOUNT */}
          {activeTab === 'privacy' && (
            <div className="space-y-6">
              {/* Storage Footprint & Counts */}
              <Card>
                <CardHeader>
                  <CardTitle>Storage Footprint & Academic Portfolio</CardTitle>
                  <p className="text-xs text-text-muted">
                    Total records and resources persisted in your ScholarTwin knowledge repository.
                  </p>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-center">
                    <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-default">
                      <span className="text-2xl font-extrabold text-brand-teal block">
                        {dashboardStats?.documentsCount || 0}
                      </span>
                      <span className="text-[11px] uppercase font-semibold text-text-muted tracking-wider">
                        Documents
                      </span>
                    </div>
                    <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-default">
                      <span className="text-2xl font-extrabold text-brand-gold block">
                        {dashboardStats?.knowledgeCount || 0}
                      </span>
                      <span className="text-[11px] uppercase font-semibold text-text-muted tracking-wider">
                        Knowledge Items
                      </span>
                    </div>
                    <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-default">
                      <span className="text-2xl font-extrabold text-text-primary block">
                        {dashboardStats?.dsaSolvedCount || 0}
                      </span>
                      <span className="text-[11px] uppercase font-semibold text-text-muted tracking-wider">
                        Solved DSA
                      </span>
                    </div>
                    <div className="p-4 rounded-xl bg-bg-surface-elevated border border-border-default">
                      <span className="text-2xl font-extrabold text-status-success-text block">
                        {dashboardStats?.resumesCount || 0}
                      </span>
                      <span className="text-[11px] uppercase font-semibold text-text-muted tracking-wider">
                        Resumes
                      </span>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Data Export Button */}
              <Card>
                <CardHeader>
                  <CardTitle>Export Personal Data Archive</CardTitle>
                  <p className="text-xs text-text-muted">
                    Download a comprehensive JSON package containing your profile, documents metadata, DSA problem records, resume extractions, and knowledge items.
                  </p>
                </CardHeader>
                <CardContent className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                  <div className="text-xs text-text-secondary max-w-md">
                    Export format is standard JSON complying with digital data portability requirements.
                  </div>
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={handleExportData}
                    isLoading={isExporting}
                    icon={
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
                      </svg>
                    }
                  >
                    Download My Data (JSON)
                  </Button>
                </CardContent>
              </Card>

              {/* Danger Zone: Account Deletion */}
              <div className="p-6 rounded-2xl bg-status-error-bg/20 border border-status-error-border space-y-4">
                <div className="flex items-start gap-3">
                  <div className="w-9 h-9 rounded-xl bg-red-500/20 text-red-500 flex items-center justify-center shrink-0">
                    <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="text-sm font-bold text-status-error-text">Delete Account Permanently</h3>
                    <p className="text-xs text-text-secondary mt-1 leading-relaxed">
                      Permanently wipes your account profile, all uploaded research papers and PDFs from disk storage, extracted resume profiles, DSA attempts, and Knowledge Hub items. This action is irreversible.
                    </p>
                  </div>
                </div>

                <div className="flex justify-end pt-2">
                  <Button
                    variant="error"
                    size="sm"
                    onClick={() => {
                      setDeleteConfirmationText('');
                      setIsDeleteModalOpen(true);
                    }}
                  >
                    Delete My Account
                  </Button>
                </div>
              </div>
            </div>
          )}
        </main>
      </div>

      {/* Logout Confirmation Modal */}
      <Modal
        isOpen={isLogoutModalOpen}
        onClose={() => setIsLogoutModalOpen(false)}
        title="Sign Out of AI ScholarTwin"
        subtitle="Are you sure you want to log out of this session?"
        maxWidth="max-w-md"
      >
        <div className="space-y-4 py-2">
          <p className="text-xs text-text-secondary leading-relaxed">
            Your saved knowledge notes, study targets, and practice progress will remain securely saved.
          </p>
          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="secondary" onClick={() => setIsLogoutModalOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={() => {
                logout();
                window.location.href = '/login';
              }}
            >
              Sign Out
            </Button>
          </div>
        </div>
      </Modal>

      {/* Account Deletion Confirmation Modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        title="Confirm Irreversible Account Deletion"
        subtitle="This action cannot be undone"
        maxWidth="max-w-md"
      >
        <div className="space-y-4 py-2">
          <p className="text-xs text-text-secondary leading-relaxed">
            To confirm permanent deletion of your account and all associated document storage, please type{' '}
            <span className="font-bold text-status-error-text">DELETE MY ACCOUNT</span> below:
          </p>
          <Input
            value={deleteConfirmationText}
            onChange={(e) => setDeleteConfirmationText(e.target.value)}
            placeholder="DELETE MY ACCOUNT"
            required
          />
          <div className="flex justify-end gap-3 pt-3 border-t border-border-subtle">
            <Button variant="secondary" onClick={() => setIsDeleteModalOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="error"
              isLoading={isDeletingAccount}
              disabled={deleteConfirmationText !== 'DELETE MY ACCOUNT' || isDeletingAccount}
              onClick={handleDeleteAccount}
            >
              Permanently Delete
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
