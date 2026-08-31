param (
    [string]$BackupFile = "",
    [string]$PostgresImage = "postgres:17-alpine"
)

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  CARMATS SAFE RESTORE VERIFICATION       " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

if (-not $BackupFile) {
    $latest = Get-ChildItem -Path "backups" -Filter "backup_*.sql" -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if (-not $latest) {
        Write-Host "[ERROR] No backup file found in backups/ directory." -ForegroundColor Red
        exit 1
    }
    $BackupFile = $latest.FullName
}

if (-not (Test-Path $BackupFile)) {
    Write-Host "[ERROR] Backup file not found: $BackupFile" -ForegroundColor Red
    exit 1
}

# Generate dynamic isolated temporary container and random single-use password
$TempContainer = "carmats-verify-temp-" + (Get-Random -Minimum 100000 -Maximum 999999)
$VerifyDb = "carmats_verify_temp_db"
$VerifyUser = "carmats_verify_temp_user"
$VerifyPass = [System.Guid]::NewGuid().ToString("N")

Write-Host "Backup File      : $BackupFile"
Write-Host "Temp Container   : $TempContainer"
Write-Host "Temp Database    : $VerifyDb"
Write-Host "Isolation Target : Ephemeral isolated container (zero impact on active DB)"
Write-Host "------------------------------------------"

try {
    Write-Host "[1/5] Starting isolated ephemeral container..."
    docker run -d --name $TempContainer -e POSTGRES_DB=$VerifyDb -e POSTGRES_USER=$VerifyUser -e POSTGRES_PASSWORD=$VerifyPass $PostgresImage | Out-Null

    Write-Host "[2/5] Waiting for isolated PostgreSQL engine readiness..."
    $retries = 30
    $ready = $false
    Start-Sleep -Seconds 2
    while ($retries -gt 0) {
        try {
            $prevEA = $ErrorActionPreference
            $ErrorActionPreference = "SilentlyContinue"
            $check = docker exec -e PGPASSWORD=$VerifyPass $TempContainer psql -U $VerifyUser -d $VerifyDb -c "SELECT 1;" 2>$null
            $ErrorActionPreference = $prevEA
            if ($LASTEXITCODE -eq 0 -and ($check -match "1")) {
                $ready = $true
                break
            }
        } catch {
            # continue loop
        }
        Start-Sleep -Seconds 1
        $retries--
    }

    if (-not $ready) {
        throw "Temporary PostgreSQL container failed to become ready for connections."
    }

    Write-Host "[3/5] Restoring backup into isolated ephemeral database..."
    Get-Content $BackupFile -Raw -Encoding utf8 | docker exec -i -e PGPASSWORD=$VerifyPass $TempContainer psql -U $VerifyUser -d $VerifyDb | Out-Null

    Write-Host "[4/5] Running data integrity and schema verification queries..."
    $tableCount = (docker exec -e PGPASSWORD=$VerifyPass $TempContainer psql -U $VerifyUser -d $VerifyDb -t -c "SELECT count(*) FROM information_schema.tables WHERE table_schema='public';").Trim()
    $flywayCount = (docker exec -e PGPASSWORD=$VerifyPass $TempContainer psql -U $VerifyUser -d $VerifyDb -t -c "SELECT count(*) FROM flyway_schema_history;").Trim()
    $brandCount = (docker exec -e PGPASSWORD=$VerifyPass $TempContainer psql -U $VerifyUser -d $VerifyDb -t -c "SELECT count(*) FROM vehicle_brands;").Trim()
    $productCount = (docker exec -e PGPASSWORD=$VerifyPass $TempContainer psql -U $VerifyUser -d $VerifyDb -t -c "SELECT count(*) FROM products;").Trim()
    $userCount = (docker exec -e PGPASSWORD=$VerifyPass $TempContainer psql -U $VerifyUser -d $VerifyDb -t -c "SELECT count(*) FROM users;").Trim()

    Write-Host "------------------------------------------"
    Write-Host "  VERIFICATION RESULTS (Isolated DB)      " -ForegroundColor Green
    Write-Host "------------------------------------------"
    Write-Host "  Public Tables Count    : $tableCount" -ForegroundColor Green
    Write-Host "  Flyway Migrations Run  : $flywayCount" -ForegroundColor Green
    Write-Host "  Vehicle Brands Count   : $brandCount" -ForegroundColor Green
    Write-Host "  Catalog Products Count : $productCount" -ForegroundColor Green
    Write-Host "  Users Count            : $userCount" -ForegroundColor Green

    $tablesNum = [int]($tableCount -join '' -replace '\D', '')
    $flywayNum = [int]($flywayCount -join '' -replace '\D', '')

    if ($tablesNum -lt 10 -or $flywayNum -lt 1) {
        throw "Verification failed: insufficient tables or missing flyway migrations."
    }

    Write-Host "[SUCCESS] Backup restore and basic schema/data integrity verification passed." -ForegroundColor Green

} catch {
    Write-Host "[ERROR] Restore verification failed: $_" -ForegroundColor Red
    exit 1
} finally {
    Write-Host "[5/5] Cleaning up isolated temporary container..."
    docker rm -f $TempContainer 2>&1 | Out-Null
    Write-Host "Cleanup completed. Active development / production database untouched." -ForegroundColor Cyan
}