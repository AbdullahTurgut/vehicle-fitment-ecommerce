# Production Deployment Runbook

## Overview
This runbook describes the procedure for deploying the **Vehicle Fitment E-Commerce Platform** on a single Ubuntu 24.04 LTS VPS using immutable container images from GitHub Container Registry (GHCR) and Docker Compose.

---

## 1. VPS Server Prerequisites

### Hardware Requirements
- **vCPU:** 4 vCPU (x86_64)
- **RAM:** 8 GB RAM + 4 GB Swap
- **Storage:** 80–100 GB NVMe SSD
- **OS:** Ubuntu 24.04 LTS (Clean installation)

### System Packages & Docker Installation
```bash
# Update system packages
sudo apt update && sudo apt upgrade -y

# Install prerequisite tools
sudo apt install -y curl wget git ufw fail2ban jq gzip

# Install Docker Engine & Docker Compose Plugin
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo systemctl enable --now docker
```

---

## 2. Directory Layout & Permissions Setup

```bash
# 1. Create dedicated deploy user
sudo adduser --gecos "" carmats-deploy
sudo usermod -aG sudo,docker carmats-deploy

# 2. Create application and storage directories
sudo mkdir -p /opt/carmats/scripts /opt/carmats/infrastructure/nginx
sudo mkdir -p /var/lib/carmats/data /var/lib/carmats/backups
sudo mkdir -p /var/log/carmats/nginx

# 3. Set ownership and directory permissions
sudo chown -R carmats-deploy:carmats-deploy /opt/carmats /var/lib/carmats /var/log/carmats
sudo chmod 750 /opt/carmats
sudo chmod 700 /var/lib/carmats
sudo chmod 755 /var/log/carmats
```

---

## 3. Configuration & Secret Generation

1. Copy deployment files to `/opt/carmats/`:
   - `docker-compose.prod.yml`
   - `infrastructure/nginx/nginx.conf`
   - `scripts/deploy.sh`
   - `scripts/backup-production.sh`
   - `scripts/smoke-test-production.sh`
   - `.env.production.template` ➔ `/opt/carmats/.env`

2. Generate high-entropy secrets inside `/opt/carmats/.env`:
   ```bash
   # Generate database password (48 characters)
   openssl rand -hex 24

   # Generate JWT 256-bit secret (64 characters)
   openssl rand -hex 32

   # Generate initial admin bootstrap password (24 characters)
   openssl rand -base64 18
   ```

3. Set strict permissions on the environment file:
   ```bash
   chmod 600 /opt/carmats/.env
   chmod +x /opt/carmats/scripts/*.sh
   ```

---

## 4. GHCR Container Registry Authentication

To pull private container images from GHCR:

```bash
# Create a GitHub Personal Access Token (PAT) with read:packages scope
echo "$GHCR_PAT" | docker login ghcr.io -u <github-username> --password-stdin
```

---

## 5. Deployment Execution

1. Specify the immutable image tag in `/opt/carmats/.env`:
   ```env
   IMAGE_TAG=sha-1a7059a
   ```

2. Run the automated deployment script:
   ```bash
   cd /opt/carmats && ./scripts/deploy.sh
   ```

3. Verify container health status:
   ```bash
   docker compose -f docker-compose.prod.yml --env-file .env ps
   ```

---

## 6. Post-Deployment Verification
Run the automated smoke test suite:
```bash
./scripts/smoke-test-production.sh
```
