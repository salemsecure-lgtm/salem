package dev.salemlift.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.salemlift.data.catalog.Equipment
import dev.salemlift.domain.model.Muscle

/**
 * Room row mirroring [dev.salemlift.data.catalog.CatalogExercise].
 *
 * Storage choices (see [Converters]):
 * - [primaryMuscle] and [equipment] are enums, persisted by Room's built-in enum handling as their
 *   `name` strings.
 * - [secondaryMuscles] is persisted as a comma-delimited list of [Muscle] names — enum names can
 *   never contain the delimiter, so no escaping is needed.
 * - [cues] is free instruction text and is persisted as a JSON string array, because the text may
 *   contain any delimiter we could pick.
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val primaryMuscle: Muscle,
    val secondaryMuscles: Set<Muscle>,
    val equipment: Equipment,
    val level: String,
    val category: String,
    val cues: List<String>,
    val isCustom: Boolean,
)
