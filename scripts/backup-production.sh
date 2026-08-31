#!/usr/bin/env bash
# ==============================================================================
# CARMATS PRODUCTION DATABASE BACKUP SCRIPT (scripts/backup-production.sh)
#
# Execution:
#   bash /opt/carmats/scripts/backup-production.sh
#
# Requirements:
#   - Reads credentials from /opt/carmats/.env or active environment
#   - Never logs or prints secrets
#   - Sets chmod 600 on generated backup files
# ==============================================================================

set -euo pipefail

ENV_FILE="${ENV_FILE:-/opt/carmats/.env}"
BACKUP_DIR="${BACKUP_DIR:-/var/lib/carmats/backups}"
CONTAINER_NAME="${CONTAINER_NAME:-carmats-postgres}"

# Load environment file if present and variables not already exported
if [ -f "${ENV_FILE}" ]; then
    set -a
    # shellcheck disable=SC1090
    source "${ENV_FILE}"
    set +a
fi

DB_USER="${POSTGRES_USER:-carmats_prod_user}"
DB_NAME="${POSTGRES_DB:-carmats_db_prod}"
DB_PASSWORD="${POSTGRES_PASSWORD:-}"

if [ -z "${DB_PASSWORD}" ]; then
    echo "ERROR: POSTGRES_PASSWORD is not set in environment or ${ENV_FILE}." >&2
    exit 1
fi

mkdir -p "${BACKUP_DIR}"
chmod 700 "${BACKUP_DIR}"

TIMESTAMP=$(date -u +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/backup_${DB_NAME}_${TIMESTAMP}.sql.gz"

echo "Starting database backup for database: ${DB_NAME}..."

# Execute pg_dump inside container and compress on the fly
docker exec -e PGPASSWORD="${DB_PASSWORD}" "${CONTAINER_NAME}" \
    pg_dump -U "${DB_USER}" -d "${DB_NAME}" --no-owner --no-privileges --clean --if-exists \
    | gzip -9 > "${BACKUP_FILE}"

chmod 600 "${BACKUP_FILE}"

FILE_SIZE=$(du -h "${BACKUP_FILE}" | awk '{print $1}')
echo "Backup completed successfully."
echo "Location: ${BACKUP_FILE} (Size: ${FILE_SIZE})"

# Retention Policy: Clean backups older than 30 days
find "${BACKUP_DIR}" -name "backup_*.sql.gz" -type f -mtime +30 -delete
echo "Backup retention check completed (retaining 30 days)."
