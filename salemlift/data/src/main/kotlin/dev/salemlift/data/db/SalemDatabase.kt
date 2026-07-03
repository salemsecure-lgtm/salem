package dev.salemlift.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Salem Lift's Room database. Version 1 holds the exercise catalog only; workout/session tables
 * arrive in Phase 3 as versioned migrations (schemas are exported to `data/schemas/`).
 */
@Database(
    entities = [ExerciseEntity::class],
    version = SalemDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SalemDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        const val VERSION: Int = 1
        const val NAME: String = "salemlift.db"
    }
}
