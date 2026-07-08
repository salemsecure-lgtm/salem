package dev.salemlift.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.salemlift.data.catalog.Equipment
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.flow.Flow

/** Queries over the exercise catalog (seeded dataset + user-created custom exercises). */
@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name")
    fun getAll(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getById(id: String): ExerciseEntity?

    /** Prefer [searchByName]; this binds the pattern verbatim (no wildcard escaping). */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :escaped || '%' ESCAPE '\\' ORDER BY name")
    fun searchByNameEscaped(escaped: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE primaryMuscle = :muscle ORDER BY name")
    fun filterByPrimaryMuscle(muscle: Muscle): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE equipment = :equipment ORDER BY name")
    fun filterByEquipment(equipment: Equipment): Flow<List<ExerciseEntity>>

    /** Bulk insert used by the seeder; replaces on conflict so a re-seed after a wipe is safe. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    /** Single insert for user-created custom exercises; aborts on id collision. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(exercise: ExerciseEntity)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun countAll(): Int

    /** User-created exercises only — the slice the settings backup exports. */
    @Query("SELECT * FROM exercises WHERE isCustom = 1 ORDER BY id")
    suspend fun getCustom(): List<ExerciseEntity>

    /** Destructive-replace helper for backup import; the seeded catalog is untouched. */
    @Query("DELETE FROM exercises WHERE isCustom = 1")
    suspend fun deleteCustom()
}

/** Escapes SQL LIKE wildcards so user input matches literally. */
fun escapeLikeQuery(raw: String): String =
    raw
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")

/** Substring name search treating the user's input literally (a typed `%` is just a percent sign). */
fun ExerciseDao.searchByName(query: String): Flow<List<ExerciseEntity>> = searchByNameEscaped(escapeLikeQuery(query))
