package com.prayogizan.golekpilem.data.local.converter

import androidx.room.TypeConverter
import com.prayogizan.golekpilem.data.local.model.ReviewLocalModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverter handling serialization and deserialization of complex collections.
 *
 * Utilizes [kotlinx.serialization.json.Json] with lenient parsing and safe fallbacks,
 * ensuring database transactions never crash on unexpected schema alterations or corrupted JSON.
 */
class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
    }

    /**
     * Converts a collection of strings into a JSON string format.
     *
     * @param value The list of strings to serialize, or `null`.
     * @return A valid JSON array representation, or `"[]"` if null.
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        return json.encodeToString(value)
    }

    /**
     * Parses a JSON string back into a strongly-typed list of strings.
     *
     * Defensively catches deserialization failures and returns an empty list.
     *
     * @param value The raw JSON string representation.
     * @return A list of parsed strings, or [emptyList] on parsing error or blank input.
     */
    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Converts a collection of [ReviewLocalModel] instances into a JSON array string.
     *
     * @param value The list of reviews to serialize, or `null`.
     * @return A valid JSON array representation, or `"[]"` if null.
     */
    @TypeConverter
    fun fromReviewList(value: List<ReviewLocalModel>?): String {
        if (value == null) return "[]"
        return json.encodeToString(value)
    }

    /**
     * Parses a JSON string back into a list of [ReviewLocalModel] instances.
     *
     * Defensively catches deserialization errors and falls back to an empty list.
     *
     * @param value The raw JSON string representation.
     * @return A list of parsed review models, or [emptyList] on parsing error or blank input.
     */
    @TypeConverter
    fun toReviewList(value: String?): List<ReviewLocalModel> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<ReviewLocalModel>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
