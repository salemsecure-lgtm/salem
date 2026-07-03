package dev.salemlift.data.catalog

import dev.salemlift.data.db.ExerciseEntity

/** Maps a parsed [CatalogExercise] to its Room row. */
fun CatalogExercise.toEntity(): ExerciseEntity =
    ExerciseEntity(
        id = id,
        name = name,
        primaryMuscle = primaryMuscle,
        secondaryMuscles = secondaryMuscles,
        equipment = equipment,
        level = level,
        category = category,
        cues = cues,
        isCustom = isCustom,
    )

/** Maps a Room row back to the catalog model consumed by upper layers. */
fun ExerciseEntity.toCatalogExercise(): CatalogExercise =
    CatalogExercise(
        id = id,
        name = name,
        primaryMuscle = primaryMuscle,
        secondaryMuscles = secondaryMuscles,
        equipment = equipment,
        level = level,
        category = category,
        cues = cues,
        isCustom = isCustom,
    )
