# ── Create a new conversation ──
POST http://localhost:3141/conversations
Content-Type: application/json
{}
HTTP 201
[Captures]
conversation_id: jsonpath "$.id"
[Asserts]
jsonpath "$.id" isString
jsonpath "$.title" == null
jsonpath "$.agent_session_id" == null
jsonpath "$.status" == "active"
jsonpath "$.created_at" isInteger
jsonpath "$.updated_at" isInteger


# ── Create a conversation with title ──
POST http://localhost:3141/conversations
Content-Type: application/json
{"title": "Test Conversation"}
HTTP 201
[Captures]
titled_conversation_id: jsonpath "$.id"
[Asserts]
jsonpath "$.title" == "Test Conversation"
jsonpath "$.status" == "active"


# ── List conversations (should contain both) ──
GET http://localhost:3141/conversations
HTTP 200
[Asserts]
jsonpath "$" count >= 2
jsonpath "$[0].id" isString
jsonpath "$[0].status" == "active"


# ── Get single conversation ──
GET http://localhost:3141/conversations/{{conversation_id}}
HTTP 200
[Asserts]
jsonpath "$.id" == "{{conversation_id}}"
jsonpath "$.status" == "active"


# ── Get non-existent conversation ──
GET http://localhost:3141/conversations/does-not-exist
HTTP 404
[Asserts]
jsonpath "$.error" == "Conversation not found"


# ── Get messages (should be empty) ──
GET http://localhost:3141/conversations/{{conversation_id}}/messages
HTTP 200
[Asserts]
jsonpath "$" count == 0


# ── Delete conversation ──
DELETE http://localhost:3141/conversations/{{titled_conversation_id}}
HTTP 204


# ── Delete non-existent conversation ──
DELETE http://localhost:3141/conversations/does-not-exist
HTTP 404


# ── Cleanup: delete the first conversation too ──
DELETE http://localhost:3141/conversations/{{conversation_id}}
HTTP 204
