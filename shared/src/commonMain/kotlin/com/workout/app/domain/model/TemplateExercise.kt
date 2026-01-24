package com.workout.app.domain.model

/**
 * Represents an exercise within a template.
 * Stored as JSON in the Template.exercises field.
 *
 * @param exerciseId The ID of the exercise
 * @param order The position of the exercise in the template (0-indexed)
 * @param defaultSets Default number of sets for this exercise
 */
data class TemplateExercise(
    val exerciseId: String,
    val order: Int,
    val defaultSets: Int = 3
) {
    companion object {
        /**
         * Parse a list of TemplateExercise from JSON string.
         * Expected format: [{"exerciseId":"1","order":0,"defaultSets":3},...]
         */
        fun fromJsonArray(json: String): List<TemplateExercise> {
            if (json.isBlank() || json == "[]") return emptyList()

            return try {
                // Simple JSON parsing without external library
                val result = mutableListOf<TemplateExercise>()
                val trimmed = json.trim().removePrefix("[").removeSuffix("]")
                if (trimmed.isBlank()) return emptyList()

                // Split by objects - this is a simplified parser
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

            content.split(",").forEach { pair ->
                val parts = pair.split(":")
                if (parts.size == 2) {
                    val key = parts[0].trim().removeSurrounding("\"")
                    val value = parts[1].trim().removeSurrounding("\"")
                    when (key) {
                        "exerciseId", "id" -> exerciseId = value
                        "order" -> order = value.toIntOrNull() ?: 0
                        "defaultSets", "sets" -> defaultSets = value.toIntOrNull() ?: 3
                    }
                }
            }

            return exerciseId?.let {
                TemplateExercise(
                    exerciseId = it,
                    order = order,
                    defaultSets = defaultSets
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
                """{"exerciseId":"${exercise.exerciseId}","order":${exercise.order},"defaultSets":${exercise.defaultSets}}"""
            }
        }
    }
}
