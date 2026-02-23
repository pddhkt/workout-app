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

IMPORTANT: Always use present_choices to ask questions. Do NOT type out options as text — the app will render present_choices as tappable buttons which is much better UX. Every time you need user input, use present_choices.

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
      "Present a multiple-choice question to the user. The mobile app will render the options as tappable buttons.",
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
            targetValues: z.record(z.string()).optional().describe("Target values per field key, e.g. {\"reps\":\"10\"} or {\"duration\":\"30\"}"),
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

export class AgentManager {
  /**
   * Process a user message through the Claude Agent SDK.
   * Iterates the async generator returned by query() to collect messages.
   */
  async processMessage(
    conversationId: string,
    userMessage: string,
    agentSessionId?: string | null
  ): Promise<AgentResponse> {
    try {
      const options: Record<string, unknown> = {
        systemPrompt: SYSTEM_PROMPT,
        allowedTools: [
          "WebSearch",
          "WebFetch",
          "mcp__workout-tools__present_choices",
          "mcp__workout-tools__create_template_proposal",
          "mcp__workout-tools__create_exercise_proposal",
        ],
        mcpServers: {
          "workout-tools": workoutToolsServer,
        },
        maxTurns: 10,
        permissionMode: "acceptEdits" as const,
        // Unset CLAUDECODE to allow running inside a Claude Code session (e.g. during dev)
        env: {
          ...process.env,
          CLAUDECODE: undefined,
          CLAUDE_CODE_ENTRYPOINT: undefined,
        },
        stderr: (data: string) => {
          console.error(`[AgentManager/stderr] ${data.trim()}`);
        },
      };

      // Resume existing session if we have one
      if (agentSessionId) {
        options.resume = agentSessionId;
      }

      console.log(`[AgentManager] Starting query for conversation ${conversationId}...`);

      const conversation = query({
        prompt: userMessage,
        options: options as Parameters<typeof query>[0]["options"],
      });

      const textParts: string[] = [];
      const metadataItems: Record<string, unknown>[] = [];
      let sessionId: string | undefined;

      for await (const message of conversation) {
        // Track session ID from any message that has it
        if ("session_id" in message && typeof message.session_id === "string") {
          sessionId = message.session_id;
        }

        if (message.type === "assistant") {
          const assistantMsg = message as SDKAssistantMessage;
          // Extract text from the BetaMessage content blocks
          if (assistantMsg.message?.content && Array.isArray(assistantMsg.message.content)) {
            for (const block of assistantMsg.message.content) {
              if (block.type === "text" && "text" in block) {
                textParts.push(block.text);
              }
              // Check tool_use blocks for our MCP tool results
              if (block.type === "tool_use" && "name" in block) {
                const toolName = block.name as string;
                if (
                  toolName.startsWith("mcp__workout-tools__") &&
                  "input" in block
                ) {
                  // The tool input contains the structured data
                  const input = block.input as Record<string, unknown>;
                  const toolShortName = toolName.replace("mcp__workout-tools__", "");
                  if (toolShortName === "present_choices") {
                    // Client expects { type, options } at root level
                    metadataItems.push({ type: "multiple_choice", ...input });
                  } else if (toolShortName === "create_template_proposal") {
                    // Client expects { type, templateData: { name, exercises, ... } }
                    metadataItems.push({ type: "template_proposal", templateData: input });
                  } else if (toolShortName === "create_exercise_proposal") {
                    // Client expects { type, exerciseData: { name, muscleGroup, ... } }
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
    } catch (error) {
      console.error(
        `[AgentManager] Error processing message for conversation ${conversationId}:`,
        error
      );
      throw error;
    }
  }
}

// Export a singleton instance
export const agentManager = new AgentManager();
