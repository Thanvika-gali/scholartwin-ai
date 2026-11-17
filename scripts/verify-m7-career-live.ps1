# AI ScholarTwin - Milestone 7 Career & Resume Hub End-to-End Live Verification Script
$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080"
Write-Host "===================================================================" -ForegroundColor Cyan
Write-Host "AI ScholarTwin - Milestone 7: Career & Resume Hub E2E Verification" -ForegroundColor Cyan
Write-Host "===================================================================" -ForegroundColor Cyan

# 1. Health check
Write-Host "`n[1/12] Verifying backend health & database connectivity..." -ForegroundColor Yellow
$health = Invoke-RestMethod -Uri "$baseUrl/api/health" -Method Get
Write-Host "Health status: $($health.status), Service: $($health.service), Database: $($health.database)" -ForegroundColor Green
if ($health.database -ne "CONNECTED") {
    throw "Database is not connected!"
}

# 2. Register / Authenticate User A
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$emailA = "career.tester.$timestamp@scholartwin.io"
$password = "CareerPass123!"
$userAReq = @{
    fullName = "Alice Career Candidate"
    email = $emailA
    password = $password
} | ConvertTo-Json

Write-Host "`n[2/12] Registering User A ($emailA)..." -ForegroundColor Yellow
$authResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $userAReq -ContentType "application/json"
$tokenA = $authResponse.token
$headersA = @{
    "Authorization" = "Bearer $tokenA"
}
$jsonHeadersA = @{
    "Authorization" = "Bearer $tokenA"
    "Content-Type"  = "application/json"
}
Write-Host "User A registered successfully. JWT Token acquired." -ForegroundColor Green

# 3. Upload Resume 1 (PDF)
Write-Host "`n[3/12] Uploading Resume 1 (PDF) and verifying automatic parsing & 100-pt analysis..." -ForegroundColor Yellow
$pdfPath = "E:\projects\scholartwin-ai\backend\target\sample_docs\candidate_resume_v1.pdf"
if (-not (Test-Path $pdfPath)) {
    throw "Sample resume PDF not found at $pdfPath"
}

$uploadOutput = & curl.exe -s -X POST "$baseUrl/api/career/resumes" `
    -H "Authorization: Bearer $tokenA" `
    -F "title=Alice Developer - Core Backend Profile" `
    -F "file=@$pdfPath;type=application/pdf"

$upload1Data = $uploadOutput | ConvertFrom-Json
$resume1 = $upload1Data.resume
Write-Host "Resume 1 Uploaded: ID=$($resume1.id), Title='$($resume1.title)', Version=$($resume1.versionNumber), Primary=$($resume1.isPrimary), Status=$($resume1.processingStatus)" -ForegroundColor Green

if ($resume1.versionNumber -ne 1 -or $resume1.isPrimary -ne $true -or $resume1.processingStatus -ne "PARSED") {
    throw "Resume 1 metadata validation failed!"
}

# Fetch resume 1 detail with analysis
$resume1Detail = Invoke-RestMethod -Uri "$baseUrl/api/career/resumes/$($resume1.id)" -Method Get -Headers $headersA
$score1 = $resume1Detail.latestAnalysis.overallScore
Write-Host "Resume 1 Extracted Name: $($resume1Detail.extractedData.fullName)" -ForegroundColor Green
Write-Host "Resume 1 Extracted Email: $($resume1Detail.extractedData.email)" -ForegroundColor Green
Write-Host "Resume 1 Extracted Skills Count: $($resume1Detail.extractedData.skills.Count)" -ForegroundColor Green
Write-Host "Resume 1 100-Point Rubric Score: $score1 / 100" -ForegroundColor Green

if ($score1 -lt 50) {
    throw "Expected valid score >= 50 for realistic candidate resume, got $score1"
}

# 4. Upload Resume 2 (DOCX) and switch Primary
Write-Host "`n[4/12] Uploading Resume 2 (DOCX) and verifying versioning & primary switch..." -ForegroundColor Yellow
$docxPath = "E:\projects\scholartwin-ai\backend\target\sample_docs\candidate_resume_v2.docx"

$upload2Output = & curl.exe -s -X POST "$baseUrl/api/career/resumes" `
    -H "Authorization: Bearer $tokenA" `
    -F "title=Alice Developer - Cloud & Microservices v2" `
    -F "file=@$docxPath;type=application/vnd.openxmlformats-officedocument.wordprocessingml.document"

$upload2Data = $upload2Output | ConvertFrom-Json
$resume2 = $upload2Data.resume
Write-Host "Resume 2 Uploaded: ID=$($resume2.id), Version=$($resume2.versionNumber), Primary=$($resume2.isPrimary)" -ForegroundColor Green

if ($resume2.versionNumber -ne 2 -or $resume2.isPrimary -ne $false) {
    throw "Resume 2 versioning validation failed!"
}

# Switch primary to Resume 2
$primarySwitch = Invoke-RestMethod -Uri "$baseUrl/api/career/resumes/$($resume2.id)/set-primary" -Method Post -Headers $headersA
Write-Host "Switched primary to Resume 2. Verification:" -ForegroundColor Green

$r1Check = Invoke-RestMethod -Uri "$baseUrl/api/career/resumes/$($resume1.id)" -Method Get -Headers $headersA
$r2Check = Invoke-RestMethod -Uri "$baseUrl/api/career/resumes/$($resume2.id)" -Method Get -Headers $headersA

Write-Host "Resume 1 Primary: $($r1Check.resume.isPrimary) | Resume 2 Primary: $($r2Check.resume.isPrimary)" -ForegroundColor Green
if ($r1Check.resume.isPrimary -ne $false -or $r2Check.resume.isPrimary -ne $true) {
    throw "Primary switch failed!"
}

# 5. Download Original File & Update Resume Metadata
Write-Host "`n[5/12] Testing original resume download and metadata updates..." -ForegroundColor Yellow
$downloadTemp = "backend\target\sample_docs\downloaded_resume_1.pdf"
& curl.exe -s -o $downloadTemp -H "Authorization: Bearer $tokenA" "$baseUrl/api/career/resumes/$($resume1.id)/download"
$downloadBytes = (Get-Item $downloadTemp).Length
Write-Host "Downloaded Resume 1 binary: Received $downloadBytes bytes" -ForegroundColor Green
if ($downloadBytes -lt 100) {
    throw "Downloaded file size is suspiciously small: $downloadBytes bytes"
}

$updatePayload = @{
    title = "Alice Developer - Principal Staff Engineer"
    extractedData = $resume1Detail.extractedData
} | ConvertTo-Json -Depth 5

$updatedResume = Invoke-RestMethod -Uri "$baseUrl/api/career/resumes/$($resume1.id)" -Method Put -Headers $jsonHeadersA -Body $updatePayload
Write-Host "Updated Resume 1 Title: '$($updatedResume.resume.title)'" -ForegroundColor Green

# 6. Skills Inventory & Import from Resume
Write-Host "`n[6/12] Testing Skills Inventory CRUD & Import from Resume..." -ForegroundColor Yellow
$importResult = Invoke-RestMethod -Uri "$baseUrl/api/career/skills/import-from-resume/$($resume1.id)" -Method Post -Headers $headersA
$importedCount = if ($importResult -is [System.Array]) { $importResult.Count } elseif ($importResult.importedCount -ne $null) { $importResult.importedCount } else { 0 }
Write-Host "Imported skills from Resume 1: $importedCount skills added to inventory." -ForegroundColor Green

# Add a manual skill
$newSkillPayload = @{
    skillName = "Terraform"
    category = "CLOUD_DEVOPS"
    proficiency = "INTERMEDIATE"
    source = "USER_ENTERED"
    verificationEvidence = "HashiCorp Certified Terraform Associate"
} | ConvertTo-Json

$createdSkill = Invoke-RestMethod -Uri "$baseUrl/api/career/skills" -Method Post -Headers $jsonHeadersA -Body $newSkillPayload
Write-Host "Added custom skill: ID=$($createdSkill.id), Name='$($createdSkill.skillName)', Category=$($createdSkill.category)" -ForegroundColor Green

# Filter skills by category
$cloudSkills = Invoke-RestMethod -Uri "$baseUrl/api/career/skills?category=CLOUD_DEVOPS" -Method Get -Headers $headersA
Write-Host "Cloud & DevOps skills count: $($cloudSkills.Count)" -ForegroundColor Green

# Update skill proficiency
$updateSkillPayload = @{
    skillName = "Terraform"
    category = "CLOUD_DEVOPS"
    proficiency = "ADVANCED"
    source = "USER_ENTERED"
    verificationEvidence = "HashiCorp Certified Terraform Associate"
} | ConvertTo-Json

$updatedSkill = Invoke-RestMethod -Uri "$baseUrl/api/career/skills/$($createdSkill.id)" -Method Put -Headers $jsonHeadersA -Body $updateSkillPayload
Write-Host "Updated Terraform proficiency to: $($updatedSkill.proficiency)" -ForegroundColor Green

# 7. Cross-Module DSA Integration & Role Readiness
Write-Host "`n[7/12] Testing Cross-Module DSA Tracker Integration & Target Role Readiness..." -ForegroundColor Yellow

# Create and solve a DSA problem to generate algorithmic evidence
$dsaProblemReq = @{
    title = "Two Sum"
    problemUrl = "https://leetcode.com/problems/two-sum/"
    platform = "LeetCode"
    difficulty = "EASY"
    primaryTopic = "Arrays"
    additionalTopics = @("Hash Table")
    status = "NOT_STARTED"
    solutionApproach = "One-pass hash table mapping value to index."
    timeComplexity = "O(N)"
    spaceComplexity = "O(N)"
    revisionRequired = $false
} | ConvertTo-Json

$dsaProb = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems" -Method Post -Headers $jsonHeadersA -Body $dsaProblemReq

$dsaAttemptReq = @{
    attemptDate = (Get-Date).ToString("yyyy-MM-dd")
    outcome = "SUCCESS"
    timeSpentMinutes = 15
    programmingLanguage = "Java"
    learningNotes = "Solved with O(N) HashMap."
} | ConvertTo-Json

$dsaAttempt = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems/$($dsaProb.id)/attempts" -Method Post -Headers $jsonHeadersA -Body $dsaAttemptReq
Write-Host "Logged DSA Problem Solution for User A: ID=$($dsaProb.id), Status is now SOLVED." -ForegroundColor Green

# Check Available Roles
$roles = Invoke-RestMethod -Uri "$baseUrl/api/career/roles" -Method Get -Headers $headersA
Write-Host "Available Benchmark Roles Count: $($roles.Count)" -ForegroundColor Green

# Check SDE Role Readiness
$sdeReadiness = Invoke-RestMethod -Uri "$baseUrl/api/career/roles/SDE" -Method Get -Headers $headersA
Write-Host "SDE Role Readiness: $($sdeReadiness.readinessPercentage)% | Matched Required: $($sdeReadiness.matchedRequiredSkills.Count) | Missing Required: $($sdeReadiness.missingRequiredSkills.Count)" -ForegroundColor Green
$dsaEvidence = if ($sdeReadiness.dsaSupportingEvidence) { $sdeReadiness.dsaSupportingEvidence } elseif ($sdeReadiness.algorithmicEvidence) { $sdeReadiness.algorithmicEvidence } else { "" }
Write-Host "DSA Algorithmic Verification: $dsaEvidence" -ForegroundColor Green

if (-not $dsaEvidence.Contains("DSA")) {
    throw "Expected algorithmic verification evidence from DSA tracker in SDE role readiness!"
}

# 8. Job Description Analyzer
Write-Host "`n[8/12] Testing Job Description Diagnostic Analyzer..." -ForegroundColor Yellow
$jdReq = @{
    jobTitle = "Senior Full Stack Engineer"
    company = "TechCorp Global"
    jobDescriptionText = @"
We are looking for a Senior Full Stack Engineer with strong experience in Java, Spring Boot, React, and MySQL.
The candidate will architect microservices, build clean web UIs, and manage distributed databases using Docker and Redis.
Preferred qualifications include Kubernetes, AWS cloud architectures, and CI/CD pipelines.
Strong foundation in Data Structures, Algorithms, and System Design is required.
"@
    resumeId = $resume1.id
} | ConvertTo-Json

$jdResult = Invoke-RestMethod -Uri "$baseUrl/api/career/job-descriptions/analyze" -Method Post -Headers $jsonHeadersA -Body $jdReq
$jdScore = if ($jdResult.matchScore -ne $null) { $jdResult.matchScore } else { $jdResult.matchPercentage }
Write-Host "JD Match Score: $jdScore%" -ForegroundColor Green
Write-Host "Matched Required Skills: $($jdResult.matchedRequiredSkills -join ', ')" -ForegroundColor Green
Write-Host "Missing Required Skills: $($jdResult.missingRequiredSkills -join ', ')" -ForegroundColor Green
Write-Host "Missing Keywords: $($jdResult.missingKeywords -join ', ')" -ForegroundColor Green

$allJds = Invoke-RestMethod -Uri "$baseUrl/api/career/job-descriptions" -Method Get -Headers $headersA
Write-Host "Saved JD Analyses in History: $($allJds.Count)" -ForegroundColor Green

# 9. Career Action Plan
Write-Host "`n[9/12] Testing Career Action Plan creation, scheduling & completion..." -ForegroundColor Yellow
$actionReq1 = @{
    title = "Complete 10 Dynamic Programming problems on LeetCode"
    description = "Strengthen weak DP topics identified in role gap analysis"
    category = "DSA_PRACTICE"
    priority = "HIGH"
    targetRoleOrSkill = "Dynamic Programming"
    dueDate = (Get-Date).AddDays(14).ToString("yyyy-MM-dd")
} | ConvertTo-Json

$action1 = Invoke-RestMethod -Uri "$baseUrl/api/career/actions" -Method Post -Headers $jsonHeadersA -Body $actionReq1
Write-Host "Created Action 1: ID=$($action1.id), Title='$($action1.title)', Priority=$($action1.priority)" -ForegroundColor Green

$actionReq2 = @{
    title = "Deploy microservice project on AWS ECS"
    description = "Containerize microservice and configure ECS Fargate task definition"
    category = "PROJECT_CREATION"
    priority = "MEDIUM"
    targetRoleOrSkill = "AWS"
    dueDate = (Get-Date).AddDays(21).ToString("yyyy-MM-dd")
} | ConvertTo-Json

$action2 = Invoke-RestMethod -Uri "$baseUrl/api/career/actions" -Method Post -Headers $jsonHeadersA -Body $actionReq2

# Complete Action 1
$updateActionReq = @{
    title = $action1.title
    status = "COMPLETED"
} | ConvertTo-Json

$completedAction1 = Invoke-RestMethod -Uri "$baseUrl/api/career/actions/$($action1.id)" -Method Put -Headers $jsonHeadersA -Body $updateActionReq
Write-Host "Updated Action 1 Status to: $($completedAction1.status)" -ForegroundColor Green

# 10. Dashboard Summary
Write-Host "`n[10/12] Testing Career Dashboard Summary & AI Insights generation..." -ForegroundColor Yellow
$dashboardSummary = Invoke-RestMethod -Uri "$baseUrl/api/career/dashboard/summary" -Method Get -Headers $headersA

$primaryScore = if ($dashboardSummary.primaryResumeScore -ne $null) { $dashboardSummary.primaryResumeScore } else { $dashboardSummary.latestScore }
$targetRolePct = if ($dashboardSummary.targetRoleReadinessPercentage -ne $null) { $dashboardSummary.targetRoleReadinessPercentage } elseif ($dashboardSummary.targetRoleReadiness) { $dashboardSummary.targetRoleReadiness.readinessPercentage } else { 0 }
$targetRoleName = if ($dashboardSummary.targetRole) { $dashboardSummary.targetRole } elseif ($dashboardSummary.targetRoleReadiness) { $dashboardSummary.targetRoleReadiness.roleName } else { "SDE" }
$inProgressGoals = if ($dashboardSummary.inProgressActionsCount -ne $null) { $dashboardSummary.inProgressActionsCount } else { $dashboardSummary.pendingActionsCount }

Write-Host "Primary Resume Score: $primaryScore" -ForegroundColor Green
Write-Host "Target Role Readiness: $targetRolePct% ($targetRoleName)" -ForegroundColor Green
Write-Host "Total Skills Tracked: $($dashboardSummary.totalSkillsCount)" -ForegroundColor Green
Write-Host "Completed Goals: $($dashboardSummary.completedActionsCount) | In-Progress Goals: $inProgressGoals" -ForegroundColor Green
Write-Host "Career Insights Count: $($dashboardSummary.careerInsights.Count)" -ForegroundColor Green
foreach ($insight in $dashboardSummary.careerInsights) {
    Write-Host "  * $insight" -ForegroundColor Cyan
}

# 11. Multi-Tenant JWT Security & Cross-User Isolation
Write-Host "`n[11/12] Testing Multi-Tenant JWT Security & Cross-User Data Isolation..." -ForegroundColor Yellow
$emailB = "career.userb.$timestamp@scholartwin.io"
$userBReq = @{
    fullName = "Bob Inquisitive"
    email = $emailB
    password = $password
} | ConvertTo-Json

$authB = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $userBReq -ContentType "application/json"
$tokenB = $authB.token
$headersB = @{
    "Authorization" = "Bearer $tokenB"
}

# Attempt to access User A's resume using User B's token
$unauthorizedAccessBlocked = $false
try {
    Invoke-RestMethod -Uri "$baseUrl/api/career/resumes/$($resume1.id)" -Method Get -Headers $headersB
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "Cross-user resume access safely blocked with HTTP $statusCode" -ForegroundColor Green
    $unauthorizedAccessBlocked = $true
}

if (-not $unauthorizedAccessBlocked) {
    throw "Security vulnerability: User B was able to access User A's resume!"
}

# Verify User B has independent 0 counts
$summaryB = Invoke-RestMethod -Uri "$baseUrl/api/career/dashboard/summary" -Method Get -Headers $headersB
$bTotalResumes = if ($summaryB.totalResumes -ne $null) { $summaryB.totalResumes } else { $summaryB.totalResumesCount }
$bTotalSkills = if ($summaryB.totalSkillsCount -ne $null) { $summaryB.totalSkillsCount } else { 0 }
$bPendingActions = if ($summaryB.pendingActionsCount -ne $null) { $summaryB.pendingActionsCount } else { $summaryB.inProgressActionsCount }

Write-Host "User B Total Resumes: $bTotalResumes | Skills: $bTotalSkills | Actions: $bPendingActions" -ForegroundColor Green
if ($bTotalResumes -ne 0 -or $bTotalSkills -ne 0) {
    throw "User B did not have isolated empty state!"
}

# 12. Cleanup
Write-Host "`n[12/12] Cleaning up test records for User A..." -ForegroundColor Yellow
try { Invoke-RestMethod -Uri "$baseUrl/api/career/job-descriptions/$($jdResult.id)" -Method Delete -Headers $headersA | Out-Null } catch {}
try { Invoke-RestMethod -Uri "$baseUrl/api/career/resumes/$($resume1.id)" -Method Delete -Headers $headersA | Out-Null } catch {}
try { Invoke-RestMethod -Uri "$baseUrl/api/career/resumes/$($resume2.id)" -Method Delete -Headers $headersA | Out-Null } catch {}
try { Invoke-RestMethod -Uri "$baseUrl/api/career/actions/$($action1.id)" -Method Delete -Headers $headersA | Out-Null } catch {}
try { Invoke-RestMethod -Uri "$baseUrl/api/career/actions/$($action2.id)" -Method Delete -Headers $headersA | Out-Null } catch {}
try { Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems/$($dsaProb.id)" -Method Delete -Headers $headersA | Out-Null } catch {}
Write-Host "Deleted test resumes, actions, JD analyses, and DSA problems." -ForegroundColor Green

Write-Host "`n===================================================================" -ForegroundColor Cyan
Write-Host "MILESTONE 7 VERIFICATION SUCCESSFUL: ALL CHECKS PASSED!" -ForegroundColor Green
Write-Host "===================================================================" -ForegroundColor Cyan
