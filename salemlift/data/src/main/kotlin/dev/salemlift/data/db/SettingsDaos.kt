package dev.salemlift.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** User-tuned rule deltas (DOMAIN.md §5: rules are data). */
@Dao
interface RuleOverrideDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: RuleOverrideEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<RuleOverrideEntity>)

    @Query("SELECT * FROM rule_override")
    suspend fun getAll(): List<RuleOverrideEntity>

    @Query("DELETE FROM rule_override")
    suspend fun deleteAll()
}

/** The single rest-timer preference row. */
@Dao
interface RestPrefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: RestPrefEntity)

    @Query("SELECT * FROM rest_pref WHERE id = 1")
    fun observe(): Flow<RestPrefEntity?>

    @Query("SELECT * FROM rest_pref WHERE id = 1")
    suspend fun get(): RestPrefEntity?

    @Query("DELETE FROM rest_pref")
    suspend fun deleteAll()
}
