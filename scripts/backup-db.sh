#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="${POSTGRES_CONTAINER_NAME:-${1:-carmats-postgres}}"
DB_USER="${POSTGRES_USER:-${2:-carmats}}"
DB_NAME="${POSTGRES_DB:-${3:-carmats_db}}"
OUTPUT_DIR="${4:-backups}"

if [ -z "${POSTGRES_PASSWORD:-}" ]; then
    echo "[ERROR] Missing required environment variable: POSTGRES_PASSWORD" >&2
    echo "Please set POSTGRES_PASSWORD before running this script." >&2
    exit 1
fi

mkdir -p "$OUTPUT_DIR"

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${OUTPUT_DIR}/backup_${DB_NAME}_${TIMESTAMP}.sql"

echo "=========================================="
echo "  CARMATS DATABASE BACKUP AUTOMATION      "
echo "=========================================="
echo "Container : $CONTAINER_NAME"
echo "Database  : $DB_NAME"
echo "User      : $DB_USER"
echo "Output    : $BACKUP_FILE"
echo "Mode      : Read-only dump (pg_dump)"
echo "------------------------------------------"

if ! docker ps --filter "name=^/${CONTAINER_NAME}$" --format "{{.Names}}" | grep -q "^${CONTAINER_NAME}$"; then
    echo "[ERROR] Container '$CONTAINER_NAME' is not running." >&2
    exit 1
fi

docker exec -e PGPASSWORD="$POSTGRES_PASSWORD" "$CONTAINER_NAME" pg_dump -U "$DB_USER" -d "$DB_NAME" --clean --if-exists --no-owner --no-privileges > "$BACKUP_FILE"

if [ ! -s "$BACKUP_FILE" ]; then
    echo "[ERROR] Backup file was not created or is empty." >&2
    exit 1
fi

FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
echo "[SUCCESS] Backup created successfully!"
echo "File Size : $FILE_SIZE"
echo "Timestamp : $TIMESTAMP"
echo "File Path : $BACKUP_FILE"