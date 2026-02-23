package com.workout.app.domain.model

/**
 * Represents an exercise within a template.
 * Stored as JSON in the Template.exercises field.
 *
 * @param exerciseId The ID of the exercise
 * @param order The position of the exercise in the template (0-indexed)
 * @param defaultSets Default number of sets for this exercise
 * @param recordingFields Custom recording fields (null = inherit from exercise or use defaults)
 * @param targetValues Target values per field key (e.g. {"reps":"10","weight":"80"})
 */
data class TemplateExercise(
    val exerciseId: String,
    val order: Int,
    val defaultSets: Int = 3,
    val recordingFields: List<RecordingField>? = null,
    val targetValues: Map<String, String>? = null
) {
    companion object {
        /**
         * Parse a list of TemplateExercise from JSON string.
         * Expected format: [{"exerciseId":"1","order":0,"defaultSets":3},...]
         * Also supports: recordingFields and targetValues nested objects.
         */
        fun fromJsonArray(json: String): List<TemplateExercise> {
            if (json.isBlank() || json == "[]") return emptyList()

            return try {
                val result = mutableListOf<TemplateExercise>()
                val trimmed = json.trim().removePrefix("[").removeSuffix("]")
                if (trimmed.isBlank()) return emptyList()

                // Split by top-level objects
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
                result
            } catch (e: Exception) {
                emptyList()
            }
        }

        private fun parseObject(json: String): TemplateExercise? {
            val content = json.removePrefix("{").removeSuffix("}").trim()
            var exerciseId: String? = null
            var order = 0
            var defaultSets = 3
            var recordingFields: List<RecordingField>? = null
            var targetValues: Map<String, String>? = null

            val pairs = splitTopLevel(content, ',')
            for (pair in pairs) {
                val colonIdx = findTopLevelColon(pair)
                if (colonIdx == -1) continue
                val key = pair.substring(0, colonIdx).trim().removeSurrounding("\"")
                val value = pair.substring(colonIdx + 1).trim()

                when (key) {
                    "exerciseId", "id" -> exerciseId = value.removeSurrounding("\"")
                    "order" -> order = value.removeSurrounding("\"").toIntOrNull() ?: 0
                    "defaultSets", "sets" -> defaultSets = value.removeSurrounding("\"").toIntOrNull() ?: 3
                    "recordingFields" -> {
                        if (value != "null") {
                            recordingFields = RecordingField.fromJsonArray(value)
                        }
                    }
                    "targetValues" -> {
                        if (value != "null") {
                            targetValues = fieldValuesFromJson(value)
                        }
                    }
                }
            }

            return exerciseId?.let {
                TemplateExercise(
                    exerciseId = it,
                    order = order,
                    defaultSets = defaultSets,
                    recordingFields = recordingFields,
                    targetValues = targetValues
                )
            }
        }

        /**
         * Convert a list of TemplateExercise to JSON string.
         */
        fun toJsonArray(exercises: List<TemplateExercise>): String {
            if (exercises.isEmpty()) return "[]"
            return exercises.joinToString(
                prefix = "[",
                postfix = "]",
                separator = ","
            ) { exercise ->
                buildString {
                    append("{\"exerciseId\":\"${exercise.exerciseId}\"")
                    append(",\"order\":${exercise.order}")
                    append(",\"defaultSets\":${exercise.defaultSets}")
                    val rf = RecordingField.toJsonArray(exercise.recordingFields)
                    if (rf != null) {
                        append(",\"recordingFields\":$rf")
                    }
                    val tv = fieldValuesToJson(exercise.targetValues)
                    if (tv != null) {
                        append(",\"targetValues\":$tv")
                    }
                    append("}")
                }
            }
        }

        /** Split a string by a delimiter at the top nesting level only. */
        private fun splitTopLevel(s: String, delimiter: Char): List<String> {
            val result = mutableListOf<String>()
            var depth = 0
            var inString = false
            var start = 0
            for (i in s.indices) {
                when {
                    s[i] == '"' && (i == 0 || s[i - 1] != '\\') -> inString = !inString
                    !inString && (s[i] == '{' || s[i] == '[') -> depth++
                    !inString && (s[i] == '}' || s[i] == ']') -> depth--
                    !inString && depth == 0 && s[i] == delimiter -> {
                        result.add(s.substring(start, i))
                        start = i + 1
                    }
                }
            }
            if (start < s.length) result.add(s.substring(start))
            return result
        }

        /** Find the first colon at top nesting level. */
        private fun findTopLevelColon(s: String): Int {
            var depth = 0
            var inString = false
            for (i in s.indices) {
                when {
                    s[i] == '"' && (i == 0 || s[i - 1] != '\\') -> inString = !inString
                    !inString && (s[i] == '{' || s[i] == '[') -> depth++
                    !inString && (s[i] == '}' || s[i] == ']') -> depth--
                    !inString && depth == 0 && s[i] == ':' -> return i
                }
            }
            return -1
        }
    }
}
