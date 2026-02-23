import {
  query,
  createSdkMcpServer,
  tool,
  type SDKMessage,
  type SDKAssistantMessage,
  type SDKResultSuccess,
  type SDKResultError,
} from "@anthropic-ai/claude-agent-sdk";
import { z } from "zod";

const SYSTEM_PROMPT = `You are a workout planning assistant inside a fitness app. Help users create workout templates and exercises through conversation.

IMPORTANT FORMATTING RULES:
- Do NOT use markdown formatting (no **bold**, *italic*, headers, lists, or links). The app renders plain text only — markdown syntax will appear as raw characters.
- Do NOT use emojis.
- Keep responses short (1-3 sentences). The chat bubbles have limited width.

When helping users, gather information about:
- Fitness goals (strength, hypertrophy, endurance, weight loss)
- Available equipment (full gym, home gym, bodyweight only)
- Experience level (beginner, intermediate, advanced)
- Time available per session
- Target muscle groups or workout split preference

IMPORTANT: Use present_choices for single questions and present_multi_choice when you need to gather multiple pieces of information at once. Batch related questions together to reduce back-and-forth. For example, ask about goals, experience level, AND equipment in a single present_multi_choice call rather than three separate present_choices calls. Always prefer present_multi_choice when you have 2+ questions to ask. Do NOT type out options as text — the app will render them as tappable buttons which is much better UX.

Use WebSearch to look up exercise information, proper form cues, and workout programming principles when needed.

When you have enough information, use create_template_proposal or create_exercise_proposal to present the result. The user can then save it to their app.

RECORDING TYPES:
Each exercise can define custom recording fields. Default is weight (kg) + reps (count).
Available field types: "number" (integer), "decimal" (float), "duration" (seconds)
Common patterns:
  - Weight training (default): weight(decimal,kg) + reps(number) — do NOT specify recordingFields for this.
  - Bodyweight/count only: reps(number) with custom label (e.g. "Slow Blinks", "Pushups")
  - Timed hold: duration(duration,sec)
  - Distance + time: distance(decimal,km) + duration(duration,sec)
When creating non-weight exercises (e.g. eye routines, stretching, timed holds, cardio drills), include recordingFields and targetValues per exercise.
For standard weight training exercises, omit recordingFields entirely (the app uses weight+reps by default).

Focus on being helpful and actionable.`;

// In-process MCP tools using SDK's createSdkMcpServer (no subprocess needed)
const workoutToolsServer = createSdkMcpServer({
  name: "workout-tools",
  version: "1.0.0",
  tools: [
    tool(
      "present_choices",
      "Present a single multiple-choice question to the user. The mobile app will render the options as tappable buttons. Use present_multi_choice instead when you have 2+ questions.",
      {
        question: z.string().describe("The question to ask the user"),
        options: z.array(
          z.object({
            id: z.string().describe("Unique identifier for this option"),
            label: z.string().describe("Display text for this option"),
          })
        ).describe("List of options for the user to choose from"),
      },
      async ({ question, options }) => ({
        content: [{ type: "text" as const, text: JSON.stringify({ type: "multiple_choice", question, options }) }],
      })
    ),
    tool(
      "present_multi_choice",
      "Present multiple questions at once as a form. Each question has its own set of tappable options. The user answers all questions before the response is sent back. Use this to batch related questions and reduce round-trips.",
      {
        questions: z.array(z.object({
          id: z.string().describe("Unique ID for this question"),
          question: z.string().describe("The question text"),
          options: z.array(z.object({
            id: z.string().describe("Unique option ID"),
            label: z.string().describe("Display text"),
          })).describe("Options for this question"),
        })).describe("List of questions to present simultaneously"),
      },
      async ({ questions }) => ({
        content: [{ type: "text" as const, text: JSON.stringify({ type: "multi_choice", questions }) }],
      })
    ),
    tool(
      "create_template_proposal",
      "Create a workout template proposal for the user to review. The mobile app will display it as a card with a 'Save Template' button.",
      {
        name: z.string().describe("Name of the workout template"),
        description: z.string().optional().describe("Brief description of the template"),
        exercises: z.array(
          z.object({
            name: z.string().describe("Exercise name"),
            sets: z.number().describe("Number of sets"),
            reps: z.string().describe("Rep range or count (e.g. '8-12', '10', 'AMRAP', '10 slow blinks')"),
            muscleGroup: z.string().describe("Primary muscle group"),
            recordingFields: z.array(
              z.object({
                key: z.string().describe("Field key: 'weight', 'reps', 'duration', 'distance', or custom"),
                label: z.string().describe("Display label (e.g. 'Weight', 'Slow Blinks', 'Duration')"),
                type: z.string().describe("Data type: 'decimal' (float), 'number' (int), 'duration' (seconds)"),
                unit: z.string().describe("Unit label: 'kg', 'sec', 'km', '' etc."),
                required: z.boolean().optional().describe("Whether field is required to complete a set (default true)"),
              })
            ).optional().describe("Custom recording fields. Omit for standard weight+reps exercises."),
            targetValues: z.record(z.string(), z.string()).optional().describe("Target values per field key, e.g. {\"reps\":\"10\"} or {\"duration\":\"30\"}"),
          })
        ).describe("List of exercises in the template"),
        estimatedDuration: z.number().optional().describe("Estimated workout duration in minutes"),
      },
      async ({ name, description, exercises, estimatedDuration }) => ({
        content: [{
          type: "text" as const,
          text: JSON.stringify({
            type: "template_proposal", name,
            description: description ?? null, exercises,
            estimatedDuration: estimatedDuration ?? null,
          }),
        }],
      })
    ),
    tool(
      "create_exercise_proposal",
      "Create an exercise proposal for the user to review. The mobile app will display it as a card with a 'Save Exercise' button.",
      {
        name: z.string().describe("Exercise name"),
        muscleGroup: z.string().describe("Primary muscle group"),
        category: z.string().optional().describe("Exercise category (e.g. 'Compound', 'Isolation')"),
        equipment: z.string().optional().describe("Required equipment"),
        difficulty: z.string().optional().describe("Difficulty level"),
        instructions: z.string().optional().describe("Step-by-step instructions"),
        recordingFields: z.array(
          z.object({
            key: z.string().describe("Field key: 'weight', 'reps', 'duration', 'distance', or custom"),
            label: z.string().describe("Display label (e.g. 'Weight', 'Reps', 'Duration')"),
            type: z.string().describe("Data type: 'decimal' (float), 'number' (int), 'duration' (seconds)"),
            unit: z.string().describe("Unit label: 'kg', 'sec', 'km', '' etc."),
            required: z.boolean().optional().describe("Whether field is required (default true)"),
          })
        ).optional().describe("Custom recording fields. Omit for standard weight+reps exercises."),
      },
      async ({ name, muscleGroup, category, equipment, difficulty, instructions, recordingFields }) => ({
        content: [{
          type: "text" as const,
          text: JSON.stringify({
            type: "exercise_proposal", name, muscleGroup,
            category: category ?? null, equipment: equipment ?? null,
            difficulty: difficulty ?? null, instructions: instructions ?? null,
            recordingFields: recordingFields ?? null,
          }),
        }],
      })
    ),
  ],
});

export interface AgentResponse {
  response: string;
  metadata?: Record<string, unknown>[];
  newSessionId?: string;
}

export type StreamEvent =
  | { type: "status"; text: string }
  | { type: "done"; message: Record<string, unknown> };

function friendlyToolName(name: string): string {
  if (name.includes("WebSearch")) return "Searching the web...";
  if (name.includes("WebFetch")) return "Reading web page...";
  if (name.includes("present_choices") || name.includes("present_multi_choice")) return "Preparing options...";
  if (name.includes("create_template_proposal")) return "Creating template...";
  if (name.includes("create_exercise_proposal")) return "Creating exercise...";
  return "Processing...";
}

export class AgentManager {
  /**
   * Build the query options shared by both processMessage and processMessageStream.
   */
  private buildOptions(agentSessionId?: string | null): Record<string, unknown> {
    const options: Record<string, unknown> = {
      systemPrompt: SYSTEM_PROMPT,
      allowedTools: [
        "WebSearch",
        "WebFetch",
        "mcp__workout-tools__present_choices",
        "mcp__workout-tools__present_multi_choice",
        "mcp__workout-tools__create_template_proposal",
        "mcp__workout-tools__create_exercise_proposal",
      ],
      mcpServers: {
        "workout-tools": workoutToolsServer,
      },
      maxTurns: 10,
      permissionMode: "acceptEdits" as const,
      env: {
        ...process.env,
        CLAUDECODE: undefined,
        CLAUDE_CODE_ENTRYPOINT: undefined,
      },
      stderr: (data: string) => {
        console.error(`[AgentManager/stderr] ${data.trim()}`);
      },
    };

    if (agentSessionId) {
      options.resume = agentSessionId;
    }

    return options;
  }

  /**
   * Process SDK messages from the async generator, collecting text and metadata.
   * Calls onEvent for intermediate status updates when provided.
   */
  private async processConversation(
    conversationId: string,
    userMessage: string,
    options: Record<string, unknown>,
    onEvent?: (event: StreamEvent) => void
  ): Promise<AgentResponse> {
    const conversation = query({
      prompt: userMessage,
      options: options as Parameters<typeof query>[0]["options"],
    });

    const textParts: string[] = [];
    const metadataItems: Record<string, unknown>[] = [];
    let sessionId: string | undefined;
    let firstMessage = true;

    for await (const message of conversation) {
      // Emit initial "Thinking..." status on first message
      if (firstMessage && onEvent) {
        onEvent({ type: "status", text: "Thinking..." });
        firstMessage = false;
      }

      // Track session ID from any message that has it
      if ("session_id" in message && typeof message.session_id === "string") {
        sessionId = message.session_id;
      }

      // Handle tool_progress events for live status
      if (message.type === "tool_progress" && onEvent) {
        const toolMsg = message as SDKMessage & { tool_name?: string };
        if (toolMsg.tool_name) {
          onEvent({ type: "status", text: friendlyToolName(toolMsg.tool_name) });
        }
      }

      if (message.type === "assistant") {
        const assistantMsg = message as SDKAssistantMessage;
        if (assistantMsg.message?.content && Array.isArray(assistantMsg.message.content)) {
          for (const block of assistantMsg.message.content) {
            if (block.type === "text" && "text" in block) {
              textParts.push(block.text);
            }
            if (block.type === "tool_use" && "name" in block) {
              const toolName = block.name as string;

              // Emit status for tool use
              if (onEvent) {
                onEvent({ type: "status", text: friendlyToolName(toolName) });
              }

              if (
                toolName.startsWith("mcp__workout-tools__") &&
                "input" in block
              ) {
                const input = block.input as Record<string, unknown>;
                const toolShortName = toolName.replace("mcp__workout-tools__", "");
                if (toolShortName === "present_choices") {
                  metadataItems.push({ type: "multiple_choice", ...input });
                } else if (toolShortName === "present_multi_choice") {
                  metadataItems.push({ type: "multi_choice", ...input });
                } else if (toolShortName === "create_template_proposal") {
                  metadataItems.push({ type: "template_proposal", templateData: input });
                } else if (toolShortName === "create_exercise_proposal") {
                  metadataItems.push({ type: "exercise_proposal", exerciseData: input });
                }
              }
            }
          }
        }
      }

      if (message.type === "result") {
        const resultMsg = message as SDKResultSuccess | SDKResultError;
        if (resultMsg.subtype === "success") {
          const success = resultMsg as SDKResultSuccess;
          if (success.result && textParts.length === 0) {
            textParts.push(success.result);
          }
          console.log(
            `[AgentManager] Completed for conversation ${conversationId} ` +
            `(${success.num_turns} turns, $${success.total_cost_usd.toFixed(4)})`
          );
        } else {
          const error = resultMsg as SDKResultError;
          console.error(
            `[AgentManager] Error result for conversation ${conversationId}:`,
            error.subtype, error.errors
          );
        }
      }
    }

    const response = textParts.join("\n\n").trim();

    return {
      response: response || (metadataItems.length > 0 ? "" : "I apologize, but I was unable to generate a response. Please try again."),
      metadata: metadataItems.length > 0 ? metadataItems : undefined,
      newSessionId: sessionId,
    };
  }

  /**
   * Process a user message through the Claude Agent SDK.
   * Returns the complete response after all processing is done.
   */
  async processMessage(
    conversationId: string,
    userMessage: string,
    agentSessionId?: string | null
  ): Promise<AgentResponse> {
    try {
      const options = this.buildOptions(agentSessionId);
      console.log(`[AgentManager] Starting query for conversation ${conversationId}...`);
      return await this.processConversation(conversationId, userMessage, options);
    } catch (error) {
      console.error(
        `[AgentManager] Error processing message for conversation ${conversationId}:`,
        error
      );
      throw error;
    }
  }

  /**
   * Process a user message with streaming status events.
   * Calls onEvent for intermediate status updates (tool usage, thinking state).
   */
  async processMessageStream(
    conversationId: string,
    userMessage: string,
    agentSessionId: string | null | undefined,
    onEvent: (event: StreamEvent) => void
  ): Promise<AgentResponse> {
    try {
      const options = this.buildOptions(agentSessionId);
      console.log(`[AgentManager] Starting streaming query for conversation ${conversationId}...`);
      return await this.processConversation(conversationId, userMessage, options, onEvent);
    } catch (error) {
      console.error(
        `[AgentManager] Error processing streaming message for conversation ${conversationId}:`,
        error
      );
      throw error;
    }
  }
}

// Export a singleton instance
export const agentManager = new AgentManager();
