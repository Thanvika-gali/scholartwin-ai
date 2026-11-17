# ==============================================================================
# AI ScholarTwin — Automated Secure MySQL Setup & Verification
# Verifies scholartwin_user access; securely runs database/init.sql if needed
# ==============================================================================

param (
    [switch]$ForceAdmin
)

$ErrorActionPreference = "Stop"

$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysqlPath)) {
    Write-Error "MySQL CLI not found at: $mysqlPath"
    exit 1
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$initSqlPath = Join-Path $scriptDir "init.sql"

if (-not (Test-Path $initSqlPath)) {
    Write-Error "init.sql not found at: $initSqlPath"
    exit 1
}

$appPassword = if ($env:SPRING_DATASOURCE_PASSWORD) { $env:SPRING_DATASOURCE_PASSWORD } else { "scholartwin_password" }

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   AI ScholarTwin - Database Verification & Setup        " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Step 1: Check if scholartwin_user can already connect (unless -ForceAdmin was specified)
$needAdminSetup = $ForceAdmin
if (-not $ForceAdmin) {
    Write-Host "[1/2] Checking existing 'scholartwin_user' connectivity..." -ForegroundColor Yellow
    $env:MYSQL_PWD = $appPassword
    try {
        $checkOut = & $mysqlPath -h localhost -P 3306 -u scholartwin_user ai_scholartwin -e "SELECT DATABASE() AS CurrentDB, USER() AS CurrentUser, VERSION() AS MySQLVersion;" 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host $checkOut
            Write-Host "-> 'scholartwin_user' authenticated successfully to 'ai_scholartwin'!" -ForegroundColor Green
            $needAdminSetup = $false
        } else {
            Write-Host "-> Unable to connect as 'scholartwin_user'. Proceeding with administrator setup..." -ForegroundColor Yellow
            $needAdminSetup = $true
        }
    } catch {
        $needAdminSetup = $true
    } finally {
        $env:MYSQL_PWD = $null
    }
}

# Step 2: Administrative setup if needed
if ($needAdminSetup) {
    Write-Host "`nAdministrator setup required."
    Write-Host "This will execute database/init.sql to cleanly configure 'ai_scholartwin' and 'scholartwin_user'."
    Write-Host "Prompting for MySQL root password securely (masked with asterisks)..."
    
    $securePass = Read-Host -Prompt "Enter MySQL root password" -AsSecureString
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePass)
    $plainPass = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    
    try {
        Write-Host "`nExecuting database/init.sql as root..." -ForegroundColor Yellow
        $env:MYSQL_PWD = $plainPass
        Get-Content -Path $initSqlPath | & $mysqlPath -h localhost -P 3306 -u root
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Execution of init.sql failed (exit code $LASTEXITCODE). Verify root password."
            exit 1
        }
        Write-Host "-> Database and users initialized successfully." -ForegroundColor Green
    } finally {
        $env:MYSQL_PWD = $null
        $plainPass = $null
        $securePass = $null
    }
}

# Step 3: Final verification
Write-Host "`n[2/2] Running final database verification..." -ForegroundColor Yellow
$env:MYSQL_PWD = $appPassword
try {
    $verifyOutput = & $mysqlPath -h localhost -P 3306 -u scholartwin_user ai_scholartwin -e "SELECT DATABASE() AS CurrentDB, USER() AS CurrentUser, VERSION() AS MySQLVersion; SHOW TABLES;"
    Write-Host $verifyOutput
    Write-Host "-> Verification successful: 'scholartwin_user' is fully operational on 'ai_scholartwin'." -ForegroundColor Green
} catch {
    Write-Error "Verification as scholartwin_user failed: $_"
    exit 1
} finally {
    $env:MYSQL_PWD = $null
}

Write-Host "`nDatabase readiness complete! Spring Boot can connect cleanly." -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
