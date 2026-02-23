package com.workout.app.domain.model

/**
 * Defines a single input field for recording exercise performance.
 * Each exercise can define its own set of fields (e.g. weight+reps, duration, distance+time).
 *
 * @param key Unique key for this field: "weight", "reps", "duration", "distance", or custom
 * @param label Display label shown in the input UI (e.g. "Weight", "Slow Blinks", "Duration")
 * @param type Field data type: "decimal" (float), "number" (int), "duration" (seconds)
 * @param unit Unit label: "kg", "sec", "km", "" etc.
 * @param required Whether this field must have a value to complete a set
 */
data class RecordingField(
    val key: String,
    val label: String,
    val type: String,
    val unit: String,
    val required: Boolean = true
) {
    companion object {
        /** Default recording fields: weight (kg) + reps. Used when exercise has no custom fields. */
        val DEFAULT_FIELDS = listOf(
            RecordingField(key = "weight", label = "Weight", type = "decimal", unit = "kg"),
            RecordingField(key = "reps", label = "Reps", type = "number", unit = "")
        )

        /**
         * Parse a list of RecordingField from a JSON array string.
         * Expected format: [{"key":"weight","label":"Weight","type":"decimal","unit":"kg","required":true},...]
         * Returns null if the string is null/blank (meaning default fields should be used).
         */
        fun fromJsonArray(json: String?): List<RecordingField>? {
            if (json.isNullOrBlank() || json == "[]") return null

            return try {
                val result = mutableListOf<RecordingField>()
                val trimmed = json.trim().removePrefix("[").removeSuffix("]")
                if (trimmed.isBlank()) return null

                var depth = 0
                var start = 0
                for (i in trimmed.indices) {
                    when (trimmed[i]) {
                        '{' -> depth++
                        '}' -> {
                            depth--
                            if (depth == 0) {
                                val objStr = trimmed.substring(start, i + 1).trim().removePrefix(",").trim()
                                if (objStr.isNotBlank()) {
                                    parseObject(objStr)?.let { result.add(it) }
                                }
                                start = i + 1
                            }
                        }
                    }
                }
                if (result.isEmpty()) null else result
            } catch (e: Exception) {
                null
            }
        }

        private fun parseObject(json: String): RecordingField? {
            val content = json.removePrefix("{").removeSuffix("}").trim()
            var key: String? = null
            var label: String? = null
            var type: String? = null
            var unit = ""
            var required = true

            // Simple key-value parser that handles string and boolean values
            val pairs = splitTopLevelCommas(content)
            for (pair in pairs) {
                val colonIdx = pair.indexOf(':')
                if (colonIdx == -1) continue
                val k = pair.substring(0, colonIdx).trim().removeSurrounding("\"")
                val v = pair.substring(colonIdx + 1).trim().removeSurrounding("\"")
                when (k) {
                    "key" -> key = v
                    "label" -> label = v
                    "type" -> type = v
                    "unit" -> unit = v
                    "required" -> required = v.toBooleanStrictOrNull() ?: true
                }
            }

            return if (key != null && label != null && type != null) {
                RecordingField(key = key, label = label, type = type, unit = unit, required = required)
            } else {
                null
            }
        }

        /**
         * Convert a list of RecordingField to a JSON array string.
         */
        fun toJsonArray(fields: List<RecordingField>?): String? {
            if (fields == null || fields.isEmpty()) return null
            return fields.joinToString(
                prefix = "[",
                postfix = "]",
                separator = ","
            ) { field ->
                buildString {
                    append("{\"key\":\"${escapeJson(field.key)}\"")
                    append(",\"label\":\"${escapeJson(field.label)}\"")
                    append(",\"type\":\"${escapeJson(field.type)}\"")
                    append(",\"unit\":\"${escapeJson(field.unit)}\"")
                    append(",\"required\":${field.required}}")
                }
            }
        }

        /** Split by commas at top level (not inside nested braces/brackets/quotes). */
        private fun splitTopLevelCommas(s: String): List<String> {
            val result = mutableListOf<String>()
            var depth = 0
            var inString = false
            var start = 0
            for (i in s.indices) {
                when {
                    s[i] == '"' && (i == 0 || s[i - 1] != '\\') -> inString = !inString
                    !inString && (s[i] == '{' || s[i] == '[') -> depth++
                    !inString && (s[i] == '}' || s[i] == ']') -> depth--
                    !inString && depth == 0 && s[i] == ',' -> {
                        result.add(s.substring(start, i))
                        start = i + 1
                    }
                }
            }
            if (start < s.length) result.add(s.substring(start))
            return result
        }

        private fun escapeJson(s: String): String =
            s.replace("\\", "\\\\").replace("\"", "\\\"")
    }
}

/**
 * Convert a Map<String, String> of field values to a JSON object string.
 * E.g. {"weight":"80","reps":"10"}
 */
fun fieldValuesToJson(values: Map<String, String>?): String? {
    if (values.isNullOrEmpty()) return null
    return values.entries.joinToString(
        prefix = "{",
        postfix = "}",
        separator = ","
    ) { (key, value) ->
        "\"${escapeJsonString(key)}\":\"${escapeJsonString(value)}\""
    }
}

/**
 * Parse a JSON object string into a Map<String, String>.
 * E.g. {"weight":"80","reps":"10"} -> {weight=80, reps=10}
 */
fun fieldValuesFromJson(json: String?): Map<String, String>? {
    if (json.isNullOrBlank() || json == "{}") return null

    return try {
        val content = json.trim().removePrefix("{").removeSuffix("}").trim()
        if (content.isBlank()) return null

        val result = mutableMapOf<String, String>()
        val pairs = splitJsonPairs(content)
        for (pair in pairs) {
            val colonIdx = pair.indexOf(':')
            if (colonIdx == -1) continue
            val key = pair.substring(0, colonIdx).trim().removeSurrounding("\"")
            val value = pair.substring(colonIdx + 1).trim().removeSurrounding("\"")
            result[key] = value
        }
        if (result.isEmpty()) null else result
    } catch (e: Exception) {
        null
    }
}

private fun splitJsonPairs(s: String): List<String> {
    val result = mutableListOf<String>()
    var inString = false
    var start = 0
    for (i in s.indices) {
        when {
            s[i] == '"' && (i == 0 || s[i - 1] != '\\') -> inString = !inString
            !inString && s[i] == ',' -> {
                result.add(s.substring(start, i))
                start = i + 1
            }
        }
    }
    if (start < s.length) result.add(s.substring(start))
    return result
}

private fun escapeJsonString(s: String): String =
    s.replace("\\", "\\\\").replace("\"", "\\\"")
