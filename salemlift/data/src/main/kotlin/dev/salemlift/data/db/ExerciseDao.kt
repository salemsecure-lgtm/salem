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

    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :query || '%' ORDER BY name")
    fun searchByName(query: String): Flow<List<ExerciseEntity>>

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
}
