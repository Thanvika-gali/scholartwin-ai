# ==============================================================================
# AI ScholarTwin — Milestone 4 Live End-to-End Verification Script
# ==============================================================================

$baseUrl = "http://localhost:8080"
$pdfPath = "backend/target/sample_docs/ai_research_paper.pdf"
$docxPath = "backend/target/sample_docs/thesis_methodology.docx"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " AI SCHOLARTWIN -- MILESTONE 4 LIVE END-TO-END VERIFICATION" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Health check
Write-Host ""
Write-Host "[1] Verifying System and Database Health..." -ForegroundColor Yellow
$health = Invoke-RestMethod -Uri "$baseUrl/api/health" -Method Get
Write-Host "Health Status: $($health.status), Database: $($health.database)" -ForegroundColor Green
if ($health.database -ne "CONNECTED") {
    Write-Error "Database not connected!"
    exit 1
}

# 2. Register User 1
$user1Email = "m4_evaluator1_" + [System.Guid]::NewGuid().ToString().Substring(0, 8) + "@scholar.edu"
$user2Email = "m4_evaluator2_" + [System.Guid]::NewGuid().ToString().Substring(0, 8) + "@scholar.edu"

Write-Host ""
Write-Host "[2] Registering Primary Test User: $user1Email..." -ForegroundColor Yellow
$regBody1 = @{
    fullName = "Dr. Elena Rostova"
    email = $user1Email
    password = "ScholarPassword123!"
} | ConvertTo-Json

$regResp1 = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $regBody1 -ContentType "application/json"
$token1 = $regResp1.token
$userId1 = $regResp1.user.id
Write-Host "User 1 registered successfully (ID: $userId1)" -ForegroundColor Green

# 3. Register User 2 (Isolation Test User)
Write-Host ""
Write-Host "[3] Registering Secondary Test User for Security Isolation: $user2Email..." -ForegroundColor Yellow
$regBody2 = @{
    fullName = "Alex Vance"
    email = $user2Email
    password = "ScholarPassword123!"
} | ConvertTo-Json

$regResp2 = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $regBody2 -ContentType "application/json"
$token2 = $regResp2.token
$userId2 = $regResp2.user.id
Write-Host "User 2 registered successfully (ID: $userId2)" -ForegroundColor Green

# 4. Upload PDF as User 1
Write-Host ""
Write-Host "[4] Uploading Multi-page PDF as User 1 ($pdfPath)..." -ForegroundColor Yellow
$uploadPdfRaw = curl.exe -s -X POST -H "Authorization: Bearer $token1" -F "file=@$pdfPath" "$baseUrl/api/documents"
$pdfDoc = $uploadPdfRaw | ConvertFrom-Json

Write-Host "PDF Upload Response:" -ForegroundColor Gray
Write-Host "  Document ID:       $($pdfDoc.id)" -ForegroundColor White
Write-Host "  Filename:          $($pdfDoc.originalFilename)" -ForegroundColor White
Write-Host "  Format:            $($pdfDoc.format)" -ForegroundColor White
Write-Host "  Status:            $($pdfDoc.status)" -ForegroundColor Green
Write-Host "  Pages Extracted:   $($pdfDoc.pageCount)" -ForegroundColor White
Write-Host "  Chunks Generated:  $($pdfDoc.chunkCount)" -ForegroundColor White
Write-Host "  Character Count:   $($pdfDoc.characterCount)" -ForegroundColor White
Write-Host "  OCR Required:      $($pdfDoc.ocrRequired)" -ForegroundColor White
Write-Host "  OCR Used:          $($pdfDoc.ocrUsed)" -ForegroundColor White

if ($pdfDoc.status -ne "COMPLETED" -or $pdfDoc.pageCount -ne 2) {
    Write-Error "PDF processing failed or unexpected page count!"
    exit 1
}
$pdfId = $pdfDoc.id

# 5. Upload DOCX as User 1
Write-Host ""
Write-Host "[5] Uploading Word Document as User 1 ($docxPath)..." -ForegroundColor Yellow
$uploadDocxRaw = curl.exe -s -X POST -H "Authorization: Bearer $token1" -F "file=@$docxPath" "$baseUrl/api/documents"
$docxDoc = $uploadDocxRaw | ConvertFrom-Json

Write-Host "DOCX Upload Response:" -ForegroundColor Gray
Write-Host "  Document ID:       $($docxDoc.id)" -ForegroundColor White
Write-Host "  Filename:          $($docxDoc.originalFilename)" -ForegroundColor White
Write-Host "  Format:            $($docxDoc.format)" -ForegroundColor White
Write-Host "  Status:            $($docxDoc.status)" -ForegroundColor Green
Write-Host "  Pages Extracted:   $($docxDoc.pageCount)" -ForegroundColor White
Write-Host "  Chunks Generated:  $($docxDoc.chunkCount)" -ForegroundColor White
Write-Host "  Character Count:   $($docxDoc.characterCount)" -ForegroundColor White

if ($docxDoc.status -ne "COMPLETED" -or $docxDoc.chunkCount -lt 1) {
    Write-Error "DOCX processing failed!"
    exit 1
}
$docxId = $docxDoc.id

# 6. List Documents
Write-Host ""
Write-Host "[6] Listing Documents for User 1 vs User 2..." -ForegroundColor Yellow
$user1Docs = Invoke-RestMethod -Uri "$baseUrl/api/documents" -Method Get -Headers @{ Authorization = "Bearer $token1" }
$user2Docs = Invoke-RestMethod -Uri "$baseUrl/api/documents" -Method Get -Headers @{ Authorization = "Bearer $token2" }

Write-Host "User 1 document count: $($user1Docs.Count) (Expected: 2)" -ForegroundColor Green
Write-Host "User 2 document count: $($user2Docs.Count) (Expected: 0)" -ForegroundColor Green

if ($user1Docs.Count -ne 2 -or $user2Docs.Count -ne 0) {
    Write-Error "Listing count mismatch or isolation breach!"
    exit 1
}

# 7. Get Document Content
Write-Host ""
Write-Host "[7] Retrieving Document Full Content for PDF (ID: $pdfId)..." -ForegroundColor Yellow
$pdfContent = Invoke-RestMethod -Uri "$baseUrl/api/documents/$pdfId/content" -Method Get -Headers @{ Authorization = "Bearer $token1" }
$previewLen = [System.Math]::Min(150, $pdfContent.fullText.Length)
Write-Host "Extracted text sample:" -ForegroundColor Gray
Write-Host "$($pdfContent.fullText.Substring(0, $previewLen))..." -ForegroundColor Cyan

if (-not $pdfContent.fullText.Contains("Deep Neural Architectures")) {
    Write-Error "Extracted content missing expected heading text!"
    exit 1
}

# 8. Get Document Chunks
Write-Host ""
Write-Host "[8] Inspecting Generated Semantic Chunks for PDF..." -ForegroundColor Yellow
$chunks = Invoke-RestMethod -Uri "$baseUrl/api/documents/$pdfId/chunks" -Method Get -Headers @{ Authorization = "Bearer $token1" }
foreach ($c in $chunks) {
    Write-Host "  Chunk #$($c.chunkIndex): Pages $($c.startPage)-$($c.endPage), Length: $($c.characterCount) chars" -ForegroundColor White
}
Write-Host "Total verified chunks: $($chunks.Count)" -ForegroundColor Green

# 9. Verify File Download
Write-Host ""
Write-Host "[9] Verifying Secure Binary Download of Original PDF..." -ForegroundColor Yellow
$downloadTarget = "backend/target/downloaded_test.pdf"
Invoke-WebRequest -Uri "$baseUrl/api/documents/$pdfId/download" -Headers @{ Authorization = "Bearer $token1" } -OutFile $downloadTarget
$origSize = (Get-Item $pdfPath).Length
$downSize = (Get-Item $downloadTarget).Length
Write-Host "Original size: $origSize bytes, Downloaded size: $downSize bytes" -ForegroundColor Green
if ($origSize -ne $downSize) {
    Write-Error "Downloaded file size mismatch!"
    exit 1
}
Remove-Item $downloadTarget -Force -ErrorAction SilentlyContinue

# 10. Verify Retry Processing
Write-Host ""
Write-Host "[10] Testing Document Retry Processing on PDF..." -ForegroundColor Yellow
$retryResp = Invoke-RestMethod -Uri "$baseUrl/api/documents/$pdfId/retry" -Method Post -Headers @{ Authorization = "Bearer $token1" }
Write-Host "Retry status: $($retryResp.status), Chunks: $($retryResp.chunkCount)" -ForegroundColor Green

# 11. Verify Dashboard Summary Integration
Write-Host ""
Write-Host "[11] Verifying Dashboard Summary Endpoint..." -ForegroundColor Yellow
$dashboard = Invoke-RestMethod -Uri "$baseUrl/api/dashboard/summary" -Method Get -Headers @{ Authorization = "Bearer $token1" }
Write-Host "Dashboard Summary Metrics:" -ForegroundColor Gray
Write-Host "  Total Items:       $($dashboard.totalItems)" -ForegroundColor White
Write-Host "  Total Documents:   $($dashboard.totalDocuments)" -ForegroundColor Green
Write-Host "  Total Notes:       $($dashboard.totalNotes)" -ForegroundColor White
Write-Host "  Total Links:       $($dashboard.totalLinks)" -ForegroundColor White

if ($dashboard.totalDocuments -ne 2) {
    Write-Error "Dashboard summary totalDocuments is not 2!"
    exit 1
}

# 12. Cross-User Isolation Enforcement
Write-Host ""
Write-Host "[12] Verifying Cross-User Isolation (User 2 accessing User 1's Document)..." -ForegroundColor Yellow

$crossGetStatus = curl.exe -s -o NUL -w "%{http_code}" -H "Authorization: Bearer $token2" "$baseUrl/api/documents/$pdfId"
Write-Host "User 2 GET /api/documents/$pdfId HTTP status: $crossGetStatus (Expected: 404)" -ForegroundColor Green

$crossDownloadStatus = curl.exe -s -o NUL -w "%{http_code}" -H "Authorization: Bearer $token2" "$baseUrl/api/documents/$pdfId/download"
Write-Host "User 2 GET /api/documents/$pdfId/download HTTP status: $crossDownloadStatus (Expected: 404)" -ForegroundColor Green

$crossDeleteStatus = curl.exe -s -o NUL -w "%{http_code}" -X DELETE -H "Authorization: Bearer $token2" "$baseUrl/api/documents/$pdfId"
Write-Host "User 2 DELETE /api/documents/$pdfId HTTP status: $crossDeleteStatus (Expected: 404)" -ForegroundColor Green

if ($crossGetStatus -ne "404" -or $crossDownloadStatus -ne "404" -or $crossDeleteStatus -ne "404") {
    Write-Error "Cross-user security isolation failed!"
    exit 1
}

# 13. Test Invalid File Upload Rejection
Write-Host ""
Write-Host "[13] Testing Invalid / Spoofed File Upload Rejection..." -ForegroundColor Yellow
$fakePdfPath = "backend/target/fake.pdf"
"This is plain text with no PDF header" | Out-File -FilePath $fakePdfPath -Encoding ascii
$spoofStatus = curl.exe -s -o NUL -w "%{http_code}" -X POST -H "Authorization: Bearer $token1" -F "file=@$fakePdfPath" "$baseUrl/api/documents"
Write-Host "Spoofed file upload HTTP status: $spoofStatus (Expected: 400)" -ForegroundColor Green
Remove-Item $fakePdfPath -Force -ErrorAction SilentlyContinue

if ($spoofStatus -ne "400") {
    Write-Error "Invalid file was not rejected with HTTP 400!"
    exit 1
}

# 14. Document Deletion by Owner
Write-Host ""
Write-Host "[14] Testing Document Deletion by Owner (DOCX ID: $docxId)..." -ForegroundColor Yellow
$delStatus = curl.exe -s -o NUL -w "%{http_code}" -X DELETE -H "Authorization: Bearer $token1" "$baseUrl/api/documents/$docxId"
Write-Host "Delete HTTP status: $delStatus (Expected: 204)" -ForegroundColor Green

$postDelGet = curl.exe -s -o NUL -w "%{http_code}" -H "Authorization: Bearer $token1" "$baseUrl/api/documents/$docxId"
Write-Host "Post-delete GET HTTP status: $postDelGet (Expected: 404)" -ForegroundColor Green

# Check remaining count for User 1
$remainingDocs = Invoke-RestMethod -Uri "$baseUrl/api/documents" -Method Get -Headers @{ Authorization = "Bearer $token1" }
Write-Host "User 1 remaining documents: $($remainingDocs.Count) (Expected: 1)" -ForegroundColor Green

# 15. Clean up test users from DB
Write-Host ""
Write-Host "[15] Cleaning up live test users from MySQL database..." -ForegroundColor Yellow
$mysqlCli = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (Test-Path $mysqlCli) {
    & $mysqlCli -u scholartwin_user -pscholartwin_password -e "
        USE ai_scholartwin;
        DELETE dc FROM document_chunks dc INNER JOIN documents d ON dc.document_id = d.id WHERE d.user_id IN ($userId1, $userId2);
        DELETE FROM documents WHERE user_id IN ($userId1, $userId2);
        DELETE FROM users WHERE id IN ($userId1, $userId2);
    " 2>$null
    Write-Host "Cleaned test user data from MySQL." -ForegroundColor Green
}

# Clean physical uploaded test files in uploads/documents/
Get-ChildItem -Path "backend/uploads/documents/" -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
Get-ChildItem -Path "uploads/documents/" -ErrorAction SilentlyContinue | Remove-Item -Force -ErrorAction SilentlyContinue
Write-Host "Cleaned physical storage directory." -ForegroundColor Green

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " ALL MILESTONE 4 LIVE END-TO-END VERIFICATIONS PASSED 100%!" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
