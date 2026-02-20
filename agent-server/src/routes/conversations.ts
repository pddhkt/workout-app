import { Router, Request, Response } from "express";
import {
  createConversation,
  getConversations,
  getConversation,
  deleteConversation,
} from "../db.js";

const router = Router();

// Helper to safely extract a string param (Express 5 types params as string | string[])
function param(req: Request, name: string): string {
  const value = req.params[name];
  return Array.isArray(value) ? value[0] : value;
}

// GET /conversations - List all active conversations
router.get("/", (_req: Request, res: Response) => {
  try {
    const conversations = getConversations();
    res.json(conversations);
  } catch (error) {
    console.error("[conversations] Error listing conversations:", error);
    res.status(500).json({ error: "Failed to list conversations" });
  }
});

// POST /conversations - Create a new conversation
router.post("/", (req: Request, res: Response) => {
  try {
    const { title } = req.body as { title?: string };
    const id = crypto.randomUUID();
    const conversation = createConversation(id, title);
    res.status(201).json(conversation);
  } catch (error) {
    console.error("[conversations] Error creating conversation:", error);
    res.status(500).json({ error: "Failed to create conversation" });
  }
});

// GET /conversations/:id - Get a single conversation
router.get("/:id", (req: Request, res: Response) => {
  try {
    const id = param(req, "id");
    const conversation = getConversation(id);
    if (!conversation) {
      res.status(404).json({ error: "Conversation not found" });
      return;
    }
    res.json(conversation);
  } catch (error) {
    console.error("[conversations] Error getting conversation:", error);
    res.status(500).json({ error: "Failed to get conversation" });
  }
});

// DELETE /conversations/:id - Delete a conversation
router.delete("/:id", (req: Request, res: Response) => {
  try {
    const id = param(req, "id");
    const deleted = deleteConversation(id);
    if (!deleted) {
      res.status(404).json({ error: "Conversation not found" });
      return;
    }
    res.status(204).send();
  } catch (error) {
    console.error("[conversations] Error deleting conversation:", error);
    res.status(500).json({ error: "Failed to delete conversation" });
  }
});

export default router;
