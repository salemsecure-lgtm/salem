package dev.salemlift.data.db

import androidx.room.TypeConverter
import dev.salemlift.domain.model.Muscle
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Room type converters for [ExerciseEntity]'s collection columns.
 *
 * - `Set<Muscle>` ⇄ comma-delimited enum names (`"BICEPS,FOREARMS"`); enum names cannot contain
 *   commas, so the encoding is unambiguous and cheap to index/read.
 * - `List<String>` ⇄ JSON string array; technique cues are free text, so a structured encoding is
 *   required rather than a delimiter convention.
 */
class Converters {
    private val stringListSerializer = ListSerializer(String.serializer())

    @TypeConverter
    fun fromMuscleSet(muscles: Set<Muscle>): String = muscles.joinToString(separator = ",") { it.name }

    @TypeConverter
    fun toMuscleSet(raw: String): Set<Muscle> =
        if (raw.isEmpty()) emptySet() else raw.split(",").map(Muscle::valueOf).toSet()

    @TypeConverter
    fun fromStringList(values: List<String>): String = Json.encodeToString(stringListSerializer, values)

    @TypeConverter
    fun toStringList(raw: String): List<String> = Json.decodeFromString(stringListSerializer, raw)
}
