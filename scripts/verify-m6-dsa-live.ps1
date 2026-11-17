# AI ScholarTwin - Milestone 6 DSA Progress Tracker End-to-End Verification Script
$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080"
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "AI ScholarTwin - Milestone 6: DSA End-to-End Verification" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Health check
Write-Host "`n[1/8] Verifying backend health..." -ForegroundColor Yellow
$health = Invoke-RestMethod -Uri "$baseUrl/api/health" -Method Get
Write-Host "Health status: $($health.status), Database: $($health.database)" -ForegroundColor Green
if ($health.database -ne "CONNECTED") {
    throw "Database is not connected!"
}

# 2. Register / Authenticate User A
$timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$emailA = "dsa.tester.$timestamp@scholartwin.io"
$password = "StrongPass123!"
$userAReq = @{
    fullName = "DSA Algorithm Master"
    email = $emailA
    password = $password
} | ConvertTo-Json

Write-Host "`n[2/8] Registering User A ($emailA)..." -ForegroundColor Yellow
$authResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $userAReq -ContentType "application/json"
$tokenA = $authResponse.token
$headersA = @{
    "Authorization" = "Bearer $tokenA"
    "Content-Type"  = "application/json"
}
Write-Host "User A authenticated successfully. JWT obtained." -ForegroundColor Green

# 3. Create Problems
Write-Host "`n[3/8] Creating DSA problems across platforms and difficulties..." -ForegroundColor Yellow

# Problem 1: Merge Intervals (Medium, Arrays, LeetCode)
$prob1Req = @{
    title = "Merge Intervals"
    problemUrl = "https://leetcode.com/problems/merge-intervals/"
    platform = "LeetCode"
    difficulty = "MEDIUM"
    primaryTopic = "Arrays"
    additionalTopics = @("Sorting")
    status = "NOT_STARTED"
    solutionApproach = "Sort intervals by start time. Iterate and compare with previous end."
    personalNotes = "Common interview question on interval merging."
    timeComplexity = "O(N log N)"
    spaceComplexity = "O(N)"
    revisionRequired = $true
} | ConvertTo-Json

$prob1 = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems" -Method Post -Headers $headersA -Body $prob1Req
Write-Host "Created Problem 1: ID=$($prob1.id), Title='$($prob1.title)', Difficulty=$($prob1.difficulty)" -ForegroundColor Green

# Problem 2: Invert Binary Tree (Easy, Binary Trees, LeetCode)
$prob2Req = @{
    title = "Invert Binary Tree"
    problemUrl = "https://leetcode.com/problems/invert-binary-tree/"
    platform = "LeetCode"
    difficulty = "EASY"
    primaryTopic = "Binary Trees"
    additionalTopics = @("Recursion")
    status = "NOT_STARTED"
    solutionApproach = "Recursively swap left and right subtrees."
    timeComplexity = "O(N)"
    spaceComplexity = "O(H)"
    revisionRequired = $false
} | ConvertTo-Json

$prob2 = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems" -Method Post -Headers $headersA -Body $prob2Req
Write-Host "Created Problem 2: ID=$($prob2.id), Title='$($prob2.title)', Difficulty=$($prob2.difficulty)" -ForegroundColor Green

# 4. Search and Filter
Write-Host "`n[4/8] Testing Search and Filter APIs..." -ForegroundColor Yellow
$searchResult = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems?search=Merge&topic=Arrays&difficulty=MEDIUM" -Method Get -Headers $headersA
Write-Host "Search for 'Merge' in 'Arrays': Found $($searchResult.totalElements) problem(s)." -ForegroundColor Green
if ($searchResult.totalElements -lt 1) {
    throw "Search failed to return created problem!"
}

# 5. Log Attempts (FAILED -> ATTEMPTED, SUCCESS -> SOLVED)
Write-Host "`n[5/8] Testing Attempt Logging & Status State Transitions..." -ForegroundColor Yellow

# Log failed attempt on Problem 1
$failedAttempt = @{
    attemptDate = (Get-Date).ToString("yyyy-MM-dd")
    outcome = "FAILED"
    timeSpentMinutes = 25
    programmingLanguage = "Java"
    learningNotes = "Forgot to sort intervals before merging."
    difficultyEncountered = "Boundary condition error"
} | ConvertTo-Json

$att1 = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems/$($prob1.id)/attempts" -Method Post -Headers $headersA -Body $failedAttempt
Write-Host "Logged FAILED attempt: ID=$($att1.id). Checking problem status..." -ForegroundColor Green

$prob1Check = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems/$($prob1.id)" -Method Get -Headers $headersA
Write-Host "Problem 1 Status: $($prob1Check.problem.status) (Expected: ATTEMPTED)" -ForegroundColor Green
if ($prob1Check.problem.status -ne "ATTEMPTED") {
    throw "Problem status should be ATTEMPTED after failed attempt!"
}

# Log successful attempt on Problem 1
$successAttempt = @{
    attemptDate = (Get-Date).ToString("yyyy-MM-dd")
    outcome = "SUCCESS"
    timeSpentMinutes = 15
    programmingLanguage = "Java"
    learningNotes = "Sorted intervals with Arrays.sort(Comparator.comparingInt(a -> a[0]))."
} | ConvertTo-Json

$att2 = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems/$($prob1.id)/attempts" -Method Post -Headers $headersA -Body $successAttempt
Write-Host "Logged SUCCESS attempt: ID=$($att2.id). Checking problem status..." -ForegroundColor Green

$prob1Solved = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems/$($prob1.id)" -Method Get -Headers $headersA
Write-Host "Problem 1 Status: $($prob1Solved.problem.status) (Expected: SOLVED)" -ForegroundColor Green
Write-Host "Total attempts: $($prob1Solved.stats.totalAttempts), Success rate: $($prob1Solved.stats.successRatePercent)%" -ForegroundColor Green
if ($prob1Solved.problem.status -ne "SOLVED" -or $prob1Solved.stats.successRatePercent -ne 50.0) {
    throw "Problem status should be SOLVED and accuracy should be 50.0%!"
}

# 6. Spaced Revision Scheduling
Write-Host "`n[6/8] Testing Spaced Revision Scheduling & Queue..." -ForegroundColor Yellow
$revisionDate = (Get-Date).AddDays(2).ToString("yyyy-MM-dd")
$revReq = @{
    scheduledDate = $revisionDate
    revisionNotes = "Re-test boundary intervals and verify time complexity."
} | ConvertTo-Json

$rev = Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems/$($prob1.id)/revisions" -Method Post -Headers $headersA -Body $revReq
Write-Host "Revision scheduled: ID=$($rev.id), Target Date=$($rev.scheduledDate)" -ForegroundColor Green

$queue = Invoke-RestMethod -Uri "$baseUrl/api/dsa/revisions" -Method Get -Headers $headersA
Write-Host "Revision Queue: Upcoming=$($queue.upcoming.Count), Total Pending=$($queue.totalPendingCount)" -ForegroundColor Green
if ($queue.totalPendingCount -lt 1) {
    throw "Revision queue should have at least 1 pending item!"
}

# Complete revision
$updateRevReq = @{
    status = "COMPLETED"
    revisionNotes = "Completed review with optimal O(N log N) space-efficient solution."
} | ConvertTo-Json

$revCompleted = Invoke-RestMethod -Uri "$baseUrl/api/dsa/revisions/$($rev.id)" -Method Put -Headers $headersA -Body $updateRevReq
Write-Host "Updated revision ID=$($revCompleted.id) to status: $($revCompleted.status)" -ForegroundColor Green

# 7. Dashboard Summary & Topics
Write-Host "`n[7/8] Testing Dashboard Summary & Topics Progress..." -ForegroundColor Yellow
$summary = Invoke-RestMethod -Uri "$baseUrl/api/dsa/dashboard/summary" -Method Get -Headers $headersA
Write-Host "Dashboard: Total Tracked=$($summary.totalProblems), Solved=$($summary.solvedProblems), Current Streak=$($summary.currentStreakDays) day(s)" -ForegroundColor Green
Write-Host "Insights: $($summary.insights[0])" -ForegroundColor Cyan

$topics = Invoke-RestMethod -Uri "$baseUrl/api/dsa/topics" -Method Get -Headers $headersA
Write-Host "Topics count: $($topics.Count) (Expected: 20 standard topics)" -ForegroundColor Green
$arraysTopic = $topics | Where-Object { $_.topicName -eq "Arrays" }
Write-Host "Arrays Topic: Solved=$($arraysTopic.solvedProblems)/$($arraysTopic.totalProblems), Completion=$($arraysTopic.completionPercentage)%" -ForegroundColor Green

# 8. Cross-User Multi-Tenant Isolation
Write-Host "`n[8/8] Testing Cross-User Isolation..." -ForegroundColor Yellow
$emailB = "dsa.adversary.$timestamp@scholartwin.io"
$userBReq = @{
    fullName = "DSA Adversary User"
    email = $emailB
    password = $password
} | ConvertTo-Json

$authB = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $userBReq -ContentType "application/json"
$tokenB = $authB.token
$headersB = @{
    "Authorization" = "Bearer $tokenB"
    "Content-Type"  = "application/json"
}

# User B tries to access User A's problem
$isolated = $false
try {
    Invoke-RestMethod -Uri "$baseUrl/api/dsa/problems/$($prob1.id)" -Method Get -Headers $headersB
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 404) {
        $isolated = $true
        Write-Host "Strict Isolation Confirmed: User B received 404 Not Found when attempting to access User A's problem." -ForegroundColor Green
    }
}
if (-not $isolated) {
    throw "Security violation! User B was able to access User A's problem!"
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "All Milestone 6 DSA APIs and workflows verified successfully!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Cyan

