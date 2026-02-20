FROM node:22-slim

# Install build tools for native modules (better-sqlite3) and Claude Code CLI
RUN apt-get update && apt-get install -y python3 make g++ git && rm -rf /var/lib/apt/lists/*
RUN npm install -g @anthropic-ai/claude-code

WORKDIR /app

# Install pnpm
RUN corepack enable && corepack prepare pnpm@latest --activate

# Copy package files (not pnpm-workspace.yaml - its ignoredBuiltDependencies
# would skip better-sqlite3 native compilation)
COPY package.json pnpm-lock.yaml* ./

# Install dependencies (better-sqlite3 will compile native addon)
RUN pnpm install --frozen-lockfile || pnpm install

# Copy source and build
COPY tsconfig.json ./
COPY src/ ./src/
RUN pnpm build

# Create data directory for SQLite
RUN mkdir -p /app/data

ENV NODE_ENV=production
ENV SERVER_PORT=3141

EXPOSE 3141

CMD ["node", "dist/index.js"]
