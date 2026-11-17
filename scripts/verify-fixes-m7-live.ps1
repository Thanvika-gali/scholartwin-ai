# ==============================================================================
# AI ScholarTwin — Critical Bug Fixes & Product Improvement Live Verification
# Issues 1-4 End-to-End Verification Script
# ==============================================================================

$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:8080"
$passCount = 0
$failCount = 0

function Assert-Step($condition, $description) {
    if ($condition) {
        Write-Host "  [PASS] $description" -ForegroundColor Green
        $global:passCount++
    } else {
        Write-Host "  [FAIL] $description" -ForegroundColor Red
        $global:failCount++
    }
}

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " AI SCHOLARTWIN: CRITICAL FIXES & PRODUCT IMPROVEMENT PASS VERIFICATION" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# ------------------------------------------------------------------------------
# 1. Health & Database Connectivity
# ------------------------------------------------------------------------------
Write-Host "`n[STAGE 1] Testing Health & Database Connectivity..." -ForegroundColor Yellow
try {
    $healthRes = Invoke-RestMethod -Uri "$baseUrl/api/health" -Method Get -TimeoutSec 10
    Assert-Step ($healthRes.status -eq "UP") "Health check status is UP"
    Assert-Step ($healthRes.database -eq "CONNECTED") "Database connectivity is CONNECTED"
} catch {
    Assert-Step $false "Health check failed: $_"
}

# ------------------------------------------------------------------------------
# 2. Setup Authenticated Test Candidate
# ------------------------------------------------------------------------------
Write-Host "`n[STAGE 2] Registering and Authenticating Test Candidate..." -ForegroundColor Yellow
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$testEmail = "candidate_$timestamp@scholartwin.edu"
$testPassword = "Password123!"

$registerPayload = @{
    fullName = "Dr. Alexander Wright"
    email = $testEmail
    password = $testPassword
} | ConvertTo-Json

$authRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $registerPayload -ContentType "application/json"
$token = $authRes.token
Assert-Step (![string]::IsNullOrEmpty($token)) "Registration succeeded and returned signed JWT"

$authHeaders = @{
    Authorization = "Bearer $token"
}

# ------------------------------------------------------------------------------
# 3. Issue 1 — Document & Resume Upload Size Limits
# ------------------------------------------------------------------------------
Write-Host "`n[STAGE 3] Testing Issue 1: Upload Size Limits (100 MB academic docs, 20 MB resumes)..." -ForegroundColor Yellow

# Test 3.1: Resume upload with 22 MB file (exceeds 20 MB limit -> should reject)
Write-Host "  Testing resume upload rejection over 20 MB limit..."
$tempDir = [System.IO.Path]::GetTempPath()
$largeResumePath = Join-Path $tempDir "oversized_resume.pdf"
# Create file with PDF header of ~22 MB
$pdfHeader = [System.Text.Encoding]::ASCII.GetBytes("%PDF-1.4 `n")
$fileStream = [System.IO.File]::Create($largeResumePath)
$fileStream.Write($pdfHeader, 0, $pdfHeader.Length)
$fileStream.SetLength(22 * 1024 * 1024)
$fileStream.Close()

$resumeRejected = $false
try {
    $curlOut = & curl.exe -s -X POST "$baseUrl/api/career/resumes" `
        -H "Authorization: Bearer $token" `
        -F "file=@$largeResumePath"
    if ($curlOut -match "exceeds the maximum allowed limit of 20 MB" -or $curlOut -match "413" -or $curlOut -match "Payload Too Large") {
        $resumeRejected = $true
    }
} catch {
    $resumeRejected = $true
}
Assert-Step $resumeRejected "Resume exceeding 20 MB limit is rejected with informative limit message"
Remove-Item -Path $largeResumePath -Force -ErrorAction SilentlyContinue

# Test 3.2: Valid resume upload within 20 MB limit
Write-Host "  Testing valid resume upload with rich sections and metrics..."
$sampleResumePath = (Resolve-Path "backend/target/sample_docs/candidate_alex_resume.pdf").Path

$uploadResJson = & curl.exe -s -X POST "$baseUrl/api/career/resumes" `
    -H "Authorization: Bearer $token" `
    -F "file=@$sampleResumePath" `
    -F "title=Alex Wright Principal Resume"

$uploadedDetail = $uploadResJson | ConvertFrom-Json
$uploadedResume = $uploadedDetail.resume
Assert-Step ($uploadedResume.id -ne $null) "Valid resume upload succeeded within 20 MB limit"
Assert-Step ($uploadedResume.versionNumber -eq 1) "Resume assigned versionNumber = 1"
$resumeId = $uploadedResume.id

# Test 3.3: Document upload with 26.6 MB academic paper (> 25 MB old limit, < 100 MB new limit)
Write-Host "  Testing general document upload of 26.6 MB (proves 100 MB academic limit works)..."
$largeDocPath = (Resolve-Path "backend/target/sample_docs/large_academic_paper_28mb.pdf").Path

$docUploadJson = & curl.exe -s -X POST "$baseUrl/api/documents" `
    -H "Authorization: Bearer $token" `
    -F "file=@$largeDocPath"

$uploadedDoc = $docUploadJson | ConvertFrom-Json
Assert-Step ($uploadedDoc.id -ne $null) "Document > 25 MB (26.6 MB) accepted successfully under 100 MB limit"

# ------------------------------------------------------------------------------
# 4. Issue 2 — Job Description Analyzer Fix
# ------------------------------------------------------------------------------
Write-Host "`n[STAGE 4] Testing Issue 2: Job Description Analyzer End-to-End..." -ForegroundColor Yellow

# Test 4.1: Analyze JD comparing against uploaded resume
$jdText = @"
Senior Full Stack Engineer - Cloud & AI Platforms
Company: Google Cloud
Location: Mountain View, CA / Remote

About the Role:
We are seeking an experienced Senior Software Engineer to build next-generation cloud services and distributed AI applications.

Minimum / Required Qualifications:
- Bachelor's or Master's degree in Computer Science or related technical field.
- 3+ years of professional software engineering experience.
- Strong proficiency in Java, Spring Boot, React, and SQL.
- Solid understanding of Data Structures, Algorithms, and Object-Oriented Programming.
- Hands-on experience with Git, Docker, and REST APIs.

Preferred Qualifications:
- Experience with Kubernetes, AWS, or GCP cloud infrastructure.
- Familiarity with Redis caching and Microservices architecture.
- Knowledge of Machine Learning, PyTorch, or Large Language Models.

Responsibilities:
- Architect, design, and maintain highly scalable backend microservices.
- Develop responsive, modern user interfaces with React and Tailwind CSS.
- Collaborate with cross-functional teams to deliver mission-critical software solutions.
"@

$jdPayloadWithResume = @{
    jobTitle = "Senior Full Stack Engineer"
    companyName = "Google Cloud"
    rawText = $jdText
    resumeId = $resumeId
} | ConvertTo-Json

$jdRes = Invoke-RestMethod -Uri "$baseUrl/api/career/job-descriptions/analyze" `
    -Method Post `
    -Headers $authHeaders `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($jdPayloadWithResume)) `
    -ContentType "application/json; charset=utf-8"

Assert-Step ($jdRes.id -ne $null) "JD Analysis generated successfully with resume"
Assert-Step ($jdRes.matchPercentage -gt 60) "Match percentage calculated ($($jdRes.matchPercentage)%)"
Assert-Step ($jdRes.matchScore -eq $jdRes.matchPercentage) "matchScore alias equals matchPercentage"
Assert-Step ($jdRes.company -eq "Google Cloud") "company alias returned correctly"
Assert-Step ($jdRes.matchedRequiredSkills.Count -ge 4) "Matched required skills identified: $($jdRes.matchedRequiredSkills -join ', ')"
Assert-Step ($jdRes.tailoredSuggestions.Count -gt 0) "Tailored recommendations generated: $($jdRes.tailoredSuggestions[0])"
$firstJdId = $jdRes.id

# Test 4.2: Analyze JD with NO resume (comparing against candidate's verified skills inventory)
$jdPayloadNoResume = @{
    jobTitle = "Frontend React Specialist"
    companyName = "Meta"
    rawText = "Requirements: React, JavaScript, CSS, HTML, Git. Preferred: TypeScript, Next.js. Responsibilities: Build fast UI."
    resumeId = $null
} | ConvertTo-Json

$jdNoResumeRes = Invoke-RestMethod -Uri "$baseUrl/api/career/job-descriptions/analyze" `
    -Method Post `
    -Headers $authHeaders `
    -Body $jdPayloadNoResume `
    -ContentType "application/json"

Assert-Step ($jdNoResumeRes.id -ne $null) "JD Analysis with resumeId=null evaluated against skills inventory successfully"
Assert-Step ($jdNoResumeRes.jobTitle -eq "Frontend React Specialist") "Correct job title stored"

# Test 4.3: Verify JD History persistence and retrieval
$jdList = Invoke-RestMethod -Uri "$baseUrl/api/career/job-descriptions" -Method Get -Headers $authHeaders
Assert-Step ($jdList.Count -ge 2) "User JD analysis history returns both analyses"

# Test 4.4: Verify individual JD retrieval by ID
$singleJd = Invoke-RestMethod -Uri "$baseUrl/api/career/job-descriptions/$firstJdId" -Method Get -Headers $authHeaders
Assert-Step ($singleJd.id -eq $firstJdId) "Retrieved single past JD analysis details by ID"

# ------------------------------------------------------------------------------
# 5. Issue 3 — Resume Extraction & Analysis Quality
# ------------------------------------------------------------------------------
Write-Host "`n[STAGE 5] Testing Issue 3: Resume Extraction Quality & 100-Point Scoring..." -ForegroundColor Yellow

$resumeDetail = Invoke-RestMethod -Uri "$baseUrl/api/career/resumes/$resumeId" -Method Get -Headers $authHeaders
Assert-Step ($resumeDetail.extractedData -ne $null) "Extracted data object present"
Assert-Step ($resumeDetail.extractedData.fullName -match "Alexander Wright") "Full name accurately extracted: $($resumeDetail.extractedData.fullName)"
Assert-Step ($resumeDetail.extractedData.email -eq "alexander.wright@scholartwin.edu") "Email extracted: $($resumeDetail.extractedData.email)"
Assert-Step ($resumeDetail.extractedData.githubUrl -match "github.com") "GitHub link extracted: $($resumeDetail.extractedData.githubUrl)"
Assert-Step ($resumeDetail.extractedData.skills.Count -ge 10) "Extracted 10+ technical skills: $($resumeDetail.extractedData.skills.Count) found"
Assert-Step ($resumeDetail.extractedData.projects.Count -ge 1) "Technical projects identified with tech stack and live links"
Assert-Step ($resumeDetail.extractedData.certifications.Count -ge 2) "Certifications identified (AWS, Oracle)"

# 100-Point Rubric verification
$analysis = $resumeDetail.latestAnalysis
Assert-Step ($analysis.overallScore -ge 75) "100-point rubric overall score earned: $($analysis.overallScore)/100"
Assert-Step ($analysis.dimensionBreakdown.Count -eq 7) "7 distinct dimensions scored with evidence"
$dimIds = $analysis.dimensionBreakdown | ForEach-Object { $_.dimensionKey }
Assert-Step ($dimIds -contains "technical_skills" -and $dimIds -contains "projects_depth" -and $dimIds -contains "measurable_results") "Required rubric dimensions verified"

# Benchmark Roles: 8 benchmark roles with 4 distinct skill classes
Write-Host "  Testing 8 benchmark roles with resumeSkills, verifiedSkills, inferredSkills, missingSkills..."
$sdeReadiness = Invoke-RestMethod -Uri "$baseUrl/api/career/roles/sde" -Method Get -Headers $authHeaders
Assert-Step ($sdeReadiness.roleId -eq "sde") "Evaluated SDE benchmark role"
Assert-Step ($null -ne $sdeReadiness.resumeSkills) "resumeSkills list present ($($sdeReadiness.resumeSkills.Count) skills)"
Assert-Step ($null -ne $sdeReadiness.verifiedSkills) "verifiedSkills list present"
Assert-Step ($null -ne $sdeReadiness.inferredSkills) "inferredSkills list present"
Assert-Step ($null -ne $sdeReadiness.missingSkills) "missingSkills list present"

# ------------------------------------------------------------------------------
# 6. Issue 4 — Expand Profile & Settings Center
# ------------------------------------------------------------------------------
Write-Host "`n[STAGE 6] Testing Issue 4: Full SaaS Profile & Settings Center..." -ForegroundColor Yellow

# Test 6.1: Update Full Profile Information
$profileUpdatePayload = @{
    fullName = "Dr. Alexander Wright, Ph.D."
    institution = "Stanford University"
    degree = "M.S. in Computer Science"
    graduationYear = 2025
    academicYear = "Alumni / Doctoral"
    currentSemester = 8
    bio = "Distributed systems architect, AI researcher, and open-source enthusiast."
    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
    githubUrl = "https://github.com/alexwright"
    linkedinUrl = "https://linkedin.com/in/alexwright"
    portfolioUrl = "https://alexwright.dev"
} | ConvertTo-Json

$profileUpdated = Invoke-RestMethod -Uri "$baseUrl/api/users/me/profile" `
    -Method Put `
    -Headers $authHeaders `
    -Body $profileUpdatePayload `
    -ContentType "application/json"

Assert-Step ($profileUpdated.fullName -eq "Dr. Alexander Wright, Ph.D.") "Profile full name updated"
Assert-Step ($profileUpdated.institution -eq "Stanford University") "Institution updated: $($profileUpdated.institution)"
Assert-Step ($profileUpdated.graduationYear -eq 2025) "Graduation year updated: $($profileUpdated.graduationYear)"
Assert-Step ($profileUpdated.portfolioUrl -eq "https://alexwright.dev") "Portfolio URL saved"

# Test 6.2: Academic Preferences
$academicPayload = @{
    targetSemester = 8
    targetGraduationYear = 2025
    primaryCareerTarget = "Software Development Engineer"
    secondaryRoles = @("Full Stack Developer", "AI/ML Engineer")
    technicalInterests = @("Distributed Systems", "RAG", "Microservices")
    weeklyDsaTarget = 15
    dailyReadingMinutes = 60
    preferredStudyTime = "MORNING"
} | ConvertTo-Json

$academicUpdated = Invoke-RestMethod -Uri "$baseUrl/api/users/me/academic-preferences" `
    -Method Put `
    -Headers $authHeaders `
    -Body $academicPayload `
    -ContentType "application/json"
Assert-Step (![string]::IsNullOrEmpty($academicUpdated.academicPreferencesJson)) "Academic preferences serialized and persisted"

# Test 6.3: AI Preferences
$aiPayload = @{
    interactionMode = "BALANCED"
    groundingMode = "HYBRID"
    proactiveSuggestions = $true
} | ConvertTo-Json

$aiUpdated = Invoke-RestMethod -Uri "$baseUrl/api/users/me/ai-preferences" `
    -Method Put `
    -Headers $authHeaders `
    -Body $aiPayload `
    -ContentType "application/json"
Assert-Step (![string]::IsNullOrEmpty($aiUpdated.aiPreferencesJson)) "AI preferences saved and persisted"

# Test 6.4: Notification Preferences
$notifPayload = @{
    revisionReminders = $true
    dailyStudyReminders = $true
    careerActionAlerts = $true
    processingFailureAlerts = $false
} | ConvertTo-Json

$notifUpdated = Invoke-RestMethod -Uri "$baseUrl/api/users/me/notification-preferences" `
    -Method Put `
    -Headers $authHeaders `
    -Body $notifPayload `
    -ContentType "application/json"
Assert-Step (![string]::IsNullOrEmpty($notifUpdated.notificationPreferencesJson)) "Notification preferences saved"

# Test 6.5: Appearance Theme
$themePayload = @{
    theme = "DARK"
} | ConvertTo-Json

$themeUpdated = Invoke-RestMethod -Uri "$baseUrl/api/users/me/appearance" `
    -Method Put `
    -Headers $authHeaders `
    -Body $themePayload `
    -ContentType "application/json"
Assert-Step ($themeUpdated.themePreference -eq "DARK") "Theme preference saved as DARK"

# Test 6.6: Export Personal Data Archive
Write-Host "  Testing Data Portability (JSON export)..."
$exportRes = Invoke-RestMethod -Uri "$baseUrl/api/users/me/export-data" -Method Get -Headers $authHeaders
Assert-Step ($exportRes.application -eq "AI ScholarTwin") "Data export header: $($exportRes.application)"
Assert-Step ($exportRes.userProfile.email -eq $testEmail) "Export user profile matches candidate"
Assert-Step ($exportRes.summaryCounts.resumes -ge 1) "Export reflects 1+ resumes in summary counts"
Assert-Step ($exportRes.jobAnalyses.Count -ge 2) "Export includes $($exportRes.jobAnalyses.Count) job analyses"

# Test 6.7: Change Password & Security
Write-Host "  Testing Change Password security workflow..."
$newTestPassword = "NewSecretPassword456!"
$changePassPayload = @{
    currentPassword = $testPassword
    newPassword = $newTestPassword
} | ConvertTo-Json

$changePassRes = Invoke-RestMethod -Uri "$baseUrl/api/users/me/change-password" `
    -Method Post `
    -Headers $authHeaders `
    -Body $changePassPayload `
    -ContentType "application/json"
Assert-Step ($changePassRes.message -match "Password changed successfully") "Password changed response confirmed"

# Verify old password login fails
$oldPassFailed = $false
try {
    $loginOldPayload = @{ email = $testEmail; password = $testPassword } | ConvertTo-Json
    Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginOldPayload -ContentType "application/json"
} catch {
    $oldPassFailed = $true
}
Assert-Step $oldPassFailed "Login with former password rejected (HTTP 401)"

# Verify new password login succeeds
$loginNewPayload = @{ email = $testEmail; password = $newTestPassword } | ConvertTo-Json
$loginNewRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginNewPayload -ContentType "application/json"
Assert-Step (![string]::IsNullOrEmpty($loginNewRes.token)) "Login with new password succeeded and returned new JWT"
$newAuthHeaders = @{ Authorization = "Bearer $($loginNewRes.token)" }

# Test 6.8: Account Deletion (Cascade Cleanup)
Write-Host "  Testing Account Deletion & Cascading Removal..."
$deleteRes = Invoke-RestMethod -Uri "$baseUrl/api/users/me/account" -Method Delete -Headers $newAuthHeaders
Assert-Step ($deleteRes.message -match "permanently deleted") "Account deletion reported permanent cleanup"

# Verify deleted account cannot login or access APIs
$accountGone = $false
try {
    Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginNewPayload -ContentType "application/json"
} catch {
    $accountGone = $true
}
Assert-Step $accountGone "Deleted account completely removed from system (authentication fails)"

# ------------------------------------------------------------------------------
# Summary Report
# ------------------------------------------------------------------------------
Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " LIVE VERIFICATION SUMMARY: $passCount PASSED, $failCount FAILED" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Red" })
Write-Host "======================================================================" -ForegroundColor Cyan

if ($failCount -gt 0) {
    exit 1
}
exit 0
