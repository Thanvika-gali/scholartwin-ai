# ==============================================================================
# AI ScholarTwin — Live Scanned-PDF & Mixed-PDF OCR Verification Script
# ==============================================================================
$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   AI ScholarTwin - Milestone 4 Live Scanned & Mixed OCR   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"

# 1. Health check
Write-Host "`n1. Verifying Backend Health..." -ForegroundColor Yellow
$health = Invoke-RestMethod -Uri "$baseUrl/api/health" -Method Get
Write-Host "   Backend Status: $($health.status) | Database: $($health.database)" -ForegroundColor Green
if ($health.database -ne "CONNECTED") {
    Write-Error "Database is not connected!"
}

# 2. Authenticate
Write-Host "`n2. Registering and Authenticating Test User..." -ForegroundColor Yellow
$rand = Get-Random
$email = "ocr_live_user_$rand@scholartwin.io"
$password = "SecretPassword123!"
$regPayload = @{
    fullName = "Dr. OCR Live Tester"
    email = $email
    password = $password
} | ConvertTo-Json

$authResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -ContentType "application/json" -Body $regPayload
$token = $authResponse.token
Write-Host "   Authenticated user: $($authResponse.user.email) (Token acquired)" -ForegroundColor Green

$headers = @{
    "Authorization" = "Bearer $token"
}

# ------------------------------------------------------------------------------
# TEST 1: Pure Scanned PDF (Image-Only, Zero Digital Text)
# ------------------------------------------------------------------------------
Write-Host "`n3. Testing 100% Scanned PDF (Pure Image, zero digital text)..." -ForegroundColor Yellow
$scannedPdfPath = "E:\projects\scholartwin-ai\backend\target\sample_docs\synthetic_scanned_paper.pdf"
if (-not (Test-Path $scannedPdfPath)) {
    Write-Error "Scanned PDF does not exist at $scannedPdfPath"
}
$fileItem = Get-Item $scannedPdfPath
Write-Host "   Found Scanned PDF: $($fileItem.FullName) ($($fileItem.Length) bytes)" -ForegroundColor Green

Write-Host "`n4. Uploading Scanned PDF to POST /api/documents..." -ForegroundColor Yellow
$uploadOutput = & curl.exe -s -X POST "$baseUrl/api/documents" `
    -H "Authorization: Bearer $token" `
    -F "file=@$scannedPdfPath;type=application/pdf"

$uploadResponse = $uploadOutput | ConvertFrom-Json
$scannedDocId = $uploadResponse.id

Write-Host "   Scanned PDF Processing Response:" -ForegroundColor Green
Write-Host "     Document ID:      $($uploadResponse.id)" -ForegroundColor White
Write-Host "     Filename:         $($uploadResponse.originalFilename)" -ForegroundColor White
Write-Host "     Status:           $($uploadResponse.status)" -ForegroundColor White
Write-Host "     Page Count:       $($uploadResponse.pageCount)" -ForegroundColor White
Write-Host "     Character Count:  $($uploadResponse.characterCount)" -ForegroundColor White
Write-Host "     Chunk Count:      $($uploadResponse.chunkCount)" -ForegroundColor White
Write-Host "     ocrUsed:          $($uploadResponse.ocrUsed)" -ForegroundColor Green
Write-Host "     ocrRequired:      $($uploadResponse.ocrRequired)" -ForegroundColor Green

if ($uploadResponse.status -ne "COMPLETED") {
    Write-Error "Document processing did not succeed! Status: $($uploadResponse.status)"
}
if ($uploadResponse.ocrUsed -ne $true) {
    Write-Error "ocrUsed is not true for scanned PDF!"
}
if ($uploadResponse.ocrRequired -ne $false) {
    Write-Error "ocrRequired is not false after successful OCR!"
}

# Verify Scanned Content & Chunks
Write-Host "`n5. Fetching Scanned Document Content and Chunks..." -ForegroundColor Yellow
$scannedContent = Invoke-RestMethod -Uri "$baseUrl/api/documents/$scannedDocId/content" -Method Get -Headers $headers
Write-Host "   Total characters extracted: $($scannedContent.characterCount)" -ForegroundColor Green
Write-Host "   OCR Extracted Sample Text:" -ForegroundColor Cyan
Write-Host "   ------------------------------------------------------------" -ForegroundColor DarkGray
Write-Host "   $($scannedContent.fullText.Trim())" -ForegroundColor White
Write-Host "   ------------------------------------------------------------" -ForegroundColor DarkGray

$scannedChunks = Invoke-RestMethod -Uri "$baseUrl/api/documents/$scannedDocId/chunks" -Method Get -Headers $headers
Write-Host "   Total chunks created: $($scannedChunks.Count)" -ForegroundColor Green
foreach ($chunk in $scannedChunks) {
    Write-Host "     Chunk #$($chunk.chunkIndex) [Pages $($chunk.startPage)-$($chunk.endPage)]: $($chunk.characterCount) chars" -ForegroundColor White
}

# ------------------------------------------------------------------------------
# TEST 2: Mixed PDF (Page 1 = Digital Text, Page 2 = Scanned Image)
# ------------------------------------------------------------------------------
Write-Host "`n6. Testing Mixed PDF (Page 1 Digital Text + Page 2 Scanned Image)..." -ForegroundColor Yellow
$mixedPdfPath = "E:\projects\scholartwin-ai\backend\target\sample_docs\synthetic_mixed_paper.pdf"
if (-not (Test-Path $mixedPdfPath)) {
    Write-Error "Mixed PDF does not exist at $mixedPdfPath"
}
$mixedItem = Get-Item $mixedPdfPath
Write-Host "   Found Mixed PDF: $($mixedItem.FullName) ($($mixedItem.Length) bytes)" -ForegroundColor Green

Write-Host "`n7. Uploading Mixed PDF to POST /api/documents..." -ForegroundColor Yellow
$mixedUploadOutput = & curl.exe -s -X POST "$baseUrl/api/documents" `
    -H "Authorization: Bearer $token" `
    -F "file=@$mixedPdfPath;type=application/pdf"

$mixedResponse = $mixedUploadOutput | ConvertFrom-Json
$mixedDocId = $mixedResponse.id

Write-Host "   Mixed PDF Processing Response:" -ForegroundColor Green
Write-Host "     Document ID:      $($mixedResponse.id)" -ForegroundColor White
Write-Host "     Filename:         $($mixedResponse.originalFilename)" -ForegroundColor White
Write-Host "     Status:           $($mixedResponse.status)" -ForegroundColor White
Write-Host "     Page Count:       $($mixedResponse.pageCount)" -ForegroundColor White
Write-Host "     Character Count:  $($mixedResponse.characterCount)" -ForegroundColor White
Write-Host "     Chunk Count:      $($mixedResponse.chunkCount)" -ForegroundColor White
Write-Host "     ocrUsed:          $($mixedResponse.ocrUsed)" -ForegroundColor Green
Write-Host "     ocrRequired:      $($mixedResponse.ocrRequired)" -ForegroundColor Green

if ($mixedResponse.status -ne "COMPLETED") {
    Write-Error "Mixed document processing did not succeed! Status: $($mixedResponse.status)"
}
if ($mixedResponse.ocrUsed -ne $true) {
    Write-Error "ocrUsed is not true for mixed PDF where page 2 used OCR!"
}
if ($mixedResponse.ocrRequired -ne $false) {
    Write-Error "ocrRequired is not false after all pages extracted!"
}
if ($mixedResponse.pageCount -ne 2) {
    Write-Error "Page count should be 2 for mixed PDF!"
}

# Verify Mixed Content & Chunks
Write-Host "`n8. Fetching Mixed Document Content and Chunks..." -ForegroundColor Yellow
$mixedContent = Invoke-RestMethod -Uri "$baseUrl/api/documents/$mixedDocId/content" -Method Get -Headers $headers
Write-Host "   Total characters extracted: $($mixedContent.characterCount)" -ForegroundColor Green
Write-Host "   Mixed Extracted Combined Text:" -ForegroundColor Cyan
Write-Host "   ------------------------------------------------------------" -ForegroundColor DarkGray
Write-Host "   $($mixedContent.fullText.Trim())" -ForegroundColor White
Write-Host "   ------------------------------------------------------------" -ForegroundColor DarkGray

$mixedChunks = Invoke-RestMethod -Uri "$baseUrl/api/documents/$mixedDocId/chunks" -Method Get -Headers $headers
Write-Host "   Total chunks created: $($mixedChunks.Count)" -ForegroundColor Green
foreach ($chunk in $mixedChunks) {
    Write-Host "     Chunk #$($chunk.chunkIndex) [Pages $($chunk.startPage)-$($chunk.endPage)]: $($chunk.characterCount) chars" -ForegroundColor White
}

# 9. Check Dashboard Overview
Write-Host "`n9. Checking Dashboard Overview from GET /api/dashboard/summary..." -ForegroundColor Yellow
$dashboard = Invoke-RestMethod -Uri "$baseUrl/api/dashboard/summary" -Method Get -Headers $headers
Write-Host "   Total Items:         $($dashboard.totalItems)" -ForegroundColor White
Write-Host "   Total Documents:     $($dashboard.totalDocuments)" -ForegroundColor White
Write-Host "   Total Notes:         $($dashboard.totalNotes)" -ForegroundColor White
Write-Host "   Total Links:         $($dashboard.totalLinks)" -ForegroundColor White

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host "   SUCCESS: Scanned and Mixed PDF OCR Fully Verified!     " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
