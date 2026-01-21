#!/bin/bash
# Dev-Loop Stop Hook
# Intercepts session exit to check if dev-loop should continue
#
# This hook:
# 1. Checks if .claude/dev-loop-active marker exists (fast local check)
# 2. Fetches loop session from Convex API
# 3. Checks completion criteria
# 4. If complete → cleans up and allows exit
# 5. If continue → outputs JSON to continue loop

set -e

# Load environment
source .env.local 2>/dev/null || source .env 2>/dev/null || true

MARKER_FILE=".claude/dev-loop-active"

# Quick exit if no active loop
if [ ! -f "$MARKER_FILE" ]; then
  exit 0
fi

# Read session ID from marker
SESSION_ID=$(grep "session_id=" "$MARKER_FILE" 2>/dev/null | cut -d= -f2)

if [ -z "$SESSION_ID" ]; then
  # Invalid marker, clean up
  rm -f "$MARKER_FILE"
  exit 0
fi

# Check required environment variables
if [ -z "$CONVEX_SITE_URL" ] || [ -z "$CLAUDE_API_KEY" ]; then
  echo "WARNING: CONVEX_SITE_URL or CLAUDE_API_KEY not set. Cannot check dev-loop status."
  exit 0
fi

# Fetch session from Convex
SESSION=$(curl -s -X GET "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" 2>/dev/null)

# Check if session exists and is active
ACTIVE=$(echo "$SESSION" | jq -r '.active // false' 2>/dev/null)

if [ "$ACTIVE" != "true" ]; then
  # Session not active, clean up marker
  rm -f "$MARKER_FILE"
  exit 0
fi

# Extract session details
COMPLETION_TYPE=$(echo "$SESSION" | jq -r '.completionType')
COMPLETION_VALUE=$(echo "$SESSION" | jq -r '.completionValue')
CYCLE=$(echo "$SESSION" | jq -r '.cycle // 0')
MAX_CYCLES=$(echo "$SESSION" | jq -r '.maxCycles // 50')

# Check max cycles
if [ "$CYCLE" -ge "$MAX_CYCLES" ]; then
  echo "Dev-loop: Max cycles ($MAX_CYCLES) reached. Ending loop."
  # Clean up session
  curl -s -X DELETE "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" > /dev/null 2>&1
  rm -f "$MARKER_FILE"
  exit 0
fi

# Check completion based on type
check_complete() {
  case "$COMPLETION_TYPE" in
    "until-task")
      TASK_STATUS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks/${COMPLETION_VALUE}" \
        -H "Authorization: Bearer ${CLAUDE_API_KEY}" 2>/dev/null | jq -r '.status // "pending"')
      [ "$TASK_STATUS" == "completed" ]
      ;;

    "until-phase")
      PENDING=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks?status=pending&maxPhase=${COMPLETION_VALUE}" \
        -H "Authorization: Bearer ${CLAUDE_API_KEY}" 2>/dev/null | jq '.tasks | length // 0')
      [ "${PENDING:-1}" -eq 0 ]
      ;;

    "all-pending")
      PENDING=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks?status=pending" \
        -H "Authorization: Bearer ${CLAUDE_API_KEY}" 2>/dev/null | jq '.tasks | length // 0')
      [ "${PENDING:-1}" -eq 0 ]
      ;;

    "until-milestone")
      PENDING=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks?milestone=${COMPLETION_VALUE}&status=pending" \
        -H "Authorization: Bearer ${CLAUDE_API_KEY}" 2>/dev/null | jq '.tasks | length // 0')
      [ "${PENDING:-1}" -eq 0 ]
      ;;

    *)
      # Unknown type, allow exit
      return 0
      ;;
  esac
}

if check_complete; then
  echo "Dev-loop: Completion criteria met. Ending loop."
  # Clean up session
  curl -s -X DELETE "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" > /dev/null 2>&1
  rm -f "$MARKER_FILE"
  exit 0
fi

# Not complete - increment cycle and output continuation
NEW_CYCLE=$((CYCLE + 1))

# Update session cycle count
curl -s -X PATCH "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "{\"cycle\": ${NEW_CYCLE}, \"lastCycleAt\": $(date +%s000)}" > /dev/null 2>&1

# Get pending tasks for next batch
PENDING_TASKS=$(curl -s -X GET "${CONVEX_SITE_URL}/api/tasks?status=pending" \
  -H "Authorization: Bearer ${CLAUDE_API_KEY}" 2>/dev/null)

BATCH_SIZE=$(echo "$SESSION" | jq -r '.batchSize // 1')
TASKS_COMPLETED=$(echo "$SESSION" | jq -r '.tasksCompleted | length // 0')

# Select next batch (simplified - just get first N pending tasks with satisfied deps)
NEXT_BATCH=$(echo "$PENDING_TASKS" | jq -r --arg n "$BATCH_SIZE" '
  .tasks
  | sort_by(.phaseNumber)
  | .[0:($n | tonumber)]
  | map(.customId)
  | join(",")
' 2>/dev/null)

if [ -z "$NEXT_BATCH" ] || [ "$NEXT_BATCH" == "null" ]; then
  echo "Dev-loop: No eligible tasks found. Ending loop."
  curl -s -X DELETE "${CONVEX_SITE_URL}/api/loop-sessions/${SESSION_ID}" \
    -H "Authorization: Bearer ${CLAUDE_API_KEY}" > /dev/null 2>&1
  rm -f "$MARKER_FILE"
  exit 0
fi

# Output JSON for Claude to continue the loop
# This will be read by the orchestrator to continue execution
cat << EOF

=== DEV-LOOP CONTINUE ===

Session: ${SESSION_ID}
Cycle: ${NEW_CYCLE} / ${MAX_CYCLES}
Completion: ${COMPLETION_TYPE} ${COMPLETION_VALUE}
Tasks Completed: ${TASKS_COMPLETED}

Next Batch: ${NEXT_BATCH}

Continue dev-loop by running:
/task-dev ${NEXT_BATCH} --auto

=========================

EOF

# Exit with special code to indicate continuation needed
# The Claude orchestrator will see this output and continue
exit 0
