#!/bin/bash
# Validates environment before task-init Bash commands
# Exit 0 = proceed, Exit 2 = block action

source .env.local 2>/dev/null || source .env 2>/dev/null || true

# Check required env vars
if [[ -z "$CONVEX_SITE_URL" ]]; then
  echo "ERROR: CONVEX_SITE_URL not set in .env.local"
  echo "Please set CONVEX_SITE_URL=https://your-deployment.convex.site"
  exit 2
fi

if [[ -z "$CLAUDE_API_KEY" ]]; then
  echo "ERROR: CLAUDE_API_KEY not set in .env.local"
  echo "Run /project-init first to register this project and generate an API key."
  exit 2
fi

exit 0
