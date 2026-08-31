param (
    [string]$ContainerName = $env:POSTGRES_CONTAINER_NAME,
    [string]$DbUser = $env:POSTGRES_USER,
    [string]$DbName = $env:POSTGRES_DB,
    [string]$OutputDir = "backups"
)

$ErrorActionPreference = "Stop"

# Resolve defaults if environment variables are not set
if (-not $ContainerName) { $ContainerName = "carmats-postgres" }
if (-not $DbUser) { $DbUser = "carmats" }
if (-not $DbName) { $DbName = "carmats_db" }

# Database password MUST be supplied via POSTGRES_PASSWORD environment variable
$DbPassword = $env:POSTGRES_PASSWORD
if (-not $DbPassword) {
    $DbPassword = [System.Environment]::GetEnvironmentVariable("POSTGRES_PASSWORD", "User")
}
if (-not $DbPassword) {
    $DbPassword = [System.Environment]::GetEnvironmentVariable("POSTGRES_PASSWORD", "Machine")
}
if (-not $DbPassword) {
    Write-Host "[ERROR] Missing required environment variable: POSTGRES_PASSWORD" -ForegroundColor Red
    Write-Host "Please set POSTGRES_PASSWORD before running this script." -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path -Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$BackupFile = Join-Path $OutputDir "backup_${DbName}_${Timestamp}.sql"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  CARMATS DATABASE BACKUP AUTOMATION      " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Container : $ContainerName"
Write-Host "Database  : $DbName"
Write-Host "User      : $DbUser"
Write-Host "Output    : $BackupFile"
Write-Host "Mode      : Read-only dump (pg_dump)"
Write-Host "------------------------------------------"

try {
    # Check if container is running
    $running = docker ps --filter "name=^/${ContainerName}$" --format "{{.Names}}"
    if (-not $running) {
        throw "Container '$ContainerName' is not running. Please ensure the container is started."
    }

    # Execute read-only pg_dump inside container and pipe to file
    docker exec -e PGPASSWORD=$DbPassword $ContainerName pg_dump -U $DbUser -d $DbName --clean --if-exists --no-owner --no-privileges | Out-File -FilePath $BackupFile -Encoding utf8

    if (-not (Test-Path $BackupFile) -or (Get-Item $BackupFile).Length -eq 0) {
        throw "Backup file was not created or is empty."
    }

    $FileSizeKB = [math]::Round((Get-Item $BackupFile).Length / 1KB, 2)
    Write-Host "[SUCCESS] Backup created successfully!" -ForegroundColor Green
    Write-Host "File Size : $FileSizeKB KB" -ForegroundColor Green
    Write-Host "Timestamp : $Timestamp" -ForegroundColor Green
    Write-Host "File Path : $BackupFile" -ForegroundColor Green

} catch {
    Write-Host "[ERROR] Backup failed: $_" -ForegroundColor Red
    exit 1
}