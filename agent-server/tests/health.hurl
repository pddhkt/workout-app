# Health check endpoint
GET http://localhost:3141/health
HTTP 200
[Asserts]
jsonpath "$.status" == "ok"
jsonpath "$.timestamp" isInteger
