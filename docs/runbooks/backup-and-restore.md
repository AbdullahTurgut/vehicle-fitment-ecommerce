# Database Backup & Restore Runbook

## Overview
This runbook defines the automated backup schedules, retention policies, offsite replication strategy, and disaster recovery restore procedures for the production PostgreSQL database.

---

## 1. Automated Backup Schedule

### Crontab Setup
On the production VPS, schedule daily backups at `03:00 UTC`:

```bash
# Edit crontab for carmats-deploy user
crontab -e

# Add automated daily backup entry:
0 3 * * * /opt/carmats/scripts/backup-production.sh >> /var/log/carmats/backup.log 2>&1
```

---

## 2. Retention Policy

| Tier | Retention Period | Frequency | Purpose |
|---|---|---|---|
| **Daily** | 7 Days | Daily (03:00 UTC) | Fast point-in-time recovery |
| **Weekly** | 4 Weeks | Every Sunday | Rolling medium-term recovery |
| **Monthly** | 3 Months | 1st of each month | Long-term operational archival |

Local backup files older than 30 days are automatically purged by `scripts/backup-production.sh`.

---

## 3. Offsite Cold Storage Replication (Phase 2)

To protect against catastrophic server or datacenter loss, replicate compressed dumps to S3-compatible cloud storage (Cloudflare R2, AWS S3, or Backblaze B2) using `rclone` with client-side encryption.

```bash
# Sync local backups to encrypted remote bucket
rclone sync /var/lib/carmats/backups/ remote-carmats-backups:carmats-db-prod/ --fast-list
```

---

## 4. Disaster Recovery & Isolated Restore Procedure

> [!CAUTION]
> Never perform unverified database restores directly against the active production container. Always verify restore integrity in an isolated ephemeral test container first.

### Step 1: Isolated Restore Verification
```bash
# Run isolated restore verification against a temporary test container
./scripts/restore-verification.sh /var/lib/carmats/backups/backup_carmats_db_prod_YYYYMMDD_HHMMSS.sql.gz
```

### Step 2: Production Database Recovery (Emergency Only)
```bash
# 1. Stop backend container to halt incoming transactions
docker compose -f docker-compose.prod.yml stop backend

# 2. Restore database from verified backup dump
gunzip -c /var/lib/carmats/backups/backup_carmats_db_prod_YYYYMMDD_HHMMSS.sql.gz | \
  docker exec -i carmats-postgres psql -U carmats_prod_user -d carmats_db_prod

# 3. Restart backend container
docker compose -f docker-compose.prod.yml start backend

# 4. Verify system health
./scripts/smoke-test-production.sh
```
