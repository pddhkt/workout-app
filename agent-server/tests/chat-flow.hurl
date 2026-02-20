# ═══════════════════════════════════════════════════
# Full chat flow: user asks agent to create a workout
# ═══════════════════════════════════════════════════
#
# This test walks through a multi-turn conversation
# where the agent asks questions via multiple-choice
# and ultimately proposes a workout template.
#
# NOTE: Requires the agent server running with a valid
# CLAUDE_CODE_OAUTH_TOKEN. Agent responses are non-deterministic
# so we only assert on structure, not exact content.
#
# Run with:
#   hurl --test --max-time 300 tests/chat-flow.hurl


# ── Step 1: Create a new conversation ──
POST http://localhost:3141/conversations
Content-Type: application/json
{}
HTTP 201
[Captures]
conversation_id: jsonpath "$.id"
[Asserts]
jsonpath "$.id" isString
jsonpath "$.status" == "active"


# ── Step 2: Send opening message ──
# Agent should respond with text and a multiple-choice question
POST http://localhost:3141/conversations/{{conversation_id}}/messages
Content-Type: application/json
{"content": "Hi, I want to create a leg day workout template"}
[Options]
max-time: 300
HTTP 201
[Captures]
msg1_id: jsonpath "$.id"
[Asserts]
jsonpath "$.conversation_id" == "{{conversation_id}}"
jsonpath "$.role" == "assistant"
jsonpath "$.content" isString
jsonpath "$.created_at" isInteger


# ── Step 3: Reply with selections ──
# Give the agent all the info it needs in one go to speed things up
POST http://localhost:3141/conversations/{{conversation_id}}/messages
Content-Type: application/json
{"content": "My goal is hypertrophy. I have a full gym with barbells, dumbbells, and machines. I'm intermediate level and have about 60 minutes."}
[Options]
max-time: 300
HTTP 201
[Captures]
msg2_id: jsonpath "$.id"
[Asserts]
jsonpath "$.role" == "assistant"
jsonpath "$.content" isString


# ── Step 4: If agent asks more questions, confirm ──
POST http://localhost:3141/conversations/{{conversation_id}}/messages
Content-Type: application/json
{"content": "That looks great, please create the template"}
[Options]
max-time: 300
HTTP 201
[Captures]
msg3_id: jsonpath "$.id"
[Asserts]
jsonpath "$.role" == "assistant"
jsonpath "$.content" isString


# ── Step 5: Verify conversation history ──
GET http://localhost:3141/conversations/{{conversation_id}}/messages
HTTP 200
[Asserts]
# 3 user messages + 3 assistant messages = 6 total
jsonpath "$" count == 6
# First message is from user
jsonpath "$[0].role" == "user"
# Second is assistant response
jsonpath "$[1].role" == "assistant"
# Alternating pattern continues
jsonpath "$[2].role" == "user"
jsonpath "$[3].role" == "assistant"
jsonpath "$[4].role" == "user"
jsonpath "$[5].role" == "assistant"


# ── Step 6: Verify conversation was auto-titled ──
GET http://localhost:3141/conversations/{{conversation_id}}
HTTP 200
[Asserts]
jsonpath "$.title" isString
jsonpath "$.agent_session_id" isString


# ── Step 7: Cleanup ──
DELETE http://localhost:3141/conversations/{{conversation_id}}
HTTP 204
