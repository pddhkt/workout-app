#!/bin/bash
# Validates environment before project-init API calls
# Exit codes:
#   0 - Allow action to proceed
#   2 - Block action (agent will ask user what to do)

# Check for required env vars
check_env() {
  if [[ -z "$CONVEX_SITE_URL" ]]; then
    echo "ERROR: CONVEX_SITE_URL not set in .env.local"
    exit 2  # Block the action
  fi
  if [[ -z "$ADMIN_API_KEY" ]]; then
    echo "ERROR: ADMIN_API_KEY not set in .env.local"
    exit 2
  fi
}

# Check if project already exists (if creating)
# Parses the curl command to extract project name from JSON payload
check_project_exists() {
  local cmd="$1"

  # Only check for POST /api/projects (project creation)
  if [[ "$cmd" == *"POST"* ]] && [[ "$cmd" == *"/api/projects"* ]] && [[ "$cmd" != *"/api/projects/"* ]]; then
    # Try to extract project name from -d payload
    local project_name=""
    if [[ "$cmd" =~ \"name\"[[:space:]]*:[[:space:]]*\"([^\"]+)\" ]]; then
      project_name="${BASH_REMATCH[1]}"
    fi

    if [[ -n "$project_name" ]]; then
      local result=$(curl -s "${CONVEX_SITE_URL}/api/projects?name=${project_name}" \
        -H "Authorization: Bearer ${ADMIN_API_KEY}" 2>/dev/null)

      if echo "$result" | grep -q '"_id"'; then
        echo "BLOCKED: Project '$project_name' already exists in Convex"
        echo ""
        echo "Existing project data:"
        echo "$result" | jq '.' 2>/dev/null || echo "$result"
        echo ""
        echo "Options: Update existing project, use different name, or cancel"
        exit 2  # Block action - agent will ask user what to do
      fi
    fi
  fi
}

# Check if CLAUDE_API_KEY already exists (informational only)
check_existing_key() {
  if [[ -f ".env.local" ]] && grep -q "CLAUDE_API_KEY=" .env.local; then
    echo "INFO: CLAUDE_API_KEY already exists in .env.local"
  fi
}

# Main execution
main() {
  # Source env files
  source .env.local 2>/dev/null || source .env 2>/dev/null || true

  # Run environment check
  check_env

  # Check for existing API key (info only)
  check_existing_key

  # Check for duplicate project if this is a curl command
  # The command being validated is passed via TOOL_INPUT env var or stdin
  if [[ -n "$TOOL_INPUT" ]]; then
    check_project_exists "$TOOL_INPUT"
  fi

  # All checks passed
  exit 0
}

main "$@"
