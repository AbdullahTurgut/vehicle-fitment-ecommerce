# Production Rollback Runbook

## Overview
This runbook defines the emergency rollback procedure when a production release candidate exhibits critical defects or fails post-deployment smoke tests.

---

## 1. Application-Only Rollback (No Schema Changes)

If the failed deployment did not introduce forward database schema migrations, rollback is instant and zero-recompilation by switching the immutable image tag.

### Step 1: Update Image Tag
Edit `/opt/carmats/.env` and revert `IMAGE_TAG` to the previous known good commit SHA:
```env
# Previous working release candidate SHA
IMAGE_TAG=sha-previous-known-good-sha
```

### Step 2: Recreate Application Containers
```bash
cd /opt/carmats
docker compose -f docker-compose.prod.yml --env-file .env up -d --remove-orphans backend frontend
```

### Step 3: Run Smoke Verification
```bash
./scripts/smoke-test-production.sh
```

---

## 2. Full Rollback with Database Schema Recovery

> [!WARNING]
> Flyway database migrations are **forward-only**. If a new migration has already executed on startup and the schema is backward-incompatible, you must restore from the pre-deployment database backup.

### Step 1: Stop Application Traffic
```bash
docker compose -f docker-compose.prod.yml --env-file .env stop backend frontend
```

### Step 2: Restore Database from Pre-Deployment Dump
```bash
# Locate pre-deployment backup dump
LATEST_BACKUP=$(ls -t /var/lib/carmats/backups/backup_*.sql.gz | head -n 1)

echo "Restoring database from: ${LATEST_BACKUP}"

gunzip -c "${LATEST_BACKUP}" | \
  docker exec -i carmats-postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}"
```

### Step 3: Revert Image Tag & Start Services
```bash
# Set IMAGE_TAG to previous working SHA in .env
docker compose -f docker-compose.prod.yml --env-file .env up -d
./scripts/smoke-test-production.sh
```
