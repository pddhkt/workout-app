#!/bin/bash
# Generates codebase inventory for scout agents
# Scans project structure and outputs markdown inventory

set -e

echo "## Codebase Inventory"
echo ""
echo "Auto-generated structure of the codebase."
echo ""

# ============================================
# CUSTOM COMPONENTS (excluding UI library)
# ============================================
echo "### Custom Components"
echo ""
echo "| Component | Path |"
echo "|-----------|------|"

# Find .tsx files in src/components, exclude ui/ directory
find src/components -name "*.tsx" -not -path "*/ui/*" -not -path "*/node_modules/*" 2>/dev/null | sort | while read -r file; do
  # Extract component name from filename
  name=$(basename "$file" .tsx)
  # Skip index files, show the directory component name instead
  if [[ "$name" == "index" ]]; then
    name=$(basename "$(dirname "$file")")
  fi
  echo "| $name | $file |"
done

echo ""

# ============================================
# UI COMPONENTS (shadcn/ui library)
# ============================================
echo "### UI Components (shadcn/ui)"
echo ""
echo "Available UI primitives in \`src/components/ui/\`:"
echo ""

# List UI components as comma-separated
ui_components=$(find src/components/ui -name "*.tsx" 2>/dev/null | xargs -I {} basename {} .tsx | sort | tr '\n' ', ' | sed 's/,$//')
echo "$ui_components"
echo ""

# ============================================
# HOOKS
# ============================================
echo "### Custom Hooks"
echo ""
echo "| Hook | Path |"
echo "|------|------|"

find src/hooks -name "*.ts" -not -name "*.d.ts" 2>/dev/null | sort | while read -r file; do
  name=$(basename "$file" .ts)
  echo "| $name | $file |"
done

echo ""

# ============================================
# ROUTES
# ============================================
echo "### Routes"
echo ""
echo "| Route File | URL Path |"
echo "|------------|----------|"

find src/routes -name "*.tsx" 2>/dev/null | sort | while read -r file; do
  name=$(basename "$file" .tsx)

  # Convert file name to URL path
  # __root.tsx -> (root layout)
  # index.tsx -> /
  # login.tsx -> /login
  # projects.$projectId.tsx -> /projects/:projectId

  if [[ "$name" == "__root" ]]; then
    url="(root layout)"
  elif [[ "$name" == "index" ]]; then
    url="/"
  else
    # Replace $ with : for params, . with /
    url=$(echo "$name" | sed 's/\$/:/' | sed 's/\./\//g')
    url="/$url"
  fi

  echo "| $name.tsx | $url |"
done

echo ""

# ============================================
# BACKEND FUNCTIONS (Convex)
# ============================================
echo "### Backend Functions (Convex)"
echo ""
echo "| File | Exports |"
echo "|------|---------|"

# For each convex/*.ts file (excluding _generated, schema, auth)
for file in convex/*.ts; do
  [[ -f "$file" ]] || continue
  name=$(basename "$file")

  # Skip generated and config files
  [[ "$name" == "schema.ts" ]] && continue
  [[ "$name" == "auth.ts" ]] && continue
  [[ "$name" == "auth.config.ts" ]] && continue

  # Extract exported const names (query, mutation, action definitions)
  exports=$(grep -E "^export const [a-zA-Z]+" "$file" 2>/dev/null | sed 's/export const //' | sed 's/ .*//' | tr '\n' ', ' | sed 's/,$//')

  if [[ -n "$exports" ]]; then
    echo "| $name | $exports |"
  fi
done

echo ""

# ============================================
# DATABASE SCHEMA
# ============================================
echo "### Database Tables"
echo ""

if [[ -f "convex/schema.ts" ]]; then
  echo "| Table | Indexes |"
  echo "|-------|---------|"

  # Extract table definitions - look for "tableName: defineTable" pattern
  grep -E "^\s+[a-zA-Z]+: defineTable" convex/schema.ts 2>/dev/null | while read -r line; do
    # Extract table name (before the colon)
    table=$(echo "$line" | sed 's/:.*//' | tr -d ' ')

    # Try to find indexes for this table
    # Look for .index('index_name', ...) after the table definition
    indexes=$(grep -A 50 "^  $table: defineTable" convex/schema.ts 2>/dev/null | \
              grep -oE "\.index\('[^']+'" | \
              sed "s/.index('//" | sed "s/'//" | \
              tr '\n' ', ' | sed 's/,$//')

    if [[ -n "$indexes" ]]; then
      echo "| $table | $indexes |"
    else
      echo "| $table | - |"
    fi
  done
fi

echo ""

# ============================================
# TYPES
# ============================================
echo "### Shared Types"
echo ""

if [[ -d "src/types" ]]; then
  echo "| File | Types |"
  echo "|------|-------|"

  find src/types -name "*.ts" -not -name "*.d.ts" 2>/dev/null | sort | while read -r file; do
    name=$(basename "$file")
    # Extract type/interface names
    types=$(grep -E "^export (type|interface) [A-Z]" "$file" 2>/dev/null | sed 's/export type //' | sed 's/export interface //' | sed 's/ .*//' | tr '\n' ', ' | sed 's/,$//')

    if [[ -n "$types" ]]; then
      echo "| $name | $types |"
    else
      echo "| $name | (see file) |"
    fi
  done
fi

echo ""
echo "---"
echo "_Generated at: $(date -Iseconds)_"
