#!/bin/bash
# Pre-fetches task data for /task-dev command
# UserPromptSubmit hook - runs before Claude processes the prompt
# Exit 0 = proceed (with output injected), Exit 2 = block action

# Source environment variables
source .env.local 2>/dev/null || source .env 2>/dev/null || true

# Generate codebase inventory to file (for task-scout agent)
# This keeps inventory out of main context, scout reads from file
INVENTORY_FILE=".claude/cache/inventory.md"
mkdir -p "$(dirname "$INVENTORY_FILE")"
if [[ -x ".claude/scripts/generate-inventory.sh" ]]; then
  ./.claude/scripts/generate-inventory.sh > "$INVENTORY_FILE" 2>/dev/null
fi

# Validate required environment variables
if [[ -z "$CONVEX_SITE_URL" ]] || [[ -z "$CLAUDE_API_KEY" ]]; then
  echo "ERROR: API not configured."
  echo "Required: CONVEX_SITE_URL and CLAUDE_API_KEY in .env or .env.local"
  echo "Run /project-init first to register this project and generate an API key."
  exit 2
fi

# Read the prompt from stdin (Claude Code passes JSON with prompt field)
INPUT=$(cat)
USER_PROMPT=$(echo "$INPUT" | jq -r '.prompt // empty' 2>/dev/null)

# Fallback: if jq parsing fails, use the raw input
if [[ -z "$USER_PROMPT" ]]; then
  USER_PROMPT="$INPUT"
fi

# Extract task IDs from the prompt
# Supports: /task-dev FT-001,FT-002 or /task-dev FT-001 FT-002
# Pattern matches: FT-NNN, BF-NNN, RF-NNN (any number of digits)
TASK_IDS=$(echo "$USER_PROMPT" | grep -oE '(FT|BF|RF)-[0-9]+' | sort -u | tr '\n' ' ')

# If no task IDs found, let Claude handle it (maybe --help or other args)
if [[ -z "$TASK_IDS" ]]; then
  exit 0
fi

# Convert space-separated IDs to JSON array
# FT-001 FT-002 -> ["FT-001","FT-002"]
JSON_ARRAY=$(echo "$TASK_IDS" | tr ' ' '\n' | grep -v '^$' | jq -R . | jq -s .)

# Call bulk-fetch endpoint
RESPONSE=$(curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/bulk-fetch" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "{\"customIds\": $JSON_ARRAY}" 2>/dev/null)

# Check for curl errors
if [[ $? -ne 0 ]]; then
  echo "WARNING: Failed to prefetch tasks from API. Claude will fetch manually."
  exit 0  # Don't block, just proceed without prefetch
fi

# Check for API errors
if echo "$RESPONSE" | jq -e '.error' > /dev/null 2>&1; then
  ERROR_MSG=$(echo "$RESPONSE" | jq -r '.error')
  echo "WARNING: API error during prefetch: $ERROR_MSG"
  exit 0  # Don't block, just proceed without prefetch
fi

# Extract counts
TASK_COUNT=$(echo "$RESPONSE" | jq '.tasks | length')
NOT_FOUND_COUNT=$(echo "$RESPONSE" | jq '.notFound | length')
NOT_FOUND=$(echo "$RESPONSE" | jq -r '.notFound | join(", ")')

# Update status to in_progress for all found tasks (single API call)
TASK_IDS_ARRAY=$(echo "$RESPONSE" | jq '[.tasks[].customId]')
STATUS_RESULT=$(curl -s -X POST "${CONVEX_SITE_URL}/api/tasks/bulk-status" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "{\"customIds\": $TASK_IDS_ARRAY, \"status\": \"in_progress\"}" 2>/dev/null)

UPDATED_IDS=$(echo "$STATUS_RESULT" | jq -r '.updated | join(", ")' 2>/dev/null)

# Output context for Claude (this gets injected into the prompt context)
echo "<task-context>"
echo "## Pre-fetched Task Data"
echo ""
echo "The following ${TASK_COUNT} task(s) were pre-fetched and marked as **in_progress**."
echo "Task data and status are ready - proceed directly to Phase 1 (Scout)."
echo ""

# Output each task in a structured format
echo "$RESPONSE" | jq -r '.tasks[] | "### \(.customId): \(.title)

| Field | Value |
|-------|-------|
| Type | \(.type) |
| Status | \(.status) |
| Priority | \(.priority) |
| Domain | \(.domain // "not set") |
| Complexity | \(.complexity // "not set") |

**Description:**
\(.description // "No description provided.")

**Acceptance Criteria:**
\(if .acceptanceCriteria and (.acceptanceCriteria | length) > 0 then (.acceptanceCriteria | map("- " + .) | join("\n")) else "- None specified" end)

**Dependencies:** \(if .dependencies and (.dependencies | length) > 0 then (.dependencies | join(", ")) else "None" end)
**Tags:** \(if .tags and (.tags | length) > 0 then (.tags | join(", ")) else "None" end)

---
"'

# Report any not found tasks
if [[ "$NOT_FOUND_COUNT" -gt 0 ]] && [[ -n "$NOT_FOUND" ]]; then
  echo ""
  echo "**Warning:** The following task ID(s) were not found: ${NOT_FOUND}"
  echo ""
fi

echo "</task-context>"
echo ""
echo "**Instructions:** Task data is pre-loaded and status is already set to in_progress. Skip Phase 0 entirely and proceed directly to Phase 1 (Scout)."

exit 0
