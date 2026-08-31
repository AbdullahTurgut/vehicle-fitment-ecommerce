#!/usr/bin/env bash
set -euo pipefail

BACKUP_FILE="${1:-}"
POSTGRES_IMAGE="${2:-postgres:17-alpine}"

echo "=========================================="
echo "  CARMATS SAFE RESTORE VERIFICATION       "
echo "=========================================="

if [ -z "$BACKUP_FILE" ]; then
    BACKUP_FILE=$(find backups -name "backup_*.sql" 2>/dev/null | sort | tail -n 1)
    if [ -z "$BACKUP_FILE" ]; then
        echo "[ERROR] No backup file found in backups/ directory." >&2
        exit 1
    fi
fi

if [ ! -f "$BACKUP_FILE" ]; then
    echo "[ERROR] Backup file not found: $BACKUP_FILE" >&2
    exit 1
fi

TEMP_CONTAINER="carmats-verify-temp-$RANDOM"
VERIFY_DB="carmats_verify_temp_db"
VERIFY_USER="carmats_verify_temp_user"
VERIFY_PASS=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c 32 || echo "temp_verify_token_$RANDOM")

echo "Backup File      : $BACKUP_FILE"
echo "Temp Container   : $TEMP_CONTAINER"
echo "Temp Database    : $VERIFY_DB"
echo "Isolation Target : Ephemeral isolated container (zero impact on active DB)"
echo "------------------------------------------"

cleanup() {
    echo "[5/5] Cleaning up isolated temporary container..."
    docker rm -f "$TEMP_CONTAINER" >/dev/null 2>&1 || true
    echo "Cleanup completed. Active development / production database untouched."
}
trap cleanup EXIT

echo "[1/5] Starting isolated ephemeral container..."
docker run -d --name "$TEMP_CONTAINER" -e POSTGRES_DB="$VERIFY_DB" -e POSTGRES_USER="$VERIFY_USER" -e POSTGRES_PASSWORD="$VERIFY_PASS" "$POSTGRES_IMAGE" >/dev/null

echo "[2/5] Waiting for isolated PostgreSQL engine readiness..."
retries=30
sleep 2
while [ $retries -gt 0 ]; do
    if docker exec -e PGPASSWORD="$VERIFY_PASS" "$TEMP_CONTAINER" psql -U "$VERIFY_USER" -d "$VERIFY_DB" -c "SELECT 1;" >/dev/null 2>&1; then
        break
    fi
    sleep 1
    retries=$((retries - 1))
done

if [ $retries -eq 0 ]; then
    echo "[ERROR] Temporary PostgreSQL container failed to become ready for connections." >&2
    exit 1
fi

echo "[3/5] Restoring backup into isolated ephemeral database..."
docker exec -i -e PGPASSWORD="$VERIFY_PASS" "$TEMP_CONTAINER" psql -U "$VERIFY_USER" -d "$VERIFY_DB" < "$BACKUP_FILE" >/dev/null

echo "[4/5] Running data integrity and schema verification queries..."
TABLE_COUNT=$(docker exec -e PGPASSWORD="$VERIFY_PASS" "$TEMP_CONTAINER" psql -U "$VERIFY_USER" -d "$VERIFY_DB" -t -c "SELECT count(*) FROM information_schema.tables WHERE table_schema='public';" | tr -d '[:space:]')
FLYWAY_COUNT=$(docker exec -e PGPASSWORD="$VERIFY_PASS" "$TEMP_CONTAINER" psql -U "$VERIFY_USER" -d "$VERIFY_DB" -t -c "SELECT count(*) FROM flyway_schema_history;" | tr -d '[:space:]')
BRAND_COUNT=$(docker exec -e PGPASSWORD="$VERIFY_PASS" "$TEMP_CONTAINER" psql -U "$VERIFY_USER" -d "$VERIFY_DB" -t -c "SELECT count(*) FROM vehicle_brands;" | tr -d '[:space:]')
PRODUCT_COUNT=$(docker exec -e PGPASSWORD="$VERIFY_PASS" "$TEMP_CONTAINER" psql -U "$VERIFY_USER" -d "$VERIFY_DB" -t -c "SELECT count(*) FROM products;" | tr -d '[:space:]')
USER_COUNT=$(docker exec -e PGPASSWORD="$VERIFY_PASS" "$TEMP_CONTAINER" psql -U "$VERIFY_USER" -d "$VERIFY_DB" -t -c "SELECT count(*) FROM users;" | tr -d '[:space:]')

echo "------------------------------------------"
echo "  VERIFICATION RESULTS (Isolated DB)      "
echo "------------------------------------------"
echo "  Public Tables Count    : $TABLE_COUNT"
echo "  Flyway Migrations Run  : $FLYWAY_COUNT"
echo "  Vehicle Brands Count   : $BRAND_COUNT"
echo "  Catalog Products Count : $PRODUCT_COUNT"
echo "  Users Count            : $USER_COUNT"

if [ "$TABLE_COUNT" -lt 10 ] || [ "$FLYWAY_COUNT" -lt 1 ]; then
    echo "[ERROR] Verification failed: insufficient tables or missing flyway migrations." >&2
    exit 1
fi

echo "[SUCCESS] Backup restore and basic schema/data integrity verification passed."