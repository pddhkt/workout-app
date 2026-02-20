# ═══════════════════════════════════════════
# Input validation and error handling tests
# ═══════════════════════════════════════════


# ── Empty message body ──
POST http://localhost:3141/conversations/some-id/messages
Content-Type: application/json
{"content": ""}
HTTP 400
[Asserts]
jsonpath "$.error" == "Message content is required"


# ── Missing content field ──
POST http://localhost:3141/conversations/some-id/messages
Content-Type: application/json
{"text": "wrong field name"}
HTTP 400
[Asserts]
jsonpath "$.error" == "Message content is required"


# ── Whitespace-only content ──
POST http://localhost:3141/conversations/some-id/messages
Content-Type: application/json
{"content": "   "}
HTTP 400
[Asserts]
jsonpath "$.error" == "Message content is required"


# ── Message to non-existent conversation ──
POST http://localhost:3141/conversations/does-not-exist-999/messages
Content-Type: application/json
{"content": "Hello"}
HTTP 404
[Asserts]
jsonpath "$.error" == "Conversation not found"


# ── Messages for non-existent conversation ──
GET http://localhost:3141/conversations/does-not-exist-999/messages
HTTP 404
[Asserts]
jsonpath "$.error" == "Conversation not found"
