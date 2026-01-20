#!/bin/bash
# Pre-fetches tasks for duplicate-checker skill
# Writes task list to temp file for agent to analyze
# Exit 0 = proceed, Exit 2 = block action

source .env.local 2>/dev/null || source .env 2>/dev/null || true

if [[ -z "$CONVEX_SITE_URL" ]] || [[ -z "$CLAUDE_API_KEY" ]]; then
  echo "ERROR: API not configured. CONVEX_SITE_URL and CLAUDE_API_KEY required."
  exit 2
fi

# Fetch all tasks (agent will filter by status)
TASKS=$(curl -s "${CONVEX_SITE_URL}/api/tasks" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}")

# Check for curl errors
if [[ $? -ne 0 ]]; then
  echo "ERROR: Failed to fetch tasks from API"
  exit 2
fi

# Check for API errors
if echo "$TASKS" | grep -q '"error"'; then
  echo "ERROR: API returned error:"
  echo "$TASKS"
  exit 2
fi

# Write to temp file for agent to read
echo "$TASKS" > /tmp/tasks-for-duplicate-check.json

# Log task count for visibility
TASK_COUNT=$(echo "$TASKS" | jq '.tasks | length' 2>/dev/null || echo "unknown")
echo "INFO: Fetched ${TASK_COUNT} tasks for duplicate checking"

exit 0
