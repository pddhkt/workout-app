import Database from "better-sqlite3";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const DB_PATH = path.join(__dirname, "..", "data", "workout-agent.db");

// Ensure the data directory exists
import { mkdirSync } from "node:fs";
mkdirSync(path.dirname(DB_PATH), { recursive: true });

const db = new Database(DB_PATH);

// Enable WAL mode for better concurrent read performance
db.pragma("journal_mode = WAL");
db.pragma("foreign_keys = ON");

// Create tables
db.exec(`
  CREATE TABLE IF NOT EXISTS conversations (
    id TEXT PRIMARY KEY,
    title TEXT,
    agent_session_id TEXT,
    status TEXT DEFAULT 'active',
    created_at INTEGER,
    updated_at INTEGER
  );

  CREATE TABLE IF NOT EXISTS messages (
    id TEXT PRIMARY KEY,
    conversation_id TEXT REFERENCES conversations(id) ON DELETE CASCADE,
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    metadata TEXT,
    created_at INTEGER
  );
`);

// Prepared statements for conversations
const insertConversation = db.prepare(`
  INSERT INTO conversations (id, title, agent_session_id, status, created_at, updated_at)
  VALUES (?, ?, ?, ?, ?, ?)
`);

const selectConversations = db.prepare(`
  SELECT * FROM conversations WHERE status = 'active' ORDER BY updated_at DESC
`);

const selectConversation = db.prepare(`
  SELECT * FROM conversations WHERE id = ?
`);

const deleteConversationStmt = db.prepare(`
  DELETE FROM conversations WHERE id = ?
`);

const updateSessionId = db.prepare(`
  UPDATE conversations SET agent_session_id = ?, updated_at = ? WHERE id = ?
`);

const updateConversationTitle = db.prepare(`
  UPDATE conversations SET title = ?, updated_at = ? WHERE id = ?
`);

// Prepared statements for messages
const insertMessage = db.prepare(`
  INSERT INTO messages (id, conversation_id, role, content, metadata, created_at)
  VALUES (?, ?, ?, ?, ?, ?)
`);

const selectMessages = db.prepare(`
  SELECT * FROM messages WHERE conversation_id = ? ORDER BY created_at ASC
`);

// --- Conversation helpers ---

export interface Conversation {
  id: string;
  title: string | null;
  agent_session_id: string | null;
  status: string;
  created_at: number;
  updated_at: number;
}

export function createConversation(id: string, title?: string): Conversation {
  const now = Date.now();
  insertConversation.run(id, title ?? null, null, "active", now, now);
  return selectConversation.get(id) as Conversation;
}

export function getConversations(): Conversation[] {
  return selectConversations.all() as Conversation[];
}

export function getConversation(id: string): Conversation | undefined {
  return selectConversation.get(id) as Conversation | undefined;
}

export function deleteConversation(id: string): boolean {
  const result = deleteConversationStmt.run(id);
  return result.changes > 0;
}

export function updateAgentSessionId(
  conversationId: string,
  sessionId: string
): void {
  const now = Date.now();
  updateSessionId.run(sessionId, now, conversationId);
}

export function setConversationTitle(
  conversationId: string,
  title: string
): void {
  const now = Date.now();
  updateConversationTitle.run(title, now, conversationId);
}

// --- Message helpers ---

export interface Message {
  id: string;
  conversation_id: string;
  role: string;
  content: string;
  metadata: string | null;
  created_at: number;
}

export function createMessage(
  id: string,
  conversationId: string,
  role: string,
  content: string,
  metadata?: object | null
): Message {
  const now = Date.now();
  const metadataStr = metadata ? JSON.stringify(metadata) : null;
  insertMessage.run(id, conversationId, role, content, metadataStr, now);
  return {
    id,
    conversation_id: conversationId,
    role,
    content,
    metadata: metadataStr,
    created_at: now,
  };
}

export function getMessages(conversationId: string): Message[] {
  return selectMessages.all(conversationId) as Message[];
}

export default db;
