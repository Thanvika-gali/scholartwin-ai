/**
 * API Service for AI ScholarTwin
 * Handles communication with Spring Boot backend through the Vite proxy (/api -> :8080)
 */

const TOKEN_KEY = 'scholartwin_token';

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export function removeStoredToken() {
  localStorage.removeItem(TOKEN_KEY);
}

/**
 * Generic fetch wrapper attaching JSON headers and JWT authorization.
 */
async function request(endpoint, options = {}) {
  const token = getStoredToken();
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    ...(options.headers || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(endpoint, {
    ...options,
    headers,
  });

  // Handle 204 No Content
  if (response.status === 204) {
    return { success: true, status: 204 };
  }

  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const errorMessage = (data && data.message) || (data && data.error) || `Request failed with status ${response.status}`;
    const validationErrors = (data && data.validationErrors) || null;
    const error = new Error(errorMessage);
    error.status = response.status;
    error.validationErrors = validationErrors;
    throw error;
  }

  return data;
}

/**
 * Backend Health Endpoint
 */
export async function fetchHealthStatus() {
  try {
    const data = await request('/api/health');
    return {
      success: true,
      data,
    };
  } catch (error) {
    return {
      success: false,
      error: error.message || 'Unable to connect to backend',
    };
  }
}

/**
 * Registration API
 */
export async function registerApi(fullName, email, password) {
  return await request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ fullName, email, password }),
  });
}

/**
 * Login API
 */
export async function loginApi(email, password) {
  return await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

/**
 * Current User Profile via Auth Endpoint
 */
export async function fetchCurrentUserApi() {
  return await request('/api/auth/me', {
    method: 'GET',
  });
}

/**
 * Profile Retrieval via Users Endpoint
 */
export async function fetchProfileApi() {
  return await request('/api/users/me', {
    method: 'GET',
  });
}

/**
 * Profile Update API (Legacy Name)
 */
export async function updateProfileApi(fullName) {
  return await request('/api/users/me', {
    method: 'PUT',
    body: JSON.stringify({ fullName }),
  });
}

/**
 * Full User Profile Information Update
 */
export async function updateFullProfileApi(profileData) {
  return await request('/api/users/me/profile', {
    method: 'PUT',
    body: JSON.stringify(profileData),
  });
}

/**
 * Academic Preferences Update
 */
export async function updateAcademicPreferencesApi(academicPreferences) {
  return await request('/api/users/me/academic-preferences', {
    method: 'PUT',
    body: JSON.stringify(academicPreferences),
  });
}

/**
 * AI Preferences Update
 */
export async function updateAiPreferencesApi(aiPreferences) {
  return await request('/api/users/me/ai-preferences', {
    method: 'PUT',
    body: JSON.stringify(aiPreferences),
  });
}

/**
 * Notification Preferences Update
 */
export async function updateNotificationPreferencesApi(notificationPreferences) {
  return await request('/api/users/me/notification-preferences', {
    method: 'PUT',
    body: JSON.stringify(notificationPreferences),
  });
}

/**
 * Appearance Theme Update
 */
export async function updateAppearanceApi(theme) {
  return await request('/api/users/me/appearance', {
    method: 'PUT',
    body: JSON.stringify({ theme }),
  });
}

/**
 * Change Account Password
 */
export async function changePasswordApi(currentPassword, newPassword) {
  return await request('/api/users/me/change-password', {
    method: 'POST',
    body: JSON.stringify({ currentPassword, newPassword }),
  });
}

/**
 * Export All Personal Data in JSON
 */
export async function exportUserDataApi() {
  return await request('/api/users/me/export-data', {
    method: 'GET',
  });
}

/**
 * Permanently Delete Account & Personal Data
 */
export async function deleteAccountApi() {
  return await request('/api/users/me/account', {
    method: 'DELETE',
  });
}

/**
 * Logout API
 */
export async function logoutApi() {
  try {
    return await request('/api/auth/logout', {
      method: 'POST',
    });
  } catch {
    // Ignore backend logout errors since client clears token regardless
    return { success: true };
  }
}

/**
 * Dashboard Summary API
 * Returns: { totalItems, totalNotes, totalLinks, recentItems: [...] }
 */
export async function fetchDashboardSummary() {
  return await request('/api/dashboard/summary', {
    method: 'GET',
  });
}

/**
 * Knowledge Items List API
 * Supports optional filters: type (NOTE | LINK), search (string), category (string)
 */
export async function fetchKnowledgeItems(params = {}) {
  const query = new URLSearchParams();
  if (params.type && params.type !== 'ALL') {
    query.set('type', params.type);
  }
  if (params.search && params.search.trim()) {
    query.set('search', params.search.trim());
  }
  if (params.category && params.category.trim() && params.category !== 'ALL') {
    query.set('category', params.category.trim());
  }

  const queryString = query.toString();
  const endpoint = queryString ? `/api/knowledge-items?${queryString}` : '/api/knowledge-items';

  return await request(endpoint, {
    method: 'GET',
  });
}

/**
 * Knowledge Item by ID API
 */
export async function fetchKnowledgeItemById(id) {
  return await request(`/api/knowledge-items/${id}`, {
    method: 'GET',
  });
}

/**
 * Create Knowledge Item API
 */
export async function createKnowledgeItem(itemData) {
  return await request('/api/knowledge-items', {
    method: 'POST',
    body: JSON.stringify(itemData),
  });
}

/**
 * Update Knowledge Item API
 */
export async function updateKnowledgeItem(id, itemData) {
  return await request(`/api/knowledge-items/${id}`, {
    method: 'PUT',
    body: JSON.stringify(itemData),
  });
}

/**
 * Delete Knowledge Item API
 */
export async function deleteKnowledgeItem(id) {
  return await request(`/api/knowledge-items/${id}`, {
    method: 'DELETE',
  });
}

/**
 * -----------------------------------------------------------------------------
 * Milestone 4: Document Processing & Knowledge Extraction APIs
 * -----------------------------------------------------------------------------
 */

/**
 * Upload Document API (supports progress callback via XHR, folders, versioning, duplicate override)
 */
export async function uploadDocumentApi(file, onProgress, options = {}) {
  const token = getStoredToken();
  const formData = new FormData();
  formData.append('file', file);
  if (options.folderId != null) {
    formData.append('folderId', options.folderId);
  }
  if (options.parentDocumentId != null) {
    formData.append('parentDocumentId', options.parentDocumentId);
  }
  if (options.allowDuplicate != null) {
    formData.append('allowDuplicate', options.allowDuplicate);
  }

  if (onProgress) {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', '/api/documents');
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      }

      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          const percent = Math.round((e.loaded / e.total) * 100);
          onProgress(percent);
        }
      };

      xhr.onload = () => {
        try {
          const data = JSON.parse(xhr.responseText || '{}');
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve(data);
          } else {
            const error = new Error(data.message || data.error || `Upload failed with status ${xhr.status}`);
            error.status = xhr.status;
            reject(error);
          }
        } catch (_) {
          reject(new Error('Invalid response from server'));
        }
      };

      xhr.onerror = () => reject(new Error('Network error during file upload'));
      xhr.send(formData);
    });
  }

  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch('/api/documents', {
    method: 'POST',
    headers,
    body: formData,
  });

  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const error = new Error((data && data.message) || (data && data.error) || `Upload failed with status ${response.status}`);
    error.status = response.status;
    throw error;
  }
  return data;
}

/**
 * List Documents API
 */
export async function fetchDocuments(params = {}) {
  const query = new URLSearchParams();
  if (params.status) query.append('status', params.status);
  if (params.format) query.append('format', params.format);
  if (params.search) query.append('search', params.search);

  const queryString = query.toString();
  const endpoint = queryString ? `/api/documents?${queryString}` : '/api/documents';
  return await request(endpoint, { method: 'GET' });
}

/**
 * Get Document Metadata API
 */
export async function fetchDocumentById(id) {
  return await request(`/api/documents/${id}`, { method: 'GET' });
}

/**
 * Get Document Status API
 */
export async function fetchDocumentStatus(id) {
  return await request(`/api/documents/${id}/status`, { method: 'GET' });
}

/**
 * Get Document Content API (full text and page breakdown)
 */
export async function fetchDocumentContent(id) {
  return await request(`/api/documents/${id}/content`, { method: 'GET' });
}

/**
 * Get Document Chunks API
 */
export async function fetchDocumentChunks(id, page = 0, size = 50) {
  return await request(`/api/documents/${id}/chunks?page=${page}&size=${size}`, { method: 'GET' });
}

/**
 * Download Original Document API
 */
export async function downloadDocumentApi(id, filename) {
  const token = getStoredToken();
  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`/api/documents/${id}/download`, {
    method: 'GET',
    headers,
  });

  if (!response.ok) {
    let errorMsg = `Download failed with status ${response.status}`;
    try {
      const errData = await response.json();
      errorMsg = errData.message || errorMsg;
    } catch (_) {}
    throw new Error(errorMsg);
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename || `document_${id}`;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  document.body.removeChild(a);
}

/**
 * Retry Document Processing API
 */
export async function retryDocumentApi(id) {
  return await request(`/api/documents/${id}/retry`, { method: 'POST' });
}

/**
 * Delete Document API
 */
export async function deleteDocumentApi(id) {
  return await request(`/api/documents/${id}`, { method: 'DELETE' });
}

/* ==========================================================================
   Milestone 6: DSA & Coding Progress Tracker APIs
   ========================================================================== */

/**
 * Fetch DSA Dashboard Summary
 */
export async function fetchDsaDashboard() {
  return await request('/api/dsa/dashboard/summary', { method: 'GET' });
}

/**
 * Search & List DSA Problems with pagination and filtering
 */
export async function fetchDsaProblems({
  search = '',
  topic = '',
  difficulty = '',
  platform = '',
  status = '',
  revisionRequired = null,
  page = 0,
  size = 15,
  sort = 'createdAt,desc',
} = {}) {
  const params = new URLSearchParams();
  if (search) params.append('search', search);
  if (topic) params.append('topic', topic);
  if (difficulty) params.append('difficulty', difficulty);
  if (platform) params.append('platform', platform);
  if (status) params.append('status', status);
  if (revisionRequired !== null && revisionRequired !== undefined && revisionRequired !== '') {
    params.append('revisionRequired', revisionRequired);
  }
  params.append('page', page);
  params.append('size', size);
  if (sort) params.append('sort', sort);

  return await request(`/api/dsa/problems?${params.toString()}`, { method: 'GET' });
}

/**
 * Get Problem Detail with attempt history, revisions, and statistics
 */
export async function fetchDsaProblem(id) {
  return await request(`/api/dsa/problems/${id}`, { method: 'GET' });
}

/**
 * Create a new DSA Problem
 */
export async function createDsaProblem(data) {
  return await request('/api/dsa/problems', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * Update an existing DSA Problem
 */
export async function updateDsaProblem(id, data) {
  return await request(`/api/dsa/problems/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

/**
 * Delete a DSA Problem
 */
export async function deleteDsaProblem(id) {
  return await request(`/api/dsa/problems/${id}`, { method: 'DELETE' });
}

/**
 * List 20 standard DSA topics with user progress counts & percentages
 */
export async function fetchDsaTopics() {
  return await request('/api/dsa/topics', { method: 'GET' });
}

/**
 * Get problems for a specific topic drilldown
 */
export async function fetchDsaTopicProblems(topic) {
  return await request(`/api/dsa/topics/${encodeURIComponent(topic)}/problems`, { method: 'GET' });
}

/**
 * Log a solving attempt for a problem
 */
export async function logDsaAttempt(problemId, data) {
  return await request(`/api/dsa/problems/${problemId}/attempts`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * Get all attempts for a problem
 */
export async function fetchDsaAttempts(problemId) {
  return await request(`/api/dsa/problems/${problemId}/attempts`, { method: 'GET' });
}

/**
 * Fetch categorized Revision Queue (overdue, due today, upcoming, completed)
 */
export async function fetchDsaRevisions() {
  return await request('/api/dsa/revisions', { method: 'GET' });
}

/**
 * Schedule a revision for a problem
 */
export async function scheduleDsaRevision(problemId, data) {
  return await request(`/api/dsa/problems/${problemId}/revisions`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * Update a revision (complete, reschedule, notes)
 */
export async function updateDsaRevision(id, data) {
  return await request(`/api/dsa/revisions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

/**
 * Delete a revision
 */
export async function deleteDsaRevision(id) {
  return await request(`/api/dsa/revisions/${id}`, { method: 'DELETE' });
}

/**
 * Get Difficulty Analytics Breakdown
 */
export async function fetchDsaDifficultyAnalytics() {
  return await request('/api/dsa/analytics/difficulty', { method: 'GET' });
}

/**
 * Get Activity Timeline
 */
export async function fetchDsaActivityTimeline(days = 14) {
  return await request(`/api/dsa/analytics/activity?days=${days}`, { method: 'GET' });
}

/**
 * -----------------------------------------------------------------------------
 * Milestone 7: Resume & Career Readiness Intelligence APIs
 * -----------------------------------------------------------------------------
 */

/**
 * Upload Resume API (PDF / DOCX, max 10MB)
 */
export async function uploadResumeApi(file, title, onProgress) {
  const token = getStoredToken();
  const formData = new FormData();
  formData.append('file', file);
  if (title && title.trim()) {
    formData.append('title', title.trim());
  }

  if (onProgress) {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      xhr.open('POST', '/api/career/resumes');
      if (token) {
        xhr.setRequestHeader('Authorization', `Bearer ${token}`);
      }

      xhr.upload.onprogress = (e) => {
        if (e.lengthComputable) {
          const percent = Math.round((e.loaded / e.total) * 100);
          onProgress(percent);
        }
      };

      xhr.onload = () => {
        try {
          const data = JSON.parse(xhr.responseText || '{}');
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve(data);
          } else {
            const error = new Error(data.message || data.error || `Upload failed with status ${xhr.status}`);
            error.status = xhr.status;
            reject(error);
          }
        } catch (_) {
          reject(new Error('Invalid response from server'));
        }
      };

      xhr.onerror = () => reject(new Error('Network error during resume upload'));
      xhr.send(formData);
    });
  }

  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch('/api/career/resumes', {
    method: 'POST',
    headers,
    body: formData,
  });

  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const error = new Error((data && data.message) || (data && data.error) || `Upload failed with status ${response.status}`);
    error.status = response.status;
    throw error;
  }
  return data;
}

/**
 * List all Resumes
 */
export async function fetchResumes() {
  return await request('/api/career/resumes', { method: 'GET' });
}

/**
 * Get Resume Detail with full extracted data and latest analysis
 */
export async function fetchResumeDetail(id) {
  return await request(`/api/career/resumes/${id}`, { method: 'GET' });
}

/**
 * Download original Resume file
 */
export async function downloadResumeApi(id, filename) {
  const token = getStoredToken();
  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`/api/career/resumes/${id}/download`, {
    method: 'GET',
    headers,
  });

  if (!response.ok) {
    let errorMsg = `Download failed with status ${response.status}`;
    try {
      const errData = await response.json();
      errorMsg = errData.message || errorMsg;
    } catch (_) {}
    throw new Error(errorMsg);
  }

  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename || `resume_${id}`;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  document.body.removeChild(a);
}

/**
 * Update Resume (title, extractedData)
 */
export async function updateResumeApi(id, data) {
  return await request(`/api/career/resumes/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

/**
 * Delete Resume
 */
export async function deleteResumeApi(id) {
  return await request(`/api/career/resumes/${id}`, { method: 'DELETE' });
}

/**
 * Set Resume as Primary
 */
export async function setPrimaryResumeApi(id) {
  return await request(`/api/career/resumes/${id}/set-primary`, { method: 'POST' });
}

/**
 * Re-run 100-point Analysis for a Resume
 */
export async function reanalyzeResumeApi(id) {
  return await request(`/api/career/resumes/${id}/analyze`, { method: 'POST' });
}

/**
 * Get Resume Analysis by Resume ID
 */
export async function fetchResumeAnalysis(id) {
  return await request(`/api/career/analysis/resume/${id}`, { method: 'GET' });
}

/**
 * Get Analysis History
 */
export async function fetchAnalysisHistory() {
  return await request('/api/career/analysis/history', { method: 'GET' });
}

/**
 * List Skills in Skills Inventory (optional category filter)
 */
export async function fetchCareerSkills(category = null) {
  const query = category && category !== 'ALL' ? `?category=${category}` : '';
  return await request(`/api/career/skills${query}`, { method: 'GET' });
}

/**
 * Add a Skill manually
 */
export async function addCareerSkill(data) {
  return await request('/api/career/skills', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * Update a Skill
 */
export async function updateCareerSkill(id, data) {
  return await request(`/api/career/skills/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

/**
 * Delete a Skill
 */
export async function deleteCareerSkill(id) {
  return await request(`/api/career/skills/${id}`, { method: 'DELETE' });
}

/**
 * Import skills into inventory from a parsed resume
 */
export async function importSkillsFromResumeApi(resumeId) {
  return await request(`/api/career/skills/import-from-resume/${resumeId}`, { method: 'POST' });
}

/**
 * List available benchmark roles
 */
export async function fetchAvailableRoles() {
  return await request('/api/career/roles', { method: 'GET' });
}

/**
 * Get readiness evaluation for a specific role
 */
export async function fetchRoleReadiness(roleId) {
  return await request(`/api/career/roles/${roleId}`, { method: 'GET' });
}

/**
 * Get detailed skill gap analysis for target role
 */
export async function fetchSkillGapAnalysis(targetRole = 'SDE') {
  return await request(`/api/career/skills/gap-analysis?targetRole=${encodeURIComponent(targetRole)}`, { method: 'GET' });
}

/**
 * Analyze Job Description against user's primary/selected resume
 */
export async function analyzeJobDescriptionApi(data) {
  return await request('/api/career/job-descriptions/analyze', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * List user's past Job Description analyses
 */
export async function fetchJobAnalyses() {
  return await request('/api/career/job-descriptions', { method: 'GET' });
}

/**
 * Get Job Description Analysis Detail by ID
 */
export async function fetchJobAnalysisDetail(id) {
  return await request(`/api/career/job-descriptions/${id}`, { method: 'GET' });
}

/**
 * Delete a Job Description Analysis
 */
export async function deleteJobAnalysisApi(id) {
  return await request(`/api/career/job-descriptions/${id}`, { method: 'DELETE' });
}

/**
 * List Career Action Plan items (optional status filter)
 */
export async function fetchCareerActions(status = null) {
  const query = status && status !== 'ALL' ? `?status=${status}` : '';
  return await request(`/api/career/actions${query}`, { method: 'GET' });
}

/**
 * Create a new Career Action Plan item
 */
export async function createCareerAction(data) {
  return await request('/api/career/actions', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/**
 * Update an existing Career Action Plan item
 */
export async function updateCareerAction(id, data) {
  return await request(`/api/career/actions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

/**
 * Delete a Career Action Plan item
 */
export async function deleteCareerAction(id) {
  return await request(`/api/career/actions/${id}`, { method: 'DELETE' });
}

/**
 * Get Comprehensive Career Dashboard Summary
 */
export async function fetchCareerDashboardSummary() {
  return await request('/api/career/dashboard/summary', { method: 'GET' });
}

/**
 * -----------------------------------------------------------------------------
 * Smart Knowledge Hub 2.0 APIs
 * -----------------------------------------------------------------------------
 */

// --- Folders ---
export async function fetchFoldersApi() {
  return await request('/api/knowledge/folders', { method: 'GET' });
}

export async function fetchFolderTreeApi() {
  return await request('/api/knowledge/folders/tree', { method: 'GET' });
}

export async function createFolderApi(folderData) {
  return await request('/api/knowledge/folders', {
    method: 'POST',
    body: JSON.stringify(folderData),
  });
}

export async function updateFolderApi(id, folderData) {
  return await request(`/api/knowledge/folders/${id}`, {
    method: 'PUT',
    body: JSON.stringify(folderData),
  });
}

export async function moveFolderApi(id, targetParentId) {
  return await request(`/api/knowledge/folders/${id}/move`, {
    method: 'PUT',
    body: JSON.stringify({ targetParentId }),
  });
}

export async function deleteFolderApi(id, reassignOption = 'INBOX', targetFolderId = null) {
  return await request(`/api/knowledge/folders/${id}`, {
    method: 'DELETE',
    body: JSON.stringify({ reassignOption, targetFolderId }),
  });
}

// --- Unified Resources ---
export async function fetchKnowledgeResourcesApi(params = {}) {
  const query = new URLSearchParams();
  if (params.type && params.type !== 'ALL') query.append('type', params.type);
  if (params.folderId != null) query.append('folderId', params.folderId);
  if (params.inboxOnly) query.append('inboxOnly', 'true');
  if (params.favoriteOnly) query.append('favoriteOnly', 'true');
  if (params.pinnedOnly) query.append('pinnedOnly', 'true');
  if (params.archivedOnly) query.append('archivedOnly', 'true');
  if (params.search && params.search.trim()) query.append('search', params.search.trim());
  if (params.sortBy) query.append('sortBy', params.sortBy);

  const qs = query.toString();
  return await request(qs ? `/api/knowledge/resources?${qs}` : '/api/knowledge/resources', { method: 'GET' });
}

export async function fetchSmartInboxApi() {
  return await request('/api/knowledge/inbox', { method: 'GET' });
}

export async function moveResourceApi(resourceType, id, targetFolderId) {
  return await request(`/api/knowledge/resources/${resourceType}/${id}/move`, {
    method: 'PUT',
    body: JSON.stringify({ targetFolderId }),
  });
}

export async function toggleFavoriteResourceApi(resourceType, id) {
  return await request(`/api/knowledge/resources/${resourceType}/${id}/favorite`, { method: 'POST' });
}

export async function togglePinResourceApi(resourceType, id) {
  return await request(`/api/knowledge/resources/${resourceType}/${id}/pin`, { method: 'POST' });
}

export async function toggleArchiveResourceApi(resourceType, id) {
  return await request(`/api/knowledge/resources/${resourceType}/${id}/archive`, { method: 'POST' });
}

export async function recordResourceViewApi(resourceType, id) {
  return await request(`/api/knowledge/resources/${resourceType}/${id}/view`, { method: 'POST' });
}

export async function fetchRecentlyViewedApi(limit = 10) {
  return await request(`/api/knowledge/resources/recent?limit=${limit}`, { method: 'GET' });
}

export async function fetchRelatedResourcesApi(resourceType, id) {
  return await request(`/api/knowledge/resources/${resourceType}/${id}/related`, { method: 'GET' });
}

export async function connectResourceApi(sourceType, sourceId, targetType, targetId, relationType = 'RELATED') {
  return await request('/api/knowledge/resources/connect', {
    method: 'POST',
    body: JSON.stringify({ sourceType, sourceId, targetType, targetId, relationType }),
  });
}

export async function disconnectResourceApi(sourceType, sourceId, targetType, targetId) {
  const query = new URLSearchParams({ sourceType, sourceId, targetType, targetId }).toString();
  return await request(`/api/knowledge/resources/disconnect?${query}`, { method: 'DELETE' });
}

export async function executeBulkActionApi(action, resourceType, resourceIds, targetFolderId = null, tag = null) {
  return await request('/api/knowledge/bulk', {
    method: 'POST',
    body: JSON.stringify({ action, resourceType, resourceIds, targetFolderId, tag }),
  });
}

// --- Suggestions & Smart Organization ---
export async function fetchSuggestionsApi() {
  return await request('/api/knowledge/suggestions', { method: 'GET' });
}

export async function acceptSuggestionApi(id, targetFolderId = null, tags = null) {
  return await request(`/api/knowledge/suggestions/${id}/accept`, {
    method: 'POST',
    body: JSON.stringify({ action: 'ACCEPT', targetFolderId, tags }),
  });
}

export async function rejectSuggestionApi(id) {
  return await request(`/api/knowledge/suggestions/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify({ action: 'REJECT' }),
  });
}

export async function bulkOrganizeApi(suggestionIds = [], autoAcceptAll = false) {
  return await request('/api/knowledge/organize/bulk', {
    method: 'POST',
    body: JSON.stringify({ suggestionIds, autoAcceptAll }),
  });
}

// --- Organization Rules ---
export async function fetchRulesApi() {
  return await request('/api/knowledge/rules', { method: 'GET' });
}

export async function createRuleApi(ruleData) {
  return await request('/api/knowledge/rules', {
    method: 'POST',
    body: JSON.stringify(ruleData),
  });
}

export async function updateRuleApi(id, ruleData) {
  return await request(`/api/knowledge/rules/${id}`, {
    method: 'PUT',
    body: JSON.stringify(ruleData),
  });
}

export async function deleteRuleApi(id) {
  return await request(`/api/knowledge/rules/${id}`, { method: 'DELETE' });
}

// --- Collections ---
export async function fetchCollectionsApi() {
  return await request('/api/knowledge/collections', { method: 'GET' });
}

export async function createCollectionApi(data) {
  return await request('/api/knowledge/collections', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function updateCollectionApi(id, data) {
  return await request(`/api/knowledge/collections/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export async function deleteCollectionApi(id) {
  return await request(`/api/knowledge/collections/${id}`, { method: 'DELETE' });
}

export async function fetchCollectionItemsApi(id) {
  return await request(`/api/knowledge/collections/${id}/items`, { method: 'GET' });
}

export async function addCollectionItemApi(id, resourceType, resourceId, notes = '') {
  return await request(`/api/knowledge/collections/${id}/items`, {
    method: 'POST',
    body: JSON.stringify({ resourceType, resourceId, notes }),
  });
}

export async function removeCollectionItemApi(id, resourceType, resourceId) {
  return await request(`/api/knowledge/collections/${id}/items/${resourceType}/${resourceId}`, {
    method: 'DELETE',
  });
}

// --- Search & Analytics ---
export async function searchKnowledgeApi(params = {}) {
  const query = new URLSearchParams();
  if (params.q) query.append('q', params.q);
  if (params.type && params.type !== 'ALL') query.append('type', params.type);
  if (params.folderId != null) query.append('folderId', params.folderId);
  if (params.favoriteOnly) query.append('favoriteOnly', 'true');
  if (params.archivedOnly) query.append('archivedOnly', 'true');
  if (params.sortBy) query.append('sortBy', params.sortBy);

  const qs = query.toString();
  return await request(qs ? `/api/knowledge/search?${qs}` : '/api/knowledge/search', { method: 'GET' });
}

export async function fetchKnowledgeAnalyticsApi() {
  return await request('/api/knowledge/analytics', { method: 'GET' });
}

// --- Duplicates & Intelligence ---
export async function checkDuplicateDocumentApi(file) {
  const token = getStoredToken();
  const formData = new FormData();
  formData.append('file', file);

  const headers = {};
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch('/api/documents/check-duplicate', {
    method: 'POST',
    headers,
    body: formData,
  });

  const contentType = response.headers.get('content-type');
  const isJson = contentType && contentType.includes('application/json');
  return isJson ? await response.json() : await response.text();
}

export async function fetchDocumentIntelligenceApi(id) {
  return await request(`/api/documents/${id}/intelligence`, { method: 'GET' });
}

export async function regenerateDocumentIntelligenceApi(id) {
  return await request(`/api/documents/${id}/intelligence/regenerate`, { method: 'POST' });
}





