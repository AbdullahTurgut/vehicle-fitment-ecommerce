#!/usr/bin/env bash
# ==============================================================================
# CARMATS PRODUCTION SMOKE TEST SCRIPT (scripts/smoke-test-production.sh)
#
# Execution:
#   bash /opt/carmats/scripts/smoke-test-production.sh
# ==============================================================================

set -euo pipefail

BASE_URL="${SMOKE_BASE_URL:-http://127.0.0.1}"

echo "============================================================"
echo "RUNNING PRODUCTION SMOKE TESTS"
echo "Target URL: ${BASE_URL}"
echo "============================================================"

# Test 1: Storefront Root
echo -n "Test 1: Storefront Home (GET /)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/")
if [ "${HTTP_CODE}" -eq 200 ]; then
    echo "PASS (${HTTP_CODE})"
else
    echo "FAIL (Expected 200, got ${HTTP_CODE})" >&2
    exit 1
fi

# Test 2: Catalog Page
echo -n "Test 2: Catalog Page (GET /katalog)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/katalog")
if [ "${HTTP_CODE}" -eq 200 ]; then
    echo "PASS (${HTTP_CODE})"
else
    echo "FAIL (Expected 200, got ${HTTP_CODE})" >&2
    exit 1
fi

# Test 3: Public Catalog Categories API
echo -n "Test 3: Catalog Categories API (GET /api/v1/catalog/categories)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/v1/catalog/categories")
if [ "${HTTP_CODE}" -eq 200 ]; then
    echo "PASS (${HTTP_CODE})"
else
    echo "FAIL (Expected 200, got ${HTTP_CODE})" >&2
    exit 1
fi

# Test 4: Actuator Health Endpoint
echo -n "Test 4: Actuator Health (GET /actuator/health)... "
HEALTH_RESPONSE=$(curl -s "${BASE_URL}/actuator/health" || echo "")
if echo "${HEALTH_RESPONSE}" | grep -q '"status":"UP"'; then
    echo "PASS (status: UP)"
else
    echo "FAIL (Actuator is not UP: ${HEALTH_RESPONSE})" >&2
    exit 1
fi

# Test 5: Actuator Sensitive Endpoint Blocked (/actuator/env)
echo -n "Test 5: Actuator Sensitive Block (/actuator/env)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/env")
if [ "${HTTP_CODE}" -eq 404 ]; then
    echo "PASS (Blocked with 404)"
else
    echo "FAIL (Expected 404, got ${HTTP_CODE})" >&2
    exit 1
fi

# Test 6: Actuator Sensitive Block (/actuator/beans)
echo -n "Test 6: Actuator Sensitive Block (/actuator/beans)... "
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/beans")
if [ "${HTTP_CODE}" -eq 404 ]; then
    echo "PASS (Blocked with 404)"
else
    echo "FAIL (Expected 404, got ${HTTP_CODE})" >&2
    exit 1
fi

# Test 7: Container Health Status Verification
echo -n "Test 7: Docker Compose Container Health... "
for container in carmats-postgres carmats-backend carmats-frontend carmats-nginx; do
    STATUS=$(docker inspect --format='{{json .State.Health.Status}}' "${container}" 2>/dev/null || echo '"unknown"')
    if [ "${STATUS}" != '"healthy"' ]; then
        echo "FAIL (${container} status is ${STATUS})" >&2
        exit 1
    fi
done
echo "PASS (All 4 containers healthy)"

echo "============================================================"
echo "ALL SMOKE TESTS PASSED (7/7)"
echo "============================================================"
