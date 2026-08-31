# Administrator Bootstrapping Runbook

## Overview
This runbook describes the lifecycle of the initial administrator account creation and the decommissioning of bootstrap credentials.

---

## 1. Initial Administrator Provisioning Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor Admin as System Administrator
    participant Env as /opt/carmats/.env
    participant App as Spring Boot (AdminUserInitializer)
    participant DB as PostgreSQL Database

    Admin->>Env: Set ADMIN_INITIAL_EMAIL & ADMIN_INITIAL_PASSWORD
    Admin->>App: Launch Stack (docker compose up -d)
    App->>DB: Query existsByRoles_Name('ROLE_ADMIN')
    DB-->>App: Returns FALSE (0 administrators found)
    App->>App: BCrypt encode ADMIN_INITIAL_PASSWORD
    App->>DB: Create Admin User (ROLE_ADMIN, ROLE_CUSTOMER)
    App-->>Admin: Log "Initial administrative account successfully initialized"
    Admin->>Env: Remove / comment out ADMIN_INITIAL_PASSWORD
    Admin->>App: Restart Container Stack
    App->>DB: Query existsByRoles_Name('ROLE_ADMIN')
    DB-->>App: Returns TRUE
    App-->>Admin: Log "Administrative account verified; skipping initial bootstrap"
```

---

## 2. Step-by-Step Execution

### Step 1: Configure Initial Credentials in `.env`
In `/opt/carmats/.env`, set:
```env
ADMIN_INITIAL_EMAIL=admin@carmats.local
ADMIN_INITIAL_PASSWORD=CHANGE_ME_GENERATE_HIGH_ENTROPY_PASSWORD
ADMIN_INITIAL_FIRST_NAME=Admin
ADMIN_INITIAL_LAST_NAME=Sistem
```

### Step 2: Start Stack & Verify Bootstrap
```bash
docker compose -f docker-compose.prod.yml --env-file .env up -d

# Check backend startup log for confirmation:
docker logs carmats-backend | grep "AdminUserInitializer"
# Expected output:
# INFO c.c.config.security.AdminUserInitializer : Initial administrative account successfully initialized for: admin@carmats.local
```

### Step 3: Decommission Bootstrap Password
Edit `/opt/carmats/.env` and remove or comment out `ADMIN_INITIAL_PASSWORD`:
```env
ADMIN_INITIAL_EMAIL=admin@carmats.local
# ADMIN_INITIAL_PASSWORD= (Removed after first boot)
ADMIN_INITIAL_FIRST_NAME=Admin
ADMIN_INITIAL_LAST_NAME=Sistem
```

### Step 4: Restart Containers & Confirm Persistence
```bash
docker compose -f docker-compose.prod.yml --env-file .env restart backend

# Verify log output:
docker logs carmats-backend | grep "AdminUserInitializer"
# Expected output:
# INFO c.c.config.security.AdminUserInitializer : Administrative account verified; skipping initial bootstrap.
```
> [!NOTE]
> `AdminUserInitializer` will never overwrite, alter, or rotate an existing administrator.
