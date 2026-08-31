#!/usr/bin/env bash
# ==============================================================================
# CARMATS PRODUCTION DEPLOYMENT SCRIPT (scripts/deploy.sh)
#
# Execution:
#   cd /opt/carmats && ./scripts/deploy.sh
#
# Steps:
#   1. Preflight environment and tool verification
#   2. Pre-deployment database backup (pg_dump)
#   3. Pull immutable GHCR container images
#   4. Recreate and start updated containers
#   5. Wait for container healthcheck validation
#   6. Execute production smoke tests
# ==============================================================================

set -euo pipefail

APP_DIR="/opt/carmats"
COMPOSE_FILE="${APP_DIR}/docker-compose.prod.yml"
ENV_FILE="${APP_DIR}/.env"
BACKUP_SCRIPT="${APP_DIR}/scripts/backup-production.sh"
SMOKE_SCRIPT="${APP_DIR}/scripts/smoke-test-production.sh"

echo "============================================================"
echo "CARMATS PRODUCTION DEPLOYMENT START"
echo "Timestamp: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo "============================================================"

# 1. Preflight Checks
echo "[1/6] Running preflight checks..."
if [ ! -f "${ENV_FILE}" ]; then
    echo "ERROR: Production environment file not found at ${ENV_FILE}" >&2
    exit 1
fi

if [ ! -f "${COMPOSE_FILE}" ]; then
    echo "ERROR: Production compose file not found at ${COMPOSE_FILE}" >&2
    exit 1
fi

# Load environment variables
set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

if [ -z "${IMAGE_TAG:-}" ]; then
    echo "ERROR: IMAGE_TAG is not set in ${ENV_FILE}. Specify an immutable tag (e.g. IMAGE_TAG=sha-1a7059a)." >&2
    exit 1
fi

echo "Target immutable image tag: ${IMAGE_TAG}"

# 2. Automated Pre-Deployment Database Backup
echo "[2/6] Executing pre-deployment database backup..."
if [ -f "${BACKUP_SCRIPT}" ]; then
    bash "${BACKUP_SCRIPT}" || {
        echo "WARNING: Backup script returned non-zero. Check if database container is running."
    }
else
    echo "NOTICE: Backup script not found at ${BACKUP_SCRIPT}, skipping pre-deployment backup."
fi

# 3. Pull Immutable Container Images
echo "[3/6] Pulling immutable images from GHCR..."
docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" pull backend frontend nginx

# 4. Recreate & Start Containers
echo "[4/6] Starting updated containers with Docker Compose..."
docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" up -d --remove-orphans

# 5. Wait for Container Healthchecks
echo "[5/6] Waiting for container health checks..."
MAX_WAIT=120
ELAPSED=0
ALL_HEALTHY=false

while [ ${ELAPSED} -lt ${MAX_WAIT} ]; do
    UNHEALTHY_COUNT=0
    for container in carmats-postgres carmats-backend carmats-frontend carmats-nginx; do
        STATUS=$(docker inspect --format='{{json .State.Health.Status}}' "${container}" 2>/dev/null || echo '"unknown"')
        if [ "${STATUS}" != '"healthy"' ]; then
            UNHEALTHY_COUNT=$((UNHEALTHY_COUNT + 1))
        fi
    done

    if [ ${UNHEALTHY_COUNT} -eq 0 ]; then
        ALL_HEALTHY=true
        break
    fi

    sleep 5
    ELAPSED=$((ELAPSED + 5))
    echo "Waiting for services to become healthy (${ELAPSED}s / ${MAX_WAIT}s)..."
done

if [ "${ALL_HEALTHY}" != "true" ]; then
    echo "============================================================"
    echo "DEPLOYMENT FAILED: Containers did not reach healthy state."
    echo "============================================================"
    docker compose -f "${COMPOSE_FILE}" --env-file "${ENV_FILE}" ps
    echo ""
    echo "ROLLBACK INSTRUCTIONS:"
    echo "1. Edit ${ENV_FILE} and set IMAGE_TAG to previous working SHA tag."
    echo "2. Run: docker compose -f ${COMPOSE_FILE} --env-file ${ENV_FILE} up -d"
    echo "3. (Optional) If database was modified, restore from /var/lib/carmats/backups/"
    exit 1
fi

# 6. Execute Production Smoke Tests
echo "[6/6] Executing smoke tests..."
if [ -f "${SMOKE_SCRIPT}" ]; then
    bash "${SMOKE_SCRIPT}" || {
        echo "============================================================"
        echo "SMOKE TEST FAILED: Verification checks returned errors."
        echo "============================================================"
        exit 1
    }
fi

echo "============================================================"
echo "DEPLOYMENT SUCCESSFUL"
echo "Active Image Tag: ${IMAGE_TAG}"
echo "Completed At: $(date -u +"%Y-%m-%dT%H:%M:%SZ")"
echo "============================================================"
