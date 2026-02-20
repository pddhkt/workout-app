import { Router, Request, Response } from "express";
import {
  getConversation,
  createMessage,
  getMessages,
  updateAgentSessionId,
  setConversationTitle,
} from "../db.js";
import { agentManager } from "../agent.js";

const router = Router();

// Helper to safely extract a string param (Express 5 types params as string | string[])
function param(req: Request, name: string): string {
  const value = req.params[name];
  return Array.isArray(value) ? value[0] : value;
}

// GET /conversations/:conversationId/messages - Get all messages for a conversation
router.get("/:conversationId/messages", (req: Request, res: Response) => {
  try {
    const conversationId = param(req, "conversationId");

    const conversation = getConversation(conversationId);
    if (!conversation) {
      res.status(404).json({ error: "Conversation not found" });
      return;
    }

    const messages = getMessages(conversationId);

    // Parse metadata JSON strings back to objects for the response
    const parsed = messages.map((m) => ({
      ...m,
      metadata: m.metadata ? JSON.parse(m.metadata) : null,
    }));

    res.json(parsed);
  } catch (error) {
    console.error("[messages] Error getting messages:", error);
    res.status(500).json({ error: "Failed to get messages" });
  }
});

// POST /conversations/:conversationId/messages - Send a message and get agent response
router.post(
  "/:conversationId/messages",
  async (req: Request, res: Response) => {
    try {
      const conversationId = param(req, "conversationId");
      const { content } = req.body as { content: string };

      if (!content || typeof content !== "string" || content.trim().length === 0) {
        res.status(400).json({ error: "Message content is required" });
        return;
      }

      // Verify conversation exists
      const conversation = getConversation(conversationId);
      if (!conversation) {
        res.status(404).json({ error: "Conversation not found" });
        return;
      }

      // 1. Store the user message
      const userMessageId = crypto.randomUUID();
      createMessage(
        userMessageId,
        conversationId,
        "user",
        content.trim()
      );

      // 2. Call the agent
      console.log(
        `[messages] Processing message for conversation ${conversationId}...`
      );

      const agentResponse = await agentManager.processMessage(
        conversationId,
        content.trim(),
        conversation.agent_session_id
      );

      // 3. Store the assistant message with metadata
      const assistantMessageId = crypto.randomUUID();
      // Send the first metadata item directly (not wrapped in { items: [...] })
      // The mobile app expects a flat object with "type" at the root level
      const metadata = agentResponse.metadata?.length
        ? agentResponse.metadata[0]
        : null;
      const assistantMessage = createMessage(
        assistantMessageId,
        conversationId,
        "assistant",
        agentResponse.response,
        metadata
      );

      // 4. Update the conversation's agent session ID if we got a new one
      if (agentResponse.newSessionId) {
        updateAgentSessionId(conversationId, agentResponse.newSessionId);
      }

      // 5. Auto-set conversation title from first user message if not set
      if (!conversation.title) {
        const title =
          content.trim().length > 60
            ? content.trim().substring(0, 57) + "..."
            : content.trim();
        setConversationTitle(conversationId, title);
      }

      // 6. Return the assistant message
      const responseMessage = {
        ...assistantMessage,
        metadata: assistantMessage.metadata
          ? JSON.parse(assistantMessage.metadata)
          : null,
      };

      console.log(
        `[messages] Agent responded for conversation ${conversationId}`
      );

      res.status(201).json(responseMessage);
    } catch (error) {
      console.error("[messages] Error processing message:", error);
      res.status(500).json({
        error: "Failed to process message",
        details: error instanceof Error ? error.message : "Unknown error",
      });
    }
  }
);

export default router;
